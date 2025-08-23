package com.creativem.tiendacontrol.pin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.creativem.tiendacontrol.R;
import com.creativem.tiendacontrol.monitor.BaseDatos;

public class InicioPin extends AppCompatActivity {
    private EditText editTextAccessCode;
    private static final String PREFS_NAME = "CodePrefs";
    private static final String CODE_KEY = "accesscode";
    private static final String TAG = "InicioPin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entra_pin);

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
            Toast.makeText(InicioPin.this, "Primero debes configurar el Pin de acceso personalizado", Toast.LENGTH_LONG).show();
        } else {
            if (validateCode(inputCode)) {
                Intent intent = new Intent(InicioPin.this, BaseDatos.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(InicioPin.this, "Pin incorrecto", Toast.LENGTH_SHORT).show();
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