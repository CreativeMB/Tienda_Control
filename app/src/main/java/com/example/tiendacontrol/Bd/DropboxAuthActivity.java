package com.example.tiendacontrol.Bd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import com.dropbox.core.android.Auth;
import com.example.tiendacontrol.R;

public class DropboxAuthActivity extends Activity {

    private static final String TAG = "DropboxAuthActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicia la autenticación de Dropbox
        Auth.startOAuth2Authentication(this, getString(R.string.dropbox_app_key));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Obtenemos el token de acceso de Dropbox si está disponible
        String accessToken = Auth.getOAuth2Token();

        if (accessToken != null) {
            // Guardar el token de acceso en las preferencias compartidas o pasarlo a la actividad principal
            // Aquí puedes enviar el token de vuelta a la actividad que lo necesita
            Intent resultIntent = new Intent();
            resultIntent.putExtra("ACCESS_TOKEN", accessToken);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else {
            Log.e(TAG, "Error: el token de acceso es nulo.");
        }
    }
}
