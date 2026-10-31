package com.roni.library.network.handler;

import com.roni.library.contracts.api.IApiCallback;
import com.roni.library.contracts.api.IApiHandler;
import com.roni.library.contracts.api.NativeApiError;
import com.roni.library.contracts.api.NativeApiRequest;
import com.roni.library.contracts.api.NativeApiResponse;
import com.roni.library.network.http.HttpClientEngine;
import com.roni.library.network.http.HttpMethod;
import com.roni.library.network.http.HttpRequest;
import com.roni.library.network.http.HttpResponse;
import com.roni.library.network.monitor.NetworkMonitor;
import com.roni.library.network.service.NativeNetworkService;
import com.roni.library.network.ws.WebSocketClient;
import com.roni.library.network.ws.WebSocketListener;

import java.util.HashMap;
import java.util.Map;

public class NetworkCommandHandler implements IApiHandler, WebSocketListener {
    private final NativeNetworkService service;

    public NetworkCommandHandler(NativeNetworkService service) {
        this.service = service;
    }

    @Override
    public String getPath() {
        return "network";
    }

    @Override
    public String getAction() {
        return "*";
    }

    public String getRoutePrefix() {
        return "network";
    }

    @Override
    public NativeApiResponse handleRequest(NativeApiRequest request) {
        if (request == null) {
            return NativeApiResponse.error("unknown", 400, new NativeApiError("NULL_REQUEST", "Request cannot be null"));
        }

        String reqId = request.getRequestId() != null ? request.getRequestId() : "unknown";
        String action = request.getAction();

        try {
            if ("httpRequest".equalsIgnoreCase(action)) {
                String url = request.getStringParam("url", "");
                String methodStr = request.getStringParam("method", "GET");
                String body = request.getStringParam("body", null);

                HttpRequest req = HttpRequest.builder()
                        .setUrl(url)
                        .setMethod(HttpMethod.fromString(methodStr))
                        .setBody(body)
                        .build();

                HttpResponse resp = service.getHttpClient().execute(req);
                Map<String, Object> data = new HashMap<>();
                data.put("statusCode", resp.getStatusCode());
                data.put("body", resp.getBody());
                data.put("latencyMs", resp.getLatencyMs());
                return NativeApiResponse.success(reqId, data);
            } else if ("wsConnect".equalsIgnoreCase(action)) {
                String sessionId = request.getStringParam("sessionId", "default");
                String url = request.getStringParam("url", "");
                WebSocketClient client = new WebSocketClient(url, this);
                service.registerWebSocket(sessionId, client);
                client.connect();

                Map<String, Object> data = new HashMap<>();
                data.put("status", "connecting");
                data.put("sessionId", sessionId);
                return NativeApiResponse.success(reqId, data);
            } else if ("wsSend".equalsIgnoreCase(action)) {
                String sessionId = request.getStringParam("sessionId", "default");
                String message = request.getStringParam("message", "");
                WebSocketClient client = service.getWebSocket(sessionId);
                if (client != null) {
                    boolean sent = client.send(message);
                    Map<String, Object> data = new HashMap<>();
                    data.put("sent", sent);
                    return NativeApiResponse.success(reqId, data);
                } else {
                    return NativeApiResponse.error(reqId, 404, new NativeApiError("WS_NOT_FOUND", "No active WebSocket session"));
                }
            } else if ("wsClose".equalsIgnoreCase(action)) {
                String sessionId = request.getStringParam("sessionId", "default");
                service.removeWebSocket(sessionId);
                Map<String, Object> data = new HashMap<>();
                data.put("status", "closed");
                return NativeApiResponse.success(reqId, data);
            } else if ("getNetworkStatus".equalsIgnoreCase(action)) {
                NetworkMonitor monitor = service.getNetworkMonitor();
                Map<String, Object> data = new HashMap<>();
                data.put("connected", monitor.isConnected());
                data.put("connectionType", monitor.getConnectionType().getValue());
                data.put("quality", monitor.getBandwidthQuality().getLabel());
                return NativeApiResponse.success(reqId, data);
            } else {
                return NativeApiResponse.error(reqId, 404, new NativeApiError("UNKNOWN_ACTION", "Action not supported: " + action));
            }
        } catch (Exception e) {
            return NativeApiResponse.error(reqId, 500, new NativeApiError("INTERNAL_ERROR", e.getMessage()));
        }
    }

    public void handle(NativeApiRequest request, IApiCallback callback) {
        if (callback == null) return;
        try {
            NativeApiResponse response = handleRequest(request);
            if (response.isSuccess()) {
                callback.onResponse(response);
            } else {
                callback.onError(response.getError());
            }
        } catch (Throwable t) {
            callback.onError(new NativeApiError("HANDLE_ERROR", t.getMessage()));
        }
    }

    public void onSuccess(HttpResponse resp) {}
    public void onError(Throwable error) {}

    @Override
    public void onOpen(WebSocketClient ws) {}

    @Override
    public void onMessage(WebSocketClient ws, String text) {}

    @Override
    public void onMessage(WebSocketClient ws, byte[] bytes) {}

    @Override
    public void onClosing(WebSocketClient ws, int code, String reason) {}

    @Override
    public void onClosed(WebSocketClient ws, int code, String reason) {}

    @Override
    public void onFailure(WebSocketClient ws, Throwable t) {}
}
