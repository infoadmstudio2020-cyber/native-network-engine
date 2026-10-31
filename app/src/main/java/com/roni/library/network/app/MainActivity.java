package com.roni.library.network.app;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.roni.library.network.NativeNetworkEngine;
import com.roni.library.network.NativeNetworkEngineConfig;
import com.roni.library.network.http.HttpClientEngine;
import com.roni.library.network.http.HttpMethod;
import com.roni.library.network.http.HttpRequest;
import com.roni.library.network.http.HttpResponse;
import com.roni.library.network.monitor.BandwidthQuality;
import com.roni.library.network.monitor.ConnectionType;
import com.roni.library.network.ws.WebSocketClient;
import com.roni.library.network.ws.WebSocketListener;

public class MainActivity extends AppCompatActivity {
    private TextView tvStatus;
    private NativeNetworkEngine networkEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tv_status);
        tvStatus.setText("NativeNetworkEngine: Initializing...");

        // Initialize engine
        networkEngine = NativeNetworkEngine.getInstance();
        NativeNetworkEngineConfig config = NativeNetworkEngineConfig.builder()
                .debugEnabled(true)
                .tag("DemoApp")
                .timeout(15000)
                .build();
        networkEngine.init(this, config);

        // Verify monitor
        ConnectionType connType = networkEngine.getNetworkMonitor().getConnectionType();
        BandwidthQuality quality = networkEngine.getNetworkMonitor().getBandwidthQuality();

        // Verify HTTP client with MockTransport
        HttpClientEngine httpClient = networkEngine.getHttpClient();
        httpClient.setMockTransport(request -> new HttpResponse(200, null, "{\"status\":\"ok\",\"service\":\"NativeNetworkEngine\"}", 15));

        HttpRequest request = HttpRequest.builder()
                .setUrl("https://api.example.com/v1/ping")
                .setMethod(HttpMethod.GET)
                .build();

        httpClient.executeAsync(request, new HttpClientEngine.HttpCallback() {
            @Override
            public void onSuccess(HttpResponse response) {
                runOnUiThread(() -> {
                    String statusText = "NativeNetworkEngine v1.0.1 Ready!\n\n" +
                            "Network: " + connType.getValue() + " (" + quality.getLabel() + ")\n" +
                            "HTTP Status: " + response.getStatusCode() + "\n" +
                            "HTTP Body: " + response.getBody() + "\n" +
                            "Latency: " + response.getLatencyMs() + "ms";
                    tvStatus.setText(statusText);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> tvStatus.setText("HTTP Error: " + t.getMessage()));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkEngine != null) {
            networkEngine.release();
        }
    }
}
