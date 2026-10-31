package com.roni.library.network;

import com.roni.library.network.http.HttpClientEngine;
import com.roni.library.network.http.HttpRequest;
import com.roni.library.network.http.HttpResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RequestDispatcher {
    private final ExecutorService executor;
    private final HttpClientEngine clientEngine;

    public RequestDispatcher(HttpClientEngine clientEngine, int threadCount) {
        this.clientEngine = clientEngine != null ? clientEngine : new HttpClientEngine();
        this.executor = Executors.newFixedThreadPool(Math.max(1, threadCount));
    }

    public RequestDispatcher() {
        this(new HttpClientEngine(), 4);
    }

    public Future<HttpResponse> dispatch(HttpRequest request, HttpClientEngine.HttpCallback callback) {
        return executor.submit(() -> {
            try {
                HttpResponse resp = clientEngine.execute(request);
                if (callback != null) {
                    callback.onSuccess(resp);
                }
                return resp;
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
}
