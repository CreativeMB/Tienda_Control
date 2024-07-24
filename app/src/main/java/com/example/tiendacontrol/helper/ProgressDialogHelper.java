package com.example.tiendacontrol.helper;
import android.app.ProgressDialog;
import android.content.Context;

import java.lang.ref.WeakReference;

public class ProgressDialogHelper {
    private static WeakReference<ProgressDialog> progressDialogRef;

    // Método estático para mostrar el diálogo de progreso
    public static void showProgressDialog(Context context, String message) {
        ProgressDialog progressDialog = progressDialogRef != null ? progressDialogRef.get() : null;

        // Inicializar el ProgressDialog si es null
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(message); // Establecer el mensaje que se mostrará
            progressDialog.setCancelable(false); // El diálogo no se puede cancelar tocando fuera de él
            progressDialogRef = new WeakReference<>(progressDialog);
        }
        progressDialog.show(); // Mostrar el diálogo de progreso
    }

    // Método estático para ocultar el diálogo de progreso
    public static void dismissProgressDialog() {
        ProgressDialog progressDialog = progressDialogRef != null ? progressDialogRef.get() : null;

        // Verificar si el diálogo está inicializado y actualmente visible
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss(); // Ocultar el diálogo de progreso
            progressDialogRef = null; // Establecer el objeto a null para liberar recursos
        }
    }
}