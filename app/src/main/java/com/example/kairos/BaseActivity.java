package com.example.kairos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected void setupBottomBar() {
        attach(R.id.nav_travels, TravelActivity.class);
        attach(R.id.nav_fun,     FunActivity.class);
        attach(R.id.nav_journal, JournalActivity.class);
        attach(R.id.nav_sport,   SportHobbyActivity.class);
        attach(R.id.nav_habits,  HabitActivity.class);
    }

    private void attach(int viewId, Class<?> target) {
        View icon = findViewById(viewId);
        if (icon == null) return;
        icon.setClickable(true);
        icon.setFocusable(true);
        icon.setOnClickListener(v -> openActivity(target));
    }

    private void openActivity(Class<?> target) {
        if (!getClass().equals(target)) {
            Intent i = new Intent(this, target);
            i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);
            overridePendingTransition(0, 0);
        }
    }
}
