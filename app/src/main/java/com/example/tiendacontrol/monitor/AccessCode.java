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
import com.example.tiendacontrol.login.Login;

public class AccessCode extends AppCompatActivity {

    private EditText editTextAccessCode;
    private Button buttonAccess;
    private static final String PREFS_NAME = "CodePrefs";
    private static final String CODE_KEY = "access_code";
Button btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.access_code);

        editTextAccessCode = findViewById(R.id.editTextAccessCode);
        buttonAccess = findViewById(R.id.buttonAccess);
        btnLogin = findViewById(R.id.btnLogin);

        // Set up "Login" button listener to redirect the user to the login screen
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccessCode.this, Login.class);
                startActivity(intent);
                finish(); // Close this activity so the user cannot navigate back
            }
        });

        buttonAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputCode = editTextAccessCode.getText().toString().trim();
                if (validateCode(inputCode)) {
                    Intent intent = new Intent(AccessCode.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Cierra la actividad después de acceder al MainActivity
                } else {
                    Toast.makeText(AccessCode.this, "Código incorrecto", Toast.LENGTH_SHORT).show();
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

