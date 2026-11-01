# NativeNetworkEngine (Layer 2 Domain Engine)

[![Release](https://img.shields.io/badge/Release-v1.0.4-blue.svg)](https://github.com/infoadmstudio2020-cyber/native-network-engine/releases)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)
[![License](https://img.shields.io/badge/License-Apache-2.0-blue.svg)](LICENSE)
[![Architecture](https://img.shields.io/badge/Architecture-NativeRuntime%20L2-orange.svg)](#architecture)

High-performance, resilient HTTP/HTTPS communication, real-time WebSocket protocol, adaptive network state monitoring, connection pooling, SSL pinning, and file transfer engine for the **NativeRuntime Android SDK Ecosystem**.

---

## Architecture Overview

`NativeNetworkEngine` is classified under **Layer 2: Domain Engine Subsystems** within the 6-layer NativeRuntime architecture.

```
+-----------------------------------------------------------------------------------+
| Layer 4 / 5: Application / Master SDK (NativeRuntimeSDK)                          |
+-----------------------------------------------------------------------------------+
                                        | Dispatches RPC
+-----------------------------------------------------------------------------------+
| Layer 3: Transport Bridge (NativeWebBridgeSDK)                                    |
+-----------------------------------------------------------------------------------+
                                        | Routes Network Requests
+-----------------------------------------------------------------------------------+
| Layer 2: Domain Engines                                                           |
|  -> NativeNetworkEngine (HttpClient • WebSocket • Monitor • SslPinning • Download)|
+-----------------------------------------------------------------------------------+
                                        | Managed Service & Diagnostics
+-----------------------------------------------------------------------------------+
| Layer 1: Core Infrastructure (NativeServiceSDK, NativeLoggerSDK)                  |
+-----------------------------------------------------------------------------------+
                                        | Inherits
+-----------------------------------------------------------------------------------+
| Layer 0: Universal Foundation Contracts (NativeRuntimeContracts)                  |
+-----------------------------------------------------------------------------------+
```

---

## Key Subsystems & Features

1. **HTTP Client Engine (`HttpClientEngine` / `HttpClient`)**:
   - Synchronous and asynchronous non-blocking HTTP requests (`GET`, `POST`, `PUT`, `DELETE`).
   - Built-in `RetryInterceptor` with exponential backoff and transient failure recovery.
   - Pluggable `MockTransport` interface for reliable offline UI and unit testing.
2. **Real-time WebSocket Client (`WebSocketClient`)**:
   - RFC 6455 compliant client supporting text/binary payloads, automatic ping-pong keep-alive, and connection lifecycle callbacks.
3. **Adaptive Network Monitor (`NetworkMonitor`)**:
   - Real-time connection detection (`WIFI`, `CELLULAR`, `ETHERNET`, `NONE`).
   - Dynamic bandwidth quality estimation (`EXCELLENT`, `GOOD`, `MODERATE`, `POOR`).
   - Observer pattern with multi-listener callbacks.
4. **SSL / TLS Certificate Pinning (`SslPinningManager`)**:
   - SHA-256 public key hash pinning against hostnames to prevent Man-in-the-Middle (MitM) attacks.
5. **Background Download Manager (`DownloadManager`)**:
   - Multi-threaded file streaming with chunk progress reporting and file persistence.
6. **Command Handler (`NetworkCommandHandler`)**:
   - Standardized `NativeApiRequest` / `NativeApiResponse` RPC dispatcher for web bridge integration.

---

## Consumer Integration Guide

### 1. Add Repository in `settings.gradle`
```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### 2. Declare Dependency in `app/build.gradle`
```groovy
dependencies {
    implementation 'com.github.infoadmstudio2020-cyber:NativeRuntimeContracts:v1.0.1'
    implementation 'com.github.infoadmstudio2020-cyber:NativeServiceSDK:1.0.1'
    implementation 'com.github.infoadmstudio2020-cyber:native-network-engine:1.0.4'
}
```

### 3. ProGuard / R8 Rules (`consumer-rules.pro`)
Consumer rules are automatically included in the AAR:
```proguard
-keep public class com.roni.library.network.** { public *; }
-keep interface com.roni.library.network.** { *; }
-keep enum com.roni.library.network.** { *; }
-keep class com.roni.library.network.http.** { *; }
-keep class com.roni.library.network.monitor.** { *; }
-keep class com.roni.library.network.ws.** { *; }
-keep class com.roni.library.network.service.** { *; }
-keep class com.roni.library.network.handler.** { *; }
-keep class com.roni.library.network.NativeNetworkEngineResult { *; }
-keep class com.roni.library.network.NativeNetworkEngineConfig { *; }
```

---

## Code Examples

### Initialization
```java
NativeNetworkEngineConfig config = NativeNetworkEngineConfig.builder()
        .debugEnabled(BuildConfig.DEBUG)
        .tag("NetworkEngine")
        .timeout(15000)
        .connectTimeout(5000)
        .readTimeout(10000)
        .maxRetries(3)
        .build();

NativeNetworkEngine.getInstance().init(context, config);
```

### Async HTTP Request
```java
HttpRequest request = HttpRequest.builder()
        .setUrl("https://api.example.com/v1/users")
        .setMethod(HttpMethod.POST)
        .addHeader("Authorization", "Bearer my_jwt_token")
        .setBody("{\"role\":\"tester\"}")
        .build();

NativeNetworkEngine.getInstance().getHttpClient().executeAsync(request, new HttpClientEngine.HttpCallback() {
    @Override
    public void onSuccess(HttpResponse response) {
        int code = response.getStatusCode();
        String body = response.getBody();
    }

    @Override
    public void onFailure(Throwable t) {
        // Handle network exception
    }
});
```

### Real-time WebSocket Connection
```java
WebSocketClient ws = new WebSocketClient("wss://echo.example.com", new WebSocketListener() {
    @Override
    public void onOpen(WebSocketClient client) {
        client.send("{\"type\":\"greeting\"}");
    }

    @Override
    public void onMessage(WebSocketClient client, String text) {
        // Handle incoming WebSocket message
    }

    @Override
    public void onClosed(WebSocketClient client, int code, String reason) {}

    @Override
    public void onFailure(WebSocketClient client, Throwable t) {}
});

ws.connect();
```

### Network Monitoring
```java
NetworkMonitor monitor = NativeNetworkEngine.getInstance().getNetworkMonitor();
boolean isOnline = monitor.isConnected();
ConnectionType type = monitor.getConnectionType();

monitor.addListener((connType, quality, connected) -> {
    // React to connectivity or bandwidth change
});
```

---

## Interactive Companion Demo App

The companion demonstration app in `:app` includes:
- **Network Dashboard:** Real-time engine state, active connection type, and bandwidth quality.
- **Feature Explorer:** Live forms to execute GET/POST requests, burst tester (x5), simulated network state switches (WIFI / 4G / Offline), and real-time WebSocket echo test.
- **Integration Test Center:** Automated 9-step test suite with instant PASS/FAIL feedback.
- **Runtime Console:** Real-time scrolling request/response log with clear and copy capabilities.

---

## Testing & Verification

Run JVM Unit Tests:
```bash
./gradlew :native-network-engine:test
```

Build Release Artifacts:
```bash
./gradlew clean :native-network-engine:assembleRelease :app:assembleRelease
```

---

## License

```
Copyright 2026 infoadmstudio2020-cyber

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
