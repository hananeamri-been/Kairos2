package com.example.kairos;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class NotesActivity extends BaseActivity {

    private ListView listNotes;
    private ImageButton btnAddNote;
    private JournalStore store;
    private YearMonth currentMonth;
    private List<JournalStore.Note> notes;
    private NoteListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        setupBottomBar();

        listNotes = findViewById(R.id.listNotes);
        btnAddNote = findViewById(R.id.btnAddNote);
        store = new JournalStore(this);

        String ymStr = getIntent().getStringExtra("EXTRA_YEAR_MONTH");
        currentMonth = (ymStr != null) ? YearMonth.parse(ymStr) : YearMonth.from(LocalDate.now());

        btnAddNote.setOnClickListener(v -> {
            // Créer d’abord pour avoir un id; sauvegarder; ouvrir
            JournalStore.Note n = new JournalStore.Note();
            n.date = LocalDate.now().toString();
            notes.add(0, n);
            saveNotes();
            openEditor(n);
        });
    }

    @Override protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        notes = store.loadNotes(currentMonth);

        if (adapter == null) {
            adapter = new NoteListAdapter(
                    this,
                    notes,
                    // onClick
                    note -> {
                        if (note.locked && note.pin != null) {
                            askPin("Entrer le PIN de cette note", pin -> {
                                if (pin != null && pin.equals(note.pin)) openEditor(note);
                                else Toast.makeText(this, "PIN incorrect ❌", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            openEditor(note);
                        }
                    },
                    // onLong
                    note -> {
                        String[] opts = note.locked
                                ? new String[]{"Déverrouiller", "Supprimer"}
                                : new String[]{"Verrouiller", "Supprimer"};
                        new android.app.AlertDialog.Builder(this)
                                .setTitle("Actions sur la note")
                                .setItems(opts, (d, which) -> {
                                    if (which == 0) { // lock/unlock
                                        if (note.locked) {
                                            askPin("Entrer le PIN", pin -> {
                                                if (pin != null && pin.equals(note.pin)) {
                                                    note.locked = false;
                                                    saveNotes();
                                                    Toast.makeText(this, "Note déverrouillée 🔓", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(this, "PIN incorrect ❌", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            askPin("Créer un PIN (4+ chiffres)", pin -> {
                                                if (pin != null && !pin.trim().isEmpty()) {
                                                    note.pin = pin.trim();
                                                    note.locked = true;
                                                    saveNotes();
                                                    Toast.makeText(this, "Note verrouillée 🔒", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    } else { // delete
                                        new android.app.AlertDialog.Builder(this)
                                                .setTitle("Supprimer la note")
                                                .setMessage("Voulez-vous vraiment supprimer cette note ?")
                                                .setPositiveButton("Supprimer", (dd, ww) -> {
                                                    notes.remove(note);
                                                    saveNotes();
                                                    Toast.makeText(this, "Note supprimée 🗑️", Toast.LENGTH_SHORT).show();
                                                })
                                                .setNegativeButton("Annuler", null)
                                                .show();
                                    }
                                })
                                .show();
                    }
            );
            listNotes.setAdapter(adapter);
        } else {
            adapter.setData(notes);
        }
    }

    private void saveNotes() {
        store.saveNotes(currentMonth, notes);
        if (adapter != null) adapter.setData(notes);
    }

    private void openEditor(JournalStore.Note note) {
        Intent i = new Intent(this, NoteEditorActivity.class);
        i.putExtra("EXTRA_YEAR_MONTH", currentMonth.toString());
        i.putExtra("EXTRA_NOTE_ID", note.id);
        startActivity(i);
    }

    // ------- PIN helper -------
    private interface PinCallback { void onPin(String pin); }
    private void askPin(String title, PinCallback cb) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint("4 chiffres minimum");
        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setView(input)
                .setPositiveButton("OK", (d, w) -> cb.onPin(input.getText().toString()))
                .setNegativeButton("Annuler", (d, w) -> cb.onPin(null))
                .show();
    }

    public JournalStore getStore() { return store; }
    public YearMonth getCurrentMonth() { return currentMonth; }
}
