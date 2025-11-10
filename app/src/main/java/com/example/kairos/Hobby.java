package com.example.kairos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class Hobby {
    public String id;
    public String title;
    /** jours prévus (dim..sam) */
    public boolean[] freq = new boolean[7];
    /** cases cochées de la semaine affichée (dim..sam) */
    public boolean[] checked = new boolean[7];

    public Hobby(String title, boolean[] freq) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        if (freq != null && freq.length == 7) {
            System.arraycopy(freq, 0, this.freq, 0, 7);
        }

    }

    public Hobby(String id, String title, boolean[] freq, boolean[] checked) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.title = title != null ? title : "";
        if (freq != null && freq.length == 7) System.arraycopy(freq, 0, this.freq, 0, 7);
        if (checked != null && checked.length == 7) System.arraycopy(checked, 0, this.checked, 0, 7);
    }

    /** Copie pour nouvelle semaine : on garde le titre+freq, on remet checked à zéro */
    public Hobby copyForNewWeek() {
        return new Hobby(id, title, freq, new boolean[7]);
    }

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", id);
            o.put("title", title);
            JSONArray jf = new JSONArray();
            JSONArray jc = new JSONArray();
            for (int i = 0; i < 7; i++) {
                jf.put(freq[i]);
                jc.put(checked[i]);
            }
            o.put("freq", jf);
            o.put("checked", jc);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return o;
    }

    public static Hobby fromJson(JSONObject o) {
        String id = o.optString("id", UUID.randomUUID().toString());
        String title = o.optString("title", "");
        JSONArray jf = o.optJSONArray("freq");
        JSONArray jc = o.optJSONArray("checked");
        boolean[] f = new boolean[7];
        boolean[] c = new boolean[7];
        if (jf != null) for (int i = 0; i < 7 && i < jf.length(); i++) f[i] = jf.optBoolean(i, false);
        if (jc != null) for (int i = 0; i < 7 && i < jc.length(); i++) c[i] = jc.optBoolean(i, false);
        return new Hobby(id, title, f, c);
    }

    public boolean[] getFrequency() { return freq; }
}
