package com.example.kairos;

import android.content.Context;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class CountryCityRepository {
    private final LinkedHashMap<String, ArrayList<String>> map = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> codeToName = new LinkedHashMap<>();

    public CountryCityRepository(Context ctx) {

        List<CountryCityData> sources = new ArrayList<>();
        sources.add(new CountryCityData_Europe());
        sources.add(new CountryCityData_Americas());
        sources.add(new CountryCityData_Africa());
        sources.add(new CountryCityData_Asia());
        sources.add(new CountryCityData_Oceania());


        for (CountryCityData src : sources) {
            for (CountryCityData.Country c : src.countries()) {
                codeToName.put(c.code, c.name);
                map.put(c.code, new ArrayList<>(c.cities));
            }
        }
    }

    public ArrayList<String> allCountryCodes() { return new ArrayList<>(map.keySet()); }
    public String nameForCode(String code) { String n = codeToName.get(code); return n != null ? n : code; }
    public ArrayList<String> citiesFor(String countryCode) {
        ArrayList<String> list = map.get(countryCode);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }
    public boolean hasCountry(String code) { return map.containsKey(code); }
}
