package com.roni.library.network;

public class NativeNetworkEngineConfig {
    private final boolean debugEnabled;
    private final String tag;
    private final int timeout;
    private final int connectTimeout;
    private final int readTimeout;
    private final int maxRetries;

    private NativeNetworkEngineConfig(Builder builder) {
        this.debugEnabled = builder.debugEnabled;
        this.tag = builder.tag;
        this.timeout = builder.timeout;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.maxRetries = builder.maxRetries;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public String getTag() {
        return tag;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean debugEnabled = false;
        private String tag = "NativeNetworkEngine";
        private int timeout = 30000;
        private int connectTimeout = 15000;
        private int readTimeout = 15000;
        private int maxRetries = 3;

        public Builder() {}

        public Builder debugEnabled(boolean debugEnabled) {
            this.debugEnabled = debugEnabled;
            return this;
        }

        public Builder tag(String tag) {
            this.tag = (tag != null && !tag.trim().isEmpty()) ? tag : "NativeNetworkEngine";
            return this;
        }

        public Builder timeout(int timeout) {
            this.timeout = Math.max(1000, timeout);
            return this;
        }

        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = Math.max(500, connectTimeout);
            return this;
        }

        public Builder readTimeout(int readTimeout) {
            this.readTimeout = Math.max(500, readTimeout);
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = Math.max(0, maxRetries);
            return this;
        }

        public NativeNetworkEngineConfig build() {
            return new NativeNetworkEngineConfig(this);
        }
    }
}
