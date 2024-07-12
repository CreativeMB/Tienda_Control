package com.example.tiendacontrol.Bd;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DropboxHelper {
    private static final String TAG = "DropboxHelper";
    private Context mContext;
    private BdHelper bdHelper;

    public DropboxHelper(Context context) {
        mContext = context;
        bdHelper = new BdHelper(context);
    }

    public void exportarBaseDatos(String nombreBaseDatos) {
        File dbFile = mContext.getDatabasePath(nombreBaseDatos);
        if (dbFile.exists()) {
            SharedPreferences prefs = mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE);
            String accessToken = prefs.getString("dropbox_access_token", null);

            if (accessToken == null) {
                // Si no hay token de acceso, iniciar la actividad de autenticación de Dropbox
                mContext.startActivity(new Intent(mContext, DropboxAuthActivity.class));
                Toast.makeText(mContext, "Autenticación requerida. Intente nuevamente después de autenticarse.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Construir el path en Dropbox para el archivo CSV
            String dropboxPath = "/" + nombreBaseDatos.replace(".db", "") + ".csv";
            DbxRequestConfig config = DbxRequestConfig.newBuilder("Tienda Control").build();

            // Ejecutar la tarea de exportación en segundo plano
            new ExportTask(dbFile, dropboxPath, config, accessToken).execute();
        } else {
            Log.e(TAG, "Base de datos no encontrada en la ruta especificada.");
            Toast.makeText(mContext, "Base de datos no encontrada.", Toast.LENGTH_SHORT).show();
        }
    }

    private class ExportTask extends AsyncTask<Void, Void, Boolean> {
        private File dbFile;
        private String dropboxPath;
        private DbxRequestConfig config;
        private String accessToken;

        ExportTask(File dbFile, String dropboxPath, DbxRequestConfig config, String accessToken) {
            this.dbFile = dbFile;
            this.dropboxPath = dropboxPath;
            this.config = config;
            this.accessToken = accessToken;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Crear cliente Dropbox con la configuración y el token de acceso
                DbxClientV2 client = new DbxClientV2(config, accessToken);

                // Convertir la base de datos SQLite a formato CSV
                List<String> csvData = convertirBaseDatosACSV(dbFile);

                // Escribir los datos CSV en un archivo local
                File csvFile = escribirDatosEnCSV(csvData);

                if (csvFile != null) {
                    try (InputStream in = new FileInputStream(csvFile)) {
                        // Subir el archivo CSV a Dropbox
                        FileMetadata metadata = client.files().uploadBuilder(dropboxPath)
                                .uploadAndFinish(in);
                        Log.d(TAG, "Archivo subido a Dropbox: " + metadata.getPathLower());
                        return true;
                    } catch (UploadErrorException e) {
                        Log.e(TAG, "Error al subir el archivo a Dropbox: " + e.getMessage());
                    }
                }
            } catch (DbxException | IOException e) {
                Log.e(TAG, "Error al subir el archivo a Dropbox: " + e.getMessage());
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(mContext, "Exportación exitosa", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Error en la exportación", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private List<String> convertirBaseDatosACSV(File dbFile) {
        List<String> csvData = new ArrayList<>();
        if (bdHelper == null) {
            Log.e(TAG, "BdHelper no inicializado correctamente");
            return csvData;
        }

        SQLiteDatabase db = bdHelper.getReadableDatabase();
        Cursor cursor = db.query(
                BdHelper.TABLE_VENTAS,
                new String[]{"producto", "valor", "detalles", "cantidad", "fecha_registro"},
                null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            csvData.add("producto,valor,detalles,cantidad,fecha_registro");
            do {
                String producto = cursor.getString(cursor.getColumnIndex("producto"));
                double valor = cursor.getDouble(cursor.getColumnIndex("valor"));
                String detalles = cursor.getString(cursor.getColumnIndex("detalles"));
                int cantidad = cursor.getInt(cursor.getColumnIndex("cantidad"));
                String fecha_registro = cursor.getString(cursor.getColumnIndex("fecha_registro"));
                String csvLine = String.format("%s,%.2f,%s,%d,%s",
                        producto, valor, detalles, cantidad, fecha_registro);
                csvData.add(csvLine);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return csvData;
    }

    private File escribirDatosEnCSV(List<String> csvData) {
        File csvFile = null;
        try {
            // Crear el archivo CSV en el directorio de archivos externos de la aplicación
            csvFile = new File(mContext.getExternalFilesDir(null), "exportacion.csv");
            OutputStream os = new FileOutputStream(csvFile);
            for (String line : csvData) {
                os.write(line.getBytes(StandardCharsets.UTF_8));
                os.write("\n".getBytes(StandardCharsets.UTF_8));
            }
            os.close();
        } catch (IOException e) {
            Log.e(TAG, "Error al escribir datos en CSV: " + e.getMessage());
        }
        return csvFile;
    }
}
