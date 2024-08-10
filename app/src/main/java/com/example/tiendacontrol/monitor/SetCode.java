package com.example.tiendacontrol.monitor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.example.tiendacontrol.R;
import com.example.tiendacontrol.dialogFragment.MenuDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SetCode extends AppCompatActivity {
    private FloatingActionButton fabMenu; // Botón flotante para mostrar el menú
    private EditText editTextCode; // Campo para ingresar el nuevo código de acceso
    private Button buttonSaveCode; // Botón para guardar el nuevo código de acceso
    private static final String PREFS_NAME = "CodePrefs"; // Nombre del archivo de preferencias para guardar el código
    private static final String CODE_KEY = "access_code"; // Clave para almacenar el código de acceso
    private static final String TAG = "SetCode"; // Etiqueta para los mensajes de log

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_code); // Establece el diseño de la actividad

        // Inicializa las vistas
        editTextCode = findViewById(R.id.editTextCode);
        buttonSaveCode = findViewById(R.id.buttonSaveCode);
        fabMenu = findViewById(R.id.fabMenu);

//        // Configura el botón flotante para mostrar el menú cuando se hace clic
//        fabMenu.setOnClickListener(view -> {
//            FragmentManager fragmentManager = getSupportFragmentManager();
//            MenuDialogFragment menuDialogFragment = MenuDialogFragment.newInstance();
//            menuDialogFragment.show(fragmentManager, "servicios_dialog");
//        });

        // Configura el botón "Guardar código" para guardar el nuevo código ingresado
        buttonSaveCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editTextCode.getText().toString().trim(); // Obtiene el código ingresado

                // Verifica si el campo de código no está vacío
                if (!code.isEmpty()) {
                    saveCode(code); // Guarda el código en las preferencias
                    Toast.makeText(SetCode.this, "Código guardado correctamente", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Código guardado correctamente: " + code); // Mensaje de log para depuración
                    finish(); // Cierra la actividad después de guardar el código
                } else {
                    Toast.makeText(SetCode.this, "El campo de código está vacío", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "El campo de código está vacío"); // Mensaje de log para depuración
                }
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
}