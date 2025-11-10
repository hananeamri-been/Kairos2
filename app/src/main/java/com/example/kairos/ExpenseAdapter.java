package com.example.kairos;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.VH> {

    public interface OnChanged { void onChanged(); }

    private final Context ctx;
    private final ArrayList<ExpenseItem> data;
    private final OnChanged changed;

    public ExpenseAdapter(Context ctx, ArrayList<ExpenseItem> data, OnChanged changed) {
        this.ctx = ctx;
        this.data = data;
        this.changed = changed;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_expense, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        ExpenseItem it = data.get(i);
        h.bind(it, changed, () -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                data.remove(pos);
                notifyItemRemoved(pos);
                if (changed != null) changed.onChanged();
            }
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        EditText etName, etPrice;
        ImageButton btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            etName    = itemView.findViewById(R.id.etName);
            etPrice   = itemView.findViewById(R.id.etPrice);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(ExpenseItem it, OnChanged changed, Runnable onDelete) {
            etName.setText(it.name != null ? it.name : "");
            etPrice.setText(it.price != null && it.price != 0 ? String.valueOf(it.price) : "");

            TextWatcher w = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    it.name = etName.getText().toString().trim();
                    try { it.price = Double.parseDouble(etPrice.getText().toString().trim().replace(',', '.')); }
                    catch (Exception e) { it.price = 0.0; }
                    if (changed != null) changed.onChanged();
                }
            };
            etName.addTextChangedListener(w);
            etPrice.addTextChangedListener(w);

            btnDelete.setOnClickListener(v -> onDelete.run());
        }
    }
}
