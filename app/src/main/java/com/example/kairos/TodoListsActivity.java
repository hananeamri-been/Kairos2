package com.example.kairos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TodoListsActivity extends BaseActivity {

    private TextView txtMonth;
    private ImageButton btnAddList;
    private ListView listTodoLists;

    private JournalStore store;
    private YearMonth currentMonth;
    private List<JournalStore.Todo> monthTodos = new ArrayList<>();
    private List<DateGroup> groups = new ArrayList<>();
    private TodoListAdapter adapter;

    private final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DateTimeFormatter MONTH_LABEL =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_lists);
        setupBottomBar();

        txtMonth = findViewById(R.id.txtMonth);
        btnAddList = findViewById(R.id.btnAddList);
        listTodoLists = findViewById(R.id.listTodoLists);

        store = new JournalStore(this);
        currentMonth = YearMonth.from(LocalDate.now());

        txtMonth.setText("To-Do — " + capitalize(currentMonth.format(MONTH_LABEL)));

        btnAddList.setOnClickListener(v -> {
            if (monthTodos == null) monthTodos = new ArrayList<>();
            if (groups == null) groups = new ArrayList<>();

            LocalDate today = LocalDate.now();
            if (!YearMonth.from(today).equals(currentMonth)) {
                toast("Vous n’êtes pas sur le mois courant");
                return;
            }

            String todayIso = today.format(ISO);

            // vérifie si une liste existe déjà pour aujourd’hui
            boolean exists = false;
            for (DateGroup g : groups) {
                if (g.date.equals(todayIso)) { exists = true; break; }
            }

            // 👉 NE CRÉE PLUS DE TÂCHE PAR DÉFAUT
            if (!exists) {
                store.saveTodos(currentMonth, monthTodos);
                regroup();
            }

            openEditor(todayIso); // ouvre directement l’écran vide pour ce jour
        });


        listTodoLists.setOnItemClickListener((p, v, pos, id) -> openEditor(groups.get(pos).date));

        listTodoLists.setOnItemLongClickListener((p, v, pos, id) -> {
            DateGroup g = groups.get(pos);
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Supprimer la liste ?")
                    .setMessage("Supprimer la liste du " + g.date + " et toutes ses tâches ?")
                    .setPositiveButton("Supprimer", (d, w) -> {
                        // on supprime toutes les tâches avec cette date
                        Iterator<JournalStore.Todo> it = monthTodos.iterator();
                        while (it.hasNext()) {
                            if (g.date.equals(it.next().date)) it.remove();
                        }
                        store.saveTodos(currentMonth, monthTodos);
                        regroup();
                        toast("Liste supprimée 🗑️");
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
            return true;
        });
    }

    @Override protected void onResume() {
        super.onResume();
        monthTodos = store.loadTodos(currentMonth);
        regroup();
    }

    private void regroup() {
        // regroupe par date
        Map<String,Integer> counts = new HashMap<>();
        for (JournalStore.Todo t : monthTodos) {
            if (t.date == null || t.date.isEmpty()) continue;
            counts.put(t.date, counts.getOrDefault(t.date, 0) + 1);
        }
        List<String> dates = new ArrayList<>(counts.keySet());
        Collections.sort(dates); // croissant
        Collections.reverse(dates); // plus récente d’abord

        groups.clear();
        for (String d : dates) groups.add(new DateGroup(d, counts.get(d)));

        if (adapter == null) {
            adapter = new TodoListAdapter(groups);
            listTodoLists.setAdapter(adapter);
        } else adapter.notifyDataSetChanged();
    }

    private void openEditor(String isoDate) {
        Intent i = new Intent(this, TodoEditorActivity.class);
        i.putExtra("EXTRA_YEAR_MONTH", currentMonth.toString());
        i.putExtra("EXTRA_DATE", isoDate);
        startActivity(i);
    }

    private JournalStore.Todo makeTodo(String isoDate, String title) {
        JournalStore.Todo t = new JournalStore.Todo();
        t.date = isoDate;
        t.title = title;
        t.done = false;
        return t;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    // ---------- structures & adapter ----------

    private static class DateGroup {
        final String date; // ISO yyyy-MM-dd
        final int count;
        DateGroup(String d, int c) { date=d; count=c; }
    }

    private class TodoListAdapter extends BaseAdapter {
        private final List<DateGroup> data;
        TodoListAdapter(List<DateGroup> d) { data=d; }
        @Override public int getCount() { return data.size(); }
        @Override public Object getItem(int position) { return data.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
            android.view.View v;
            if (convertView == null) {
                v = getLayoutInflater().inflate(R.layout.item_todo_list, parent, false);
            } else v = convertView;

            DateGroup g = data.get(position);
            TextView title = v.findViewById(R.id.todoListTitle);
            TextView sub   = v.findViewById(R.id.todoListSubtitle);
            title.setText(g.date);
            sub.setText(g.count + (g.count<=1 ? " task" : " tasks"));
            return v;
        }
    }
}
