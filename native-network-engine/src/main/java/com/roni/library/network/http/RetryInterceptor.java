package com.roni.library.network.http;

import java.io.IOException;

public class RetryInterceptor implements HttpInterceptor {
    private final int maxRetries;
    private final long initialDelayMs;
    private int executionAttempts = 0;

    public RetryInterceptor(int maxRetries, long initialDelayMs) {
        this.maxRetries = Math.max(0, maxRetries);
        this.initialDelayMs = Math.max(0, initialDelayMs);
    }

    public RetryInterceptor(int maxRetries) {
        this(maxRetries, 200);
    }

    public RetryInterceptor() {
        this(3, 200);
    }

    public int getExecutionAttempts() {
        return executionAttempts;
    }

    @Override
    public HttpResponse intercept(Chain chain) throws Exception {
        executionAttempts = 0;
        Exception lastException = null;
        long currentDelay = initialDelayMs;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            executionAttempts++;
            try {
                HttpResponse response = chain.proceed(chain.request());
                if (response != null && response.getStatusCode() < 500) {
                    return response;
                }
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(currentDelay);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        return response;
                    }
                    currentDelay *= 2;
                } else {
                    return response;
                }
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(currentDelay);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                    currentDelay *= 2;
                }
            }
        }

        if (lastException != null) {
            throw lastException;
        }
        throw new IOException("Failed to execute HTTP request after " + executionAttempts + " attempts");
    }
}
