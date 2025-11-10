package com.example.kairos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

public class Travel {
    public String id;

    // Base
    public String countryCode;     // ISO-2
    public String city;
    public double initialBudget;

    // Dates
    public String startDate;       // "yyyy-MM-dd" (stocké brut)
    public String endDate;

    // Transports / hébergement
    public double flightPrice;
    public double carPrice;
    public String hotelName;
    public String hotelAddress;
    public double hotelPrice;

    // Dépenses
    public ArrayList<ExpenseItem> restaurants = new ArrayList<>();
    public ArrayList<ExpenseItem> activities  = new ArrayList<>();
    public ArrayList<ExpenseItem> others      = new ArrayList<>();

    public Travel(String countryCode, String city, double initialBudget) {
        this.id = UUID.randomUUID().toString();
        this.countryCode = countryCode;
        this.city = city;
        this.initialBudget = initialBudget;
    }

    public double totalSpent() {
        double total = 0;
        total += flightPrice;
        total += carPrice;
        total += hotelPrice;
        total += sum(restaurants);
        total += sum(activities);
        total += sum(others);
        return total;
    }

    public double remainingBudget() {
        return initialBudget - totalSpent();
    }

    public static double sum(ArrayList<ExpenseItem> list) {
        double s = 0;
        for (ExpenseItem e : list) {
            s += (e.price != null ? e.price : 0.0);
        }
        return s;
    }

    public int daysCount() {
        try {
            if (startDate == null || endDate == null || startDate.isEmpty() || endDate.isEmpty()) return 0;
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            sdf.setLenient(false);
            java.util.Date ds = sdf.parse(startDate);
            java.util.Date de = sdf.parse(endDate);
            if (ds == null || de == null) return 0;
            long diff = de.getTime() - ds.getTime();
            if (diff < 0) return 0;
            // +1 jour si tu veux compter les deux extrémités
            return (int) (diff / (1000 * 60 * 60 * 24)) + 1;
        } catch (Exception e) {
            return 0;
        }
    }


    // ------------- JSON -------------
    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("countryCode", countryCode);
        o.put("city", city);
        o.put("initialBudget", initialBudget);
        o.put("startDate", startDate);
        o.put("endDate", endDate);
        o.put("flightPrice", flightPrice);
        o.put("carPrice", carPrice);
        o.put("hotelName", hotelName);
        o.put("hotelAddress", hotelAddress);
        o.put("hotelPrice", hotelPrice);
        o.put("restaurants", ExpenseItem.listToJson(restaurants));
        o.put("activities",  ExpenseItem.listToJson(activities));
        o.put("others",      ExpenseItem.listToJson(others));
        return o;
    }

    public static Travel fromJson(JSONObject o) throws JSONException {
        Travel t = new Travel(
                o.optString("countryCode", ""),
                o.optString("city", ""),
                o.optDouble("initialBudget", 0.0)
        );
        t.id = o.optString("id", UUID.randomUUID().toString());
        t.startDate   = o.optString("startDate", null);
        t.endDate     = o.optString("endDate", null);
        t.flightPrice = o.optDouble("flightPrice", 0.0);
        t.carPrice    = o.optDouble("carPrice", 0.0);
        t.hotelName   = o.optString("hotelName", null);
        t.hotelAddress= o.optString("hotelAddress", null);
        t.hotelPrice  = o.optDouble("hotelPrice", 0.0);
        t.restaurants = ExpenseItem.listFromJson(o.optJSONArray("restaurants"));
        t.activities  = ExpenseItem.listFromJson(o.optJSONArray("activities"));
        t.others      = ExpenseItem.listFromJson(o.optJSONArray("others"));
        return t;
    }

    public static JSONArray listToJson(ArrayList<Travel> list) throws JSONException {
        JSONArray a = new JSONArray();
        for (Travel t : list) a.put(t.toJson());
        return a;
    }

    public static ArrayList<Travel> listFromJson(JSONArray arr) throws JSONException {
        ArrayList<Travel> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) list.add(fromJson(arr.getJSONObject(i)));
        return list;
    }
}
