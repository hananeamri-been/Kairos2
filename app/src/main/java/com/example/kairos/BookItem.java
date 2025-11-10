package com.example.kairos;

import org.json.JSONException;
import org.json.JSONObject;

public class BookItem {
    public String title;
    public boolean read;
    public int rating;
    public String summary;
    public String buyUrl;
    public String thumbUrl;

    public BookItem(String title, String buyUrl) {
        this(title, buyUrl, null);
    }

    public BookItem(String title, String buyUrl, String thumbUrl) {
        this.title = title;
        this.buyUrl = buyUrl;
        this.thumbUrl = thumbUrl;
        this.read = false;
        this.rating = 0;
        this.summary = "";
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("title", title);
        o.put("read", read);
        o.put("rating", rating);
        o.put("summary", summary);
        o.put("buyUrl", buyUrl);
        o.put("thumbUrl", thumbUrl); // NEW
        return o;
    }

    public static BookItem fromJson(JSONObject o) throws JSONException {
        BookItem b = new BookItem(
                o.optString("title"),
                o.optString("buyUrl", null),
                o.optString("thumbUrl", null) // NEW
        );
        b.read = o.optBoolean("read");
        b.rating = o.optInt("rating");
        b.summary = o.optString("summary");
        return b;
    }
}
