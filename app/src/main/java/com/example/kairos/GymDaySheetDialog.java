package com.example.kairos;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.List;

public class GymDaySheetDialog extends Dialog {

    public interface OnSaveListener {
        void onSave(List<GymSession> updatedSessions);
    }

    private final List<GymSession> sessions;   // liste mutable du jour
    private final OnSaveListener listener;

    private LinearLayout containerRows;

    public GymDaySheetDialog(@NonNull Context context, List<GymSession> sessions, OnSaveListener listener) {
        super(context);
        this.sessions = sessions;
        this.listener = listener;
    }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.dialog_gym_day_sheet);

        containerRows = findViewById(R.id.containerRows);
        ImageButton btnSave = findViewById(R.id.btnSaveSheet);

        renderRows();

        btnSave.setOnClickListener(v -> {
            listener.onSave(sessions);
            dismiss();
        });
    }

    private void renderRows() {
        containerRows.removeAllViews();
        for (GymSession s : sessions) {
            TextView title = new TextView(getContext());
            title.setText("• " + s.muscleGroup);
            title.setTextSize(16);
            title.setPadding(4, 12, 4, 4);
            containerRows.addView(title);

            Iterator<ExerciseEntry> it = s.exercises.iterator();
            while (it.hasNext()) {
                ExerciseEntry ex = it.next();

                LinearLayout row = new LinearLayout(getContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(4, 4, 4, 4);

                TextView name = new TextView(getContext());
                name.setText(ex.name);
                name.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));

                EditText kg = new EditText(getContext());
                kg.setHint("kg");
                kg.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                kg.setText(ex.weight == 0 ? "" : String.valueOf(ex.weight));
                kg.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

                EditText sets = new EditText(getContext());
                sets.setHint("sets");
                sets.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                sets.setText(ex.sets == 0 ? "" : String.valueOf(ex.sets));
                sets.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

                EditText reps = new EditText(getContext());
                reps.setHint("reps");
                reps.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                reps.setText(ex.reps == 0 ? "" : String.valueOf(ex.reps));
                reps.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

                ImageButton delete = new ImageButton(getContext());
                delete.setImageResource(android.R.drawable.ic_menu_delete);
                delete.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                delete.setOnClickListener(v -> {
                    it.remove();        // supprime l’exercice
                    renderRows();       // refresh
                });


                kg.addTextChangedListener(new SimpleWatcher(t -> ex.weight = parseD(t)));
                sets.addTextChangedListener(new SimpleWatcher(t -> ex.sets = parseI(t)));
                reps.addTextChangedListener(new SimpleWatcher(t -> ex.reps = parseI(t)));

                row.addView(name);
                row.addView(kg);
                row.addView(sets);
                row.addView(reps);
                row.addView(delete);

                containerRows.addView(row);
            }
        }
    }

    private static class SimpleWatcher implements android.text.TextWatcher {
        interface Cb { void run(String s); }
        private final Cb cb;
        SimpleWatcher(Cb cb){ this.cb = cb; }
        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) { cb.run(s.toString()); }
        @Override public void afterTextChanged(android.text.Editable s) {}
    }

    private static double parseD(String s){ try { return Double.parseDouble(s.trim()); } catch(Exception e){ return 0; } }
    private static int parseI(String s){ try { return Integer.parseInt(s.trim()); } catch(Exception e){ return 0; } }

    @Override protected void onStart() {
        super.onStart();
        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
