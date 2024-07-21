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
    private Button btnLogin;
    private static final String PREFS_NAME = "CodePrefs";
    private static final String CODE_KEY = "access_code";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.access_code);

        editTextAccessCode = findViewById(R.id.editTextAccessCode);
        buttonAccess = findViewById(R.id.buttonAccess);
        btnLogin = findViewById(R.id.btnLogin);

        // Redirigir a la pantalla de inicio de sesión
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccessCode.this, Login.class);
                startActivity(intent);
                finish(); // Cierra esta actividad para que el usuario no pueda navegar de regreso
            }
        });

        // Validar el código de acceso al hacer clic en el botón "Acceder"
        buttonAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputCode = editTextAccessCode.getText().toString().trim();
                // Verificar si hay un código guardado
                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String savedCode = sharedPreferences.getString(CODE_KEY, "");

                // Si no hay código configurado, mostrar un mensaje y no permitir el acceso
                if (savedCode.isEmpty()) {
                    Toast.makeText(AccessCode.this, "Primero debes Inicia sesión para configurar el código acceso personalizado ", Toast.LENGTH_LONG).show();
                } else {
                    // Si hay un código configurado, validar el código ingresado
                    if (validateCode(inputCode)) {
                        Intent intent = new Intent(AccessCode.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Cierra la actividad después de acceder al MainActivity
                    } else {
                        Toast.makeText(AccessCode.this, "Código incorrecto", Toast.LENGTH_SHORT).show();
                    }
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