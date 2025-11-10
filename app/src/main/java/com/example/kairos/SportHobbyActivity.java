package com.example.kairos;

import android.content.Intent; // ✅ ← ajoute cette ligne
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SportHobbyActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_hobby);
        setupBottomBar();

        findViewById(R.id.cardHobby).setOnClickListener(v ->
                startActivity(new Intent(this, HobbyTrackerActivity.class)));

        findViewById(R.id.cardSport).setOnClickListener(v ->
                startActivity(new Intent(this, SportTrackerActivity.class)));
    }
}
