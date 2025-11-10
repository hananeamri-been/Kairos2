package com.example.kairos;

import java.util.ArrayList;

public class SportWeekData {
    public RunningPlan running = new RunningPlan();
    public GymPlan gym = new GymPlan();

    /** clone logique pour une nouvelle semaine : on garde les fréquences, on remet les valeurs */
    public SportWeekData cloneForNewWeek() {
        SportWeekData d = new SportWeekData();
        for (int i = 0; i < 7; i++) {
            d.running.freq.set(i, this.running.freq.get(i));
            d.running.entries.set(i, new RunningEntry(false, 0, 0, 0));

            d.gym.freq.set(i, this.gym.freq.get(i));
            d.gym.sessions.set(i, new ArrayList<>());
        }
        return d;
    }
}
