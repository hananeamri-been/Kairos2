package com.example.kairos;

import org.json.JSONException;
import org.json.JSONObject;

public class WatchItem {
    public String title;
    public boolean watched;
    public int rating;     // 0..5
    public String comment;
    public String thumbUrl; // poster/miniature

    public WatchItem(String title) {
        this(title, null);
    }

    public WatchItem(String title, String thumbUrl) {
        this.title = title;
        this.thumbUrl = thumbUrl;
        this.watched = false;
        this.rating = 0;
        this.comment = "";
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("title", title);
        o.put("watched", watched);
        o.put("rating", rating);
        o.put("comment", comment);
        o.put("thumbUrl", thumbUrl);
        return o;
    }

    public static WatchItem fromJson(JSONObject o) throws JSONException {
        WatchItem w = new WatchItem(
                o.optString("title"),
                o.optString("thumbUrl", null)
        );
        w.watched = o.optBoolean("watched");
        w.rating  = o.optInt("rating");
        w.comment = o.optString("comment");
        return w;
    }
}
