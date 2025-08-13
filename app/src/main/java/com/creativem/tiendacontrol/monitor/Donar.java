package com.creativem.tiendacontrol.monitor;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.creativem.tiendacontrol.R;

public class Donar extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.donar);

        TextView keyTextView = findViewById(R.id.key);
        keyTextView.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("llave bancaria", keyTextView.getText());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(Donar.this, "¡Llave copiada al portapapeles!", Toast.LENGTH_SHORT).show();
        });
    }
}
