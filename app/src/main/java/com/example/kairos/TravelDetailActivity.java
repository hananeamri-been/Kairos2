package com.example.kairos;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TravelDetailActivity extends BaseActivity {

    private TravelStore store;
    private Travel current;

    private EditText etStart, etEnd, etFlight, etCar;
    private EditText etHotelName, etHotelPrice;
    private TextView tvDays, tvTotal, tvRemaining;

    private RecyclerView rvR, rvA, rvO;
    private ExpenseAdapter adRestaurants, adActivities, adOthers;
    private final NumberFormat money = NumberFormat.getCurrencyInstance(Locale.getDefault());

    private boolean isInitializing = false; // évite les autosave pendant le chargement

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_detail);

        isInitializing = true; // début init

        // Scroll root (pour remonter avec le clavier)
        ScrollView root = findViewById(R.id.rootScroll);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
                // padding bas = hauteur du clavier quand il est visible
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), ime.bottom);
                return insets;
            });
        }

        store = new TravelStore(this);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Travel details");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            // flèche retour
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Récupération de l’ID du voyage
        String id = getIntent().getStringExtra("travel_id");
        if (id == null) id = getIntent().getStringExtra("id");
        current = findById(id);
        if (current == null) {
            finish();
            return;
        }

        bindViews();
        bindWatchers();
        syncUIFromModel(); // on peuple les champs avant les listes
        bindLists();

        // Bouton SAVE
        Button btnSave = findViewById(R.id.btnSaveAll);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                store.upsert(current);
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        }

        setupBottomBar();

        isInitializing = false; // fin init
    }

    private Travel findById(String id) {
        ArrayList<Travel> all = store.loadAll();
        for (Travel t : all) if (t.id.equals(id)) return t;
        return null;
    }

    private void bindViews() {
        etStart = findViewById(R.id.etStart);
        etEnd = findViewById(R.id.etEnd);
        etFlight = findViewById(R.id.etFlight);
        etCar = findViewById(R.id.etCar);
        etHotelName = findViewById(R.id.etHotelName);
        etHotelPrice = findViewById(R.id.etHotelPrice);
        tvDays = findViewById(R.id.tvDays);
        tvTotal = findViewById(R.id.tvTotal);
        tvRemaining = findViewById(R.id.tvRemaining);

        // Sélecteurs de date
        etStart.setFocusable(false);
        etEnd.setFocusable(false);
        etStart.setOnClickListener(v -> showDatePicker(etStart));
        etEnd.setOnClickListener(v -> showDatePicker(etEnd));
    }

    private void bindLists() {
        rvR = findViewById(R.id.rvRestaurants);
        rvA = findViewById(R.id.rvActivities);
        rvO = findViewById(R.id.rvOthers);

        if (rvR != null) {
            rvR.setLayoutManager(new LinearLayoutManager(this));
            rvR.setNestedScrollingEnabled(false);
            adRestaurants = new ExpenseAdapter(this, current.restaurants, this::onDataEdited);
            rvR.setAdapter(adRestaurants);
        }

        if (rvA != null) {
            rvA.setLayoutManager(new LinearLayoutManager(this));
            rvA.setNestedScrollingEnabled(false);
            adActivities = new ExpenseAdapter(this, current.activities, this::onDataEdited);
            rvA.setAdapter(adActivities);
        }

        if (rvO != null) {
            rvO.setLayoutManager(new LinearLayoutManager(this));
            rvO.setNestedScrollingEnabled(false);
            adOthers = new ExpenseAdapter(this, current.others, this::onDataEdited);
            rvO.setAdapter(adOthers);
        }

        // Boutons d'ajout
        Button btnAddRestaurant = findViewById(R.id.btnAddRestaurant);
        if (btnAddRestaurant != null) {
            btnAddRestaurant.setOnClickListener(v -> {
                current.restaurants.add(new ExpenseItem());
                if (adRestaurants != null) {
                    adRestaurants.notifyItemInserted(current.restaurants.size() - 1);
                    rvR.scrollToPosition(current.restaurants.size() - 1);
                }
                store.upsert(current);
            });
        }

        Button btnAddActivity = findViewById(R.id.btnAddActivity);
        if (btnAddActivity != null) {
            btnAddActivity.setOnClickListener(v -> {
                current.activities.add(new ExpenseItem());
                if (adActivities != null) {
                    adActivities.notifyItemInserted(current.activities.size() - 1);
                    rvA.scrollToPosition(current.activities.size() - 1);
                }
                store.upsert(current);
            });
        }

        Button btnAddOther = findViewById(R.id.btnAddOther);
        if (btnAddOther != null) {
            btnAddOther.setOnClickListener(v -> {
                current.others.add(new ExpenseItem());
                if (adOthers != null) {
                    adOthers.notifyItemInserted(current.others.size() - 1);
                    rvO.scrollToPosition(current.others.size() - 1);
                }
                store.upsert(current);
            });
        }
    }

    private void bindWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { onDataEdited(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        etStart.addTextChangedListener(watcher);
        etEnd.addTextChangedListener(watcher);
        etFlight.addTextChangedListener(watcher);
        etCar.addTextChangedListener(watcher);
        etHotelName.addTextChangedListener(watcher);
        etHotelPrice.addTextChangedListener(watcher);
    }

    private void onDataEdited() {
        if (isInitializing) return; // ignore pendant le chargement

        current.startDate = toStr(etStart);
        current.endDate = toStr(etEnd);
        current.flightPrice = parseDouble(etFlight);
        current.carPrice = parseDouble(etCar);
        current.hotelName = toStr(etHotelName);
        current.hotelPrice = parseDouble(etHotelPrice);

        updateTotalsUI();
        store.upsert(current);
    }

    private void updateTotalsUI() {
        if (tvDays != null) tvDays.setText(current.daysCount() + " days");
        if (tvTotal != null) tvTotal.setText(money.format(current.totalSpent()));
        if (tvRemaining != null) {
            double remaining = current.remainingBudget();
            tvRemaining.setText(money.format(remaining));
            tvRemaining.setTextColor(remaining < 0 ? 0xFFFF0000 : 0xFF000000);
        }
    }

    private void syncUIFromModel() {
        etStart.setText(current.startDate != null ? current.startDate : "");
        etEnd.setText(current.endDate != null ? current.endDate : "");
        etFlight.setText(numOrEmpty(current.flightPrice));
        etCar.setText(numOrEmpty(current.carPrice));
        etHotelName.setText(current.hotelName != null ? current.hotelName : "");
        etHotelPrice.setText(numOrEmpty(current.hotelPrice));
        updateTotalsUI();
    }

    private void showDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            Calendar sel = Calendar.getInstance();
            sel.set(y, m, d);
            String s = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(sel.getTime());
            target.setText(s);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String numOrEmpty(double v) { return v == 0 ? "" : String.valueOf(v); }
    private String toStr(EditText e) { return e.getText().toString().trim(); }
    private double parseDouble(EditText e) {
        try {
            String s = toStr(e).replace(',', '.');
            return s.isEmpty() ? 0.0 : Double.parseDouble(s);
        } catch (Exception ex) { return 0.0; }
    }
}
