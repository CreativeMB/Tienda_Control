package com.example.tiendacontrol.monitor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tiendacontrol.R;

public class SetCodeActivity extends AppCompatActivity {

    private EditText editTextCode;
    private Button buttonSaveCode;
    private static final String PREFS_NAME = "CodePrefs";
    private static final String CODE_KEY = "access_code";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_code);

        editTextCode = findViewById(R.id.editTextCode);
        buttonSaveCode = findViewById(R.id.buttonSaveCode);

        buttonSaveCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editTextCode.getText().toString().trim();
                if (!code.isEmpty()) {
                    saveCode(code);
                    finish(); // Cierra la actividad después de guardar el código
                }
            }
        });
    }

    private void saveCode(String code) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CODE_KEY, code);
        editor.apply();
    }
}

