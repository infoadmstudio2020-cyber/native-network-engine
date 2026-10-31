package com.roni.library.network.http;

public interface HttpInterceptor {
    HttpResponse intercept(Chain chain) throws Exception;

    interface Chain {
        HttpRequest request();
        HttpResponse proceed(HttpRequest request) throws Exception;
    }
}
