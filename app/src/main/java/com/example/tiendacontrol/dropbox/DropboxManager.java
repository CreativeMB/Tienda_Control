package com.example.tiendacontrol.dropbox;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.android.AuthActivity;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.users.FullAccount;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DropboxManager {

    private static final String TAG = "DropboxManager";
    private static final String APP_KEY = "7vazzg1njz2v1co"; // Reemplaza con tu App Key
    private static final String ACCESS_TOKEN_KEY = "dropbox_access_token";
    private static final String REDIRECT_URI = "https://com.example.tiendacontrol/callback";
    public static final int AUTH_REQUEST_CODE = 1001;

    private static DropboxManager instance; // Instancia única
    private DbxClientV2 mDbxClient;
    private Activity mActivity; // Se puede eliminar si no se utiliza

    // Constructor privado para evitar instanciación directa
    private DropboxManager(Activity activity) {
        this.mActivity = activity;
    }

    // Método de acceso para obtener la instancia única
    public static synchronized DropboxManager getInstance(Activity activity) {
        if (instance == null) {
            instance = new DropboxManager(activity);
        }
        return instance;
    }

    public void authenticate() {
        String accessToken = getAccessToken();
        if (accessToken == null) {
            startAuth();
        } else {
            initializeClient(accessToken);
            loadAccount();
        }
    }

    private void startAuth() {
        try {
            // Crea una instancia de DbxRequestConfig
            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("Tienda_Control")
                    .withUserLocale("es") // Ajusta el idioma si necesario
                    .build();

            // Crea una Intent para AuthActivity
            // Define los scopes que necesitas (por ejemplo, 'files.content.read' y 'files.content.write')
            String[] scopes = {"files.content.read", "files.content.write"};
            Intent intent = AuthActivity.makeIntent(
                    mActivity, APP_KEY, REDIRECT_URI, scopes, null, null, null); // Valores por defecto para los nuevos parámetros

            // Ejecuta startActivityForResult() en el hilo principal a través de un Handler
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> mActivity.startActivityForResult(intent, AUTH_REQUEST_CODE));

        } catch (Exception e) {
            Log.e(TAG, "Error al iniciar la autenticación: " + e.getMessage());
            Toast.makeText(mActivity, "Error al iniciar la autenticación", Toast.LENGTH_SHORT).show();
        }
    }

    public void handleAuthResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTH_REQUEST_CODE && resultCode == Activity.RESULT_OK) { // Verifica con la constante personalizada
            // Manejar el resultado de la autenticación
            String accessToken = Auth.getOAuth2Token(); // Obtén el token de acceso
            if (accessToken != null) { // Verifica si el token es válido
                Log.d(TAG, "Autenticación exitosa. Token de acceso: " + accessToken);
                saveAccessToken(accessToken);
                loadAccount();
            } else {
                Log.e(TAG, "Error en la autenticación.");
                Toast.makeText(mActivity, "Error en la autenticación", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeClient(String accessToken) {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("Tienda_Control")
                .withUserLocale("es")
                .build();
        mDbxClient = new DbxClientV2(config, accessToken);
    }

    private void saveAccessToken(String accessToken) {
        SharedPreferences prefs = mActivity.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ACCESS_TOKEN_KEY, accessToken);
        editor.apply();
    }

    private String getAccessToken() {
        SharedPreferences prefs = mActivity.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        return prefs.getString(ACCESS_TOKEN_KEY, null);
    }

    private void loadAccount() {
        new Thread(() -> {
            try {
                FullAccount account = mDbxClient.users().getCurrentAccount();
                Log.d(TAG, "Usuario autenticado: " + account.getName().getDisplayName());
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(mActivity, "Autenticación exitosa", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Log.e(TAG, "Error al cargar la cuenta: " + e.getMessage());
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(mActivity, "Error al cargar la cuenta", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    public void uploadFile(File file, String destinationPath) {
        new Thread(() -> {
            try (FileInputStream inputStream = new FileInputStream(file)) {
                FileMetadata response = mDbxClient.files().uploadBuilder(destinationPath)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream);
                Log.d(TAG, "Archivo subido: " + response.getPathLower());
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(mActivity, "Archivo subido correctamente", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Log.e(TAG, "Error al subir el archivo: " + e.getMessage());
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(mActivity, "Error al subir el archivo", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
