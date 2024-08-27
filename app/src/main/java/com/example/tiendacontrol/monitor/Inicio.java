package com.example.tiendacontrol.monitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tiendacontrol.R;

public class Inicio extends AppCompatActivity {
    private Patron patron;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inicio);

        TextView enlaceConfiguracionPin = findViewById(R.id.pin);
        TextView enlaceConfiguracionPatron = findViewById(R.id.patron);
        TextView EntraPin = findViewById(R.id.entrar);

        // Inicializa la clase Patron
        patron = new Patron(this);

        // Configurar el botón para iniciar la autenticación con patrón
        enlaceConfiguracionPatron.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                patron.iniciarAutenticacion();
            }
        });

        enlaceConfiguracionPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reemplaza con la actividad de configuración por PIN
                Intent intent = new Intent(Inicio.this, Pin.class);
                startActivity(intent);
            }
        });

        EntraPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reemplaza con la actividad de configuración por PIN
                Intent intent = new Intent(Inicio.this, EntraPin.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            patron.onActivityResult(requestCode, resultCode, data);
        }
    }
}