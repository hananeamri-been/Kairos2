package com.example.kairos;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

public class BookSummaryDialog extends Dialog {

    public interface Callback { void onSaved(String text); }

    private final String initial;
    private final Callback callback;

    public BookSummaryDialog(@NonNull Context ctx, String initialText, Callback cb) {
        super(ctx);
        this.initial = initialText == null ? "" : initialText;
        this.callback = cb;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_book_summary);

        Window window = getWindow();
        if (window != null) {
            DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
            int width = WindowManager.LayoutParams.MATCH_PARENT;
            int height = (int) (dm.heightPixels * 0.7f);
            window.setLayout(width, height);
        }

        EditText input = findViewById(R.id.inputSummary);
        input.setText(initial);

        Button cancel = findViewById(R.id.btnCancel);
        Button save   = findViewById(R.id.btnSave);

        cancel.setOnClickListener(v -> dismiss());
        save.setOnClickListener(v -> {
            if (callback != null) callback.onSaved(input.getText().toString());
            dismiss();
        });
    }
}


