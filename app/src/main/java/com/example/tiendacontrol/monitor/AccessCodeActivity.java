package com.example.tiendacontrol.monitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tiendacontrol.R;

public class AccessCodeActivity extends AppCompatActivity {

    private EditText editTextAccessCode;
    private Button buttonAccess;
    private static final String PREFS_NAME = "CodePrefs";
    private static final String CODE_KEY = "access_code";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_code);

        editTextAccessCode = findViewById(R.id.editTextAccessCode);
        buttonAccess = findViewById(R.id.buttonAccess);

        buttonAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputCode = editTextAccessCode.getText().toString().trim();
                if (validateCode(inputCode)) {
                    Intent intent = new Intent(AccessCodeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Cierra la actividad después de acceder al MainActivity
                } else {
                    Toast.makeText(AccessCodeActivity.this, "Código incorrecto", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateCode(String inputCode) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedCode = sharedPreferences.getString(CODE_KEY, "");
        return inputCode.equals(savedCode);
    }
}

