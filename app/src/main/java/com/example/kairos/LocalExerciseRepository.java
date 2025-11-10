package com.example.kairos;

import java.util.*;

public class LocalExerciseRepository implements ExerciseRepository {
    private final Map<String, List<String>> data = new HashMap<>();

    public LocalExerciseRepository() { load(); }

    @Override
    public void getByGroup(String group, String search, Callback cb) {
        List<String> list = data.getOrDefault(group, Collections.emptyList());
        List<String> out = new ArrayList<>();
        String s = (search == null) ? "" : search.trim().toLowerCase(Locale.US);
        for (String ex : list) {
            if (s.isEmpty() || ex.toLowerCase(Locale.US).contains(s)) out.add(ex);
        }
        cb.onSuccess(out);
    }

    private void load() {
        data.put("Chest", Arrays.asList(
                "Bench Press","Incline Bench Press","Decline Bench Press","Chest Fly","Cable Crossover",
                "Push-Ups","Dumbbell Bench Press","Pec Deck","Svend Press","Machine Chest Press",
                "Incline Dumbbell Fly","Decline Dumbbell Press","Diamond Push-Up","Dumbbell Pullover","Single Arm Chest Press"
        ));
        data.put("Back", Arrays.asList(
                "Pull-Up","Chin-Up","Lat Pulldown","Seated Cable Row","Bent-Over Row","Deadlift",
                "T-Bar Row","One Arm Dumbbell Row","Reverse Fly","Cable Row","Inverted Row",
                "Machine Pulldown","Good Morning","Straight Arm Pulldown","Kroc Row"
        ));
        data.put("Shoulders", Arrays.asList(
                "Overhead Press","Arnold Press","Lateral Raise","Front Raise","Rear Delt Fly","Upright Row",
                "Face Pull","Military Press","Seated Dumbbell Press","Cable Lateral Raise","Dumbbell Shrug",
                "Machine Shoulder Press","Reverse Cable Fly","Barbell Overhead Press","Standing Dumbbell Fly"
        ));
        data.put("Arms", Arrays.asList(
                "Bicep Curl","Hammer Curl","Concentration Curl","Barbell Curl","Preacher Curl","Cable Curl",
                "Tricep Pushdown","Overhead Tricep Extension","Skull Crusher","Close Grip Bench Press",
                "Tricep Dips","Zottman Curl","Reverse Curl","Incline Dumbbell Curl","Dumbbell Kickback"
        ));
        data.put("Legs", Arrays.asList(
                "Back Squat","Front Squat","Hack Squat","Leg Press","Lunges","Bulgarian Split Squat",
                "Step-Up","Leg Extension","Leg Curl","Romanian Deadlift","Sumo Deadlift","Goblet Squat",
                "Walking Lunge","Glute Bridge","Wall Sit"
        ));
        data.put("Core", Arrays.asList(
                "Crunch","Sit-Up","Russian Twist","Leg Raise","Plank","Side Plank","Hanging Leg Raise",
                "Bicycle Crunch","Mountain Climber","Ab Rollout","Flutter Kicks","Cable Crunch","V-Up",
                "Toe Touch","Weighted Sit-Up"
        ));
        data.put("Glutes", Arrays.asList(
                "Hip Thrust","Glute Bridge","Cable Kickback","Donkey Kick","Sumo Deadlift",
                "Bulgarian Split Squat","Step-Up","Reverse Lunge","Kettlebell Swing","Curtsy Lunge",
                "Single Leg Deadlift","Frog Pump","Glute Kickback","Smith Machine Hip Thrust","Barbell Glute Bridge"
        ));
        data.put("Quads", Arrays.asList(
                "Leg Extension","Front Squat","Hack Squat","Walking Lunge","Step-Up","Bulgarian Split Squat",
                "Wall Sit","Sissy Squat","Bodyweight Squat","Pistol Squat","Smith Machine Squat",
                "Machine Leg Press","Barbell Squat","Zercher Squat","Goblet Squat"
        ));
        data.put("Hamstrings", Arrays.asList(
                "Romanian Deadlift","Good Morning","Leg Curl","Glute Ham Raise","Kettlebell Swing",
                "Nordic Curl","Cable Pull-Through","Stiff Leg Deadlift","Single Leg Deadlift","Lying Leg Curl",
                "Swiss Ball Curl","Reverse Lunge","Bridge Curl","Barbell Hip Thrust","Seated Leg Curl"
        ));
    }
}
