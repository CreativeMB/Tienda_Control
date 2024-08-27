package com.example.tiendacontrol.monitor;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import com.example.tiendacontrol.R;

import java.util.Calendar;

public class Recordatorio extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        showNotification(context);
        reScheduleAlarm(context);
    }

    private void showNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Crear un canal de notificación para Android O y superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "your_channel_id",
                    "Recordatorios Diarios",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal para recordatorios diarios");
            notificationManager.createNotificationChannel(channel);
        }

        // Crear un Intent que se ejecutará cuando se toque la notificación
        Intent notificationIntent = new Intent(context, Inicio.class);  // Cambia Database por la actividad que deseas abrir
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Crear la notificación y asociar el PendingIntent
        Notification notification = new NotificationCompat.Builder(context, "your_channel_id")
                .setContentTitle("Mi Contabilidad")
                .setContentText("No Olvides Realizar Las Cuentas Hoy.")
                .setSmallIcon(R.drawable.contabilidad)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)  // Asociar el PendingIntent aquí
                .build();

        // Mostrar la notificación
        notificationManager.notify(1, notification);
    }
    private void reScheduleAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, Recordatorio.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Reprogramar la alarma para el siguiente día a la misma hora
        Calendar nextTriggerTime = Calendar.getInstance();
        nextTriggerTime.add(Calendar.DAY_OF_YEAR, 1);
        nextTriggerTime.set(Calendar.SECOND, 0);

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // API 31 y superior
                if (!canScheduleExactAlarms(context)) {
                    // Solicitar permiso para alarmas exactas
                    Intent permissionIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(permissionIntent);
                    return;
                }
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextTriggerTime.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                // Para versiones anteriores a API 31
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        nextTriggerTime.getTimeInMillis(),
                        pendingIntent
                );
            }
        }
    }

    private boolean canScheduleExactAlarms(Context context) {
        // Para API 31 y superior, se utiliza el método canScheduleExactAlarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        // Para versiones anteriores, siempre devolver verdadero
        return true;
    }
}