package com.example.tiendacontrol.helper;
import static com.example.tiendacontrol.helper.BdHelper.DATABASE_NAME;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

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
    // Método para exportar la base de datos
    public void exportDatabase(String dbFileName) {
        if (!isStoragePermissionGranted()) {
            Toast.makeText(context, "Permisos de almacenamiento no concedidos", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Iniciando exportación de la base de datos.");

        File dbFile = context.getDatabasePath(dbFileName);
        if (!dbFile.exists()) {
            Log.e(TAG, "El archivo de la base de datos no existe: " + dbFileName);
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

    // Método para importar la base de datos
    public void importDatabase(String dbFileName) {
        if (!isStoragePermissionGranted()) {
            Toast.makeText(context, "Permisos de almacenamiento no concedidos", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Iniciando importación de la base de datos desde la carpeta de descargas.");

        // Obtener la carpeta de descargas del dispositivo
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadDir.exists()) {
            Log.e(TAG, "La carpeta de descargas no existe: " + downloadDir.getAbsolutePath());
            Toast.makeText(context, "La carpeta de descargas no existe", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construir la ruta completa del archivo de la base de datos en la carpeta de descargas
        File sourceFile = new File(downloadDir, dbFileName);
        if (!sourceFile.exists()) {
            Log.e(TAG, "El archivo de la base de datos no existe en la carpeta de descargas: " + sourceFile.getAbsolutePath());
            Toast.makeText(context, "El archivo de la base de datos no existe en la carpeta de descargas", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Ruta del archivo a importar desde descargas: " + sourceFile.getAbsolutePath());

        // Ruta de destino para la importación (la base de datos actual de la app)
        File destFile = context.getDatabasePath(dbFileName);
        Log.d(TAG, "Ruta de destino para la importación: " + destFile.getAbsolutePath());

        try {
            // Eliminar el archivo de base de datos existente si existe
            if (destFile.exists()) {
                boolean deleted = destFile.delete();
                if (deleted) {
                    Log.d(TAG, "Archivo existente eliminado: " + destFile.getAbsolutePath());
                } else {
                    Log.e(TAG, "Error al eliminar el archivo existente: " + destFile.getAbsolutePath());
                }
            }

            // Copiar el archivo desde la carpeta de descargas a la ubicación de la base de datos de la app
            copyFile(sourceFile, destFile);
            Log.d(TAG, "Base de datos importada exitosamente desde descargas a: " + destFile.getAbsolutePath());
            Toast.makeText(context, "Base de datos importada correctamente desde descargas", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error al importar la base de datos desde descargas: " + e.getMessage());
            Toast.makeText(context, "Error al importar la base de datos desde descargas", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para verificar si los permisos de almacenamiento están concedidos
    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // Versions prior to Android M do not need runtime permission
            return true;
        }
    }

    // Método para copiar un archivo
    private void copyFile(File sourceFile, File destFile) throws IOException {
        FileInputStream fis = new FileInputStream(sourceFile);
        FileOutputStream fos = new FileOutputStream(destFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }
        fos.flush();
        fos.close();
        fis.close();
    }
}
//    public void  exportDatabase(String dbFilePath) {
//        if (!((MainActivity) context).isStoragePermissionGranted()) {
//            Toast.makeText(context, "Permisos de almacenamiento no concedidos", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Log.d(TAG, "Iniciando exportación de la base de datos.");
//
//        File dbFile = new File(dbFilePath);
//        if (!dbFile.exists()) {
//            Log.e(TAG, "El archivo de la base de datos no existe: " + dbFilePath);
//            Toast.makeText(context, "El archivo de la base de datos no existe", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        Log.d(TAG, "Ruta de la base de datos original: " + dbFile.getAbsolutePath());
//
//        File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        File exportFile = new File(exportDir, dbFile.getName());
//        Log.d(TAG, "Ruta de destino para la exportación: " + exportFile.getAbsolutePath());
//
//        try {
//            if (exportFile.exists()) {
//                boolean deleted = exportFile.delete();
//                if (deleted) {
//                    Log.d(TAG, "Archivo existente eliminado: " + exportFile.getAbsolutePath());
//                } else {
//                    Log.e(TAG, "Error al eliminar el archivo existente: " + exportFile.getAbsolutePath());
//                }
//            }
//
//            copyFile(dbFile, exportFile);
//            Log.d(TAG, "Base de datos exportada exitosamente a: " + exportFile.getAbsolutePath());
//            Toast.makeText(context, "Base de datos exportada correctamente a " + exportFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.e(TAG, "Error al exportar la base de datos: " + e.getMessage());
//            Toast.makeText(context, "Error al exportar la base de datos", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    public void importDatabase(String dbFileName) {
//        if (!((MainActivity) context).isStoragePermissionGranted()) {
//            Toast.makeText(context, "Permisos de almacenamiento no concedidos", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Log.d(TAG, "Iniciando importación de la base de datos desde la carpeta de descargas.");
//
//        // Obtener la carpeta de descargas del dispositivo
//        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        if (!downloadDir.exists()) {
//            Log.e(TAG, "La carpeta de descargas no existe: " + downloadDir.getAbsolutePath());
//            Toast.makeText(context, "La carpeta de descargas no existe", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Construir la ruta completa del archivo de la base de datos en la carpeta de descargas
//        File sourceFile = new File(downloadDir, dbFileName);
//        if (!sourceFile.exists()) {
//            Log.e(TAG, "El archivo de la base de datos no existe en la carpeta de descargas: " + sourceFile.getAbsolutePath());
//            Toast.makeText(context, "El archivo de la base de datos no existe en la carpeta de descargas", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Log.d(TAG, "Ruta del archivo a importar desde descargas: " + sourceFile.getAbsolutePath());
//
//        // Ruta de destino para la importación (la base de datos actual de la app)
//        File destFile = context.getDatabasePath(BdHelper.DATABASE_NAME);
//        Log.d(TAG, "Ruta de destino para la importación: " + destFile.getAbsolutePath());
//
//        try {
//            // Eliminar el archivo de base de datos existente si existe
//            if (destFile.exists()) {
//                boolean deleted = destFile.delete();
//                if (deleted) {
//                    Log.d(TAG, "Archivo existente eliminado: " + destFile.getAbsolutePath());
//                } else {
//                    Log.e(TAG, "Error al eliminar el archivo existente: " + destFile.getAbsolutePath());
//                }
//            }
//
//            // Copiar el archivo desde la carpeta de descargas a la ubicación de la base de datos de la app
//            copyFile(sourceFile, destFile);
//            Log.d(TAG, "Base de datos importada exitosamente desde descargas a: " + destFile.getAbsolutePath());
//            Toast.makeText(context, "Base de datos importada correctamente desde descargas", Toast.LENGTH_LONG).show();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.e(TAG, "Error al importar la base de datos desde descargas: " + e.getMessage());
//            Toast.makeText(context, "Error al importar la base de datos desde descargas", Toast.LENGTH_SHORT).show();
//        }
//    }
//        // Método para copiar archivos
//    private void copyFile(File sourceFile, File destFile) throws IOException {
//        FileChannel sourceChannel = null;
//        FileChannel destChannel = null;
//
//        try {
//            sourceChannel = new FileInputStream(sourceFile).getChannel();
//            destChannel = new FileOutputStream(destFile).getChannel();
//            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
//        } finally {
//            if (sourceChannel != null) {
//                sourceChannel.close();
//            }
//            if (destChannel != null) {
//                destChannel.close();
//            }
//        }
//        if (!sourceFile.exists()) {
//            throw new IOException("Archivo de origen no existe: " + sourceFile.getAbsolutePath());
//        }
//
//        try (InputStream inputStream = new FileInputStream(sourceFile);
//             OutputStream outputStream = new FileOutputStream(destFile)) {
//
//            byte[] buffer = new byte[1024];
//            int length;
//            while ((length = inputStream.read(buffer)) > 0) {
//                outputStream.write(buffer, 0, length);
//            }
//
//        } catch (IOException e) {
//            throw new IOException("Error al copiar archivo: " + e.getMessage());
//        }
//    }
//}