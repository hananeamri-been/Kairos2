package com.example.kairos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/** Adapter avec clics gérés via callbacks. Affiche seulement titre + date. */
public class NoteListAdapter extends BaseAdapter {

    public interface OnNoteClick { void onClick(JournalStore.Note note); }
    public interface OnNoteLongClick { void onLongClick(JournalStore.Note note); }

    private final Context ctx;
    private final LayoutInflater inflater;
    private List<JournalStore.Note> notes;
    private final OnNoteClick onClick;
    private final OnNoteLongClick onLong;

    public NoteListAdapter(Context ctx,
                           List<JournalStore.Note> notes,
                           OnNoteClick onClick,
                           OnNoteLongClick onLong) {
        this.ctx = ctx;
        this.inflater = LayoutInflater.from(ctx);
        this.notes = notes;
        this.onClick = onClick;
        this.onLong = onLong;
    }

    public void setData(List<JournalStore.Note> newData) {
        this.notes = newData;
        notifyDataSetChanged();
    }

    @Override public int getCount() { return notes.size(); }
    @Override public Object getItem(int position) { return notes.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VH h;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_note, parent, false);
            h = new VH();
            h.title = convertView.findViewById(R.id.noteTitle);
            h.date  = convertView.findViewById(R.id.noteDate);
            convertView.setTag(h);
        } else {
            h = (VH) convertView.getTag();
        }

        final JournalStore.Note n = notes.get(position);

        String title = (n.title == null || n.title.trim().isEmpty()) ? "Untitled" : n.title;
        h.title.setText(n.locked ? "🔒 " + title : title);
        h.date.setText(n.date != null ? n.date : "");

        convertView.setOnClickListener(v -> { if (onClick != null) onClick.onClick(n); });
        convertView.setOnLongClickListener(v -> {
            if (onLong != null) onLong.onLongClick(n);
            return true;
        });

        return convertView;
    }

    static class VH { TextView title, date; }
}
