package com.example.kairos;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SportTrackerActivity extends BaseActivity {

    /* ===== Store & semaine ===== */
    private SportStore store;
    private Calendar today;
    private Calendar realCurrentWeekStart;   // début semaine réelle (dimanche)
    private Calendar currentWeekStart;       // semaine affichée
    private String currentWeekKey;

    private boolean isCurrentWeek;
    private boolean isLockedWeek;

    /* ===== UI ===== */
    private TextView weekLabel;
    private LinearLayout runningTable, gymTable;

    /* ===== Données ===== */
    private SportWeekData data;

    /* ===== Struct vues internes ===== */
    private static class RunCol {
        CheckBox check;
        EditText km, min, pace;
    }
    private static class GymCol {
        CheckBox check;
        ImageButton more;
        ImageButton fiche;
    }

    private final List<RunCol> runCols = new ArrayList<>();
    private final List<GymCol> gymCols = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_tracker);
        setupBottomBar();

        store = new SportStore(this);

        weekLabel = findViewById(R.id.weekLabel);
        runningTable = findViewById(R.id.runningTable);
        gymTable = findViewById(R.id.gymTable);

        findViewById(R.id.btnPrevWeek).setOnClickListener(v -> {
            currentWeekStart.add(Calendar.DAY_OF_MONTH, -7);
            refresh();
        });
        findViewById(R.id.btnNextWeek).setOnClickListener(v -> {
            currentWeekStart.add(Calendar.DAY_OF_MONTH, 7);
            refresh();
        });

        findViewById(R.id.btnEditRunningFreq).setOnClickListener(v -> openRunningFreqDialog());
        findViewById(R.id.btnEditGymFreq).setOnClickListener(v -> openGymFreqDialog());

        today = Calendar.getInstance();
        realCurrentWeekStart = SportStore.weekStartFor(today);
        currentWeekStart = (Calendar) realCurrentWeekStart.clone();

        refresh();
    }

    /* =========================================================
       ===============   CYCLE D’AFFICHAGE   ===================
       ========================================================= */
    private void refresh() {
        currentWeekKey = SportStore.weekKey(currentWeekStart);
        data = store.ensureWeekInitialized(currentWeekKey);

        isCurrentWeek = SportStore.isSameWeek(currentWeekStart, realCurrentWeekStart);
        isLockedWeek = !isCurrentWeek;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar end = (Calendar) currentWeekStart.clone();
        end.add(Calendar.DAY_OF_MONTH, 6);
        weekLabel.setText(sdf.format(currentWeekStart.getTime()) + " - " + sdf.format(end.getTime()));

        buildRunning();
        buildGym();
    }

    /* =========================================================
       =====================   RUNNING   =======================
       ========================================================= */
    private void buildRunning() {
        runningTable.removeAllViews();
        runCols.clear();

        for (int i = 0; i < 7; i++) {
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.CENTER_HORIZONTAL);
            col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            RunCol rc = new RunCol();

            rc.check = new CheckBox(this);
            rc.check.setChecked(data.running.entries.get(i).enabled);
            rc.check.setEnabled(!isLockedWeek && data.running.freq.get(i));
            col.addView(rc.check);

            rc.km = makeNumberField("km");
            rc.min = makeNumberField("min");
            rc.pace = makeReadonlyField("pace");

            // init vals
            rc.km.setText(z(data.running.entries.get(i).distanceKm));
            rc.min.setText(z(data.running.entries.get(i).durationMin));
            rc.pace.setText(z(data.running.entries.get(i).paceMinPerKm));

            boolean fieldsEnabled = !isLockedWeek && data.running.freq.get(i) && rc.check.isChecked();
            setEnabled(rc.km, fieldsEnabled);
            setEnabled(rc.min, fieldsEnabled);

            final int day = i;
            rc.check.setOnCheckedChangeListener((b, v) -> {
                if (isLockedWeek) { rc.check.setChecked(!v); return; }
                data.running.entries.get(day).enabled = v;
                setEnabled(rc.km, v);
                setEnabled(rc.min, v);
                save();
            });

            addTextWatcher(rc.km, (t) -> onRunChanged(day, rc));
            addTextWatcher(rc.min, (t) -> onRunChanged(day, rc));

            runCols.add(rc);
            col.addView(rc.km);
            col.addView(rc.min);
            col.addView(rc.pace);

            runningTable.addView(col);
        }
    }

    private void onRunChanged(int day, RunCol rc) {
        if (isLockedWeek) return;
        try { data.running.entries.get(day).distanceKm = parseD(rc.km.getText().toString()); } catch (Exception ignore) {}
        try { data.running.entries.get(day).durationMin = parseD(rc.min.getText().toString()); } catch (Exception ignore) {}
        data.running.entries.get(day).recomputePace();
        rc.pace.setText(z(data.running.entries.get(day).paceMinPerKm));
        save();
    }

    /* =========================================================
       =======================   GYM   =========================
       ========================================================= */
    private void buildGym() {
        gymTable.removeAllViews();
        gymCols.clear();

        for (int i = 0; i < 7; i++) {
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.CENTER_HORIZONTAL);
            col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            GymCol gc = new GymCol();

            // coche = il y a au moins une séance ce jour
            gc.check = new CheckBox(this);
            gc.check.setChecked(!data.gym.sessions.get(i).isEmpty());
            gc.check.setEnabled(!isLockedWeek && data.gym.freq.get(i));
            final int day = i;
            gc.check.setOnCheckedChangeListener((b, v) -> {
                if (isLockedWeek) { gc.check.setChecked(!v); return; }
                if (!data.gym.freq.get(day)) { gc.check.setChecked(false); return; }
                if (!v) {
                    // décocher => vider la journée
                    data.gym.sessions.get(day).clear();
                    save();
                } else {
                    // cocher => proposer d’ajouter directement
                    if (!data.gym.freq.get(day)) return;
                    openMoreDialog(day, null);
                }
            });
            col.addView(gc.check);

            // MORE : sélection d’exercices (ajoute dans la fiche)
            gc.more = new ImageButton(this);
            gc.more.setImageResource(R.drawable.more);
            gc.more.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            gc.more.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            gc.more.setAdjustViewBounds(true);
            gc.more.setPadding(6,6,6,6);
            gc.more.setLayoutParams(new LinearLayout.LayoutParams(dp(48), dp(48)));
            gc.more.setEnabled(!isLockedWeek && data.gym.freq.get(i));
            gc.more.setOnClickListener(v -> {
                if (isLockedWeek || !data.gym.freq.get(day)) return;
                openMoreDialog(day, gc);
            });
            col.addView(gc.more);

            // FICHE : édition des exos du jour (kg/sets/reps)
            gc.fiche = new ImageButton(this);
            gc.fiche.setImageResource(R.drawable.fiche);
            gc.fiche.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            gc.fiche.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            gc.fiche.setAdjustViewBounds(true);
            gc.fiche.setPadding(6,6,6,6);
            gc.fiche.setLayoutParams(new LinearLayout.LayoutParams(dp(48), dp(48)));
            gc.fiche.setEnabled(!isLockedWeek && data.gym.freq.get(i));
            gc.fiche.setOnClickListener(v -> {
                if (isLockedWeek) return;
                new GymDaySheetDialog(this, data.gym.sessions.get(day), updated -> {
                    // la liste passée est mutable ; on persiste juste
                    save();
                    gc.check.setChecked(!data.gym.sessions.get(day).isEmpty());
                }).show();
            });
            col.addView(gc.fiche);

            gymCols.add(gc);
            gymTable.addView(col);
        }
    }

    /** Ouvre le dialog MORE (sélection multi d’exos) et insère dans la journée */
    private void openMoreDialog(int day, GymCol gc) {
        new SelectExercisesDialog(this, (group, selectedNames) -> {
            // trouve (ou crée) la session du groupe
            GymSession target = null;
            for (GymSession s : data.gym.sessions.get(day)) {
                if (group.equals(s.muscleGroup)) { target = s; break; }
            }
            if (target == null) {
                target = new GymSession(group);
                data.gym.sessions.get(day).add(target);
            }
            // ajoute si pas présent
            for (String name : selectedNames) {
                boolean exists = false;
                for (ExerciseEntry e : target.exercises) {
                    if (e.name.equals(name)) { exists = true; break; }
                }
                if (!exists) target.exercises.add(new ExerciseEntry(name, 0, 0, 0));
            }
            save();
            if (gc != null) gc.check.setChecked(!data.gym.sessions.get(day).isEmpty());
        }).show();
    }

    /* =========================================================
       ===================  Fréquences  ========================
       ========================================================= */
    private void openRunningFreqDialog() {
        if (isLockedWeek) return;
        new EditRunningFreqDialog(this, data.running.freq, freq -> {
            data.running.freq = freq;
            store.saveGlobalModel(modelFromCurrent());
            store.clearFutureWeeks(currentWeekKey);
            save();
            refresh();
        }).show();
    }

    private void openGymFreqDialog() {
        if (isLockedWeek) return;
        new EditGymFreqDialog(this, data.gym.freq, freq -> {
            data.gym.freq = freq;
            store.saveGlobalModel(modelFromCurrent());
            store.clearFutureWeeks(currentWeekKey);
            save();
            refresh();
        }).show();
    }

    private SportWeekData modelFromCurrent() {
        SportWeekData m = new SportWeekData();
        for (int i = 0; i < 7; i++) {
            m.running.freq.set(i, data.running.freq.get(i));
            m.gym.freq.set(i, data.gym.freq.get(i));
        }
        return m;
    }

    private void save() { store.saveWeek(currentWeekKey, data); }

    /* =========================================================
       ====================  Helpers UI  =======================
       ========================================================= */
    private EditText makeNumberField(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        e.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return e;
    }

    private EditText makeReadonlyField(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setEnabled(false);
        e.setFocusable(false);
        e.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return e;
    }

    private void setEnabled(EditText e, boolean en) {
        e.setEnabled(en);
        e.setFocusable(en);
        e.setFocusableInTouchMode(en);
    }

    private interface OnTextChange { void changed(String t); }
    private void addTextWatcher(EditText e, OnTextChange cb) {
        e.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { cb.changed(s.toString()); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private static String z(double v) {
        return (v == 0) ? "" : (v == (long) v ? String.valueOf((long) v) : String.valueOf(v));
    }
    private static double parseD(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0; }
    }
    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(v * d);
    }
}
