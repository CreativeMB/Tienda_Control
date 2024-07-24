package com.example.tiendacontrol.helper;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BaseExporter {
    private static final String TAG = "BaseExporter";
    private Context context;

    // Constructor que recibe el contexto de la aplicación
    public BaseExporter(Context context) {
        this.context = context;
    }

    // Método para exportar la base de datos
    public void exportDatabase(String dbFileName) {
        if (!isStoragePermissionGranted()) {
            Toast.makeText(context, "Permisos de almacenamiento no concedidos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar el ProgressDialog y ejecutar la exportación en segundo plano
        ProgressDialogHelper.showProgressDialog(context, "Exportando base de datos...");
        new ExportTask(context, dbFileName).execute();
    }

    // Método para importar la base de datos
    public void importDatabase(String dbFileName) {
        if (!isStoragePermissionGranted()) {
            Toast.makeText(context, "Permisos de almacenamiento no concedidos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar el ProgressDialog y ejecutar la importación en segundo plano
        ProgressDialogHelper.showProgressDialog(context, "Importando base de datos...");
        new ImportTask(context, dbFileName).execute();
    }

    // Método para verificar si los permisos de almacenamiento están concedidos
    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Para Android 11 y versiones posteriores
            return Environment.isExternalStorageManager();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Para Android 6.0 a 10
            return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // Para versiones anteriores a Android 6.0
            return true;
        }
    }

    // Tarea asíncrona para exportar la base de datos
    private static class ExportTask extends AsyncTask<Void, Void, Boolean> {
        private Context context;
        private String dbFileName;

        ExportTask(Context context, String dbFileName) {
            this.context = context;
            this.dbFileName = dbFileName;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            File dbFile = context.getDatabasePath(dbFileName);
            if (!dbFile.exists()) {
                Log.e(TAG, "El archivo de la base de datos no existe: " + dbFileName);
                return false;
            }

            File exportDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS); // Use internal storage directory
            if (exportDir == null) {
                Log.e(TAG, "El directorio de exportación no es accesible.");
                return false;
            }

            File exportFile = new File(exportDir, dbFile.getName());

            try {
                if (exportFile.exists() && !exportFile.delete()) {
                    Log.e(TAG, "Error al eliminar el archivo existente: " + exportFile.getAbsolutePath());
                    return false;
                }

                copyFile(dbFile, exportFile);
                return true;

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error al exportar la base de datos: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            ProgressDialogHelper.dismissProgressDialog();
            if (success) {
                Toast.makeText(context, "Base de datos exportada correctamente a " + context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Error al exportar la base de datos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Tarea asíncrona para importar la base de datos
    private static class ImportTask extends AsyncTask<Void, Void, Boolean> {
        private Context context;
        private String dbFileName;

        ImportTask(Context context, String dbFileName) {
            this.context = context;
            this.dbFileName = dbFileName;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            File downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS); // Use internal storage directory
            if (downloadDir == null) {
                Log.e(TAG, "El directorio de descargas no es accesible.");
                return false;
            }

            File sourceFile = new File(downloadDir, dbFileName);
            if (!sourceFile.exists()) {
                Log.e(TAG, "El archivo de la base de datos no existe en la carpeta de descargas: " + sourceFile.getAbsolutePath());
                return false;
            }

            File destFile = context.getDatabasePath(dbFileName);

            try {
                if (destFile.exists() && !destFile.delete()) {
                    Log.e(TAG, "Error al eliminar el archivo existente: " + destFile.getAbsolutePath());
                    return false;
                }

                copyFile(sourceFile, destFile);
                return true;

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error al importar la base de datos desde descargas: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            ProgressDialogHelper.dismissProgressDialog();
            if (success) {
                Toast.makeText(context, "Base de datos importada correctamente desde descargas", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Error al importar la base de datos desde descargas", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para copiar un archivo de un lugar a otro
    private static void copyFile(File sourceFile, File destFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }
}