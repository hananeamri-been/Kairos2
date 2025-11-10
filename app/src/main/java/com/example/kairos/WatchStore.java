package com.example.kairos;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WatchStore {
    public static final int TAB_MOVIE  = 0;
    public static final int TAB_SERIES = 1;
    public static final int TAB_ANIME  = 2;

    private static final String PREF_MOVIE  = "watch_store_movie";
    private static final String PREF_SERIES = "watch_store_series";
    private static final String PREF_ANIME  = "watch_store_anime";
    private static final String KEY = "items";

    private static String prefForTab(int tabType) {
        switch (tabType) {
            case TAB_SERIES: return PREF_SERIES;
            case TAB_ANIME:  return PREF_ANIME;
            case TAB_MOVIE:
            default:         return PREF_MOVIE;
        }
    }

    public static List<WatchItem> load(Context ctx, int tabType) {
        SharedPreferences sp = ctx.getSharedPreferences(prefForTab(tabType), Context.MODE_PRIVATE);
        String json = sp.getString(KEY, "[]");
        ArrayList<WatchItem> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(WatchItem.fromJson(o));
            }
        } catch (JSONException ignored) {}
        return list;
    }

    public static void save(Context ctx, List<WatchItem> items, int tabType) {
        JSONArray arr = new JSONArray();
        for (WatchItem w : items) {
            try { arr.put(w.toJson()); } catch (JSONException ignored) {}
        }
        ctx.getSharedPreferences(prefForTab(tabType), Context.MODE_PRIVATE)
                .edit().putString(KEY, arr.toString()).apply();
    }
}
