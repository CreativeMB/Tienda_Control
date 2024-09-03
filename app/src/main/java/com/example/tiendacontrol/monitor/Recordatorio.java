package com.example.tiendacontrol.monitor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;


import androidx.core.app.NotificationCompat;

import com.example.tiendacontrol.R;
import com.example.tiendacontrol.model.SplashActivity;


public class Recordatorio extends BroadcastReceiver {
    private static final String CHANNEL_ID = "recordatorios_diarios";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        mostrarNotificacion(context);
    }

    private void mostrarNotificacion(Context context) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        crearCanalNotificacion(context, notificationManager);

        // Intent para abrir la aplicación al tocar la notificación
        Intent notificationIntent = new Intent(context, SplashActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.contabilidad) // Reemplaza con tu icono
                .setContentTitle("Mi Contabilidad")
                .setContentText("No Olvides Realizar Las Cuentas Hoy.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void crearCanalNotificacion(Context context, NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Recordatorios Diarios",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Canal para recordatorios diarios");
            notificationManager.createNotificationChannel(channel);
        }
    }
}