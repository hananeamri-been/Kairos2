package com.example.kairos;

import java.util.List;

public interface CountryCityData {

    final class Country {
        public final String code;   // ISO-2
        public final String name;
        public final List<String> cities;
        public Country(String code, String name, List<String> cities) {
            this.code = code;
            this.name = name;
            this.cities = cities;
        }
    }

    List<Country> countries();
}
