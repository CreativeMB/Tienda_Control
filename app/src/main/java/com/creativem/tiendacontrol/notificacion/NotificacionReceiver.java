package com.creativem.tiendacontrol.notificacion;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.creativem.tiendacontrol.R;
import com.creativem.tiendacontrol.monitor.AnimacionInicio;

import java.util.List;

public class NotificacionReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "recordatorios_channel";
    private static final String TAG = "NotificacionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: Alarma recibida.");

        String titulo = intent.getStringExtra("titulo");
        String mensaje = intent.getStringExtra("mensaje");
        int recordatorioId = intent.getIntExtra("recordatorio_id", -1);
        RecordatorioModel recordatorioModel = (RecordatorioModel) intent.getSerializableExtra("recordatorio_model");

        if (recordatorioId == -1 || recordatorioModel == null) {
            Log.e(TAG, "onReceive: Recordatorio ID o Modelo no encontrado en el Intent. No se puede procesar.");
            return;
        }
        Log.d(TAG, "onReceive: Procesando recordatorio ID: " + recordatorioId + ", Titulo: " + recordatorioModel.getTitulo());

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Tarea Pendiente",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal para Tarea Pendiente de TiendaControl");
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "onReceive: Canal de notificación creado/verificado.");
        }

        // --- CAMBIO CLAVE AQUÍ: Apuntar a la actividad principal (AnimacionInicio.class) ---
        Intent notificationIntent = new Intent(context, AnimacionInicio.class); // <-- CAMBIO AQUI
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntentActivity = PendingIntent.getActivity(
                context,
                recordatorioId, // Usar el ID del recordatorio como requestCode
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.contabilidad)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntentActivity)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle());

        try {
            RemoteViews customNotificationLayout = new RemoteViews(context.getPackageName(), R.layout.custom_notification_layout);


            // Establecer el icono GRANDE DENTRO de tu layout personalizado
            customNotificationLayout.setImageViewResource(R.id.notification_icon_large, R.drawable.icono);


            // Establecer los textos DENTRO de tu layout personalizado
            customNotificationLayout.setTextViewText(R.id.notification_title, titulo != null ? titulo : "Tarea Pendiente");
            customNotificationLayout.setTextViewText(R.id.notification_message, mensaje != null ? mensaje : recordatorioModel.getTitulo());


            builder.setCustomContentView(customNotificationLayout);


        } catch (Exception e) {
            // Si el RemoteViews falla, volvemos a mostrar la notificación estándar (fallback)
            builder.setContentTitle(titulo != null ? titulo : "Tarea Pendiente");
            builder.setContentText(mensaje != null ? mensaje : recordatorioModel.getTitulo());
        }
        // --- FIN DE LA PERSONALIZACIÓN DE LA NOTIFICACIÓN ---


        if (notificationManager != null) {
            notificationManager.notify(recordatorioId, builder.build());
        } else {
            Log.e(TAG, "onReceive: NotificationManager es nulo, no se pudo mostrar la notificación.");
        }

        // Lógica para RE-PROGRAMAR la próxima alarma (si es repetitiva y activa)
        List<RecordatorioModel> recordatoriosGuardados = PrefsHelper.cargarLista(context);
        RecordatorioModel recordatorioDesdePrefs = null;
        for (RecordatorioModel r : recordatoriosGuardados) {
            if (r.getId() == recordatorioId) {
                recordatorioDesdePrefs = r;
                break;
            }
        }

        if (recordatorioDesdePrefs != null && recordatorioDesdePrefs.isActivo()) {
            if (!recordatorioDesdePrefs.getRepeticion().equalsIgnoreCase("Una vez")) {
                Log.d(TAG, "onReceive: Recordatorio ID " + recordatorioId + " es repetitivo y activo. Reprogramando próxima alarma.");
                AlarmScheduler.scheduleAlarm(context, recordatorioDesdePrefs);
            } else {
                Log.d(TAG, "onReceive: Recordatorio ID " + recordatorioId + " es 'Una vez' y ha sido disparado. No se reprograma.");
                AlarmScheduler.cancelAlarm(context, recordatorioDesdePrefs);
            }
        } else {
            Log.d(TAG, "onReceive: Recordatorio ID " + recordatorioId + " no encontrado en preferencias o está inactivo. No se reprograma.");
            AlarmScheduler.cancelAlarm(context, recordatorioModel);
        }
    }
}