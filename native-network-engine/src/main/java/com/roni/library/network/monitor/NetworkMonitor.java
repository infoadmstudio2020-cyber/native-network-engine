package com.roni.library.network.monitor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkMonitor {
    public interface NetworkStateListener {
        void onNetworkStateChanged(ConnectionType type, BandwidthQuality quality, boolean connected);
    }

    private volatile ConnectionType connectionType = ConnectionType.WIFI;
    private volatile BandwidthQuality bandwidthQuality = BandwidthQuality.EXCELLENT;
    private final Set<String> capabilities = new HashSet<>();
    private final List<NetworkStateListener> listeners = new CopyOnWriteArrayList<>();

    public NetworkMonitor() {
        capabilities.add("INTERNET");
        capabilities.add("VALIDATED");
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public boolean isConnected() {
        return connectionType != null && connectionType.isConnected();
    }

    public BandwidthQuality getBandwidthQuality() {
        return bandwidthQuality;
    }

    public Set<String> getCapabilities() {
        return Collections.unmodifiableSet(new HashSet<>(capabilities));
    }

    public void updateConnection(ConnectionType type, BandwidthQuality quality) {
        this.connectionType = type != null ? type : ConnectionType.NONE;
        this.bandwidthQuality = quality != null ? quality : BandwidthQuality.UNKNOWN;
        boolean connected = isConnected();
        for (NetworkStateListener listener : listeners) {
            try {
                listener.onNetworkStateChanged(this.connectionType, this.bandwidthQuality, connected);
            } catch (Throwable ignored) {}
        }
    }

    public void addListener(NetworkStateListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(NetworkStateListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public void clear() {
        listeners.clear();
    }
}
