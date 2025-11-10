package com.example.kairos;

import android.content.Context;
import android.content.SharedPreferences;

public class PlaceImportStore {
    private static final String PREF = "place_import";
    private static final String K_TARGET = "target"; // ex: "hotel" ou "resto:3" ou "act:1" ou "other:0"
    private static final String K_NAME   = "name";
    private static final String K_ADDR   = "addr";

    public static void setPendingTarget(Context ctx, String target) {
        sp(ctx).edit().putString(K_TARGET, target).remove(K_NAME).remove(K_ADDR).apply();
    }
    public static void saveImported(Context ctx, String name, String addr) {
        sp(ctx).edit().putString(K_NAME, name).putString(K_ADDR, addr).apply();
    }
    public static String consumeTarget(Context ctx) {
        String t = sp(ctx).getString(K_TARGET, null);
        if (t == null) return null;
        return t;
    }
    public static String[] consumePlace(Context ctx) {
        SharedPreferences s = sp(ctx);
        String name = s.getString(K_NAME, null);
        String addr = s.getString(K_ADDR, null);
        if (name == null && addr == null) return null;
        s.edit().remove(K_NAME).remove(K_ADDR).remove(K_TARGET).apply();
        return new String[]{name, addr};
    }
    private static SharedPreferences sp(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }
}
