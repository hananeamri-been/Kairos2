// ExerciseEntry.java
package com.example.kairos;

public class ExerciseEntry {
    public String name;
    public int sets;     // ✅ nouveau
    public int reps;
    public double weight;

    public ExerciseEntry() {}

    public ExerciseEntry(String name, int sets, int reps, double weight) {
        this.name = name;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
    }
}
