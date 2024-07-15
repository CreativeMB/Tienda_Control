//package com.example.tiendacontrol.dropbox;
//
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.IBinder;
//import android.util.Log;
//
//public class DropboxService extends Service {
//
//    // Variable para guardar el token de acceso
//    private String accessToken;
//
//    // Acción del Intent para recibir el token de acceso
//    private static final String ACTION_DROPBOX_TOKEN = "com.example.tiendacontrol.ACTION_DROPBOX_TOKEN";
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//
//        // Registrar un BroadcastReceiver para recibir el token
//        registerReceiver(tokenReceiver, new IntentFilter(ACTION_DROPBOX_TOKEN));
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        // Código para iniciar el servicio en segundo plano, si es necesario
//        // ...
//
//        return START_STICKY; // Indica que el servicio debe ser reiniciado si es destruido
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        // Si necesitas usar un Binder para la comunicación, puedes implementarlo aquí
//        return null; // Por defecto, no se usa Binder en este ejemplo
//    }
//
//    @Override
//    public void onDestroy() {
//        // Desregistrar el BroadcastReceiver cuando se destruye el servicio
//        unregisterReceiver(tokenReceiver);
//        super.onDestroy();
//    }
//
//    // BroadcastReceiver para recibir el token de acceso
//    private BroadcastReceiver tokenReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // Extraer el token de acceso del Intent
//            accessToken = intent.getStringExtra("access_token");
//
//            // Ejemplo de manejo del token recibido
//            if (accessToken != null) {
//                Log.d("DropboxService", "Token de acceso recibido: " + accessToken);
//
//                // Ejemplo: Pasar el token a DropboxManager para su uso
//                DropboxManager.getInstance(context).setAccessToken(accessToken);
//            }
//        }
//    };
//
//    // Método público para iniciar el servicio y enviar el token de acceso
//    public static void startServiceWithToken(Context context, String accessToken) {
//        Intent intent = new Intent(context, DropboxService.class);
//        intent.putExtra("access_token", accessToken);
//        context.startService(intent);
//    }
//}