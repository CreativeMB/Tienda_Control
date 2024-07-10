package com.example.tiendacontrol.Bd;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.Metadata;
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
    private DbxClientV2 dropboxClient;
    public DropboxHelper(Context context) {
        mContext = context;
        bdHelper = new BdHelper(context); // Inicializa BdHelper aquí
    }

    public void exportarBaseDatos(String nombreBaseDatos) {
        File dbFile = mContext.getDatabasePath(nombreBaseDatos);
        if (dbFile.exists()) {
            // Ruta de destino en Dropbox
            String dropboxPath = "/" + nombreBaseDatos.replace(".db", "") + ".csv";

            // Configuración del cliente Dropbox
            DbxRequestConfig config = DbxRequestConfig.newBuilder("Tienda Control")
                    .build();
// AsyncTask para manejar la operación de exportación en segundo plano
            AsyncTask<Void, Void, File> task = new AsyncTask<Void, Void, File>() {
                @Override
                protected File doInBackground(Void... voids) {
            try {
                // Inicializar cliente de Dropbox
                DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
                // Exportar base de datos SQLite a CSV
                List<String> csvData = convertirBaseDatosACSV(dbFile);
                File csvFile = escribirDatosEnCSV(csvData);

                // Subir archivo CSV a Dropbox
                if (csvFile != null) {
                    try (InputStream in = new FileInputStream(csvFile)) {
                        FileMetadata metadata = client.files().uploadBuilder(dropboxPath)
                                .uploadAndFinish(in);
                        Log.d(TAG, "Archivo subido a Dropbox: " + metadata.getPathLower());
                        return csvFile;
                    } catch (UploadErrorException e) {
                        Log.e(TAG, "Error al subir el archivo a Dropbox: " + e.getMessage());
                    }
                }
            } catch (DbxException | IOException e) {
                Log.e(TAG, "Error al subir el archivo a Dropbox: " + e.getMessage());
            }
                    return null;
                }

                @Override
                protected void onPostExecute(File result) {
                    super.onPostExecute(result);
                    // Aquí puedes manejar la finalización de la tarea si es necesario
                }
            };

            // Ejecutar la tarea AsyncTask
            task.execute();

        } else {
            Log.e(TAG, "Base de datos no encontrada en la ruta especificada.");
        }
    }
    private List<String> convertirBaseDatosACSV(File dbFile) {
        List<String> csvData = new ArrayList<>();
        // Verificar que bdHelper no sea nulo
        if (bdHelper == null) {
            Log.e("DropboxHelper", "BdHelper no inicializado correctamente");
            return csvData; // Retorna una lista vacía si BdHelper es nulo
        }

        SQLiteDatabase db = bdHelper.getReadableDatabase();

        // Consultar los datos de la tabla Ventas
        Cursor cursor = db.query(
                BdHelper.TABLE_VENTAS, // Nombre de la tabla
                new String[]{"producto", "valor", "detalles", "cantidad", "fecha_registro"}, // Columnas que deseas obtener
                null, // WHERE clause (sin condiciones)
                null, // Argumentos para WHERE clause
                null, // GROUP BY
                null, // HAVING
                null  // ORDER BY
        );

        // Iterar sobre el cursor y formatear los datos como líneas CSV
        if (cursor != null && cursor.moveToFirst()) {
            // Encabezado CSV
            csvData.add("producto,valor,detalles,cantidad,fecha_registro");

            do {
                // Obtener datos de cada columna
                String producto = cursor.getString(cursor.getColumnIndex("producto"));
                double valor = cursor.getDouble(cursor.getColumnIndex("valor"));
                String detalles = cursor.getString(cursor.getColumnIndex("detalles"));
                int cantidad = cursor.getInt(cursor.getColumnIndex("cantidad"));
                String fecha_registro = cursor.getString(cursor.getColumnIndex("fecha_registro"));

                // Formatear datos como línea CSV
                String csvLine = String.format("%s,%.2f,%s,%d,%s",
                        producto, valor, detalles, cantidad, fecha_registro);

                // Agregar la línea CSV a la lista
                csvData.add(csvLine);
            } while (cursor.moveToNext());

            cursor.close();
        }

        // Cerrar la base de datos
        db.close();

        return csvData;
    }

    private File escribirDatosEnCSV(List<String> csvData) {
        File csvFile = null;
        try {
            csvFile = new File(mContext.getExternalFilesDir(null), "exportacion.csv");
            OutputStream os = new FileOutputStream(csvFile);
            for (String line : csvData) {
                os.write(line.getBytes(StandardCharsets.UTF_8));
                os.write("\n".getBytes(StandardCharsets.UTF_8));
            }
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvFile;
    }
}