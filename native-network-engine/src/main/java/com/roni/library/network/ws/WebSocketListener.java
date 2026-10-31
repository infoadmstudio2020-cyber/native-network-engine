package com.roni.library.network.ws;

public interface WebSocketListener {
    void onOpen(WebSocketClient ws);
    void onMessage(WebSocketClient ws, String text);
    void onMessage(WebSocketClient ws, byte[] bytes);
    void onClosing(WebSocketClient ws, int code, String reason);
    void onClosed(WebSocketClient ws, int code, String reason);
    void onFailure(WebSocketClient ws, Throwable t);
}
