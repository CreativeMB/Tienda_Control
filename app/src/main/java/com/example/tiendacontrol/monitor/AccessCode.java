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
    private EditText editTextAccessCode; // Campo para ingresar el código de acceso
    private Button buttonAccess; // Botón para validar el código de acceso
    private Button btnLogin; // Botón para redirigir a la pantalla de inicio de sesión
    private static final String PREFS_NAME = "CodePrefs"; // Nombre del archivo de preferencias
    private static final String CODE_KEY = "accesscode"; // Clave para almacenar el código de acceso

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accesscode); // Establece el diseño de la actividad

        // Inicializa las vistas
        editTextAccessCode = findViewById(R.id.editTextAccessCode);
        buttonAccess = findViewById(R.id.buttonAccess);
        btnLogin = findViewById(R.id.btnLogin);

        // Configura el botón "Login" para redirigir al usuario a la pantalla de inicio de sesión
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccessCode.this, Login.class);
                startActivity(intent); // Inicia la actividad de inicio de sesión
                finish(); // Cierra esta actividad para que el usuario no pueda navegar de regreso
            }
        });

        // Configura el botón "Acceder" para validar el código de acceso
        buttonAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputCode = editTextAccessCode.getText().toString().trim(); // Obtiene el código ingresado

                // Obtiene el código guardado en las preferencias
                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String savedCode = sharedPreferences.getString(CODE_KEY, "");

                // Si no hay código configurado, muestra un mensaje de error
                if (savedCode.isEmpty()) {
                    Toast.makeText(AccessCode.this, "Primero debes Iniciar sesión para configurar el código de acceso personalizado", Toast.LENGTH_LONG).show();
                } else {
                    // Si hay un código configurado, valida el código ingresado
                    if (validateCode(inputCode)) {
                        Intent intent = new Intent(AccessCode.this, Database.class);
                        startActivity(intent); // Inicia la actividad principal
                        finish(); // Cierra la actividad después de acceder al MainActivity
                    } else {
                        Toast.makeText(AccessCode.this, "Código incorrecto", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    // Método para validar el código ingresado comparándolo con el código guardado
    private boolean validateCode(String inputCode) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedCode = sharedPreferences.getString(CODE_KEY, "");
        return inputCode.equals(savedCode); // Retorna verdadero si el código ingresado coincide con el guardado
    }
}