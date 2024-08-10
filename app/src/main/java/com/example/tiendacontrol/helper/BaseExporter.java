//package com.example.tiendacontrol.helper;
//
//import static androidx.core.app.ActivityCompat.startActivityForResult;
//import static androidx.core.content.ContentProviderCompat.requireContext;
//
//import static com.example.tiendacontrol.monitor.MainActivity.REQUEST_CODE_STORAGE_PERMISSION;
//
//import android.Manifest;
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.os.AsyncTask;
//import android.os.Build;
//import android.os.Environment;
//import android.provider.Settings;
//import android.util.Log;
//import android.widget.Toast;
//
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
//public class BaseExporter {
//    private static final String TAG = "BaseExporter";
//    private Context context;
//    private Activity activity;
//    private String databaseName; // Añadir nombre de la base de datos
//
//    public BaseExporter(Context context, Activity activity, String databaseName) {
//        this.context = context;
//        this.activity = activity;
//        this.databaseName = databaseName; // Inicializar el nombre de la base de datos
//    }
//
//    public void exportDatabase() {
//        if (!isStoragePermissionGranted()) {
//            Toast.makeText(context, "Permisos de almacenamiento no concedidos", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Log.d(TAG, "Iniciando exportación de la base de datos.");
//        File dbFile = context.getDatabasePath(databaseName); // Usar el nombre de la base de datos
//        if (!dbFile.exists()) {
//            Log.e(TAG, "El archivo de la base de datos no existe: " + databaseName);
//            Toast.makeText(context, "El archivo de la base de datos no existe", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        File exportFile = new File(exportDir, dbFile.getName());
//        try {
//            if (exportFile.exists() && !exportFile.delete()) {
//                Log.e(TAG, "Error al eliminar el archivo existente: " + exportFile.getAbsolutePath());
//                return;
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
//    public void importDatabase() {
//        if (!isStoragePermissionGranted()) {
//            Toast.makeText(context, "Permisos de almacenamiento no concedidos", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Log.d(TAG, "Iniciando importación de la base de datos desde la carpeta de descargas.");
//        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        if (!downloadDir.exists()) {
//            Log.e(TAG, "La carpeta de descargas no existe: " + downloadDir.getAbsolutePath());
//            Toast.makeText(context, "La carpeta de descargas no existe", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        File sourceFile = new File(downloadDir, databaseName); // Usar el nombre de la base de datos
//        if (!sourceFile.exists()) {
//            Log.e(TAG, "El archivo de la base de datos no existe en la carpeta de descargas: " + sourceFile.getAbsolutePath());
//            Toast.makeText(context, "El archivo de la base de datos no existe en la carpeta de descargas", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        File destFile = context.getDatabasePath(databaseName); // Usar el nombre de la base de datos
//        try {
//            if (destFile.exists() && !destFile.delete()) {
//                Log.e(TAG, "Error al eliminar el archivo existente: " + destFile.getAbsolutePath());
//                return;
//            }
//
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
//
//    private void copyFile(File sourceFile, File destFile) throws IOException {
//        try (FileInputStream fis = new FileInputStream(sourceFile);
//             FileOutputStream fos = new FileOutputStream(destFile)) {
//            byte[] buffer = new byte[1024];
//            int length;
//            while ((length = fis.read(buffer)) > 0) {
//                fos.write(buffer, 0, length);
//            }
//            fos.flush();
//        }
//    }
//
//    public boolean isStoragePermissionGranted() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            // Android 11 y versiones posteriores - Verificar permiso "MANAGE_EXTERNAL_STORAGE"
//            return Environment.isExternalStorageManager();
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            // Android 6.0 a Android 10 - Verificar permiso "WRITE_EXTERNAL_STORAGE"
//            return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
//        } else {
//            // Versiones anteriores a Android 6.0
//            return true;
//        }
//    }
//
//    public void requestStoragePermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            ActivityCompat.requestPermissions(activity,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    REQUEST_CODE_STORAGE_PERMISSION);
//        }
//    }
//}