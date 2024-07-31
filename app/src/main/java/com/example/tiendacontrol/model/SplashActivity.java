package com.example.tiendacontrol.model;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tiendacontrol.R;
import com.example.tiendacontrol.login.Login;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);


        // Cargar la animación desde un archivo XML
        Animation animacion = AnimationUtils.loadAnimation(this, R.anim.animacion);

        // Asignar un Listener a la animación
        animacion.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // La animación ha comenzado
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // La animación ha terminado, iniciar la actividad deseada
                Intent intent = new Intent(SplashActivity.this, Login.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // La animación se ha repetido
            }
        });

        // Iniciar la animación en una vista específica
        View vistaAnimada = findViewById(R.id.vista_animada);
        vistaAnimada.startAnimation(animacion);

    }
}
