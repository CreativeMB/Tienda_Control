package com.example.tiendacontrol.model;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tiendacontrol.R;
import com.example.tiendacontrol.monitor.Database;
import com.example.tiendacontrol.login.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
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
                // Redirigir siempre al usuario a la actividad de login
                redirectToLogin();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // La animación se ha repetido
            }
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(SplashActivity.this, Login.class);
        startActivity(intent);
        finish();
    }
}