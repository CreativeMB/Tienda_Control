package com.example.tiendacontrol.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExcelExporter {
    private static final String TAG = "ExcelExporter";

    // Método público para iniciar la exportación a Excel
    public static void exportToExcel(Context context) {
        // Mostrar el ProgressDialog mientras se realiza la exportación
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Exportando en Excel a Descargas...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Ejecutar la exportación en segundo plano utilizando AsyncTask
        new ExportTask(context, progressDialog).execute();
    }

    // Tarea en segundo plano para exportar los datos a Excel
    private static class ExportTask extends AsyncTask<Void, Void, Boolean> {
        private Context context;
        private ProgressDialog progressDialog;

        ExportTask(Context context, ProgressDialog progressDialog) {
            this.context = context;
            this.progressDialog = progressDialog;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            BdHelper dbHelper = new BdHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            // Consulta para obtener todos los datos de la tabla de ventas
            String query = "SELECT * FROM " + BdHelper.TABLE_VENTAS;
            Cursor cursor = db.rawQuery(query, null);

            Workbook workbook = new XSSFWorkbook(); // Crear un nuevo libro de Excel
            CreationHelper createHelper = workbook.getCreationHelper();

            // Crear una hoja dentro del libro
            Sheet sheet = workbook.createSheet("Datos");

            // Escribir los encabezados en la primera fila
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Producto", "Valor", "Detalles", "Cantidad", "Fecha Registro"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Escribir los datos obtenidos de la base de datos
            int rowNum = 1;
            while (cursor.moveToNext()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(cursor.getInt(cursor.getColumnIndex("id")));
                row.createCell(1).setCellValue(cursor.getString(cursor.getColumnIndex("producto")));
                row.createCell(2).setCellValue(cursor.getDouble(cursor.getColumnIndex("valor")));
                row.createCell(3).setCellValue(cursor.getString(cursor.getColumnIndex("detalles")));
                row.createCell(4).setCellValue(cursor.getInt(cursor.getColumnIndex("cantidad")));
                row.createCell(5).setCellValue(cursor.getString(cursor.getColumnIndex("fecha_registro")));
            }

            cursor.close(); // Cerrar el cursor
            db.close(); // Cerrar la base de datos

            // Generar un nombre de archivo único para el archivo Excel
            String fileName = generateFileName();

            // Guardar el libro de Excel en la carpeta de descargas del dispositivo
            try {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        Log.e(TAG, "Error: No se pudo crear el directorio de destino para la exportación.");
                        return false;
                    }
                }

                File file = new File(dir, fileName + ".xlsx");
                FileOutputStream outputStream = new FileOutputStream(file);
                workbook.write(outputStream); // Escribir el libro en el archivo
                outputStream.close(); // Cerrar el flujo de salida
                return true;

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error al exportar a Excel: " + e.getMessage());
                return false;
            } finally {
                try {
                    workbook.close(); // Cerrar el libro de Excel
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            // Ocultar el ProgressDialog y mostrar un mensaje Toast según el resultado
            progressDialog.dismiss();
            if (success) {
                Toast.makeText(context, "En Descargas: " + generateFileName() + ".xlsx", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error al exportar a Excel", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para generar un nombre de archivo único basado en la fecha y hora actuales
    private static String generateFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return "Mi_contabilidad_" + timeStamp;
    }

}
