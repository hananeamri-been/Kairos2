package com.example.kairos;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BookStore {
    private static final String PREF = "books_store";
    private static final String KEY  = "books";

    public static List<BookItem> load(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String json = sp.getString(KEY, "[]");
        ArrayList<BookItem> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(BookItem.fromJson(o));
            }
        } catch (JSONException e) { /* ignore */ }
        return list;
    }

    public static void save(Context ctx, List<BookItem> items) {
        JSONArray arr = new JSONArray();
        for (BookItem b : items) {
            try { arr.put(b.toJson()); } catch (JSONException ignored) {}
        }
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putString(KEY, arr.toString()).apply();
    }
}
