package com.example.kairos;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acceuil); // ton layout de page de garde

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, HabitActivity.class));
            finish(); // on ferme le splash
        }, 3000); // 3 secondes
    }
}
