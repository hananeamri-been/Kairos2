package com.example.kairos;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Dialog d'ajout d'un voyage
 * - Sélection de pays (avec drapeau) via un adapter typé (pas de parsing de string)
 * - Liste de villes dépendante du pays
 * - Budget initial validé (supporte virgules)
 */
public class AddTravelDialog extends Dialog {

    public interface Listener {
        void onTravelCreated(Travel t);
    }

    private Listener listener;
    private CountryCityRepository repo;

    private AutoCompleteTextView acCountry;
    private AutoCompleteTextView acCity;
    private EditText etBudget;
    private Button btnSave;


    private String selectedCountryCode = null;

    public AddTravelDialog(@NonNull Context context, Listener l) {
        super(context);
        this.listener = l;
        this.repo = new CountryCityRepository(context);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_travel);

        setCanceledOnTouchOutside(false);
        if (getWindow() != null) {
            getWindow().setDimAmount(0.6f); // scrim
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        acCountry = findViewById(R.id.acCountry);
        acCity    = findViewById(R.id.acCity);
        etBudget  = findViewById(R.id.etBudget);
        btnSave   = findViewById(R.id.btnSave);

        setupCountryField();
        setupCityField();
        setupBudgetField();
        setupSave();
    }

    private void setupCountryField() {
        // Construire la liste typée
        ArrayList<String> codes = repo.allCountryCodes();
        ArrayList<CountryRow> rows = new ArrayList<>(codes.size());
        for (String c : codes) {
            rows.add(new CountryRow(c, repo.nameForCode(c), FlagUtils.countryCodeToFlag(c)));
        }

        CountryAdapter countryAdapter = new CountryAdapter(getContext(), rows);
        acCountry.setAdapter(countryAdapter);
        acCountry.setThreshold(0);
        acCountry.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) acCountry.showDropDown(); });

        acCountry.setOnItemClickListener((parent, view, position, id) -> {
            CountryRow row = countryAdapter.getItem(position);
            if (row != null) {
                selectedCountryCode = row.code;
                loadCitiesFor(selectedCountryCode);
            }
        });

        acCountry.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                // si le texte ne correspond plus exactement à une ligne du dropdown → on "oublie" le code
                selectedCountryCode = null;
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCityField() {
        acCity.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, new ArrayList<>()));
        acCity.setThreshold(0);
        acCity.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && selectedCountryCode != null) acCity.showDropDown();
        });
    }

    private void loadCitiesFor(String code) {
        ArrayList<String> cities = repo.citiesFor(code);
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, cities);
        acCity.setAdapter(cityAdapter);
        acCity.setText("");
        acCity.showDropDown();
    }


    private void setupBudgetField() {
    }


    private void setupSave() {
        btnSave.setOnClickListener(v -> {

            if (selectedCountryCode == null) {
                Toast.makeText(getContext(), "Please pick a country from the list.", Toast.LENGTH_SHORT).show();
                acCountry.requestFocus();
                acCountry.showDropDown();
                return;
            }

            String citySel = acCity.getText().toString().trim();
            if (TextUtils.isEmpty(citySel)) {
                Toast.makeText(getContext(), "Please select a city.", Toast.LENGTH_SHORT).show();
                acCity.requestFocus();
                acCity.showDropDown();
                return;
            }
            if (!repo.citiesFor(selectedCountryCode).contains(citySel)) {
                Toast.makeText(getContext(), "Pick a city from the suggested list.", Toast.LENGTH_SHORT).show();
                acCity.requestFocus();
                acCity.showDropDown();
                return;
            }

            String budgetStr = etBudget.getText().toString().trim().replace(',', '.');
            double budget = 0.0;
            if (!budgetStr.isEmpty()) {
                try { budget = Double.parseDouble(budgetStr); }
                catch (Exception ex) {
                    Toast.makeText(getContext(), "Invalid budget number.", Toast.LENGTH_SHORT).show();
                    etBudget.requestFocus();
                    return;
                }
            }

            Travel t = new Travel(selectedCountryCode, citySel, budget);
            if (listener != null) listener.onTravelCreated(t);

            v.postDelayed(this::dismiss, 120);
        });
    }


    private static class CountryRow {
        final String code;
        final String name;
        final String flag;
        CountryRow(String code, String name, String flag) {
            this.code = code; this.name = name; this.flag = flag;
        }
        @Override public String toString() {
            return String.format(Locale.getDefault(), "%s %s (%s)", flag, name, code);
        }
    }

    private static class CountryAdapter extends ArrayAdapter<CountryRow> {
        public CountryAdapter(@NonNull Context ctx, ArrayList<CountryRow> rows) {
            super(ctx, android.R.layout.simple_dropdown_item_1line, rows);
        }
    }
}
