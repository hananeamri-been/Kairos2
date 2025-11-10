package com.example.kairos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.VH> {

    public interface Callbacks {
        void onToggle(String habitId, int dayIndex, boolean checked);
        void onDelete(String habitId);
        void onEdit(Habit habit);
    }

    private List<Habit> data;
    private final Callbacks cb;
    private boolean locked = false;
    private int todayIndex = 0; // 0..6 (Dim..Sam)
    private boolean isCurrentWeek = false;

    public HabitAdapter(List<Habit> data, Callbacks cb) {
        this.data = data;
        this.cb = cb;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        notifyDataSetChanged();
    }

    public void setTodayIndex(int idx, boolean isCurrentWeek) {
        this.todayIndex = idx;
        this.isCurrentWeek = isCurrentWeek;
        notifyDataSetChanged();
    }

    public void updateData(List<Habit> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Habit it = data.get(position);
        h.title.setText(it.title);

        // 7 jours
        CheckBox[] cbs = h.cbs;
        ImageView[] offs = h.offs;
        for (int i = 0; i < 7; i++) {
            boolean planned = it.freq[i];
            if (planned) {
                cbs[i].setVisibility(View.VISIBLE);
                offs[i].setVisibility(View.GONE);
                cbs[i].setChecked(it.checked[i]);

                boolean enabled = !locked; // semaine verrouillée ?
                if (enabled && isCurrentWeek) {
                    // On ne coche que jours passés + aujourd'hui
                    enabled = (i <= todayIndex);
                } else if (enabled) {
                    // Semaine future => verrouillée aussi
                    enabled = false;
                }
                cbs[i].setEnabled(enabled);

                final int day = i;
                cbs[i].setOnClickListener(v -> {
                    boolean nv = ((CheckBox) v).isChecked();
                    it.checked[day] = nv;
                    if (cb != null) cb.onToggle(it.id, day, nv);
                });

            } else {
                cbs[i].setVisibility(View.GONE);
                offs[i].setVisibility(View.VISIBLE);
            }
        }

        // Actions
        h.btnDelete.setVisibility(locked ? View.GONE : View.VISIBLE);
        h.btnEdit.setVisibility(locked ? View.GONE : View.VISIBLE);

        h.btnDelete.setOnClickListener(v -> { if (cb != null) cb.onDelete(it.id); });
        h.btnEdit.setOnClickListener(v -> { if (cb != null) cb.onEdit(it); });
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        CheckBox[] cbs = new CheckBox[7];
        ImageView[] offs = new ImageView[7];
        ImageButton btnDelete, btnEdit;

        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.txtTitle);
            cbs[0] = v.findViewById(R.id.cb0);
            cbs[1] = v.findViewById(R.id.cb1);
            cbs[2] = v.findViewById(R.id.cb2);
            cbs[3] = v.findViewById(R.id.cb3);
            cbs[4] = v.findViewById(R.id.cb4);
            cbs[5] = v.findViewById(R.id.cb5);
            cbs[6] = v.findViewById(R.id.cb6);

            offs[0] = v.findViewById(R.id.off0);
            offs[1] = v.findViewById(R.id.off1);
            offs[2] = v.findViewById(R.id.off2);
            offs[3] = v.findViewById(R.id.off3);
            offs[4] = v.findViewById(R.id.off4);
            offs[5] = v.findViewById(R.id.off5);
            offs[6] = v.findViewById(R.id.off6);

            btnDelete = v.findViewById(R.id.btnDelete);
            btnEdit = v.findViewById(R.id.btnEdit);
        }
    }
}
