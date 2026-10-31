package com.roni.library.network.service;

import com.roni.library.network.http.HttpClientEngine;
import com.roni.library.network.monitor.NetworkMonitor;
import com.roni.library.network.ws.WebSocketClient;
import com.roni.library.service.BaseService;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NativeNetworkService extends BaseService {
    private final HttpClientEngine httpClient = new HttpClientEngine();
    private final NetworkMonitor networkMonitor = new NetworkMonitor();
    private final Map<String, WebSocketClient> activeWebSockets = new ConcurrentHashMap<>();

    public NativeNetworkService() {
        super("NativeNetworkService");
    }

    public HttpClientEngine getHttpClient() {
        return httpClient;
    }

    public NetworkMonitor getNetworkMonitor() {
        return networkMonitor;
    }

    public void registerWebSocket(String sessionId, WebSocketClient client) {
        if (sessionId != null && client != null) {
            activeWebSockets.put(sessionId, client);
        }
    }

    public WebSocketClient getWebSocket(String sessionId) {
        return sessionId != null ? activeWebSockets.get(sessionId) : null;
    }

    public void removeWebSocket(String sessionId) {
        if (sessionId != null) {
            WebSocketClient client = activeWebSockets.remove(sessionId);
            if (client != null) {
                client.shutdown();
            }
        }
    }

    public Map<String, WebSocketClient> getActiveWebSockets() {
        return Collections.unmodifiableMap(activeWebSockets);
    }

    @Override
    public void onInitialize() {
        try {
            super.onInitialize();
        } catch (Exception ignored) {}
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDispose() {
        try {
            super.onDispose();
        } catch (Exception ignored) {}
        httpClient.shutdown();
        for (WebSocketClient client : activeWebSockets.values()) {
            client.shutdown();
        }
        activeWebSockets.clear();
        networkMonitor.clear();
    }
}
