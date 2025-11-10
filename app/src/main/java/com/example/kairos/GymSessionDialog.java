package com.example.kairos;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Gravity;
import android.widget.*;

import androidx.annotation.NonNull;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class GymSessionDialog extends Dialog {

    public interface OnSaveListener { void onSave(GymSession session); }

    private final OnSaveListener listener;
    private final ExerciseRepository repo = new LocalExerciseRepository();

    private Spinner spinnerGroup;
    private EditText inputSearch;
    private LinearLayout containerExercises, containerSelected;
    private ImageButton btnSave;

    public GymSessionDialog(@NonNull Context context, OnSaveListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_gym_session);

        spinnerGroup = findViewById(R.id.spinnerGroup);
        inputSearch = findViewById(R.id.inputSearch);
        containerExercises = findViewById(R.id.containerExercises);
        containerSelected = findViewById(R.id.containerSelected);
        btnSave = findViewById(R.id.btnSaveSession);

        setupGroupSpinner();
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { reloadExercises(); }
            @Override public void afterTextChanged(Editable s) {}
        });
        btnSave.setOnClickListener(v -> saveSession());

        reloadExercises();
    }

    private void setupGroupSpinner() {
        String[] groups = {"Chest","Back","Shoulders","Arms","Legs","Core","Glutes","Quads","Hamstrings"};
        ArrayAdapter<String> ad = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, groups);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroup.setAdapter(ad);
        spinnerGroup.setSelection(0);
        spinnerGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { reloadExercises(); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void reloadExercises() {
        String group = spinnerGroup.getSelectedItem().toString();
        String search = inputSearch.getText().toString();

        containerExercises.removeAllViews();
        repo.getByGroup(group, search, new ExerciseRepository.Callback() {
            @Override public void onSuccess(List<String> list) {
                for (String name : list) addExerciseRow(name);
            }
            @Override public void onError(Exception e) {
                Toast.makeText(getContext(), "Error loading exercises", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addExerciseRow(String name) {
        TextView tv = new TextView(getContext());
        tv.setText(name);
        tv.setTextSize(16);
        tv.setPadding(8, 12, 8, 12);
        tv.setOnClickListener(v -> addSelectedExercise(name));
        containerExercises.addView(tv);
    }



    private void addSelectedExercise(String name) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(8, 8, 8, 8);

        TextView label = new TextView(getContext());
        label.setText(name);
        label.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));

        EditText kg = new EditText(getContext());
        kg.setHint("kg");
        kg.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        kg.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        EditText sets = new EditText(getContext());          // ✅ nouveau
        sets.setHint("sets");
        sets.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        sets.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        EditText reps = new EditText(getContext());
        reps.setHint("reps");
        reps.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        reps.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        ImageButton del = new ImageButton(getContext());
        del.setImageResource(android.R.drawable.ic_menu_delete);
        del.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        del.setOnClickListener(v -> containerSelected.removeView(row));

        row.addView(label);
        row.addView(kg);
        row.addView(sets);    // ✅ inséré
        row.addView(reps);
        row.addView(del);

        containerSelected.addView(row);
    }

    private void saveSession() {
        String group = spinnerGroup.getSelectedItem().toString();
        GymSession session = new GymSession(group);

        int n = containerSelected.getChildCount();
        for (int i = 0; i < n; i++) {
            LinearLayout row = (LinearLayout) containerSelected.getChildAt(i);
            TextView label = (TextView) row.getChildAt(0);
            EditText kg   = (EditText) row.getChildAt(1);
            EditText sets = (EditText) row.getChildAt(2);   // ✅ index mis à jour
            EditText reps = (EditText) row.getChildAt(3);   // ✅ index mis à jour

            String name = label.getText().toString();
            double w = 0; int s = 0; int r = 0;
            try { w = Double.parseDouble(kg.getText().toString()); } catch (Exception ignore) {}
            try { s = Integer.parseInt(sets.getText().toString()); } catch (Exception ignore) {}
            try { r = Integer.parseInt(reps.getText().toString()); } catch (Exception ignore) {}

            session.exercises.add(new ExerciseEntry(name, s, r, w));  // ✅ sets inclus
        }

        if (session.exercises.isEmpty()) {
            Toast.makeText(getContext(), "No exercises selected", Toast.LENGTH_SHORT).show();
            return;
        }

        listener.onSave(session);
        dismiss();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getWindow() != null) {
            getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }


}
