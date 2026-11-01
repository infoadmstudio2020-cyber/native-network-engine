# Changelog

All notable changes to NativeNetworkEngine are documented here.
Format based on [Keep a Changelog](https://keepachangelog.com).

## [1.0.5] — 2026-11-01

### Added
- Upgraded companion demonstration app to a complete interactive testing application with Dashboard, HTTP Explorer, Network Simulator, WebSocket client, Test Center, and Live Console.
- Enhanced ProGuard / R8 consumer preservation rules in `consumer-rules.pro` covering all HTTP, WebSocket, monitor, service, and handler models.
- Comprehensive consumer integration guide and architecture documentation in `README.md`.
- Expanded automated integration test suite covering 9 core network engine subsystems.
- Pre-configured GitHub Actions CI and Release pipelines.

## [1.0.0] — 2026-10-31

### Added
- Initial release of NativeNetworkEngine (Layer 2 Domain Engine).
- HTTP client engine with pluggable MockTransport and RetryInterceptor.
- RFC 6455 compliant WebSocket client.
- Adaptive network connection and bandwidth monitor.
- Multi-threaded file download manager.
- SSL / TLS certificate pinning manager.
- Standardized RPC command handler for bridge dispatch.
