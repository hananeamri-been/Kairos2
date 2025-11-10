package com.example.kairos;

import android.os.Bundle;
import android.text.InputType;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TodoEditorActivity extends BaseActivity {

    private TextView txtTitle;
    private ImageButton btnAdd, btnBack;
    private ListView list;

    private JournalStore store;
    private YearMonth ym;
    private String isoDate;

    private List<JournalStore.Todo> monthTodos;
    private final List<JournalStore.Todo> itemsForDay = new ArrayList<>();
    private TaskAdapter adapter;

    private boolean readOnly; // true si date passée

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_editor);
        setupBottomBar();

        txtTitle = findViewById(R.id.txtTitle);
        btnAdd   = findViewById(R.id.btnAdd);
        btnBack  = findViewById(R.id.btnBack);
        list     = findViewById(R.id.listTasks);

        // IMPORTANT quand un item contient une CheckBox
        list.setItemsCanFocus(false);

        store = new JournalStore(this);
        String ymStr = getIntent().getStringExtra("EXTRA_YEAR_MONTH");
        isoDate = getIntent().getStringExtra("EXTRA_DATE");
        ym = (ymStr != null) ? YearMonth.parse(ymStr) : YearMonth.from(LocalDate.now());

        txtTitle.setText("To-Do — " + isoDate);

        LocalDate d = LocalDate.parse(isoDate);
        readOnly = d.isBefore(LocalDate.now());

        btnBack.setOnClickListener(v -> finish());

        btnAdd.setOnClickListener(v -> {
            if (readOnly) { toast("Liste passée : lecture seule"); return; }
            askText("Nouvelle tâche", "", text -> {
                if (text == null) return;
                String t = text.trim();
                if (t.isEmpty()) return;               // pas de tâche vide
                JournalStore.Todo todo = new JournalStore.Todo();
                todo.date = isoDate;
                todo.title = t;
                todo.done = false;
                monthTodos.add(todo);
                save();
            });
        });

        // TAP = éditer le texte
        list.setOnItemClickListener((p, v, pos, id) -> {
            if (readOnly) { toast("Lecture seule"); return; }
            JournalStore.Todo t = itemsForDay.get(pos);
            askText("Modifier la tâche", t.title, newTitle -> {
                if (newTitle == null) return;
                String s = newTitle.trim();
                if (s.isEmpty()) return;
                t.title = s;
                save();
            });
        });

        // LONG PRESS = supprimer
        list.setOnItemLongClickListener((p, v, pos, id) -> {
            if (readOnly) return true;
            JournalStore.Todo t = itemsForDay.get(pos);
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Supprimer la tâche ?")
                    .setMessage(t.title)
                    .setPositiveButton("Supprimer", (d1, w) -> {
                        monthTodos.remove(t);
                        save();
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        monthTodos = store.loadTodos(ym);

        // Nettoyage auto : supprime les tâches à titre vide pour ce jour (héritage ancien)
        boolean removed = false;
        for (Iterator<JournalStore.Todo> it = monthTodos.iterator(); it.hasNext(); ) {
            JournalStore.Todo t = it.next();
            if (isoDate.equals(t.date) && (t.title == null || t.title.trim().isEmpty())) {
                it.remove();
                removed = true;
            }
        }
        if (removed) store.saveTodos(ym, monthTodos);

        rebuildItemsForDay();
    }

    private void rebuildItemsForDay() {
        itemsForDay.clear();
        if (monthTodos != null) {
            for (JournalStore.Todo t : monthTodos)
                if (isoDate.equals(t.date)) itemsForDay.add(t);
        }
        if (adapter == null) {
            adapter = new TaskAdapter();
            list.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        btnAdd.setEnabled(!readOnly);
    }

    private void save() {
        store.saveTodos(ym, monthTodos);
        rebuildItemsForDay();
    }

    // ===== UI helpers =====

    private void askText(String title, String preset, TextCallback cb) {
        final EditText input = new EditText(this);
        input.setHint("Intitulé");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(preset);
        input.setSelection(input.getText().length());
        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setView(input)
                .setPositiveButton("OK", (d, w) -> cb.onText(input.getText().toString()))
                .setNegativeButton("Annuler", (d, w) -> cb.onText(null))
                .show();
    }
    private void askText(String title, TextCallback cb) { askText(title, "", cb); }
    interface TextCallback { void onText(String s); }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    // ===== Adapter des tâches =====
    private class TaskAdapter extends android.widget.BaseAdapter {
        @Override public int getCount() { return itemsForDay.size(); }
        @Override public Object getItem(int position) { return itemsForDay.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
            android.view.View v = (convertView == null)
                    ? getLayoutInflater().inflate(R.layout.item_todo, parent, false)
                    : convertView;

            JournalStore.Todo t = itemsForDay.get(position);
            CheckBox cb = v.findViewById(R.id.checkTodo);
            TextView  tv = v.findViewById(R.id.txtTodo);

            // La CheckBox ne doit PAS prendre le focus/clic global
            cb.setFocusable(false);
            cb.setFocusableInTouchMode(false);

            tv.setText(t.title);
            cb.setChecked(t.done);
            cb.setEnabled(!readOnly);

            // Toggle uniquement si pas lecture seule
            cb.setOnClickListener(view -> {
                if (readOnly) { cb.setChecked(t.done); toast("Lecture seule"); return; }
                t.done = cb.isChecked();
                save();
            });

            return v;
        }
    }
}
