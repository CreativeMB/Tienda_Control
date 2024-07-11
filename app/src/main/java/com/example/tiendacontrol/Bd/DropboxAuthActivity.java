package com.example.tiendacontrol.Bd;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.dropbox.core.android.Auth;
import com.example.tiendacontrol.R;

public class DropboxAuthActivity extends Activity {

    private static final String TAG = "DropboxAuthActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicia la autenticación de Dropbox
        Auth.startOAuth2Authentication(this, getString(R.string.dropbox_app_key));
        Log.d(TAG, "Iniciando autenticación de Dropbox.");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Obtenemos el token de acceso de Dropbox si está disponible
        String accessToken = Auth.getOAuth2Token();
        Log.d(TAG, "Token de acceso obtenido: " + accessToken);

        if (accessToken != null) {
            // Guardar el token de acceso en las preferencias compartidas
            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            prefs.edit().putString("dropbox_access_token", accessToken).apply();

            // Enviar el token de vuelta a la actividad que lo necesita
            Intent resultIntent = new Intent();
            resultIntent.putExtra("ACCESS_TOKEN", accessToken);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else {
            // Manejar el caso donde no se obtiene el token de acceso
            Log.e(TAG, "Error: el token de acceso es nulo.");
            Toast.makeText(this, "Error: no se pudo obtener el token de acceso de Dropbox.", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_CANCELED); // Opcional: indicar que la actividad finalizó sin éxito
            finish();
        }
    }
}
