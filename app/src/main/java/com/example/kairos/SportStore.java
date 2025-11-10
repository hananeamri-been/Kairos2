package com.example.kairos;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.List;

public class SportStore {
    private static final String PREF = "sport_store";
    private static final String KEY_GLOBAL = "sport_global_model";

    private final SharedPreferences sp;

    public SportStore(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    /* ======== Semaine actuelle ======== */
    public static Calendar weekStartFor(Calendar any) {
        Calendar c = (Calendar) any.clone();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        int dow = c.get(Calendar.DAY_OF_WEEK);
        c.add(Calendar.DAY_OF_MONTH, -(dow - Calendar.SUNDAY));
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
        return "sport_week_" + sdf.format(weekStart.getTime());
    }

    public static String previousWeekKey(String wk) {
        try {
            String date = wk.substring("sport_week_".length());
            String[] p = date.split("-");
            Calendar w = Calendar.getInstance();
            w.set(Calendar.YEAR, Integer.parseInt(p[0]));
            w.set(Calendar.MONTH, Integer.parseInt(p[1]) - 1);
            w.set(Calendar.DAY_OF_MONTH, Integer.parseInt(p[2]));
            w.add(Calendar.DAY_OF_MONTH, -7);
            return weekKey(w);
        } catch (Exception e) { return wk; }
    }

    /* ======== CRUD Semaine ======== */
    public boolean hasWeek(String wk) { return sp.contains(wk); }

    public SportWeekData getWeek(String wk) {
        String json = sp.getString(wk, null);
        if (json == null) return null;
        try {
            return fromJson(new JSONObject(json));
        } catch (JSONException e) {
            return new SportWeekData();
        }
    }

    public void saveWeek(String wk, SportWeekData data) {
        sp.edit().putString(wk, toJson(data).toString()).apply();
    }

    public SportWeekData getGlobalModel() {
        String json = sp.getString(KEY_GLOBAL, null);
        if (json == null) return new SportWeekData();
        try {
            return fromJson(new JSONObject(json));
        } catch (JSONException e) {
            return new SportWeekData();
        }
    }

    public void saveGlobalModel(SportWeekData model) {
        sp.edit().putString(KEY_GLOBAL, toJson(model).toString()).apply();
    }

    public SportWeekData ensureWeekInitialized(String wk) {
        if (hasWeek(wk)) return getWeek(wk);

        String prev = previousWeekKey(wk);
        if (hasWeek(prev)) {
            SportWeekData last = getWeek(prev);
            SportWeekData fresh = last.cloneForNewWeek();
            saveWeek(wk, fresh);
            return fresh;
        } else {
            SportWeekData model = getGlobalModel();
            SportWeekData fresh = model.cloneForNewWeek();
            saveWeek(wk, fresh);
            return fresh;
        }
    }

    public void clearFutureWeeks(String fromWeekKey) {
        long from = parseWeekMillis(fromWeekKey);
        SharedPreferences.Editor ed = sp.edit();
        Set<String> keys = new HashSet<>(sp.getAll().keySet());
        for (String k : keys) {
            if (k.startsWith("sport_week_")) {
                long t = parseWeekMillis(k);
                if (t > from) ed.remove(k);
            }
        }
        ed.apply();
    }

    private long parseWeekMillis(String wk) {
        try {
            String date = wk.substring("sport_week_".length());
            String[] p = date.split("-");
            Calendar w = Calendar.getInstance();
            w.set(Calendar.YEAR, Integer.parseInt(p[0]));
            w.set(Calendar.MONTH, Integer.parseInt(p[1]) - 1);
            w.set(Calendar.DAY_OF_MONTH, Integer.parseInt(p[2]));
            w.set(Calendar.HOUR_OF_DAY, 0);
            w.set(Calendar.MINUTE, 0);
            w.set(Calendar.SECOND, 0);
            w.set(Calendar.MILLISECOND, 0);
            return w.getTimeInMillis();
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    /* ======== Conversion JSON ======== */
    private JSONObject toJson(SportWeekData data) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("running_freq", boolListToJson(data.running.freq));
            JSONArray runs = new JSONArray();
            for (RunningEntry e : data.running.entries) {
                JSONObject o = new JSONObject();
                o.put("enabled", e.enabled);
                o.put("distance", e.distanceKm);
                o.put("duration", e.durationMin);
                o.put("pace", e.paceMinPerKm);
                runs.put(o);
            }
            obj.put("running_entries", runs);

            obj.put("gym_freq", boolListToJson(data.gym.freq));
            JSONArray gymDays = new JSONArray();
            for (int i = 0; i < 7; i++) {
                JSONArray sessions = new JSONArray();
                for (GymSession s : data.gym.sessions.get(i)) {
                    JSONObject so = new JSONObject();
                    so.put("group", s.muscleGroup);
                    JSONArray exs = new JSONArray();
                    for (ExerciseEntry ex : s.exercises) {
                        JSONObject exo = new JSONObject();
                        exo.put("name", ex.name);
                        exo.put("reps", ex.reps);
                        exo.put("weight", ex.weight);
                        exs.put(exo);
                    }
                    so.put("exercises", exs);
                    sessions.put(so);
                }
                gymDays.put(sessions);
            }
            obj.put("gym_sessions", gymDays);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    private SportWeekData fromJson(JSONObject obj) throws JSONException {
        SportWeekData d = new SportWeekData();

        d.running.freq = jsonToBoolList(obj.optJSONArray("running_freq"));
        JSONArray runs = obj.optJSONArray("running_entries");
        if (runs != null) {
            d.running.entries.clear();
            for (int i = 0; i < runs.length(); i++) {
                JSONObject o = runs.getJSONObject(i);
                RunningEntry e = new RunningEntry(
                        o.optBoolean("enabled"),
                        o.optDouble("distance"),
                        o.optDouble("duration"),
                        o.optDouble("pace")
                );
                d.running.entries.add(e);
            }
        }

        d.gym.freq = jsonToBoolList(obj.optJSONArray("gym_freq"));
        JSONArray gymDays = obj.optJSONArray("gym_sessions");
        if (gymDays != null) {
            d.gym.sessions.clear();
            for (int i = 0; i < gymDays.length(); i++) {
                JSONArray sessions = gymDays.getJSONArray(i);
                List<GymSession> list = new java.util.ArrayList<>();
                for (int j = 0; j < sessions.length(); j++) {
                    JSONObject so = sessions.getJSONObject(j);
                    GymSession s = new GymSession(so.optString("group"));
                    JSONArray exs = so.optJSONArray("exercises");
                    for (int k = 0; k < exs.length(); k++) {
                        JSONObject ex = exs.getJSONObject(k);
                        s.exercises.add(new ExerciseEntry(
                                ex.optString("name"),
                                ex.optInt("sets"),       // ✅ nouveau champ
                                ex.optInt("reps"),
                                ex.optDouble("weight")
                        ));

                    }
                    list.add(s);
                }
                d.gym.sessions.add(list);
            }
        }
        return d;
    }

    private JSONArray boolListToJson(java.util.List<Boolean> list) {
        JSONArray arr = new JSONArray();
        for (Boolean b : list) arr.put(b);
        return arr;
    }

    private java.util.List<Boolean> jsonToBoolList(JSONArray arr) {
        java.util.List<Boolean> list = new java.util.ArrayList<>();
        if (arr == null) {
            for (int i = 0; i < 7; i++) list.add(false);
            return list;
        }
        for (int i = 0; i < arr.length(); i++) list.add(arr.optBoolean(i, false));
        while (list.size() < 7) list.add(false);
        return list;
    }
}
