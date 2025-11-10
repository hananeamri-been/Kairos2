package com.example.kairos;

import java.util.List;

public interface ExerciseRepository {
    interface Callback {
        void onSuccess(List<String> exercises);
        void onError(Exception e);
    }
    void getByGroup(String group, String search, Callback callback);
}
