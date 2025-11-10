package com.example.kairos;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HobbyStore {
    private static final String PREFS_NAME = "hobby_store";
    private static final String KEY_GLOBAL = "global_active_hobbies";

    private final SharedPreferences prefs;

    public HobbyStore(Context ctx) {
        this.prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /* ===================== Semaine (Dimanche comme début) ===================== */

    public static Calendar weekStartFor(Calendar any) {
        Calendar c = (Calendar) any.clone();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        // Place sur dimanche
        int dow = c.get(Calendar.DAY_OF_WEEK); // 1=dim, 7=sam
        int offset = dow - Calendar.SUNDAY; // 0 si dimanche
        c.add(Calendar.DAY_OF_MONTH, -offset);
        return c;
    }

    public static boolean isSameWeek(Calendar a, Calendar b) {
        Calendar wa = weekStartFor(a);
        Calendar wb = weekStartFor(b);
        return wa.get(Calendar.YEAR) == wb.get(Calendar.YEAR)
                && wa.get(Calendar.DAY_OF_YEAR) == wb.get(Calendar.DAY_OF_YEAR);
    }

    public static String weekKey(Calendar weekStart) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return "hobby_week_" + sdf.format(weekStart.getTime());
    }

    public static String labelForWeek(Calendar weekStart) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        Calendar end = (Calendar) weekStart.clone();
        end.add(Calendar.DAY_OF_MONTH, 6);
        return sdf.format(weekStart.getTime()) + " — " + sdf.format(end.getTime());
    }

    public static int todayIndexInWeek(Calendar today) {
        // 0 = Dimanche ... 6 = Samedi
        int dow = today.get(Calendar.DAY_OF_WEEK); // 1..7
        return (dow - Calendar.SUNDAY); // 0..6
    }

    /* ===================== CRUD listes de hobbies par semaine ===================== */

    public boolean hasWeek(String wk) {
        return prefs.contains(wk);
    }

    public List<Hobby> getHobbiesForWeek(String wk) {
        String json = prefs.getString(wk, null);
        List<Hobby> list = new ArrayList<>();
        if (json == null) return list;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) list.add(Hobby.fromJson(arr.getJSONObject(i)));
        } catch (JSONException e) { e.printStackTrace(); }
        return list;
    }

    public void saveHobbiesForWeek(String wk, List<Hobby> list) {
        JSONArray arr = new JSONArray();
        for (Hobby h : list) arr.put(h.toJson());
        prefs.edit().putString(wk, arr.toString()).apply();
    }

    public static String previousWeekKey(String wk) {
        try {
            String date = wk.substring("hobby_week_".length()); // yyyy-MM-dd
            String[] parts = date.split("-");
            Calendar w = Calendar.getInstance();
            w.set(Calendar.YEAR, Integer.parseInt(parts[0]));
            w.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
            w.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
            w.set(Calendar.HOUR_OF_DAY, 0); w.set(Calendar.MINUTE, 0); w.set(Calendar.SECOND, 0); w.set(Calendar.MILLISECOND, 0);
            w.add(Calendar.DAY_OF_MONTH, -7);
            return weekKey(w);
        } catch (Exception e) { return wk; }
    }

    /* ===================== Héritage / Global ===================== */

    public List<Hobby> getGlobalActive() {
        String json = prefs.getString(KEY_GLOBAL, null);
        List<Hobby> list = new ArrayList<>();
        if (json == null) return list;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) list.add(Hobby.fromJson(arr.getJSONObject(i)));
        } catch (JSONException e) { e.printStackTrace(); }
        return list;
    }

    public void saveGlobalActive(List<Hobby> list) {
        JSONArray arr = new JSONArray();
        for (Hobby h : list) arr.put(h.toJson());
        prefs.edit().putString(KEY_GLOBAL, arr.toString()).apply();
    }

    /** Initialise une semaine (snapshot) soit depuis la semaine précédente, soit depuis le global. */
    public List<Hobby> ensureWeekInitialized(String wk) {
        if (hasWeek(wk)) return getHobbiesForWeek(wk);

        // 1) depuis la semaine précédente SI elle est avant la semaine demandée
        String prev = previousWeekKey(wk);
        long prevMillis = parseWeekMillis(prev);
        long currentMillis = parseWeekMillis(wk);
        if (hasWeek(prev) && prevMillis < currentMillis) {
            List<Hobby> prevList = getHobbiesForWeek(prev);
            List<Hobby> cloned = new ArrayList<>();
            for (Hobby h : prevList) cloned.add(h.copyForNewWeek());
            saveHobbiesForWeek(wk, cloned);
            return cloned;
        }

        // 2) sinon, depuis global
        List<Hobby> global = getGlobalActive();
        List<Hobby> cloned = new ArrayList<>();
        for (Hobby h : global) cloned.add(h.copyForNewWeek());
        saveHobbiesForWeek(wk, cloned);
        return cloned;
    }

    /** Supprime tous les snapshots des semaines STRICTEMENT après wk (pour les régénérer via le global). */
    public void clearFutureWeeks(String fromWeekKey) {
        Set<String> keys = new HashSet<>(prefs.getAll().keySet());
        long from = parseWeekMillis(fromWeekKey);
        SharedPreferences.Editor ed = prefs.edit();
        for (String k : keys) {
            if (k.startsWith("hobby_week_")) {
                long t = parseWeekMillis(k);
                if (t > from) ed.remove(k);
            }
        }
        ed.apply();
    }

    private long parseWeekMillis(String wk) {
        try {
            String date = wk.substring("hobby_week_".length()); // yyyy-MM-dd
            String[] p = date.split("-");
            Calendar w = Calendar.getInstance();
            w.set(Calendar.YEAR, Integer.parseInt(p[0]));
            w.set(Calendar.MONTH, Integer.parseInt(p[1]) - 1);
            w.set(Calendar.DAY_OF_MONTH, Integer.parseInt(p[2]));
            w.set(Calendar.HOUR_OF_DAY, 0); w.set(Calendar.MINUTE, 0); w.set(Calendar.SECOND, 0); w.set(Calendar.MILLISECOND, 0);
            return w.getTimeInMillis();
        } catch (Exception e) { return Long.MAX_VALUE; }
    }
}
