package com.example.kairos;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HobbyTrackerActivity extends BaseActivity
        implements AddHobbyDialog.Listener, HobbyAdapter.Callbacks {

    private RecyclerView list;
    private HobbyStore store;

    private HobbyAdapter adapter;

    private Calendar currentWeekStart;   // semaine affichée (dimanche)
    private String currentWeekKey;

    private Calendar today;              // date du jour
    private Calendar realCurrentWeekStart; // semaine du jour

    private TextView weekLabel;
    private ImageButton btnPrev, btnNext, btnAdd;

    private List<Hobby> hobbies = new ArrayList<>();

    private boolean isLockedWeek = false;    // verrouillage de la semaine
    private boolean isCurrentWeek = false;   // est-ce la semaine en cours ?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hobby_tracker);
        setupBottomBar();

        store = new HobbyStore(this);

        weekLabel = findViewById(R.id.weekLabelHobby);
        btnPrev = findViewById(R.id.btnPrevWeekHobby);
        btnNext = findViewById(R.id.btnNextWeekHobby);
        btnAdd  = findViewById(R.id.addHobby);

        list = findViewById(R.id.hobbiesRecycler);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HobbyAdapter(hobbies, this);
        list.setAdapter(adapter);

        // initialise dates
        today = Calendar.getInstance();
        realCurrentWeekStart = HobbyStore.weekStartFor(today);
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
            if (isLockedWeek) return;
            AddHobbyDialog.newInstance(null).show(getSupportFragmentManager(), "add");
        });
    }

    private void refreshWeek() {
        currentWeekKey = HobbyStore.weekKey(currentWeekStart);
        weekLabel.setText(HobbyStore.labelForWeek(currentWeekStart));

        // verrouillage de la semaine
        isCurrentWeek = HobbyStore.isSameWeek(currentWeekStart, realCurrentWeekStart);
        isLockedWeek = !isCurrentWeek; // seule la semaine courante est modifiable

        // charge la semaine (init héritage si besoin)
        hobbies = store.ensureWeekInitialized(currentWeekKey);
        adapter.updateData(hobbies);

        // UI states
        adapter.setLocked(isLockedWeek);
        int todayIndex = HobbyStore.todayIndexInWeek(today);
        adapter.setTodayIndex(todayIndex, isCurrentWeek);

        btnAdd.setVisibility(isLockedWeek ? View.GONE : View.VISIBLE);
    }

    /* ================= Add / Edit / Delete ================= */

    @Override
    public void onSaveHobby(Hobby hobby, boolean isEdit) {
        if (isLockedWeek) return;

        if (isEdit) {
            // mise à jour d’un hobby existant (même id)
            for (int i = 0; i < hobbies.size(); i++) {
                if (hobbies.get(i).id.equals(hobby.id)) {
                    // on conserve l'état checked courant si toEdit != null ? déjà copié via ctor
                    hobbies.set(i, hobby);
                    break;
                }
            }
        } else {
            hobbies.add(hobby);
        }

        store.saveHobbiesForWeek(currentWeekKey, hobbies);

        // maj global (modèle) pour les semaines futures
        store.saveGlobalActive(stripChecked(hobbies));
        // supprimer snapshots des semaines futures pour que l'héritage prenne en compte la modif
        store.clearFutureWeeks(currentWeekKey);

        adapter.updateData(hobbies);
    }

    @Override
    public void onToggle(String hobbyId, int dayIndex, boolean checked) {
        if (isLockedWeek) return;
        // toggle déjà appliqué dans l'adapter; on persiste
        store.saveHobbiesForWeek(currentWeekKey, hobbies);
    }

    @Override
    public void onDelete(String hobbyId) {
        if (isLockedWeek) return;
        for (int i = 0; i < hobbies.size(); i++) {
            if (hobbies.get(i).id.equals(hobbyId)) {
                hobbies.remove(i);
                break;
            }
        }
        store.saveHobbiesForWeek(currentWeekKey, hobbies);
        store.saveGlobalActive(stripChecked(hobbies));
        store.clearFutureWeeks(currentWeekKey);
        adapter.updateData(hobbies);
    }

    @Override
    public void onEdit(Hobby hobby) {
        if (isLockedWeek) return;
        AddHobbyDialog.newInstance(hobby).show(getSupportFragmentManager(), "edit");
    }

    private List<Hobby> stripChecked(List<Hobby> src) {
        List<Hobby> out = new ArrayList<>();
        for (Hobby h : src) out.add(new Hobby(h.id, h.title, h.freq, new boolean[7]));
        return out;
    }
}
