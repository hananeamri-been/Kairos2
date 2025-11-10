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

/**
 * Stockage local par SEMAINE (semaine = Dimanche..Samedi, comme ton UI S M T W T F S).
 * - Une clé par semaine: "week_YYYY-MM-DD" (YYYY-MM-DD = Dimanche de la semaine).
 * - Une clé "global_active_habits" = la liste d'habitudes "modèle" pour générer les semaines futures.
 * - Quand on modifie la semaine courante, on met à jour "global_active_habits" et on supprime les snapshots des semaines futures
 *   (elles seront régénérées à la prochaine visite).
 */
public class HabitStore {
    private static HabitStore INSTANCE;
    private static final String PREFS = "habit_store_v2";
    private static final String KEY_GLOBAL = "global_active_habits";

    private final SharedPreferences prefs;

    public HabitStore(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }


    public static synchronized HabitStore get(Context ctx) {
        if (INSTANCE == null) {
            INSTANCE = new HabitStore(ctx.getApplicationContext());
        }
        return INSTANCE;
    }
    /* ===================== Semaine (Dimanche comme début) ===================== */

    public static Calendar weekStartFor(Calendar any) {
        Calendar c = (Calendar) any.clone();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        // Place sur dimanche
        int dow = c.get(Calendar.DAY_OF_WEEK); // 1=dim, 7=sam (Locale US-like)
        int offset = dow - Calendar.SUNDAY; // 0 si dimanche
        c.add(Calendar.DAY_OF_MONTH, -offset);
        return c;
    }

    public static String weekKey(Calendar weekStart) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return "week_" + sdf.format(weekStart.getTime());
    }

    public static String labelForWeek(Calendar weekStart) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar end = (Calendar) weekStart.clone();
        end.add(Calendar.DAY_OF_MONTH, 6);
        return sdf.format(weekStart.getTime()) + "-" + sdf.format(end.getTime());
    }

    public static boolean isSameWeek(Calendar a, Calendar b) {
        Calendar wa = weekStartFor(a);
        Calendar wb = weekStartFor(b);
        return wa.get(Calendar.YEAR) == wb.get(Calendar.YEAR)
                && wa.get(Calendar.DAY_OF_YEAR) == wb.get(Calendar.DAY_OF_YEAR);
    }

    public static int todayIndexInWeek(Calendar today) {
        // 0 = Dimanche ... 6 = Samedi
        int dow = today.get(Calendar.DAY_OF_WEEK); // 1..7
        return (dow - Calendar.SUNDAY); // 0..6
    }

    /* ===================== CRUD listes d'habitudes par semaine ===================== */

    public boolean hasWeek(String wk) {
        return prefs.contains(wk);
    }

    public List<Habit> getHabitsForWeek(String wk) {
        String json = prefs.getString(wk, null);
        List<Habit> list = new ArrayList<>();
        if (json == null) return list;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                list.add(Habit.fromJson(arr.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void saveHabitsForWeek(String wk, List<Habit> list) {
        JSONArray arr = new JSONArray();
        for (Habit h : list) {
            try { arr.put(h.toJson()); } catch (JSONException ignore) {}
        }
        prefs.edit().putString(wk, arr.toString()).apply();
    }

    public String previousWeekKey(String wk) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(0);
        // parse wk "week_yyyy-MM-dd"
        String date = wk.substring(5);
        String[] parts = date.split("-");
        Calendar w = Calendar.getInstance();
        w.set(Calendar.YEAR, Integer.parseInt(parts[0]));
        w.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
        w.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
        w.set(Calendar.HOUR_OF_DAY, 0); w.set(Calendar.MINUTE, 0); w.set(Calendar.SECOND, 0); w.set(Calendar.MILLISECOND, 0);
        w.add(Calendar.DAY_OF_MONTH, -7);
        return weekKey(w);
    }

    /* ===================== Héritage / Global ===================== */

    public List<Habit> getGlobalActive() {
        String json = prefs.getString(KEY_GLOBAL, null);
        List<Habit> list = new ArrayList<>();
        if (json == null) return list;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                // pour la "global", on ignore checked[]
                Habit h = Habit.fromJson(arr.getJSONObject(i));
                h.checked = new boolean[7];
                list.add(h);
            }
        } catch (JSONException e) { e.printStackTrace(); }
        return list;
    }

    public void saveGlobalActive(List<Habit> list) {
        JSONArray arr = new JSONArray();
        for (Habit h : list) {
            try {
                // on met checked à vide dans le global
                Habit tmp = new Habit(h.id, h.title, h.freq, new boolean[7]);
                arr.put(tmp.toJson());
            } catch (JSONException ignore) {}
        }
        prefs.edit().putString(KEY_GLOBAL, arr.toString()).apply();
    }

    /** Initialise une semaine à partir :
     * - de la semaine précédente si elle existe
     * - sinon du "global_active_habits"
     */
    public List<Habit> ensureWeekInitialized(String wk) {
        if (hasWeek(wk)) return getHabitsForWeek(wk);

        // 1) depuis la semaine précédente SI elle est VRAIMENT avant la semaine demandée
        String prev = previousWeekKey(wk);
        long prevMillis = parseWeekMillis(prev);
        long currentMillis = parseWeekMillis(wk);
        if (hasWeek(prev) && prevMillis < currentMillis) {
            List<Habit> prevList = getHabitsForWeek(prev);
            List<Habit> cloned = new ArrayList<>();
            for (Habit h : prevList) cloned.add(h.copyForNewWeek());
            saveHabitsForWeek(wk, cloned);
            return cloned;
        }


        // 2) sinon, depuis global
        List<Habit> global = getGlobalActive();
        List<Habit> cloned = new ArrayList<>();
        for (Habit h : global) cloned.add(h.copyForNewWeek());
        saveHabitsForWeek(wk, cloned);
        return cloned;
    }

    /** Supprime tous les snapshots des semaines STRICTEMENT après wk (pour les régénérer avec le global). */
    public void clearFutureWeeks(String fromWeekKey) {
        Set<String> keys = new HashSet<>(prefs.getAll().keySet());
        long from = parseWeekMillis(fromWeekKey);
        SharedPreferences.Editor ed = prefs.edit();
        for (String k : keys) {
            if (k.startsWith("week_")) {
                long t = parseWeekMillis(k);
                if (t > from) ed.remove(k);
            }
        }
        ed.apply();
    }

    private long parseWeekMillis(String wk) {
        try {
            String date = wk.substring(5); // yyyy-MM-dd
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
