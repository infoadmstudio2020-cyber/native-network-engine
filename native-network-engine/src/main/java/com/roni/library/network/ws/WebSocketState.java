package com.roni.library.network.ws;

public enum WebSocketState {
    CONNECTING,
    OPEN,
    CLOSING,
    CLOSED;

    public boolean isOpen() {
        return this == OPEN;
    }
}
