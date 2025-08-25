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
        intent.putExtra("titulo", "Tarea Pendiente"); // Título genérico para la notificación
        intent.putExtra("mensaje", recordatorio.getTitulo()); // El título del recordatorio como mensaje principal
        intent.putExtra("recordatorio_id", recordatorio.getId());
        intent.putExtra("recordatorio_model", recordatorio); // Pasar el objeto completo (Serializable)

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                recordatorio.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerAtMillis = calculateNextTriggerTime(recordatorio);
        Log.d(TAG, "scheduleAlarm: Tiempo de disparo calculado para ID " + recordatorio.getId() + ": " + triggerAtMillis + " (" + (triggerAtMillis > 0 ? new SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(triggerAtMillis) : "N/A - Ya pasó/inválido") + ")");


        if (triggerAtMillis <= 0) {
            Log.d(TAG, "scheduleAlarm: No se puede programar la alarma para " + recordatorio.getTitulo() + ". Tiempo inválido o recordatorio 'Una vez'/'Fecha' que ya pasó.");
            cancelAlarm(context, recordatorio); // Asegurarse de cancelar si ya no debe programarse
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

    /**
     * Calcula el próximo tiempo de disparo para la alarma basado en la repetición
     * usando initialTriggerMillis como la fecha y hora base.
     *
     * @param recordatorio El RecordatorioModel.
     * @return El tiempo en milisegundos para la próxima alarma, o -1 si no se debe programar.
     */
    public static long calculateNextTriggerTime(RecordatorioModel recordatorio) {
        Calendar calendar = Calendar.getInstance();
        // Establecer el calendario con el initialTriggerMillis guardado en el modelo
        calendar.setTimeInMillis(recordatorio.getInitialTriggerMillis());

        Log.d(TAG, "calculateNextTriggerTime: Initial Recordatorio Trigger Time (from model): "
                + new SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(calendar.getTimeInMillis()));
        Log.d(TAG, "calculateNextTriggerTime: System.currentTimeMillis() en este momento: "
                + new SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis()));

        // --- Manejo de recordatorios de una sola vez ("Fecha" y "Una vez") ---
        if (recordatorio.getRepeticion().equalsIgnoreCase("Fecha") || recordatorio.getRepeticion().equalsIgnoreCase("Una vez")) {
            // Si la fecha y hora del recordatorio ya pasaron
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                Log.d(TAG, "calculateNextTriggerTime: Recordatorio '" + recordatorio.getRepeticion() + "' en el pasado. No se programa.");
                return -1; // Ya pasó, no programar
            }
            Log.d(TAG, "calculateNextTriggerTime: Recordatorio '" + recordatorio.getRepeticion() + "' en el futuro. Programando tal cual.");
            return calendar.getTimeInMillis(); // Es un evento futuro de una sola vez
        }

        // --- Manejo de recordatorios repetitivos ("Diario", "Semanal", "Mensual") ---
        // Avanzar el calendario hasta que el tiempo del recordatorio esté en el futuro.
        // Se usa un bucle 'while' porque podría haber pasado múltiples veces si la app estuvo inactiva.
        while (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            Log.d(TAG, "calculateNextTriggerTime: El tiempo de recordatorio actual (" + new SimpleDateFormat("dd/MM HH:mm:ss").format(calendar.getTime()) + ") ya pasó. Avanzando a la próxima ocurrencia.");
            switch (recordatorio.getRepeticion()) {
                case "Diario":
                case "Día":
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    Log.d(TAG, "calculateNextTriggerTime: Repetición Diario, avanzado un día.");
                    break;
                case "Semanal":
                case "Semana":
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                    Log.d(TAG, "calculateNextTriggerTime: Repetición Semanal, avanzado una semana.");
                    break;
                case "Mensual":
                case "Mes":
                    // Esto es más complejo si quieres mantener el mismo día del mes exacto.
                    // Aquí simplemente avanzamos un mes.
                    calendar.add(Calendar.MONTH, 1);
                    Log.d(TAG, "calculateNextTriggerTime: Repetición Mensual, avanzado un mes.");
                    break;
                default:
                    // Esto no debería suceder si "Fecha" y "Una vez" están manejados arriba.
                    Log.e(TAG, "calculateNextTriggerTime: Repetición desconocida en bucle repetitivo: " + recordatorio.getRepeticion());
                    return -1; // Error o repetición desconocida, detener programación.
            }
        }

        long finalTriggerMillis = calendar.getTimeInMillis();
        Log.d(TAG, "calculateNextTriggerTime: Próximo tiempo de disparo FINAL: "
                + new SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(finalTriggerMillis));
        return finalTriggerMillis;
    }

    public static int[] convertirHoraAMPM(String horaAMPM) {
        // Este método se usa si RecordatorioModel.getHora() SÓLO CONTIENE HORA AM/PM (ej. "06:25 p. m.")
        // Si RecordatorioModel.getHora() para "Fecha" ahora contiene "dd/MM/yyyy hh:mm a",
        // entonces este método no será llamado para ese caso, ya que calculateNextTriggerTime usa initialTriggerMillis.
        // Para los repetitivos, donde 'hora' es "hh:mm a", este método sigue siendo válido.

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
            // En caso de error, retorna la hora actual como fallback.
            Calendar now = Calendar.getInstance();
            return new int[]{now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE)};
        }
    }
}