package com.example.kairos;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HabitActivity extends BaseActivity implements AddHabitDialog.Listener, HabitAdapter.Callbacks {

    private HabitStore store;
    private RecyclerView list;
    private HabitAdapter adapter;

    private Calendar currentWeekStart;   // semaine affichée (dimanche)
    private String currentWeekKey;

    private Calendar today;              // date du jour (pour verrouiller les jours futurs)
    private Calendar realCurrentWeekStart; // semaine du jour

    private TextView weekLabel;
    private ImageButton btnPrev, btnNext, btnAdd;

    private List<Habit> habits = new ArrayList<>();

    private boolean isLockedWeek = false;    // verrouillage de la semaine entière
    private boolean isCurrentWeek = false;   // est-ce la semaine du jour ?

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habits);
        setupBottomBar();
        store = new HabitStore(this);

        weekLabel = findViewById(R.id.weekLabel);
        btnPrev = findViewById(R.id.btnPrevWeek);
        btnNext = findViewById(R.id.btnNextWeek);
        btnAdd = findViewById(R.id.addHabit);

        list = findViewById(R.id.habitsRecycler);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HabitAdapter(habits, this);
        list.setAdapter(adapter);

        // initialise dates
        today = Calendar.getInstance();
        realCurrentWeekStart = HabitStore.weekStartFor(today);
        currentWeekStart = (Calendar) realCurrentWeekStart.clone(); // on démarre sur la semaine courante

        refreshWeek();

        btnPrev.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.DAY_OF_MONTH, -7);
            refreshWeek();
        });

        btnNext.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.DAY_OF_MONTH, 7);
            refreshWeek();
        });

        btnAdd.setOnClickListener(v -> {
            if (isLockedWeek) {
                Toast.makeText(this, "Semaine verrouillée (passée ou future)", Toast.LENGTH_SHORT).show();
                return;
            }
            AddHabitDialog.newInstance(null).show(getSupportFragmentManager(), "add");
        });
    }

    private void refreshWeek() {
        currentWeekKey = HabitStore.weekKey(currentWeekStart);
        weekLabel.setText(HabitStore.labelForWeek(currentWeekStart));

        // verrouillage de la semaine
        isCurrentWeek = HabitStore.isSameWeek(currentWeekStart, realCurrentWeekStart);
        isLockedWeek = !isCurrentWeek; // seules la semaine courante est modifiable

        // charge la semaine (init héritage si besoin)
        habits = store.ensureWeekInitialized(currentWeekKey);
        adapter.updateData(habits);

        // UI states
        adapter.setLocked(isLockedWeek);
        int todayIndex = HabitStore.todayIndexInWeek(today);
        adapter.setTodayIndex(todayIndex, isCurrentWeek);

        btnAdd.setVisibility(isLockedWeek ? View.GONE : View.VISIBLE);
    }

    /* ================= Add / Edit / Delete ================= */

    @Override
    public void onSaveHabit(Habit habit, boolean isEdit) {
        if (isLockedWeek) return;

        if (isEdit) {
            // mise à jour d'une habitude existante (même id)
            for (int i = 0; i < habits.size(); i++) {
                if (habits.get(i).id.equals(habit.id)) {
                    habits.get(i).title = habit.title;
                    System.arraycopy(habit.freq, 0, habits.get(i).freq, 0, 7);
                    break;
                }
            }
        } else {
            habits.add(habit);
        }
        // save semaine
        store.saveHabitsForWeek(currentWeekKey, habits);

        // maj global (modèle) pour les semaines futures
        store.saveGlobalActive(stripChecked(habits));
        // supprimer snapshots des semaines futures pour que l'héritage prenne en compte la modif
        store.clearFutureWeeks(currentWeekKey);

        adapter.updateData(habits);
    }

    @Override
    public void onToggle(String habitId, int dayIndex, boolean checked) {
        if (isLockedWeek) return;
        // toggle déjà appliqué dans l'adapter; on persiste
        store.saveHabitsForWeek(currentWeekKey, habits);
    }

    @Override
    public void onDelete(String habitId) {
        if (isLockedWeek) return;
        for (int i = 0; i < habits.size(); i++) {
            if (habits.get(i).id.equals(habitId)) {
                habits.remove(i);
                break;
            }
        }
        store.saveHabitsForWeek(currentWeekKey, habits);
        store.saveGlobalActive(stripChecked(habits));
        store.clearFutureWeeks(currentWeekKey);
        adapter.updateData(habits);
    }

    @Override
    public void onEdit(Habit habit) {
        if (isLockedWeek) return;
        AddHabitDialog.newInstance(habit).show(getSupportFragmentManager(), "edit");
    }

    private List<Habit> stripChecked(List<Habit> src) {
        List<Habit> out = new ArrayList<>();
        for (Habit h : src) out.add(new Habit(h.id, h.title, h.freq, new boolean[7]));
        return out;
    }
}
