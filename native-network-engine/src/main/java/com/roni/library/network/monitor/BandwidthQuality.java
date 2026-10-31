package com.roni.library.network.monitor;

public enum BandwidthQuality {
    UNKNOWN("Unknown", 0),
    POOR("Poor", 150),
    MODERATE("Moderate", 550),
    GOOD("Good", 2000),
    EXCELLENT("Excellent", 5000);

    private final String label;
    private final int minKbps;

    BandwidthQuality(String label, int minKbps) {
        this.label = label;
        this.minKbps = minKbps;
    }

    public String getLabel() {
        return label;
    }

    public int getMinKbps() {
        return minKbps;
    }
}
