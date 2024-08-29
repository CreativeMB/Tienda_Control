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
import androidx.appcompat.app.AppCompatActivity;

import com.example.tiendacontrol.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SetCode extends AppCompatActivity {
    private EditText editTextCode; // Campo para ingresar el nuevo código de acceso
    private TextView textViewDeleteCode; // TextView para eliminar el código de acceso
    private static final String PREFS_NAME = "CodePrefs"; // Nombre del archivo de preferencias para guardar el código
    private static final String CODE_KEY = "accesscode"; // Clave para almacenar el código de acceso
    private static final String TAG = "SetCode"; // Etiqueta para los mensajes de log

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setcode); // Establece el diseño de la actividad

        // Inicializa las vistas
        editTextCode = findViewById(R.id.editTextCode);
        textViewDeleteCode = findViewById(R.id.textViewDeleteCode);
        TextView guardaPin = findViewById(R.id.GuardaPin);

        // Configura el botón "Guardar código" para guardar el nuevo código ingresado
        guardaPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editTextCode.getText().toString().trim(); // Obtiene el código ingresado

                // Verifica si el campo de código no está vacío
                if (!code.isEmpty()) {
                    saveCode(code); // Guarda el código en las preferencias
                    Toast.makeText(SetCode.this, "Código guardado correctamente", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Código guardado correctamente: " + code); // Mensaje de log para depuración

                    // Redirige a la actividad Ainicio después de guardar el código
                    Intent intent = new Intent(SetCode.this, Inicio.class);
                    startActivity(intent);
                    finish(); // Cierra la actividad actual
                } else {
                    Toast.makeText(SetCode.this, "El campo de código está vacío", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "El campo de código está vacío"); // Mensaje de log para depuración
                }
            }
        });

        // Configura el TextView "Eliminar PIN" para eliminar el código de acceso
        textViewDeleteCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCode(); // Llama al método para eliminar el código
                Toast.makeText(SetCode.this, "Código eliminado", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Código eliminado"); // Mensaje de log para depuración

                // Redirige a la actividad Database después de eliminar el código
                Intent intent = new Intent(SetCode.this, Database.class);
                startActivity(intent);
                finish(); // Cierra la actividad actual
            }
        });
    }

    // Método para guardar el código en las preferencias compartidas
    private void saveCode(String code) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CODE_KEY, code); // Guarda el código en las preferencias
        editor.apply(); // Aplica los cambios
    }

    // Método para eliminar el código de las preferencias compartidas
    private void deleteCode() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(CODE_KEY); // Elimina el código de las preferencias
        editor.apply(); // Aplica los cambios
    }
}