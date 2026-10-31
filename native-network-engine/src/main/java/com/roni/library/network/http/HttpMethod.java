package com.roni.library.network.http;

public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    HEAD,
    OPTIONS;

    public static HttpMethod fromString(String name) {
        if (name == null || name.trim().isEmpty()) {
            return GET;
        }
        try {
            return HttpMethod.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return GET;
        }
    }
}
