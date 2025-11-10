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

public class AddHobbyDialog extends DialogFragment {

    public interface Listener {
        void onSaveHobby(Hobby hobby, boolean isEdit);
    }

    private Listener listener;
    private Hobby toEdit;

    public static AddHobbyDialog newInstance(@Nullable Hobby edit) {
        AddHobbyDialog d = new AddHobbyDialog();
        d.toEdit = edit;
        return d;
    }

    @Override public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof Listener) listener = (Listener) context;
        else throw new IllegalStateException("Host must implement AddHobbyDialog.Listener");
    }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View root = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_hobby, null, false);

        EditText title = root.findViewById(R.id.inputHobbyTitle);
        CheckBox[] ch = new CheckBox[7];
        ch[0] = root.findViewById(R.id.chHobby0);
        ch[1] = root.findViewById(R.id.chHobby1);
        ch[2] = root.findViewById(R.id.chHobby2);
        ch[3] = root.findViewById(R.id.chHobby3);
        ch[4] = root.findViewById(R.id.chHobby4);
        ch[5] = root.findViewById(R.id.chHobby5);
        ch[6] = root.findViewById(R.id.chHobby6);

        if (toEdit != null) {
            title.setText(toEdit.title);
            for (int i = 0; i < 7; i++) ch[i].setChecked(toEdit.freq[i]);
        }

        ImageButton save = root.findViewById(R.id.btnSaveHobby);
        save.setOnClickListener(v -> {
            String t = title.getText().toString().trim();
            if (TextUtils.isEmpty(t)) {
                Toast.makeText(requireContext(), R.string.habit_title_required, Toast.LENGTH_SHORT).show();
                return;
            }
            boolean[] f = new boolean[7];
            for (int i = 0; i < 7; i++) f[i] = ch[i].isChecked();

            Hobby h = (toEdit == null) ? new Hobby(t, f)
                    : new Hobby(toEdit.id, t, f, toEdit.checked);

            if (listener != null) listener.onSaveHobby(h, toEdit != null);
            dismiss();
        });

        return new AlertDialog.Builder(requireContext())
                .setView(root)
                .create();
    }
}
