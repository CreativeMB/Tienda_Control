package com.example.tiendacontrol.model;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tiendacontrol.R;
import com.example.tiendacontrol.monitor.Database;
import com.example.tiendacontrol.monitor.Inicio;

public class SplashActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "CodePrefs";
    private static final String CODE_KEY = "accesscode";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        // Iniciar la animación en una vista específica
        View vistaAnimada = findViewById(R.id.vista_animada);
        Animation animacion = AnimationUtils.loadAnimation(this, R.anim.animacion);
        vistaAnimada.startAnimation(animacion);

        // Asignar un Listener a la animación
        animacion.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // La animación ha comenzado
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Redirigir según si el usuario tiene un PIN guardado o no
                redirectToNextActivity();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // La animación se ha repetido
            }
        });
    }

    private void redirectToNextActivity() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedCode = sharedPreferences.getString(CODE_KEY, "");

        Intent intent;
        if (savedCode.isEmpty()) {
            // No hay PIN guardado, redirigir a Database
            intent = new Intent(SplashActivity.this, Database.class);
        } else {
            // Hay un PIN guardado, redirigir a Inicio
            intent = new Intent(SplashActivity.this, Inicio.class);
        }
        startActivity(intent);
        finish();
    }
}