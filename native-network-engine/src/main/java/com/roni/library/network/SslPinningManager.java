package com.roni.library.network;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SslPinningManager {
    private final Map<String, Set<String>> hostPins = new ConcurrentHashMap<>();

    public SslPinningManager() {}

    public void addPin(String hostname, String sha256Pin) {
        if (hostname != null && sha256Pin != null) {
            hostPins.computeIfAbsent(hostname.toLowerCase(), k -> Collections.synchronizedSet(new HashSet<>()))
                    .add(sha256Pin);
        }
    }

    public boolean hasPinsForHost(String hostname) {
        if (hostname == null) return false;
        Set<String> pins = hostPins.get(hostname.toLowerCase());
        return pins != null && !pins.isEmpty();
    }

    public Set<String> getPins(String hostname) {
        if (hostname == null) return Collections.emptySet();
        Set<String> pins = hostPins.get(hostname.toLowerCase());
        return pins != null ? Collections.unmodifiableSet(pins) : Collections.emptySet();
    }

    public void clear() {
        hostPins.clear();
    }
}
