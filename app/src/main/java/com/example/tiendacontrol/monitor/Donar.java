package com.example.tiendacontrol.monitor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tiendacontrol.R;

public class Donar extends AppCompatActivity {

    private ImageView nequiIcon;
    private ImageView daviplataIcon;
    private ImageView creditCardIcon;
    private ImageView paypalIcon;
    private TextView paypalLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.donar);

        // Inicialización de vistas
        nequiIcon = findViewById(R.id.nequi);
        daviplataIcon = findViewById(R.id.daviplata);
        creditCardIcon = findViewById(R.id.tarjeta);
        paypalIcon = findViewById(R.id.paypal);
        paypalLink = findViewById(R.id.paypallink);
        ImageView iconDatabase = findViewById(R.id.database);

        // Configuración de eventos
        setupClickListeners();

        iconDatabase.setOnClickListener(view -> {
            Intent databaseIntent = new Intent(this, Database.class);
            startActivity(databaseIntent);
        });
    }

    private void setupClickListeners() {
        // Configurar clic en el icono de Nequi
        nequiIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage("Donar con Nequi: 3014418502");
            }
        });

        // Configurar clic en el icono de Daviplata
        daviplataIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage("Donar con Daviplata: 3014418502");
            }
        });

        // Configurar clic en el icono de Tarjeta de Crédito
        creditCardIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCreditCardLink();
            }
        });

        // Configurar clic en el icono de PayPal
        paypalIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPaypalLink();
            }
        });

        // Configurar clic en el enlace de PayPal
        paypalLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPaypalLink();
            }
        });
    }

    private void openPaypalLink() {
        // Enlace de PayPal
        String url = "https://www.paypal.com/donate/?hosted_button_id=ST45F8J2AWU74";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void openCreditCardLink() {
        // Enlace para donaciones con tarjeta de crédito
        String url = "https://checkout.wompi.co/l/iFDSob"; // Sustituye con el enlace real
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void showMessage(String message) {
        // Método para mostrar un mensaje al usuario, puedes usar Toast o un Snackbar
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}