package com.example.kairos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AddHabitDialog extends DialogFragment {

    public interface Listener {
        void onSaveHabit(Habit habit, boolean isEdit);
    }

    private Listener listener;
    private Habit toEdit;

    public static AddHabitDialog newInstance(@Nullable Habit edit) {
        AddHabitDialog d = new AddHabitDialog();
        d.toEdit = edit;
        return d;
    }

    @Override public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof Listener) listener = (Listener) context;
        else throw new IllegalStateException("Host must implement AddHabitDialog.Listener");
    }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View root = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_habit, null, false);

        EditText title = root.findViewById(R.id.inputTitle);
        CheckBox[] ch = new CheckBox[7];
        ch[0] = root.findViewById(R.id.ch0);
        ch[1] = root.findViewById(R.id.ch1);
        ch[2] = root.findViewById(R.id.ch2);
        ch[3] = root.findViewById(R.id.ch3);
        ch[4] = root.findViewById(R.id.ch4);
        ch[5] = root.findViewById(R.id.ch5);
        ch[6] = root.findViewById(R.id.ch6);

        if (toEdit != null) {
            title.setText(toEdit.title);
            for (int i = 0; i < 7; i++) ch[i].setChecked(toEdit.freq[i]);
        }

        ImageButton save = root.findViewById(R.id.btnSave);
        save.setOnClickListener(v -> {
            String t = title.getText() != null ? title.getText().toString().trim() : "";
            if (TextUtils.isEmpty(t)) {
                title.setError(getString(R.string.habit_title_required));
                title.requestFocus();
                Toast.makeText(requireContext(), R.string.habit_title_required, Toast.LENGTH_SHORT).show();
                return;
            }
            boolean[] f = new boolean[7];
            for (int i = 0; i < 7; i++) f[i] = ch[i].isChecked();

            Habit h = (toEdit == null) ? new Habit(t, f)
                    : new Habit(toEdit.id, t, f, toEdit.checked);

            if (listener != null) listener.onSaveHabit(h, toEdit != null);
            dismiss();
        });

        return new AlertDialog.Builder(requireContext())
                .setView(root)
                .create();
    }
}
