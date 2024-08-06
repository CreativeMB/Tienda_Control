package com.example.tiendacontrol.model;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tiendacontrol.R;
import com.example.tiendacontrol.adapter.DatabaseManagerActivity;
import com.example.tiendacontrol.login.Login;
import com.example.tiendacontrol.monitor.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        mAuth = FirebaseAuth.getInstance();

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
                // Verificar si el usuario ya ha iniciado sesión
                checkUserLoggedIn();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // La animación se ha repetido
            }
        });
    }

    private void checkUserLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Si no hay usuario logueado, redirigir a la actividad de login
            Intent intent = new Intent(SplashActivity.this, Login.class);
            startActivity(intent);
            finish();
        } else {
            // Si hay un usuario logueado, redirigir a MainActivity
            Intent intent = new Intent(SplashActivity.this, DatabaseManagerActivity.class);
            startActivity(intent);
            finish();

            // Mostrar un mensaje de bienvenida
            Toast.makeText(this, "Bienvenido, " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
        }
    }
}