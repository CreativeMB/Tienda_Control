package com.example.tiendacontrol.monitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tiendacontrol.R;

public class EntraPin extends AppCompatActivity {
    private EditText editTextAccessCode;
    private static final String PREFS_NAME = "CodePrefs";
    private static final String CODE_KEY = "accesscode";
    private static final String TAG = "EntraPin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrapin);

        // Inicializa las vistas
        editTextAccessCode = findViewById(R.id.AccesoPin);
        TextView entrarPin = findViewById(R.id.entrar);

        // Configura el TextView "Entrar" para validar el código de acceso
        entrarPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Botón 'Entrar' presionado");
                validarCodigo();
            }
        });
    }

    private void validarCodigo() {
        String inputCode = editTextAccessCode.getText().toString().trim();
        Log.d(TAG, "Pin ingresado: " + inputCode);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedCode = sharedPreferences.getString(CODE_KEY, "");
        Log.d(TAG, "Pin guardado en preferencias: " + savedCode);

        if (savedCode.isEmpty()) {
            Toast.makeText(EntraPin.this, "Primero debes configurar el Pin de acceso personalizado", Toast.LENGTH_LONG).show();
        } else {
            if (validateCode(inputCode)) {
                Intent intent = new Intent(EntraPin.this, Database.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(EntraPin.this, "Pin incorrecto", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateCode(String inputCode) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedCode = sharedPreferences.getString(CODE_KEY, "");
        Log.d(TAG, "Pin ingresado para validación: " + inputCode);
        Log.d(TAG, "Pin guardado para validación: " + savedCode);
        return inputCode.equals(savedCode);
    }
}