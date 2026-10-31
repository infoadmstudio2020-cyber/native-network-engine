package com.roni.library.network.monitor;

public enum ConnectionType {
    NONE("none", false),
    WIFI("wifi", true),
    CELLULAR("cellular", true),
    ETHERNET("ethernet", true),
    BLUETOOTH("bluetooth", true),
    VPN("vpn", true),
    OTHER("other", true);

    private final String value;
    private final boolean connected;

    ConnectionType(String value, boolean connected) {
        this.value = value;
        this.connected = connected;
    }

    public String getValue() {
        return value;
    }

    public boolean isConnected() {
        return connected;
    }
}
