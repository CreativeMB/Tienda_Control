package com.creativem.tiendacontrol.monitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.creativem.tiendacontrol.R;
import com.creativem.tiendacontrol.pin.InicioPin;
import com.creativem.tiendacontrol.pin.Patron;

public class Inicio extends AppCompatActivity {
    private Patron patron;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inicio);

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

        EntraPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reemplaza con la actividad de configuración por PIN
                Intent intent = new Intent(Inicio.this, InicioPin.class);
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