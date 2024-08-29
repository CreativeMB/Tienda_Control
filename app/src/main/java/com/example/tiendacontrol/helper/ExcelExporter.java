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
    private String databaseName;

    public ExcelExporter(String databaseName) {
        this.databaseName = databaseName;
    }

    // Método público para iniciar la exportación a Excel
    public void exportToExcel(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Exportando en Excel...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new ExportTask(context, progressDialog).execute();
    }

    private class ExportTask extends AsyncTask<Void, Void, Boolean> {
        private Context context;
        private ProgressDialog progressDialog;

        ExportTask(Context context, ProgressDialog progressDialog) {
            this.context = context;
            this.progressDialog = progressDialog;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            BdVentas bdVentas = new BdVentas(context, databaseName);
            SQLiteDatabase db = bdVentas.getReadableDatabase();

            // Consulta para obtener todos los datos de la tabla de ventas
            // Nota: Puedes ajustar la consulta para obtener solo las columnas
            //       deseadas. Por ejemplo: SELECT id, producto, valor FROM ventas;
            String query = "SELECT * FROM " + BdVentas.TABLE_VENTAS;
            Cursor cursor = db.rawQuery(query, null);

            // Crear el libro de trabajo de Excel
            Workbook workbook = new XSSFWorkbook();
            CreationHelper createHelper = workbook.getCreationHelper();
            Sheet sheet = workbook.createSheet("Datos");

            // Escribir los encabezados en la primera fila
            Row headerRow = sheet.createRow(0);
            // Asegúrate de que los nombres de las columnas coincidan con los de la base de datos
            String[] headers = {"ID", "Producto", "Valor", "Detalles", "Cantidad", "Fecha Registro"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Escribir los datos obtenidos de la base de datos
            int rowNum = 1;
            // Verifica si el cursor tiene filas
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    Row row = sheet.createRow(rowNum++);

                    // Obtener los valores de las columnas de la base de datos
                    // y escribirlos en la fila correspondiente
                    // Verifica que las columnas existen en el cursor
                    if (cursor.getColumnIndex("id") != -1) {
                        row.createCell(0).setCellValue(cursor.getInt(cursor.getColumnIndex("id")));
                    }
                    if (cursor.getColumnIndex("producto") != -1) {
                        row.createCell(1).setCellValue(cursor.getString(cursor.getColumnIndex("producto")));
                    }
                    if (cursor.getColumnIndex("valor") != -1) {
                        row.createCell(2).setCellValue(cursor.getDouble(cursor.getColumnIndex("valor")));
                    }
                    if (cursor.getColumnIndex("detalles") != -1) {
                        row.createCell(3).setCellValue(cursor.getString(cursor.getColumnIndex("detalles")));
                    }
                    if (cursor.getColumnIndex("cantidad") != -1) {
                        row.createCell(4).setCellValue(cursor.getInt(cursor.getColumnIndex("cantidad")));
                    }
                    if (cursor.getColumnIndex("fecha_registro") != -1) {
                        row.createCell(5).setCellValue(cursor.getString(cursor.getColumnIndex("fecha_registro")));
                    }

                    cursor.moveToNext();
                }
            } else {
                Log.e(TAG, "Cursor está vacío.");
            }
            cursor.close();
            db.close();

            // Generar un nombre de archivo basado en el nombre de la base de datos
            // y una marca de tiempo
            String fileName = generateFileName(databaseName);

            // Obtener la ruta de documentos públicos
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "TiendaControl");
            if (!dir.exists() && !dir.mkdirs()) {
                Log.e(TAG, "Error al crear el directorio de documentos");
                return false;
            }

            try (FileOutputStream outputStream = new FileOutputStream(new File(dir, fileName + ".xlsx"))) {
                workbook.write(outputStream);
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error al exportar a Excel: " + e.getMessage());
                return false;
            } finally {
                try {
                    workbook.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error al cerrar el libro de Excel: " + e.getMessage());
                }
            }
        }


        @Override
        protected void onPostExecute(Boolean success) {
            progressDialog.dismiss();
            if (success) {
                Toast.makeText(context, "Exportado: " + databaseName + ".xlsx", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error al exportar a Excel", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String generateFileName(String baseName) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return baseName + "_" + timeStamp;
    }
}