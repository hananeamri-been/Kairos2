package com.example.kairos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ExpenseItem {
    public String name;
    public String address;
    public Double price;

    public ExpenseItem() {}
    public ExpenseItem(String name, String address, Double price) {
        this.name = name; this.address = address; this.price = price;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("name", name);
        o.put("address", address);
        o.put("price", price != null ? price : JSONObject.NULL);
        return o;
    }

    public static ExpenseItem fromJson(JSONObject o) {
        ExpenseItem e = new ExpenseItem();
        e.name = o.optString("name", null);
        e.address = o.optString("address", null);
        if (!o.isNull("price")) e.price = o.optDouble("price", 0.0);
        return e;
    }

    public static JSONArray listToJson(ArrayList<ExpenseItem> list) throws JSONException {
        JSONArray a = new JSONArray();
        for (ExpenseItem e : list) a.put(e.toJson());
        return a;
    }

    public static ArrayList<ExpenseItem> listFromJson(JSONArray arr) throws JSONException {
        ArrayList<ExpenseItem> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) list.add(fromJson(arr.getJSONObject(i)));
        return list;
    }
}
