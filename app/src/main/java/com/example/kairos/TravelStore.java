package com.example.kairos;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Iterator;

public class TravelStore {
    private static final String PREF = "travel_store";
    private static final String KEY  = "travels_json";
    private final SharedPreferences sp;

    public TravelStore(Context ctx) {
        this.sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public ArrayList<Travel> loadAll() {
        String raw = sp.getString(KEY, "[]");
        try {
            return Travel.listFromJson(new JSONArray(raw));
        } catch (JSONException e) {
            return new ArrayList<>();
        }
    }

    private void saveAll(ArrayList<Travel> list) {
        try {
            sp.edit().putString(KEY, Travel.listToJson(list).toString()).apply();
        } catch (JSONException ignore) {}
    }

    public void upsert(Travel t) {
        ArrayList<Travel> all = loadAll();
        boolean updated = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).id.equals(t.id)) {
                all.set(i, t);
                updated = true;
                break;
            }
        }
        if (!updated) all.add(0, t);
        saveAll(all);
    }

    public void deleteById(String id) {
        ArrayList<Travel> all = loadAll();
        Iterator<Travel> it = all.iterator();
        while (it.hasNext()) {
            if (it.next().id.equals(id)) { it.remove(); break; }
        }
        saveAll(all);
    }
}
