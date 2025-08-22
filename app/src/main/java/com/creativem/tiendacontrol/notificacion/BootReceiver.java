package com.creativem.tiendacontrol.notificacion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) {
//
//            Log.d(TAG, "Dispositivo iniciado. Reprogramando recordatorios...");
//            Toast.makeText(context, "Reprogramando recordatorios...", Toast.LENGTH_LONG).show();

            List<RecordatorioModel> recordatorios = PrefsHelper.cargarLista(context);
            for (RecordatorioModel recordatorio : recordatorios) {
                if (recordatorio.isActivo()) {
                    AlarmScheduler.scheduleAlarm(context, recordatorio);
                }
            }
        }
    }
}