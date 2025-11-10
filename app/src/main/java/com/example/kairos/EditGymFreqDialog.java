package com.example.kairos;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class EditGymFreqDialog extends Dialog {
    private final List<Boolean> freq;
    private final OnSaveListener listener;
    private final List<CheckBox> boxes = new ArrayList<>();

    public interface OnSaveListener { void onSave(List<Boolean> freq); }

    public EditGymFreqDialog(Context ctx, List<Boolean> freq, OnSaveListener listener) {
        super(ctx);
        this.freq = new ArrayList<>(freq);
        this.listener = listener;
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (getWindow() != null) {
            getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_gym_freq);

        for (int i = 0; i < 7; i++) {
            int id = getContext().getResources().getIdentifier("ch" + i, "id", getContext().getPackageName());
            CheckBox cb = findViewById(id);
            cb.setChecked(freq.get(i));
            boxes.add(cb);
        }

        ImageButton save = findViewById(R.id.btnSaveGym);
        save.setOnClickListener(v -> {
            for (int i = 0; i < 7; i++) freq.set(i, boxes.get(i).isChecked());
            listener.onSave(freq);
            dismiss();
        });
    }
}
