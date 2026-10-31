package com.roni.library.network;

import com.roni.library.network.http.HttpClientEngine;
import com.roni.library.network.http.HttpRequest;
import com.roni.library.network.http.HttpResponse;

import java.util.concurrent.Future;

public class HttpClient {
    private final HttpClientEngine engine;

    public HttpClient(HttpClientEngine engine) {
        this.engine = engine != null ? engine : new HttpClientEngine();
    }

    public HttpClient() {
        this(new HttpClientEngine());
    }

    public HttpResponse execute(HttpRequest request) throws Exception {
        return engine.execute(request);
    }

    public Future<HttpResponse> executeAsync(HttpRequest request, HttpClientEngine.HttpCallback callback) {
        return engine.executeAsync(request, callback);
    }

    public HttpClientEngine getEngine() {
        return engine;
    }
}
