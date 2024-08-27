package com.example.tiendacontrol.monitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tiendacontrol.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Pin extends AppCompatActivity {
    private EditText editTextCode; // Campo para ingresar el nuevo código de acceso
    private Button buttonSaveCode; // Botón para guardar el nuevo código de acceso
    private static final String PREFS_NAME = "CodePrefs"; // Nombre del archivo de preferencias para guardar el código
    private static final String CODE_KEY = "accesscode"; // Clave para almacenar el código de acceso
    private static final String TAG = "SetCode"; // Etiqueta para los mensajes de log

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.pin);

        // Verifica si ya existe un PIN guardado
        if (isCodeSaved()) {
            Toast.makeText(Pin.this, "Ya existe un código guardado", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Ya existe un código guardado, no se puede crear otro."); // Mensaje de log para depuración
            finish(); // Cierra la actividad si ya hay un código guardado
            return; // Termina onCreate para que no continúe con la inicialización
        }

        // Inicializa las vistas
        editTextCode = findViewById(R.id.editTextCode);
        TextView guardaPin = findViewById(R.id.GuardaPin);


        // Configura el botón "Guardar código" para guardar el nuevo código ingresado
        guardaPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editTextCode.getText().toString().trim(); // Obtiene el código ingresado

                // Verifica si el campo de código no está vacío
                if (!code.isEmpty()) {
                    saveCode(code); // Guarda el código en las preferencias
                    Toast.makeText(Pin.this, "Código guardado correctamente", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Código guardado correctamente: " + code); // Mensaje de log para depuración
                    finish(); // Cierra la actividad después de guardar el código
                } else {
                    Toast.makeText(Pin.this, "El campo de código está vacío", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "El campo de código está vacío"); // Mensaje de log para depuración
                }
            }
        });
    }

    // Método para verificar si ya existe un código guardado
    private boolean isCodeSaved() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.contains(CODE_KEY); // Retorna true si el código ya está guardado
    }

    // Método para guardar el código en las preferencias compartidas
    private void saveCode(String code) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CODE_KEY, code); // Guarda el código en las preferencias
        editor.apply(); // Aplica los cambios
    }
}