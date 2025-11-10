package com.example.kairos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

public class ImportPlaceActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String shared = null;
        Intent i = getIntent();
        if (i != null && "android.intent.action.SEND".equals(i.getAction())) {
            CharSequence cs = i.getCharSequenceExtra(Intent.EXTRA_TEXT);
            if (cs != null) shared = cs.toString();
        }
        String name = null, addr = null;
        if (!TextUtils.isEmpty(shared)) {
            // Parsing simple : 1ère ligne = nom, lignes suivantes jusqu’au 1er URL = adresse
            String[] lines = shared.split("\n");
            if (lines.length > 0) {
                name = lines[0].trim();
                StringBuilder a = new StringBuilder();
                for (int k = 1; k < lines.length; k++) {
                    String L = lines[k].trim();
                    if (L.startsWith("http://") || L.startsWith("https://")) break;
                    if (a.length() > 0) a.append(", ");
                    a.append(L);
                }
                addr = a.length() > 0 ? a.toString() : null;
            }
        }
        PlaceImportStore.saveImported(this, name, addr);
        finish();
    }
}
