package com.example.kairos;

import java.util.ArrayList;
import java.util.List;

public class RunningPlan {
    /** jours planifiés (dim..sam) */
    public List<Boolean> freq = new ArrayList<>();
    /** valeurs de la semaine */
    public List<RunningEntry> entries = new ArrayList<>();

    public RunningPlan() {
        for (int i = 0; i < 7; i++) {
            freq.add(false);
            entries.add(new RunningEntry());
        }
    }
}
