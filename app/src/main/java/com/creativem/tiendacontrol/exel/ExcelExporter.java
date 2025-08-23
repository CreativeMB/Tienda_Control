package com.creativem.tiendacontrol.exel;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExcelExporter {

    public static File exportToExcel(Context context, Map<String, List<Map<String, Object>>> datosPorBases, String fileName) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        for (Map.Entry<String, List<Map<String, Object>>> entrada : datosPorBases.entrySet()) {
            String nombreHoja = entrada.getKey();
            String nombreHojaLimpio = sanitizeSheetName(nombreHoja);

            // Evitar crear hojas con nombre vacío
            if (nombreHojaLimpio.isEmpty()) {
                nombreHojaLimpio = "Hoja_" + System.currentTimeMillis();
            }

            List<Map<String, Object>> data = entrada.getValue();

            Sheet sheet = workbook.createSheet(nombreHojaLimpio);

            // Encabezados
            String[] headers = {"fechaHora", "nombre", "nota", "valor"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                sheet.setColumnWidth(i, 6000); // ancho fijo para todas las columnas
            }

            // Datos
            int rowIndex = 1;
            for (Map<String, Object> item : data) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(getStringValue(item.get("fechaHora")));
                row.createCell(1).setCellValue(getStringValue(item.get("nombre")));
                row.createCell(2).setCellValue(getStringValue(item.get("nota")));

                Object valorObj = item.get("valor");
                if (valorObj instanceof Number) {
                    row.createCell(3).setCellValue(((Number) valorObj).doubleValue());
                } else {
                    row.createCell(3).setCellValue(getStringValue(valorObj));
                }
            }
        }

        // Guardar archivo en carpeta exports
        File exportDir = new File(context.getExternalFilesDir(null), "exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File file = new File(exportDir, fileName + ".xlsx");

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
        } finally {
            workbook.close();
        }

        return file;
    }

    private static String sanitizeSheetName(String name) {
        if (name == null) return "";
        // Reemplaza caracteres inválidos para nombres de hoja de Excel
        return name.replaceAll("[:\\\\/?*\\[\\]]", "_").trim();
    }

    private static String getStringValue(Object value) {
        return value != null ? value.toString() : "";
    }


    public static void shareExcel(Context context, File file) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(shareIntent, "Compartir archivo Excel"));
    }

}
