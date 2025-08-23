package com.creativem.tiendacontrol.notificacion;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.text.ParseException; // Importar ParseException
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date; // Importar Date
import java.util.Locale;

public class AlarmScheduler {

    private static final String TAG = "AlarmScheduler";

    public static void scheduleAlarm(Context context, RecordatorioModel recordatorio) {
        Log.d(TAG, "scheduleAlarm: Intentando programar alarma para ID " + recordatorio.getId() + " - " + recordatorio.getTitulo());

        if (!recordatorio.isActivo()) {
            cancelAlarm(context, recordatorio);
            Log.d(TAG, "scheduleAlarm: Recordatorio inactivo, cancelando alarma existente si la hay.");
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "scheduleAlarm: AlarmManager no disponible.");
            return;
        }

        Intent intent = new Intent(context, NotificacionReceiver.class);
        intent.putExtra("titulo", "Tarea Pendiente");
        intent.putExtra("mensaje", recordatorio.getTitulo());
        intent.putExtra("recordatorio_id", recordatorio.getId());
        intent.putExtra("recordatorio_model", recordatorio); // Pasar el objeto completo (Serializable)

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                recordatorio.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerAtMillis = calculateNextTriggerTime(recordatorio);
        Log.d(TAG, "scheduleAlarm: Tiempo de disparo calculado para ID " + recordatorio.getId() + ": " + triggerAtMillis + " (" + (triggerAtMillis > 0 ? new SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(triggerAtMillis) : "N/A") + ")");


        if (triggerAtMillis <= 0) {
            Log.d(TAG, "scheduleAlarm: No se puede programar la alarma para " + recordatorio.getTitulo() + ". Tiempo inválido o recordatorio 'Una vez' que ya pasó.");
            cancelAlarm(context, recordatorio);
            return;
        }

        alarmManager.cancel(pendingIntent); // Cancelar cualquier alarma existente antes de establecer una nueva
        Log.d(TAG, "scheduleAlarm: Cancelando alarma previa para ID " + recordatorio.getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            Log.d(TAG, "Alarma ID " + recordatorio.getId() + " programada con setExactAndAllowWhileIdle.");
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            Log.d(TAG, "Alarma ID " + recordatorio.getId() + " programada con setExact.");
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            Log.d(TAG, "Alarma ID " + recordatorio.getId() + " programada con set (API < KitKat).");
        }
    }

    public static void cancelAlarm(Context context, RecordatorioModel recordatorio) {
        Log.d(TAG, "cancelAlarm: Intentando cancelar alarma para ID " + recordatorio.getId() + " - " + recordatorio.getTitulo());
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "cancelAlarm: AlarmManager no disponible.");
            return;
        }

        Intent intent = new Intent(context, NotificacionReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                recordatorio.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "cancelAlarm: Alarma ID " + recordatorio.getId() + " cancelada exitosamente.");
    }

    public static long calculateNextTriggerTime(RecordatorioModel recordatorio) {
        Calendar calendar = Calendar.getInstance();
        int[] horaMin = convertirHoraAMPM(recordatorio.getHora()); // Usa la nueva función de conversión
        int horaRecordatorio = horaMin[0];
        int minutoRecordatorio = horaMin[1];

        calendar.set(Calendar.HOUR_OF_DAY, horaRecordatorio);
        calendar.set(Calendar.MINUTE, minutoRecordatorio);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long initialCalculatedTriggerAtMillis = calendar.getTimeInMillis();
        Log.d(TAG, "calculateNextTriggerTime: Hora del recordatorio seteada (en Calendar antes de ajustes): "
                + new SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(initialCalculatedTriggerAtMillis));
        Log.d(TAG, "calculateNextTriggerTime: System.currentTimeMillis() en este momento: "
                + new SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis()));


        long triggerAtMillis = initialCalculatedTriggerAtMillis;

        // Si la hora calculada es en el pasado (ya pasó hoy o es igual en el mismo minuto),
        // avanzar al día siguiente o próxima ocurrencia.
        // Se añade un pequeño buffer (ej. 1 segundo) para evitar que una alarma programada
        // en el mismo minuto pero unos segundos después se considere "pasada".
        // La condición ideal es 'estrictamente mayor'.
        if (triggerAtMillis <= (System.currentTimeMillis() + 1000) ) { // Añade 1 segundo de buffer
            Log.d(TAG, "calculateNextTriggerTime: La hora del recordatorio ya pasó HOY (o es muy cercana). Ajustando para la próxima ocurrencia.");
            switch (recordatorio.getRepeticion()) {
                case "Diario":
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    Log.d(TAG, "calculateNextTriggerTime: Repetición Diario, ajustado a mañana.");
                    break;
                case "Semanal":
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                    Log.d(TAG, "calculateNextTriggerTime: Repetición Semanal, ajustado a la próxima semana.");
                    break;
                case "Mensual":
                    calendar.add(Calendar.MONTH, 1);
                    Log.d(TAG, "calculateNextTriggerTime: Repetición Mensual, ajustado al próximo mes.");
                    break;
                case "Una vez":
                    Log.d(TAG, "calculateNextTriggerTime: Repetición 'Una vez', y la hora ya pasó. Retornando -1 (no se programa).");
                    return -1;
                default:
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    Log.d(TAG, "calculateNextTriggerTime: Repetición desconocida, ajustado a mañana.");
                    break;
            }
            triggerAtMillis = calendar.getTimeInMillis();
        }
        Log.d(TAG, "calculateNextTriggerTime: Próximo tiempo de disparo FINAL: "
                + new SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(triggerAtMillis));
        return triggerAtMillis;
    }

    // --- Versión MEJORADA de convertirHoraAMPM usando SimpleDateFormat ---
    public static int[] convertirHoraAMPM(String horaAMPM) {
        // El formato "hh:mm a" es el que genera tu TimePicker (06:25 p. m.)
        // y SimpleDateFormat es robusto para parsear esto en diferentes locales.
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        try {
            Date date = sdf.parse(horaAMPM);
            calendar.setTime(date);
            int hora = calendar.get(Calendar.HOUR_OF_DAY);
            int minuto = calendar.get(Calendar.MINUTE);
            Log.d(TAG, "convertirHoraAMPM: Parseado '" + horaAMPM + "' --> Hora 24h: " + hora + ", Minuto: " + minuto);
            return new int[]{hora, minuto};
        } catch (ParseException e) {
            Log.e(TAG, "convertirHoraAMPM: Error al parsear hora '" + horaAMPM + "': " + e.getMessage());
            // En caso de error, retorna un valor por defecto o maneja el error.
            // Por ejemplo, puedes retornar la hora actual o [0,0]
            Calendar now = Calendar.getInstance();
            return new int[]{now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE)};
        }
    }
}