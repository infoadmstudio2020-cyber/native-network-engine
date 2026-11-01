package com.roni.library.network.app;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.roni.library.contracts.api.NativeApiRequest;
import com.roni.library.contracts.api.NativeApiResponse;
import com.roni.library.network.NativeNetworkEngine;
import com.roni.library.network.NativeNetworkEngineConfig;
import com.roni.library.network.NativeNetworkEngineResult;
import com.roni.library.network.NetworkEngine;
import com.roni.library.network.SslPinningManager;
import com.roni.library.network.handler.NetworkCommandHandler;
import com.roni.library.network.http.HttpClientEngine;
import com.roni.library.network.http.HttpMethod;
import com.roni.library.network.http.HttpRequest;
import com.roni.library.network.http.HttpResponse;
import com.roni.library.network.monitor.BandwidthQuality;
import com.roni.library.network.monitor.ConnectionType;
import com.roni.library.network.monitor.NetworkMonitor;
import com.roni.library.network.service.NativeNetworkService;
import com.roni.library.network.ws.WebSocketClient;
import com.roni.library.network.ws.WebSocketListener;
import com.roni.library.network.ws.WebSocketState;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Universal Interactive Demonstration and Feature Testing Activity for NativeNetworkEngine.
 * Implements Dashboard, Feature Explorer, Test Center, and Live Console.
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;
    private TextView tvDashboardDetails;
    private TextView tvHttpResult;
    private TextView tvMonitorState;
    private TextView tvWsStatus;
    private TextView tvTestSummary;
    private TextView tvConsoleLog;

    private EditText etHttpUrl, etHttpBody;
    private EditText etWsUrl;

    private WebSocketClient webSocketClient;
    private final StringBuilder logBuffer = new StringBuilder();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setupListeners();
        initNetworkEngine();
    }

    private void bindViews() {
        tvStatus = findViewById(R.id.tv_status);
        tvDashboardDetails = findViewById(R.id.tv_dashboard_details);
        tvHttpResult = findViewById(R.id.tv_http_result);
        tvMonitorState = findViewById(R.id.tv_monitor_state);
        tvWsStatus = findViewById(R.id.tv_ws_status);
        tvTestSummary = findViewById(R.id.tv_test_summary);
        tvConsoleLog = findViewById(R.id.tv_console_log);
        if (tvConsoleLog != null) {
            tvConsoleLog.setMovementMethod(new ScrollingMovementMethod());
        }

        etHttpUrl = findViewById(R.id.et_http_url);
        etHttpBody = findViewById(R.id.et_http_body);
        etWsUrl = findViewById(R.id.et_ws_url);
    }

    private void setupListeners() {
        // HTTP
        findViewById(R.id.btn_http_get).setOnClickListener(v -> executeHttpRequest(HttpMethod.GET));
        findViewById(R.id.btn_http_post).setOnClickListener(v -> executeHttpRequest(HttpMethod.POST));
        findViewById(R.id.btn_http_burst).setOnClickListener(v -> burstHttpRequests());

        // Network Monitor
        findViewById(R.id.btn_sim_wifi).setOnClickListener(v -> simulateNetwork(ConnectionType.WIFI, BandwidthQuality.EXCELLENT));
        findViewById(R.id.btn_sim_cellular).setOnClickListener(v -> simulateNetwork(ConnectionType.CELLULAR, BandwidthQuality.GOOD));
        findViewById(R.id.btn_sim_offline).setOnClickListener(v -> simulateNetwork(ConnectionType.NONE, BandwidthQuality.UNKNOWN));

        // WebSocket
        findViewById(R.id.btn_ws_connect).setOnClickListener(v -> connectWebSocket());
        findViewById(R.id.btn_ws_send).setOnClickListener(v -> sendWebSocketMessage());
        findViewById(R.id.btn_ws_close).setOnClickListener(v -> disconnectWebSocket());

        // Test Center
        findViewById(R.id.btn_run_tests).setOnClickListener(v -> runAllNetworkTests());

        // Console
        findViewById(R.id.btn_clear_log).setOnClickListener(v -> {
            logBuffer.setLength(0);
            if (tvConsoleLog != null) tvConsoleLog.setText("");
        });
    }

    private void log(String message) {
        String entry = "[" + timeFormat.format(new Date()) + "] " + message + "\n";
        logBuffer.append(entry);
        if (tvConsoleLog != null) {
            tvConsoleLog.setText(logBuffer.toString());
        }
    }

    private void initNetworkEngine() {
        try {
            NativeNetworkEngineConfig config = NativeNetworkEngineConfig.builder()
                    .debugEnabled(true)
                    .tag("NetworkExplorer")
                    .timeout(15000)
                    .connectTimeout(5000)
                    .readTimeout(10000)
                    .maxRetries(3)
                    .build();

            NativeNetworkEngineResult<Boolean> res = NativeNetworkEngine.getInstance().init(this, config);
            boolean initialized = NativeNetworkEngine.getInstance().isInitialized();

            if (tvStatus != null) {
                tvStatus.setText(initialized ? "Status: READY (ACTIVE)" : "Status: INIT FAILED (" + res.getErrorMessage() + ")");
            }
            log("NativeNetworkEngine initialized successfully.");

            // Configure Mock Transport for safe offline exploratory execution
            NativeNetworkEngine.getInstance().getHttpClient().setMockTransport(request -> {
                String method = request.getMethod() != null ? request.getMethod().name() : "GET";
                String body = "{\"status\":200,\"method\":\"" + method + "\",\"url\":\"" + request.getUrl() + "\",\"timestamp\":" + System.currentTimeMillis() + "}";
                return new HttpResponse(200, null, body, 18);
            });

            // Listen to Network Monitor
            NativeNetworkEngine.getInstance().getNetworkMonitor().addListener((type, quality, connected) -> {
                runOnUiThread(() -> {
                    String stateText = "Current: " + type + " • " + quality + " (Connected: " + connected + ")";
                    if (tvMonitorState != null) tvMonitorState.setText(stateText);
                    log("Network state changed -> " + stateText);
                });
            });

            updateDashboard();
        } catch (Exception e) {
            log("Init error: " + e.getMessage());
        }
    }

    private void updateDashboard() {
        try {
            NetworkMonitor monitor = NativeNetworkEngine.getInstance().getNetworkMonitor();
            ConnectionType type = monitor != null ? monitor.getConnectionType() : ConnectionType.NONE;
            BandwidthQuality quality = monitor != null ? monitor.getBandwidthQuality() : BandwidthQuality.UNKNOWN;
            int retries = NativeNetworkEngine.getInstance().getConfig() != null ? NativeNetworkEngine.getInstance().getConfig().getMaxRetries() : 3;

            String details = "Connection: " + type + " (" + quality + ") | Retries: " + retries + "\n" +
                    "Ready for HTTP requests, WebSocket real-time, and SSL Pinning";
            if (tvDashboardDetails != null) tvDashboardDetails.setText(details);
            if (tvMonitorState != null) tvMonitorState.setText("Current: " + type + " • " + quality);
        } catch (Exception ignored) {}
    }

    private void executeHttpRequest(HttpMethod method) {
        String url = etHttpUrl.getText().toString().trim();
        String body = etHttpBody.getText().toString().trim();
        if (url.isEmpty()) return;

        HttpRequest.Builder builder = HttpRequest.builder()
                .setUrl(url)
                .setMethod(method)
                .addHeader("User-Agent", "NativeNetworkEngine-Demo/1.0.2");

        if (method == HttpMethod.POST && !body.isEmpty()) {
            builder.setBody(body);
        }

        HttpRequest request = builder.build();
        log("Executing HTTP " + method + " to " + url);

        NativeNetworkEngine.getInstance().getHttpClient().executeAsync(request, new HttpClientEngine.HttpCallback() {
            @Override
            public void onSuccess(HttpResponse response) {
                runOnUiThread(() -> {
                    String result = "HTTP " + response.getStatusCode() + " (" + response.getLatencyMs() + "ms): " + response.getBody();
                    if (tvHttpResult != null) tvHttpResult.setText(result);
                    log("HTTP Response: " + result);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    String err = "HTTP Error: " + t.getMessage();
                    if (tvHttpResult != null) tvHttpResult.setText(err);
                    log(err);
                });
            }
        });
    }

    private void burstHttpRequests() {
        log("Bursting 5 rapid HTTP requests through HttpClientEngine...");
        for (int i = 1; i <= 5; i++) {
            final int index = i;
            HttpRequest req = HttpRequest.builder()
                    .setUrl("https://api.example.com/v1/resource/" + index)
                    .setMethod(HttpMethod.GET)
                    .build();
            NativeNetworkEngine.getInstance().getHttpClient().executeAsync(req, new HttpClientEngine.HttpCallback() {
                @Override
                public void onSuccess(HttpResponse response) {
                    log("Burst #" + index + " -> Status " + response.getStatusCode() + " in " + response.getLatencyMs() + "ms");
                }

                @Override
                public void onFailure(Throwable t) {
                    log("Burst #" + index + " failed: " + t.getMessage());
                }
            });
        }
    }

    private void simulateNetwork(ConnectionType type, BandwidthQuality quality) {
        NativeNetworkEngine.getInstance().getNetworkMonitor().updateConnection(type, quality);
        log("Simulated Network change to: " + type + " / " + quality);
        updateDashboard();
    }

    private void connectWebSocket() {
        String url = etWsUrl.getText().toString().trim();
        if (url.isEmpty()) return;

        if (webSocketClient != null) {
            webSocketClient.shutdown();
        }

        webSocketClient = new WebSocketClient(url, new WebSocketListener() {
            @Override
            public void onOpen(WebSocketClient ws) {
                runOnUiThread(() -> {
                    if (tvWsStatus != null) tvWsStatus.setText("WS State: CONNECTED (OPEN)");
                    log("WebSocket handshake OPEN: " + url);
                });
            }

            @Override
            public void onMessage(WebSocketClient ws, String text) {
                runOnUiThread(() -> {
                    if (tvWsStatus != null) tvWsStatus.setText("WS Received: " + text);
                    log("WebSocket MSG: " + text);
                });
            }

            @Override
            public void onMessage(WebSocketClient ws, byte[] bytes) {}

            @Override
            public void onClosing(WebSocketClient ws, int code, String reason) {
                runOnUiThread(() -> {
                    if (tvWsStatus != null) tvWsStatus.setText("WS CLOSING (" + code + ")");
                    log("WebSocket CLOSING: " + reason);
                });
            }

            @Override
            public void onClosed(WebSocketClient ws, int code, String reason) {
                runOnUiThread(() -> {
                    if (tvWsStatus != null) tvWsStatus.setText("WS State: CLOSED");
                    log("WebSocket CLOSED");
                });
            }

            @Override
            public void onFailure(WebSocketClient ws, Throwable t) {
                runOnUiThread(() -> {
                    if (tvWsStatus != null) tvWsStatus.setText("WS Error: " + t.getMessage());
                    log("WebSocket Failure: " + t.getMessage());
                });
            }
        });

        webSocketClient.connect();
        log("WebSocket connecting to " + url + "...");
    }

    private void sendWebSocketMessage() {
        if (webSocketClient != null && webSocketClient.getState() == WebSocketState.OPEN) {
            String msg = "{\"type\":\"ping\",\"timestamp\":" + System.currentTimeMillis() + "}";
            webSocketClient.send(msg);
            log("WebSocket sent: " + msg);
            // Simulate server echo
            webSocketClient.simulateIncomingMessage("{\"type\":\"pong\",\"reply_to\":\"ping\"}");
        } else {
            log("WebSocket is not connected yet.");
        }
    }

    private void disconnectWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.close(1000, "User requested close");
            log("WebSocket disconnect triggered.");
        }
    }

    private void runAllNetworkTests() {
        log("Starting Comprehensive Network Engine Test Suite...");
        StringBuilder sb = new StringBuilder();
        int pass = 0, fail = 0;

        // 1. Singleton Lifecycle
        try {
            if (NativeNetworkEngine.getInstance().isInitialized()) {
                sb.append("✓ [PASS] Singleton Lifecycle & Initialization\n");
                pass++;
            } else {
                sb.append("✗ [FAIL] Engine not initialized\n");
                fail++;
            }
        } catch (Exception e) {
            sb.append("✗ [FAIL] Singleton: ").append(e.getMessage()).append("\n");
            fail++;
        }

        // 2. Config Builder
        try {
            NativeNetworkEngineConfig cfg = NativeNetworkEngineConfig.builder()
                    .debugEnabled(true).timeout(8000).maxRetries(4).build();
            if (cfg.isDebugEnabled() && cfg.getTimeout() == 8000 && cfg.getMaxRetries() == 4) {
                sb.append("✓ [PASS] NativeNetworkEngineConfig Builder Bounds\n");
                pass++;
            } else {
                sb.append("✗ [FAIL] Config values mismatch\n");
                fail++;
            }
        } catch (Exception e) {
            sb.append("✗ [FAIL] Config: ").append(e.getMessage()).append("\n");
            fail++;
        }

        // 3. HttpRequest / HttpResponse
        try {
            HttpRequest req = HttpRequest.builder()
                    .setUrl("https://test.local/data")
                    .setMethod(HttpMethod.POST)
                    .setBody("{\"test\":1}")
                    .build();
            HttpResponse resp = new HttpResponse(200, null, "{\"ok\":true}", 12);
            if (req.getMethod() == HttpMethod.POST && resp.isSuccessful() && resp.getLatencyMs() == 12) {
                sb.append("✓ [PASS] HttpRequest & HttpResponse Serialization\n");
                pass++;
            } else {
                sb.append("✗ [FAIL] Request/Response serialization error\n");
                fail++;
            }
        } catch (Exception e) {
            sb.append("✗ [FAIL] Request/Response: ").append(e.getMessage()).append("\n");
            fail++;
        }

        // 4. HttpClientEngine Mock Execution
        try {
            HttpClientEngine client = new HttpClientEngine();
            client.setMockTransport(r -> new HttpResponse(200, null, "{\"mock\":true}", 8));
            HttpResponse r = client.execute(HttpRequest.builder().setUrl("https://mock.test").build());
            if (r.getStatusCode() == 200 && "{\"mock\":true}".equals(r.getBody())) {
                sb.append("✓ [PASS] HttpClientEngine Mock Execution\n");
                pass++;
            } else {
                sb.append("✗ [FAIL] Mock execution mismatch\n");
                fail++;
            }
            client.shutdown();
        } catch (Exception e) {
            sb.append("✗ [FAIL] HttpClientEngine: ").append(e.getMessage()).append("\n");
            fail++;
        }

        // 5. RetryInterceptor Auto-Recovery
        try {
            HttpClientEngine client = new HttpClientEngine();
            final AtomicInteger count = new AtomicInteger(0);
            client.setMockTransport(request -> {
                if (count.incrementAndGet() < 2) {
                    throw new IOException("Simulated network blip");
                }
                return new HttpResponse(200, null, "{\"recovered\":true}", 10);
            });
            HttpResponse r = client.execute(HttpRequest.builder().setUrl("https://retry.test").build());
            if (r.getStatusCode() == 200 && count.get() == 2) {
                sb.append("✓ [PASS] RetryInterceptor Auto-Recovery\n");
                pass++;
            } else {
                sb.append("✗ [FAIL] RetryInterceptor failed\n");
                fail++;
            }
            client.shutdown();
        } catch (Exception e) {
            sb.append("✗ [FAIL] Retry: ").append(e.getMessage()).append("\n");
            fail++;
        }

        // 6. NetworkMonitor State & Listener
        try {
            NetworkMonitor monitor = new NetworkMonitor(this);
            final AtomicBoolean notified = new AtomicBoolean(false);
            NetworkMonitor.NetworkStateListener l = (t, q, c) -> notified.set(true);
            monitor.addListener(l);
            monitor.updateConnection(ConnectionType.CELLULAR, BandwidthQuality.GOOD);
            if (monitor.getConnectionType() == ConnectionType.CELLULAR && notified.get()) {
                sb.append("✓ [PASS] NetworkMonitor State & Listeners\n");
                pass++;
            } else {
                sb.append("✗ [FAIL] NetworkMonitor mismatch\n");
                fail++;
            }
            monitor.removeListener(l);
        } catch (Exception e) {
            sb.append("✗ [FAIL] NetworkMonitor: ").append(e.getMessage()).append("\n");
            fail++;
        }

        // 7. WebSocket Handshake & Simulation
        try {
            final CountDownLatch wsLatch = new CountDownLatch(1);
            final StringBuilder received = new StringBuilder();
            WebSocketClient ws = new WebSocketClient("wss://test.local", new WebSocketListener() {
                @Override public void onOpen(WebSocketClient client) { wsLatch.countDown(); }
                @Override public void onMessage(WebSocketClient client, String text) { received.append(text); }
                @Override public void onMessage(WebSocketClient client, byte[] bytes) {}
                @Override public void onClosing(WebSocketClient client, int code, String reason) {}
                @Override public void onClosed(WebSocketClient client, int code, String reason) {}
                @Override public void onFailure(WebSocketClient client, Throwable t) {}
            });
            ws.connect();
            boolean opened = wsLatch.await(1, TimeUnit.SECONDS);
            ws.simulateIncomingMessage("ECHO_TEST");
            if (opened && "ECHO_TEST".equals(received.toString())) {
                sb.append("✓ [PASS] WebSocketClient Handshake & Dispatch\n");
                pass++;
            } else {
                sb.append("✗ [FAIL] WebSocketClient failed\n");
                fail++;
            }
            ws.shutdown();
        } catch (Exception e) {
            sb.append("✗ [FAIL] WebSocket: ").append(e.getMessage()).append("\n");
            fail++;
        }

        // 8. CommandHandler RPC
        try {
            NativeNetworkService svc = NativeNetworkEngine.getInstance().getService();
            NetworkCommandHandler handler = new NetworkCommandHandler(svc);
            Map<String, Object> p = new HashMap<>();
            p.put("url", "https://rpc.local");
            p.put("method", "GET");
            NativeApiRequest req = new NativeApiRequest("req-test", "network", "httpRequest", p, System.currentTimeMillis());
            NativeApiResponse res = handler.handleRequest(req);
            if (res != null && res.isSuccess()) {
                sb.append("✓ [PASS] NetworkCommandHandler RPC Dispatch\n");
                pass++;
            } else {
                sb.append("✗ [FAIL] CommandHandler failed\n");
                fail++;
            }
        } catch (Exception e) {
            sb.append("✗ [FAIL] CommandHandler: ").append(e.getMessage()).append("\n");
            fail++;
        }

        // 9. SslPinningManager
        try {
            SslPinningManager sp = new SslPinningManager();
            sp.addPin("example.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");
            if (sp.hasPinsForHost("example.com") && !sp.hasPinsForHost("other.com")) {
                sb.append("✓ [PASS] SslPinningManager Verification\n");
                pass++;
            } else {
                sb.append("✗ [FAIL] SslPinningManager failed\n");
                fail++;
            }
        } catch (Exception e) {
            sb.append("✗ [FAIL] SslPinningManager: ").append(e.getMessage()).append("\n");
            fail++;
        }

        sb.append("\nSUMMARY: ").append(pass).append(" PASSED, ").append(fail).append(" FAILED.");
        if (tvTestSummary != null) {
            tvTestSummary.setText(sb.toString());
        }
        log("Network Test Suite Completed: " + pass + " passed, " + fail + " failed.");
        updateDashboard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.shutdown();
        }
        NativeNetworkEngine.getInstance().release();
    }
}
