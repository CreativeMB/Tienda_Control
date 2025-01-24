package com.creativem.tiendacontrol.model;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.creativem.tiendacontrol.Login;
import com.creativem.tiendacontrol.R;
import com.creativem.tiendacontrol.SessionManager;
import com.creativem.tiendacontrol.monitor.BaseDatos;
import com.creativem.tiendacontrol.monitor.Inicio;


public class AnimacionInicio extends AppCompatActivity {
    private static final String PREFS_NAME = "CodePrefs";
    private static final String CODE_KEY = "accesscode";

    private SessionManager sessionManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animacioninicio);
        sessionManager = new SessionManager(this); // Inicializa SessionManager aquí
        Log.d("AnimacionInicio", "AnimacionInicio - onCreate llamado");


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

//        String savedCode = sessionManager.getSavedCode();
        boolean isLoggedIn = sessionManager.isLoggedIn();
        Log.d(TAG, "SavedCode: " + savedCode);
        Log.d(TAG, "IsLoggedIn: " + isLoggedIn);

        Intent intent;

        // Si hay un PIN guardado y el usuario está logueado, redirigir a InicioPin
        if (!savedCode.isEmpty() && isLoggedIn) {
            Log.d("AnimacionInicio", "PIN guardado y usuario logueado, redirigiendo a InicioPin");
            intent = new Intent(AnimacionInicio.this, Inicio.class);
        }
        // Si no hay PIN guardado y el usuario está logueado, redirigir a BaseDatos
        else if (savedCode.isEmpty() && isLoggedIn) {
            Log.d("AnimacionInicio", "No hay PIN, pero usuario logueado, redirigiendo a BaseDatos");
            intent = new Intent(AnimacionInicio.this, BaseDatos.class);
        }
        // Si no hay PIN guardado y el usuario no está logueado, redirigir a Login
        else if (savedCode.isEmpty() && !isLoggedIn) {
            Log.d("AnimacionInicio", "No hay PIN y usuario no logueado, redirigiendo a Login");
            intent = new Intent(AnimacionInicio.this, Login.class);
        } else {
            // Fallback, aunque este caso no debería ocurrir.
            Log.d("AnimacionInicio", "Fallo en la lógica de redirección");
            intent = new Intent(AnimacionInicio.this, Login.class);
        }
        startActivity(intent);
        finish();
    }
}