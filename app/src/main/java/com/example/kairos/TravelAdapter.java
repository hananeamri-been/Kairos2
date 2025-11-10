package com.example.kairos;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TravelAdapter extends RecyclerView.Adapter<TravelAdapter.VH> {

    public interface Listener {
        void onOpen(Travel t);
        void onDelete(Travel t);
    }

    private final Context ctx;
    private final ArrayList<Travel> data = new ArrayList<>();
    private final Listener listener;
    private final CountryCityRepository repo;
    private final NumberFormat money = NumberFormat.getCurrencyInstance(Locale.getDefault());

    public TravelAdapter(Context ctx, Listener listener) {
        this.ctx = ctx;
        this.listener = listener;
        this.repo = new CountryCityRepository(ctx);
    }

    public void submit(ArrayList<Travel> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_travel, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        Travel t = data.get(i);
        String countryName = repo.nameForCode(t.countryCode);
        String flag = FlagUtils.countryCodeToFlag(t.countryCode);

        h.tvFlag.setText(flag);
        h.tvTitle.setText(countryName + " — " + t.city);
        h.tvTotal.setText(money.format(t.totalSpent()));

        double remaining = t.remainingBudget();
        h.tvRemaining.setText(money.format(remaining));
        h.tvRemaining.setTextColor(remaining < 0 ? Color.RED : Color.BLACK);

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onOpen(t); });
        h.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(t); });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvFlag, tvTitle, tvTotal, tvRemaining;
        ImageButton btnDelete;
        VH(@NonNull View item) {
            super(item);
            tvFlag = item.findViewById(R.id.tvFlag);
            tvTitle = item.findViewById(R.id.tvTitle);
            tvTotal = item.findViewById(R.id.tvTotal);
            tvRemaining = item.findViewById(R.id.tvRemaining);
            btnDelete = item.findViewById(R.id.btnDelete);
        }
    }
}
