package com.roni.library.network.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HttpClientEngine {
    public interface MockTransport {
        HttpResponse execute(HttpRequest request) throws Exception;
    }

    public interface HttpCallback {
        void onSuccess(HttpResponse response);
        void onFailure(Throwable t);
    }

    private final List<HttpInterceptor> interceptors = new CopyOnWriteArrayList<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private volatile MockTransport mockTransport;

    public HttpClientEngine() {
        this.interceptors.add(new RetryInterceptor(2, 100));
    }

    public void addInterceptor(HttpInterceptor interceptor) {
        if (interceptor != null) {
            this.interceptors.add(interceptor);
        }
    }

    public void setMockTransport(MockTransport transport) {
        this.mockTransport = transport;
    }

    public HttpResponse execute(HttpRequest request) throws Exception {
        if (request == null) {
            throw new IllegalArgumentException("HttpRequest cannot be null");
        }
        List<HttpInterceptor> chainInterceptors = new ArrayList<>(interceptors);
        RealInterceptorChain chain = new RealInterceptorChain(chainInterceptors, 0, request);
        return chain.proceed(request);
    }

    public Future<HttpResponse> executeAsync(HttpRequest request, HttpCallback callback) {
        return executor.submit(() -> {
            try {
                HttpResponse response = execute(request);
                if (callback != null) {
                    callback.onSuccess(response);
                }
                return response;
            } catch (Throwable t) {
                if (callback != null) {
                    callback.onFailure(t);
                }
                throw (t instanceof Exception) ? (Exception) t : new RuntimeException(t);
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }

    private class RealInterceptorChain implements HttpInterceptor.Chain {
        private final List<HttpInterceptor> interceptors;
        private final int index;
        private final HttpRequest request;

        RealInterceptorChain(List<HttpInterceptor> interceptors, int index, HttpRequest request) {
            this.interceptors = interceptors;
            this.index = index;
            this.request = request;
        }

        @Override
        public HttpRequest request() {
            return request;
        }

        @Override
        public HttpResponse proceed(HttpRequest req) throws Exception {
            if (index < interceptors.size()) {
                RealInterceptorChain next = new RealInterceptorChain(interceptors, index + 1, req);
                HttpInterceptor interceptor = interceptors.get(index);
                return interceptor.intercept(next);
            }
            return executeTerminal(req);
        }
    }

    private HttpResponse executeTerminal(HttpRequest req) throws Exception {
        if (mockTransport != null) {
            return mockTransport.execute(req);
        }

        long startTime = System.currentTimeMillis();
        String fullUrl = buildFullUrl(req);
        URL url = new URL(fullUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(req.getMethod().name());
        conn.setConnectTimeout(req.getConnectTimeoutMs());
        conn.setReadTimeout(req.getReadTimeoutMs());
        conn.setDoInput(true);

        for (Map.Entry<String, String> header : req.getHeaders().entrySet()) {
            conn.setRequestProperty(header.getKey(), header.getValue());
        }

        if (req.getBody() != null && (req.getMethod() == HttpMethod.POST ||
                req.getMethod() == HttpMethod.PUT || req.getMethod() == HttpMethod.PATCH)) {
            conn.setDoOutput(true);
            byte[] bodyBytes = req.getBody().getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(bodyBytes);
                os.flush();
            }
        }

        int statusCode = conn.getResponseCode();
        Map<String, String> respHeaders = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && !entry.getValue().isEmpty()) {
                respHeaders.put(entry.getKey(), entry.getValue().get(0));
            }
        }

        InputStream is = (statusCode >= 200 && statusCode < 400) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder bodyBuilder = new StringBuilder();
        if (is != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    bodyBuilder.append(line).append('\n');
                }
            }
        }

        long latency = System.currentTimeMillis() - startTime;
        return new HttpResponse(statusCode, respHeaders, bodyBuilder.toString().trim(), latency);
    }

    private String buildFullUrl(HttpRequest req) {
        String base = req.getUrl();
        if (req.getQueryParameters().isEmpty()) {
            return base;
        }
        StringBuilder sb = new StringBuilder(base);
        sb.append(base.contains("?") ? "&" : "?");
        boolean first = true;
        for (Map.Entry<String, String> entry : req.getQueryParameters().entrySet()) {
            if (!first) sb.append("&");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        return sb.toString();
    }
}
