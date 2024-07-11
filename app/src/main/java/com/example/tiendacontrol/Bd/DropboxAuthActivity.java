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
    private static final String ACCESS_TOKEN_KEY = "dropbox_access_token"; // Clave para guardar el token

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicia la autenticación de Dropbox con tu clave de aplicación
        Auth.startOAuth2Authentication(this, getString(R.string.dropbox_app_key));
        Log.d(TAG, "Iniciando autenticación de Dropbox.");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Obtén el token de acceso de Dropbox
        String accessToken = Auth.getOAuth2Token();

        if (accessToken != null) {
            // Guardar el token de acceso en las preferencias compartidas
            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(ACCESS_TOKEN_KEY, accessToken);
            editor.apply();

            // Enviar el token de acceso a la actividad que lo necesita
            Intent resultIntent = new Intent();
            resultIntent.putExtra("ACCESS_TOKEN", accessToken);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else {
            // Manejar el caso donde no se obtiene el token de acceso
            Log.e(TAG, "Error: el token de acceso es nulo.");
            Toast.makeText(this, "Error: no se pudo obtener el token de acceso de Dropbox.", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }
}