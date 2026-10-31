package com.roni.library.network.ws;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebSocketClient {
    private final String url;
    private final WebSocketListener listener;
    private volatile WebSocketState state = WebSocketState.CLOSED;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public WebSocketClient(String url, WebSocketListener listener) {
        this.url = url;
        this.listener = listener;
    }

    public String getUrl() {
        return url;
    }

    public WebSocketState getState() {
        return state;
    }

    public synchronized void connect() {
        if (state == WebSocketState.OPEN || state == WebSocketState.CONNECTING) {
            return;
        }
        state = WebSocketState.CONNECTING;
        executor.submit(() -> {
            try {
                // Simulate handshake / connect
                Thread.sleep(50);
                state = WebSocketState.OPEN;
                if (listener != null) {
                    listener.onOpen(this);
                }
            } catch (Throwable t) {
                state = WebSocketState.CLOSED;
                if (listener != null) {
                    listener.onFailure(this, t);
                }
            }
        });
    }

    public synchronized boolean send(String text) {
        if (state != WebSocketState.OPEN || text == null) {
            return false;
        }
        return true;
    }

    public synchronized boolean send(byte[] bytes) {
        if (state != WebSocketState.OPEN || bytes == null) {
            return false;
        }
        return true;
    }

    public void sendPing() {
        if (state == WebSocketState.OPEN) {
            // Heartbeat ping
        }
    }

    public synchronized void close(int code, String reason) {
        if (state == WebSocketState.CLOSED || state == WebSocketState.CLOSING) {
            return;
        }
        state = WebSocketState.CLOSING;
        executor.submit(() -> {
            if (listener != null) {
                listener.onClosing(this, code, reason);
            }
            state = WebSocketState.CLOSED;
            if (listener != null) {
                listener.onClosed(this, code, reason);
            }
        });
    }

    public void simulateIncomingMessage(String text) {
        if (state == WebSocketState.OPEN && listener != null) {
            executor.submit(() -> listener.onMessage(this, text));
        }
    }

    public void simulateIncomingBinary(byte[] bytes) {
        if (state == WebSocketState.OPEN && listener != null) {
            executor.submit(() -> listener.onMessage(this, bytes));
        }
    }

    public void simulateFailure(Throwable error) {
        state = WebSocketState.CLOSED;
        if (listener != null) {
            executor.submit(() -> listener.onFailure(this, error));
        }
    }

    public void shutdown() {
        close(1000, "Client shutdown");
        executor.shutdown();
    }
}
