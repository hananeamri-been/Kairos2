package com.example.kairos;

import java.util.ArrayList;
import java.util.List;


public class GymSession {
    public String muscleGroup;                // Shoulders / Chest / Legs ...
    public List<ExerciseEntry> exercises = new ArrayList<>();

    public GymSession() {}
    public GymSession(String muscleGroup) { this.muscleGroup = muscleGroup; }
}
