package com.example.kairos;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.YearMonth;
import java.util.*;

public class JournalStore {

    private final SharedPreferences prefs;

    public JournalStore(Context ctx) {
        prefs = ctx.getSharedPreferences("journal_data", Context.MODE_PRIVATE);
    }

    // ===================== NOTES =====================
    public static class Note {
        public String id = UUID.randomUUID().toString();
        public String date = "";             // ISO yyyy-MM-dd
        public String title = "";
        public String content = "";
        public boolean locked = false;
        public String pin = null;            // ⬅️ PIN par note (null si non défini)
        public List<String> imageUris = new ArrayList<>();
    }

    public List<Note> loadNotes(YearMonth ym) {
        String key = "notes_" + ym;
        String json = prefs.getString(key, "[]");
        List<Note> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Note n = new Note();
                n.id = o.optString("id", UUID.randomUUID().toString());
                n.date = o.optString("date", "");
                n.title = o.optString("title", "");
                n.content = o.optString("content", "");
                n.locked = o.optBoolean("locked", false);
                n.pin = o.optString("pin", null);
                JSONArray imgs = o.optJSONArray("images");
                if (imgs != null) for (int j = 0; j < imgs.length(); j++) n.imageUris.add(imgs.getString(j));
                list.add(n);
            }
        } catch (JSONException e) { e.printStackTrace(); }
        return list;
    }

    public void saveNotes(YearMonth ym, List<Note> list) {
        JSONArray arr = new JSONArray();
        try {
            for (Note n : list) {
                JSONObject o = new JSONObject();
                o.put("id", n.id);
                o.put("date", n.date);
                o.put("title", n.title);
                o.put("content", n.content);
                o.put("locked", n.locked);
                if (n.pin != null) o.put("pin", n.pin);
                JSONArray imgs = new JSONArray();
                for (String s : n.imageUris) imgs.put(s);
                o.put("images", imgs);
                arr.put(o);
            }
        } catch (JSONException e) { e.printStackTrace(); }
        prefs.edit().putString("notes_" + ym, arr.toString()).apply();
    }

    // ===================== TODOS =====================
    public static class Todo {
        public String id = UUID.randomUUID().toString();
        public String title = "";
        public boolean done = false;
        public String date = "";
    }

    public List<Todo> loadTodos(YearMonth ym) {
        String key = "todos_" + ym;
        String json = prefs.getString(key, "[]");
        List<Todo> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Todo t = new Todo();
                t.id = o.optString("id", UUID.randomUUID().toString());
                t.title = o.optString("title", "");
                t.done = o.optBoolean("done", false);
                t.date = o.optString("date", "");
                list.add(t);
            }
        } catch (JSONException e) { e.printStackTrace(); }
        return list;
    }

    public void saveTodos(YearMonth ym, List<Todo> list) {
        JSONArray arr = new JSONArray();
        try {
            for (Todo t : list) {
                JSONObject o = new JSONObject();
                o.put("id", t.id);
                o.put("title", t.title);
                o.put("done", t.done);
                o.put("date", t.date);
                arr.put(o);
            }
        } catch (JSONException e) { e.printStackTrace(); }
        prefs.edit().putString("todos_" + ym, arr.toString()).apply();
    }

    // ===================== MEMORIES =====================
    public static class MemoryEntry { public String date; public String text; }

    public Map<String, MemoryEntry> loadMemories(YearMonth ym) {
        String key = "memories_" + ym;
        String json = prefs.getString(key, "{}");
        Map<String, MemoryEntry> map = new HashMap<>();
        try {
            JSONObject obj = new JSONObject(json);
            Iterator<String> it = obj.keys();
            while (it.hasNext()) {
                String date = it.next();
                JSONObject m = obj.getJSONObject(date);
                MemoryEntry e = new MemoryEntry();
                e.date = date;
                e.text = m.optString("text", "");
                map.put(date, e);
            }
        } catch (JSONException e) { e.printStackTrace(); }
        return map;
    }

    public void saveMemories(YearMonth ym, Map<String, MemoryEntry> map) {
        JSONObject obj = new JSONObject();
        try {
            for (String date : map.keySet()) {
                MemoryEntry m = map.get(date);
                JSONObject o = new JSONObject();
                o.put("text", m.text);
                obj.put(date, o);
            }
        } catch (JSONException e) { e.printStackTrace(); }
        prefs.edit().putString("memories_" + ym, obj.toString()).apply();
    }

    // ===================== MOODS =====================
    public enum Mood { GREAT, GOOD, OK, BAD, AWFUL }
    public static class MoodEntry { public String date; public Mood mood; }

    public Map<String, MoodEntry> loadMoods(YearMonth ym) {
        String key = "moods_" + ym;
        String json = prefs.getString(key, "{}");
        Map<String, MoodEntry> map = new HashMap<>();
        try {
            JSONObject obj = new JSONObject(json);
            Iterator<String> it = obj.keys();
            while (it.hasNext()) {
                String date = it.next();
                JSONObject m = obj.getJSONObject(date);
                MoodEntry e = new MoodEntry();
                e.date = date;
                e.mood = Mood.valueOf(m.optString("mood", "OK"));
                map.put(date, e);
            }
        } catch (JSONException e) { e.printStackTrace(); }
        return map;
    }

    public void saveMoods(YearMonth ym, Map<String, MoodEntry> map) {
        JSONObject obj = new JSONObject();
        try {
            for (String date : map.keySet()) {
                MoodEntry m = map.get(date);
                JSONObject o = new JSONObject();
                o.put("mood", m.mood.name());
                obj.put(date, o);
            }
        } catch (JSONException e) { e.printStackTrace(); }
        prefs.edit().putString("moods_" + ym, obj.toString()).apply();
    }
}
