package com.example.kairos;

public class RunningEntry {
    public boolean enabled;        // case du jour cochée
    public double distanceKm;      // km
    public double durationMin;     // minutes
    public double paceMinPerKm;    // min/km (calculé)

    public RunningEntry() {}

    public RunningEntry(boolean enabled, double distanceKm, double durationMin, double paceMinPerKm) {
        this.enabled = enabled;
        this.distanceKm = distanceKm;
        this.durationMin = durationMin;
        this.paceMinPerKm = paceMinPerKm;
    }

    public void recomputePace() {
        if (distanceKm > 0 && durationMin > 0) paceMinPerKm = durationMin / distanceKm;
        else paceMinPerKm = 0;
    }
}
