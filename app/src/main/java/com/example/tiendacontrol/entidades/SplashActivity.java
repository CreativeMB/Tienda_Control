package com.example.tiendacontrol.entidades;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tiendacontrol.MainActivity;
import com.example.tiendacontrol.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000; // 2 segundos

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Este método se ejecutará cuando se complete el temporizador
                // Puedes iniciar la siguiente actividad aquí
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Cierra la actividad del splash screen para prevenir que el usuario regrese a ella presionando "Atrás"
            }
        }, SPLASH_TIME_OUT);
    }
}
