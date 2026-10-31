package com.roni.library.network;

import android.content.Context;

import com.roni.library.network.http.HttpClientEngine;
import com.roni.library.network.monitor.NetworkMonitor;
import com.roni.library.network.service.NativeNetworkService;

public class NetworkEngine {
    private final NativeNetworkEngine delegate;

    private NetworkEngine() {
        this.delegate = NativeNetworkEngine.getInstance();
    }

    private static class InstanceHolder {
        private static final NetworkEngine INSTANCE = new NetworkEngine();
    }

    public static NetworkEngine getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public void init(Context context, NativeNetworkEngineConfig config) {
        delegate.init(context, config);
    }

    public void init(Context context) {
        delegate.init(context);
    }

    public boolean isInitialized() {
        return delegate.isInitialized();
    }

    public NativeNetworkEngineConfig getConfig() {
        return delegate.getConfig();
    }

    public NativeNetworkService getService() {
        return delegate.getService();
    }

    public HttpClientEngine getHttpClient() {
        return delegate.getHttpClient();
    }

    public NetworkMonitor getNetworkMonitor() {
        return delegate.getNetworkMonitor();
    }

    public void release() {
        delegate.release();
    }
}
