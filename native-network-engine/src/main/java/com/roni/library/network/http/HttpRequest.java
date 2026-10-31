package com.roni.library.network.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final String url;
    private final HttpMethod method;
    private final Map<String, String> headers;
    private final Map<String, String> queryParameters;
    private final String body;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    private HttpRequest(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = Collections.unmodifiableMap(new HashMap<>(builder.headers));
        this.queryParameters = Collections.unmodifiableMap(new HashMap<>(builder.queryParameters));
        this.body = builder.body;
        this.connectTimeoutMs = builder.connectTimeoutMs;
        this.readTimeoutMs = builder.readTimeoutMs;
    }

    public String getUrl() {
        return url;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public String getBody() {
        return body;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String url;
        private HttpMethod method = HttpMethod.GET;
        private final Map<String, String> headers = new HashMap<>();
        private final Map<String, String> queryParameters = new HashMap<>();
        private String body;
        private int connectTimeoutMs = 15000;
        private int readTimeoutMs = 15000;

        public Builder() {}

        public Builder(HttpRequest request) {
            this.url = request.url;
            this.method = request.method;
            this.headers.putAll(request.headers);
            this.queryParameters.putAll(request.queryParameters);
            this.body = request.body;
            this.connectTimeoutMs = request.connectTimeoutMs;
            this.readTimeoutMs = request.readTimeoutMs;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setMethod(HttpMethod method) {
            this.method = method != null ? method : HttpMethod.GET;
            return this;
        }

        public Builder addHeader(String key, String value) {
            if (key != null && value != null) {
                this.headers.put(key, value);
            }
            return this;
        }

        public Builder addQueryParameter(String key, String value) {
            if (key != null && value != null) {
                this.queryParameters.put(key, value);
            }
            return this;
        }

        public Builder setBody(String body) {
            this.body = body;
            return this;
        }

        public Builder setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
            return this;
        }

        public Builder setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
            return this;
        }

        public HttpRequest build() {
            if (url == null || url.trim().isEmpty()) {
                throw new IllegalArgumentException("URL cannot be null or empty");
            }
            return new HttpRequest(this);
        }
    }
}
