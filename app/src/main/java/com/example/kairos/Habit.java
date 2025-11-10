package com.example.kairos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class Habit {
    public String id;
    public String title;
    /** jours prévus (dim..sam) */
    public boolean[] freq = new boolean[7];
    /** cases cochées de la semaine affichée (dim..sam) */
    public boolean[] checked = new boolean[7];

    public Habit(String title, boolean[] freq) {
        this(UUID.randomUUID().toString(), title, freq, new boolean[7]);
    }

    public Habit(String id, String title, boolean[] freq, boolean[] checked) {
        this.id = id;
        this.title = title;
        if (freq != null && freq.length == 7) System.arraycopy(freq, 0, this.freq, 0, 7);
        if (checked != null && checked.length == 7) System.arraycopy(checked, 0, this.checked, 0, 7);
    }

    public Habit copyForNewWeek() {
        // On hérite du titre + fréquence, mais les coches repartent à vide
        return new Habit(UUID.randomUUID().toString(), title, freq, new boolean[7]);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("title", title);
        JSONArray jf = new JSONArray();
        JSONArray jc = new JSONArray();
        for (int i = 0; i < 7; i++) { jf.put(freq[i]); jc.put(checked[i]); }
        o.put("freq", jf);
        o.put("checked", jc);
        return o;
    }

    public static Habit fromJson(JSONObject o) throws JSONException {
        String id = o.optString("id", UUID.randomUUID().toString());
        String title = o.optString("title", "");
        JSONArray jf = o.optJSONArray("freq");
        JSONArray jc = o.optJSONArray("checked");
        boolean[] f = new boolean[7];
        boolean[] c = new boolean[7];
        if (jf != null) for (int i = 0; i < 7 && i < jf.length(); i++) f[i] = jf.optBoolean(i, false);
        if (jc != null) for (int i = 0; i < 7 && i < jc.length(); i++) c[i] = jc.optBoolean(i, false);
        return new Habit(id, title, f, c);
    }
    public boolean[] getFrequency() {
        return freq;
    }

}
