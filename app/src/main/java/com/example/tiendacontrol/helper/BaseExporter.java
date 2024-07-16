package com.example.tiendacontrol.helper;
import static com.example.tiendacontrol.helper.BdHelper.DATABASE_NAME;

import android.content.Context;
import android.os.AsyncTask;
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

        // Mostrar ProgressDialog
        ProgressDialogHelper.showProgressDialog(context, "Exportando base de datos...");

        new ExportTask().execute(dbFilePath);
    }

    public void importDatabase(String dbFileName) {
        if (!((MainActivity) context).isStoragePermissionGranted()) {
            Toast.makeText(context, "Permisos de almacenamiento no concedidos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar ProgressDialog
        ProgressDialogHelper.showProgressDialog(context, "Importando base de datos...");

        new ImportTask().execute(dbFileName);
    }

    private class ExportTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String dbFilePath = params[0];
            File dbFile = new File(dbFilePath);

            if (!dbFile.exists()) {
                Log.e(TAG, "El archivo de la base de datos no existe: " + dbFilePath);
                return false;
            }

            File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
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
                Toast.makeText(context, "Base de datos exportada correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error al exportar la base de datos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ImportTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String dbFileName = params[0];

            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File sourceFile = new File(downloadDir, dbFileName);

            if (!sourceFile.exists()) {
                Log.e(TAG, "El archivo de la base de datos no existe en la carpeta de descargas: " + sourceFile.getAbsolutePath());
                return false;
            }

            File destFile = context.getDatabasePath(BdHelper.DATABASE_NAME);

            try {
                if (destFile.exists() && !destFile.delete()) {
                    Log.e(TAG, "Error al eliminar el archivo existente: " + destFile.getAbsolutePath());
                    return false;
                }

                copyFile(sourceFile, destFile);
                return true;

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error al importar la base de datos: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            ProgressDialogHelper.dismissProgressDialog();

            if (success) {
                Toast.makeText(context, "Base de datos importada correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error al importar la base de datos", Toast.LENGTH_SHORT).show();
            }
        }
    }

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
    }
}