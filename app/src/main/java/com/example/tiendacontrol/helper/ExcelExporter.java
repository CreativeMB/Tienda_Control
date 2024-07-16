package com.example.tiendacontrol.helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelExporter {

    private static final String TAG = "ExcelExporter";

    public static void exportToExcel(Context context, String fileName) {
        BdHelper dbHelper = new BdHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Consulta para obtener los datos de la base de datos
        String query = "SELECT * FROM " + BdHelper.TABLE_VENTAS;
        Cursor cursor = db.rawQuery(query, null);

        Workbook workbook = new XSSFWorkbook();
        CreationHelper createHelper = workbook.getCreationHelper();

        // Crear una hoja dentro del libro
        Sheet sheet = workbook.createSheet("Datos");

        // Escribir los encabezados
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Producto", "Valor", "Detalles", "Cantidad", "Fecha Registro"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Escribir los datos de la base de datos
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

        cursor.close();
        db.close();

        // Guardar el libro de Excel en la carpeta de descargas del dispositivo
        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e(TAG, "Error: No se pudo crear el directorio de destino para la exportación.");
                    Toast.makeText(context, "Error al crear el directorio de exportación", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            File file = new File(dir, fileName + ".xlsx");
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            outputStream.close();

            Log.d(TAG, "Archivo Excel exportado correctamente a: " + file.getAbsolutePath());
            Toast.makeText(context, "Guardado en Descargas", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error al exportar a Excel: " + e.getMessage());
            Toast.makeText(context, "Error al exportar a Excel", Toast.LENGTH_SHORT).show();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
