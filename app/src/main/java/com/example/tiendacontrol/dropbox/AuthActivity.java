//package com.example.tiendacontrol.dropbox;
//import android.app.Activity;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//
//import com.dropbox.core.android.Auth; // Importa la clase Auth de Dropbox
//
//import java.util.Arrays;
//import java.util.List;
//
//public class AuthActivity extends Activity {
//    // Código de solicitud para la autenticación
//    private static final int AUTH_REQUEST_CODE = 1234;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//// Inicia el proceso de autenticación de Dropbox
//        String[] scopes = {"files.content.read", "files.content.write"};
//        try {
//            Auth.startOAuth2Authentication(
//                    this, // Contexto de la actividad
//                    "7vazzg1njz2v1co", // Reemplaza con tu APP_KEY de Dropbox
//                    "db-7vazzg1njz2v1co://com.example.tiendacontrol/callback", // URI de redirección
//                    scopes, // Ámbitos solicitados
//                    null, // locale (opcional)
//                    null // uiLocale (opcional)
//            );
//        } catch (Exception e) {
//            Log.e("Dropbox Auth", "Error iniciando autenticación: " + e.getMessage());
//            // Manejar el error según sea necesario
//            setResult(Activity.RESULT_CANCELED);
//            finish(); // Finaliza esta actividad con resultado cancelado
//        }
//    }
//
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        // Se llama cuando la actividad se reanuda después de iniciar la autenticación
//        String accessToken = Auth.getOAuth2Token();
//
//        if (accessToken != null) {
//            // Autenticación exitosa, obtén el token de acceso
//            Intent returnIntent = new Intent();
//            returnIntent.putExtra("access_token", accessToken);
//            setResult(Activity.RESULT_OK, returnIntent);
//            finish(); // Finaliza esta actividad y regresa al llamador con el token de acceso
//        } else {
//            // Manejo de error: token de acceso nulo
//            setResult(Activity.RESULT_CANCELED);
//            finish(); // Finaliza esta actividad con resultado cancelado
//        }
//    }
//}