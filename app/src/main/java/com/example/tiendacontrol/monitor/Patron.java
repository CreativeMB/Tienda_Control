package com.example.tiendacontrol.monitor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.app.KeyguardManager;

public class Patron {

    private final Context context;

    public Patron(Context context) {
        this.context = context;
    }

    public void iniciarAutenticacion() {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager != null) {
            KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("Patron");
            if (keyguardManager.isKeyguardSecure()) {
                Intent intent = keyguardManager.createConfirmDeviceCredentialIntent("Autenticación de patrón", "Ingresa tu patrón para acceder");
                if (intent != null) {
                    ((Activity) context).startActivityForResult(intent, 1);
                }
            } else {
                Toast.makeText(context, "El dispositivo no tiene bloque de pantalla configurado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                // Acción después de la autenticación exitosa
                Toast.makeText(context, "Autenticación exitosa", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, BaseDatos.class); // Cambia BaseDatos.class por la actividad deseada
                context.startActivity(intent);
                if (context instanceof Activity) {
                    ((Activity) context).finish(); // Cierra la actividad actual si es una instancia de Activity
                }
            } else {
                Toast.makeText(context, "Autenticación fallida", Toast.LENGTH_SHORT).show();
            }
        }
    }
}