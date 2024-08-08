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
            BdHelper dbHelper = new BdHelper(context, databaseName);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            // Consulta para obtener todos los datos de la tabla de ventas
            String query = "SELECT * FROM " + BdHelper.TABLE_VENTAS;
            Cursor cursor = db.rawQuery(query, null);

            Workbook workbook = new XSSFWorkbook();
            CreationHelper createHelper = workbook.getCreationHelper();
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

//            cursor.close();
//            db.close();

            // Generar un nombre de archivo basado en el nombre de la base de datos y una marca de tiempo
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