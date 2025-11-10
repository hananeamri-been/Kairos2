package com.example.kairos;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class JournalActivity extends BaseActivity {

    private TextView txtMonth;
    private GridView gridMemories, gridMood;
    private LinearLayout btnJournal, btnTodo;

    private YearMonth currentMonth;       // mois affiché
    private LocalDate today;              // date d’aujourd’hui
    private final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMMM yyyy");

    private JournalStore store;

    // cache mémoire/mood du mois courant
    private Map<String, JournalStore.MemoryEntry> monthMemories = new HashMap<>();
    private Map<String, JournalStore.MoodEntry> monthMoods = new HashMap<>();

    // emojis mood (ordre: GREAT, GOOD, OK, BAD, AWFUL)
    private final String[] moodEmojis = {"😄", "🙂", "😐", "🙁", "😢"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        setupBottomBar(); // supprime cette ligne si tu n'as pas cette méthode

        store = new JournalStore(this);
        today = LocalDate.now();
        currentMonth = YearMonth.from(today);

        txtMonth = findViewById(R.id.txtMonth);
        gridMemories = findViewById(R.id.gridMemories);
        gridMood = findViewById(R.id.gridMood);
        btnJournal = findViewById(R.id.btnJournal);
        btnTodo = findViewById(R.id.btnTodo);

        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1);
            reloadMonth();
        });
        findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1);
            reloadMonth();
        });

        // TODO: brancher tes écrans de Journal / Todo
        btnJournal.setOnClickListener(v -> {
            Intent i = new Intent(this, NotesActivity.class);
            i.putExtra("EXTRA_YEAR_MONTH", currentMonth.toString()); // ex: 2025-10
            startActivity(i);
        });
        btnTodo.setOnClickListener(v -> {
            Intent i = new Intent(this, TodoListsActivity.class); // à faire plus tard
            i.putExtra("EXTRA_YEAR_MONTH", currentMonth.toString());
            startActivity(i);
        });


        reloadMonth();
    }

    private void reloadMonth() {
        txtMonth.setText(capitalize(currentMonth.format(MONTH_LABEL)));

        monthMemories = store.loadMemories(currentMonth);
        monthMoods = store.loadMoods(currentMonth);

        gridMemories.setAdapter(new CalendarAdapter(CalendarType.MEMORY));
        gridMood.setAdapter(new CalendarAdapter(CalendarType.MOOD));
    }

    // ------------------ ADAPTER DES CALENDRIERS ------------------

    private enum CalendarType { MEMORY, MOOD }

    private class CalendarAdapter extends BaseAdapter {
        private final CalendarType type;
        private final int daysInMonth;
        private final int firstDayOffset;
        private final List<LocalDate> cells = new ArrayList<>();

        CalendarAdapter(CalendarType type) {
            this.type = type;
            daysInMonth = currentMonth.lengthOfMonth();
            LocalDate first = currentMonth.atDay(1);
            int dow = first.getDayOfWeek().getValue(); // Lundi=1..Dimanche=7
            firstDayOffset = dow % 7; // Décalage pour alignement (Dimanche=0)
            int totalCells = firstDayOffset + daysInMonth;
            for (int i = 0; i < totalCells; i++) {
                if (i < firstDayOffset) {
                    cells.add(null);
                } else {
                    cells.add(currentMonth.atDay(i - firstDayOffset + 1));
                }
            }
        }

        @Override public int getCount() { return cells.size(); }
        @Override public Object getItem(int position) { return cells.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LocalDate date = cells.get(position);
            LinearLayout root;
            if (convertView == null) {
                root = new LinearLayout(JournalActivity.this);
                root.setOrientation(LinearLayout.VERTICAL);
                root.setPadding(dp(4), dp(6), dp(4), dp(6));
                root.setGravity(Gravity.CENTER_HORIZONTAL);
                root.setMinimumHeight(dp(44)); // hauteur mini d'une cellule

                TextView tvDay = new TextView(JournalActivity.this);
                tvDay.setId(android.R.id.text1);
                tvDay.setTextSize(14);
                tvDay.setGravity(Gravity.CENTER);

                TextView tvMark = new TextView(JournalActivity.this);
                tvMark.setId(android.R.id.text2);
                tvMark.setTextSize(12);
                tvMark.setGravity(Gravity.CENTER);

                root.addView(tvDay, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                root.addView(tvMark, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {
                root = (LinearLayout) convertView;
            }

            TextView tvDay = root.findViewById(android.R.id.text1);
            TextView tvMark = root.findViewById(android.R.id.text2);

            if (date == null) {
                tvDay.setText("");
                tvMark.setText("");
                root.setOnClickListener(null);
                root.setAlpha(1f);
                return root;
            }

            // Style selon passé / présent / futur
            boolean tempIsToday  = date.equals(today);
            boolean tempIsPast   = date.isBefore(today);
            boolean tempIsFuture = date.isAfter(today);

            YearMonth ymToday = YearMonth.from(today);
            if (currentMonth.isBefore(ymToday)) { tempIsPast = true;  tempIsFuture = false; tempIsToday = false; }
            if (currentMonth.isAfter(ymToday))  { tempIsPast = false; tempIsFuture = true;  tempIsToday = false; }

            root.setAlpha(tempIsFuture ? 0.35f : 1f);

            final boolean isTodayFinal  = tempIsToday;
            final boolean isPastFinal   = tempIsPast;
            final boolean isFutureFinal = tempIsFuture;
            final String key = date.format(ISO);

            // --- Rendu de la cellule ---
            if (type == CalendarType.MOOD) {
                JournalStore.MoodEntry me = monthMoods.get(key);
                if (me != null) {
                    // Mood présent : l'emoji REMPLACE la date (grand)
                    tvDay.setText(moodToEmoji(me.mood));
                    tvDay.setTextSize(24);
                    tvMark.setText("");
                } else {
                    // Pas de mood : afficher le numéro du jour
                    tvDay.setText(String.valueOf(date.getDayOfMonth()));
                    tvDay.setTextSize(14);
                    tvMark.setText("");
                }
            } else { // MEMORY
                tvDay.setText(String.valueOf(date.getDayOfMonth()));
                tvDay.setTextSize(14);
                JournalStore.MemoryEntry mm = monthMemories.get(key);
                tvMark.setText(mm != null && mm.text != null && !mm.text.trim().isEmpty() ? "•" : "");
            }

            // Clics
            root.setOnClickListener(v -> {
                if (type == CalendarType.MOOD) {
                    if (isFutureFinal) {
                        toast("Mood verrouillé pour les jours futurs");
                    } else if (isPastFinal) {
                        JournalStore.MoodEntry me = monthMoods.get(key);
                        toast(me != null ? "Mood: " + moodToEmoji(me.mood) : "Aucun mood");
                    } else if (isTodayFinal) {
                        openMoodPicker(key);
                    }
                } else { // MEMORY
                    if (isFutureFinal) {
                        toast("Memorable moment verrouillé pour les jours futurs");
                    } else if (isPastFinal && !isTodayFinal) {
                        openMemoryDialog(key, false);
                    } else {
                        openMemoryDialog(key, true);
                    }
                }
            });

            return root;
        }
    }

    // ------------------ DIALOGUES ------------------

    private void openMemoryDialog(@NonNull String isoDate, boolean editable) {
        String existing = "";
        JournalStore.MemoryEntry entry = monthMemories.get(isoDate);
        if (entry != null) existing = entry.text;

        EditText input = new EditText(this);
        input.setMinLines(4);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setText(existing);
        input.setSelection(input.getText().length());
        input.setEnabled(editable);

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle("Memorable moment\n" + isoDate)
                .setView(input)
                .setNegativeButton("Fermer", null);

        if (editable) {
            b.setPositiveButton("Enregistrer", (d, w) -> {
                String text = input.getText().toString().trim();
                JournalStore.MemoryEntry m = new JournalStore.MemoryEntry();
                m.date = isoDate;
                m.text = text;
                monthMemories.put(isoDate, m);
                store.saveMemories(currentMonth, monthMemories);
                reloadMonth();
            });
        }
        b.show();
    }

    private void openMoodPicker(@NonNull String isoDate) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(8), dp(16), dp(8), dp(8));
        row.setGravity(Gravity.CENTER);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Choose mood\n" + isoDate)
                .setView(row)
                .setNegativeButton("Annuler", null)
                .create();

        for (int i = 0; i < moodEmojis.length; i++) {
            final int idx = i;
            TextView tv = new TextView(this);
            tv.setText(moodEmojis[i]);
            tv.setTextSize(28);
            tv.setPadding(dp(8), dp(8), dp(8), dp(8));
            tv.setOnClickListener(v -> {
                JournalStore.MoodEntry me = new JournalStore.MoodEntry();
                me.date = isoDate;
                me.mood = idxToMood(idx);
                monthMoods.put(isoDate, me);
                store.saveMoods(currentMonth, monthMoods);
                dialog.dismiss();
                reloadMonth();
            });
            row.addView(tv);
        }
        dialog.show();
    }

    private String moodToEmoji(JournalStore.Mood m) {
        switch (m) {
            case GREAT: return "😄";
            case GOOD:  return "🙂";
            case OK:    return "😐";
            case BAD:   return "🙁";
            case AWFUL: return "😢";
        }
        return "";
    }

    private JournalStore.Mood idxToMood(int i) {
        switch (i) {
            case 0: return JournalStore.Mood.GREAT;
            case 1: return JournalStore.Mood.GOOD;
            case 2: return JournalStore.Mood.OK;
            case 3: return JournalStore.Mood.BAD;
            default: return JournalStore.Mood.AWFUL;
        }
    }

    // ------------------ UTILS ------------------

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(v * d);
    }
}
