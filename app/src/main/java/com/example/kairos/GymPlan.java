package com.example.kairos;

import java.util.ArrayList;
import java.util.List;

public class GymPlan {
    /** jours planifiés (dim..sam) */
    public List<Boolean> freq = new ArrayList<>();
    /** pour chaque jour : 0..n séances */
    public List<List<GymSession>> sessions = new ArrayList<>();

    public GymPlan() {
        for (int i = 0; i < 7; i++) {
            freq.add(false);
            sessions.add(new ArrayList<>());
        }
    }
}
