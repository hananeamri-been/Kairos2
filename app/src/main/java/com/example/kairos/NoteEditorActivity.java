package com.example.kairos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class NoteEditorActivity extends BaseActivity {

    private EditText inputTitle, inputContent;
    private ImageButton btnSave;
    private YearMonth ym;
    private JournalStore store;
    private List<JournalStore.Note> notes;
    private JournalStore.Note current;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);
        setupBottomBar();

        inputTitle = findViewById(R.id.inputTitle);
        inputContent = findViewById(R.id.inputContent);
        btnSave = findViewById(R.id.btnSaveNote);

        store = new JournalStore(this);
        String ymStr = getIntent().getStringExtra("EXTRA_YEAR_MONTH");
        ym = (ymStr != null) ? YearMonth.parse(ymStr) : YearMonth.from(LocalDate.now());
        notes = store.loadNotes(ym);

        String noteId = getIntent().getStringExtra("EXTRA_NOTE_ID");
        current = null;
        if (noteId != null) {
            for (JournalStore.Note n : notes) {
                if (n.id.equals(noteId)) { current = n; break; }
            }
        }
        if (current == null) {
            Toast.makeText(this, "Note introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        inputTitle.setText(current.title);
        inputContent.setText(current.content);

        btnSave.setOnClickListener(v -> saveAndFinish());
    }

    private void saveAndFinish() {
        current.title = inputTitle.getText().toString().trim();
        current.content = inputContent.getText().toString().trim();
        store.saveNotes(ym, notes);
        finish();
    }
}
