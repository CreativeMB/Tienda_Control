package com.example.tiendacontrol.helper;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.tiendacontrol.monitor.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class BaseExporter {

    private static final String TAG = "BaseExporter";
    private Context context;

    public BaseExporter(Context context) {
        this.context = context;
    }

    public void exportDatabase(String dbFilePath) {
        if (!((MainActivity) context).isStoragePermissionGranted()) {
            Toast.makeText(context, "Permisos de almacenamiento no concedidos", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Iniciando exportación de la base de datos.");

        File dbFile = new File(dbFilePath);
        if (!dbFile.exists()) {
            Log.e(TAG, "El archivo de la base de datos no existe: " + dbFilePath);
            Toast.makeText(context, "El archivo de la base de datos no existe", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Ruta de la base de datos original: " + dbFile.getAbsolutePath());

        File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File exportFile = new File(exportDir, dbFile.getName());
        Log.d(TAG, "Ruta de destino para la exportación: " + exportFile.getAbsolutePath());

        try {
            if (exportFile.exists()) {
                boolean deleted = exportFile.delete();
                if (deleted) {
                    Log.d(TAG, "Archivo existente eliminado: " + exportFile.getAbsolutePath());
                } else {
                    Log.e(TAG, "Error al eliminar el archivo existente: " + exportFile.getAbsolutePath());
                }
            }

            copyFile(dbFile, exportFile);
            Log.d(TAG, "Base de datos exportada exitosamente a: " + exportFile.getAbsolutePath());
            Toast.makeText(context, "Base de datos exportada correctamente a " + exportFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error al exportar la base de datos: " + e.getMessage());
            Toast.makeText(context, "Error al exportar la base de datos", Toast.LENGTH_SHORT).show();
        }
    }

    public void importDatabase(String dbFilePath) {
        if (!((MainActivity) context).isStoragePermissionGranted()) {
            Toast.makeText(context, "Permisos de almacenamiento no concedidos", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Iniciando importación de la base de datos.");

        File importDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File importFile = new File(importDir, new File(dbFilePath).getName());
        Log.d(TAG, "Ruta de destino para la importación: " + importFile.getAbsolutePath());

        try {
            copyFile(new File(dbFilePath), importFile);
            Log.d(TAG, "Base de datos importada exitosamente desde: " + importFile.getAbsolutePath());
            Toast.makeText(context, "Base de datos importada correctamente desde " + importFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error al importar la base de datos: " + e.getMessage());
            Toast.makeText(context, "Error al importar la base de datos", Toast.LENGTH_SHORT).show();
        }
    }

        // Método para copiar archivos
    private void copyFile(File sourceFile, File destFile) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;

        try {
            sourceChannel = new FileInputStream(sourceFile).getChannel();
            destChannel = new FileOutputStream(destFile).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally {
            if (sourceChannel != null) {
                sourceChannel.close();
            }
            if (destChannel != null) {
                destChannel.close();
            }
        }
        if (!sourceFile.exists()) {
            throw new IOException("Archivo de origen no existe: " + sourceFile.getAbsolutePath());
        }

        try (InputStream inputStream = new FileInputStream(sourceFile);
             OutputStream outputStream = new FileOutputStream(destFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

        } catch (IOException e) {
            throw new IOException("Error al copiar archivo: " + e.getMessage());
        }
    }
}