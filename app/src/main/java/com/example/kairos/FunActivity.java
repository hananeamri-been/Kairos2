package com.example.kairos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

public class FunActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fun);

        FrameLayout cardRead  = findViewById(R.id.cardRead);
        FrameLayout cardWatch = findViewById(R.id.cardWatch);

        cardRead.setOnClickListener(v ->
                startActivity(new Intent(FunActivity.this, BookTrackerActivity.class)));

        cardWatch.setOnClickListener(v ->
                startActivity(new Intent(FunActivity.this, WatchTrackerActivity.class)));

        setupBottomBar();
        View nav = findViewById(R.id.nav_fun);
        if (nav != null) nav.setSelected(true);
    }
}
