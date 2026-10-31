package com.roni.library.network.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final int statusCode;
    private final Map<String, String> headers;
    private final String body;
    private final long latencyMs;

    public HttpResponse(int statusCode, Map<String, String> headers, String body, long latencyMs) {
        this.statusCode = statusCode;
        this.headers = headers != null ? Collections.unmodifiableMap(new HashMap<>(headers)) : Collections.emptyMap();
        this.body = body != null ? body : "";
        this.latencyMs = latencyMs;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String name) {
        if (name == null) return null;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (name.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public String getBody() {
        return body;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "statusCode=" + statusCode +
                ", isSuccessful=" + isSuccessful() +
                ", latencyMs=" + latencyMs +
                ", bodyLength=" + (body != null ? body.length() : 0) +
                '}';
    }
}
