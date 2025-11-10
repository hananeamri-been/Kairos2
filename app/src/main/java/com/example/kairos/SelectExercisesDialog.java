package com.example.kairos;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class SelectExercisesDialog extends Dialog {

    public interface OnSaveListener {
        void onSave(String group, List<String> exerciseNames);
    }

    private final ExerciseRepository repo = new LocalExerciseRepository();
    private final OnSaveListener listener;

    private Spinner spinnerGroup;
    private EditText inputSearch;
    private LinearLayout containerExercises;
    private ImageButton btnSave;

    private final List<CheckBox> checkBoxes = new ArrayList<>();

    public SelectExercisesDialog(@NonNull Context context, OnSaveListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.dialog_select_exercises);

        spinnerGroup = findViewById(R.id.spinnerGroup);
        inputSearch = findViewById(R.id.inputSearch);
        containerExercises = findViewById(R.id.containerExercises);
        btnSave = findViewById(R.id.btnSaveSelection);

        String[] groups = {"Chest","Back","Shoulders","Arms","Legs","Core","Glutes","Quads","Hamstrings"};
        ArrayAdapter<String> ad = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, groups);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroup.setAdapter(ad);
        spinnerGroup.setSelection(0);
        spinnerGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, android.view.View v, int pos, long id) { reload(); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { reload(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnSave.setOnClickListener(v -> {
            String group = spinnerGroup.getSelectedItem().toString();
            List<String> selected = new ArrayList<>();
            for (CheckBox cb : checkBoxes) if (cb.isChecked()) selected.add(cb.getText().toString());
            if (selected.isEmpty()) {
                Toast.makeText(getContext(), "Select at least one exercise", Toast.LENGTH_SHORT).show();
                return;
            }
            listener.onSave(group, selected);
            dismiss();
        });

        reload();
    }

    private void reload() {
        containerExercises.removeAllViews();
        checkBoxes.clear();
        String group = spinnerGroup.getSelectedItem().toString();
        String search = inputSearch.getText().toString();

        repo.getByGroup(group, search, new ExerciseRepository.Callback() {
            @Override public void onSuccess(List<String> exercises) {
                for (String name : exercises) {
                    CheckBox cb = new CheckBox(getContext());
                    cb.setText(name);
                    cb.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    containerExercises.addView(cb);
                    checkBoxes.add(cb);
                }
            }
            @Override public void onError(Exception e) { Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show(); }
        });
    }

    @Override protected void onStart() {
        super.onStart();
        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
