package com.creativem.tiendacontrol.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.creativem.tiendacontrol.model.Items;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ExcelExporter {
    private static final String TAG = "ExcelExporter";
    private String databaseName;
    private DatabaseReference databaseReference;

    public ExcelExporter(String databaseName, String databasePath) {
        this.databaseName = databaseName;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReferenceFromUrl(databasePath);
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
            // Usar Firebase Database Reference en lugar de SQLiteDatabase
            ArrayList<Items> itemsList = new ArrayList<>();
            try {
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Items item = snapshot.getValue(Items.class);
                            if (item != null) {
                                itemsList.add(item);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Error al obtener data de firebase", databaseError.toException());
                    }
                });


                // Esperar a que la data se obtenga
                while(itemsList.isEmpty()){
                    Thread.sleep(20);
                }
            } catch (InterruptedException e) {
                Log.e(TAG,"Error en Thread sleep: "+e.getMessage());
            }



            // Crear el libro de trabajo de Excel
            Workbook workbook = new XSSFWorkbook();
            CreationHelper createHelper = workbook.getCreationHelper();
            Sheet sheet = workbook.createSheet("Datos");

            // Escribir los encabezados en la primera fila
            Row headerRow = sheet.createRow(0);
            // Asegúrate de que los nombres de las columnas coincidan con los campos de tu clase Item
            String[] headers = {"ID", "Producto", "Valor", "Detalles", "Cantidad", "Fecha Registro","Tipo"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            int rowNum = 1;
            if(!itemsList.isEmpty()) {
                // Escribir los datos obtenidos de Firebase
                for (Items item : itemsList) {
                    Row row = sheet.createRow(rowNum++);

                    // Escribir los datos del Item en las celdas de la fila
                    row.createCell(0).setCellValue(item.getId());
                    row.createCell(1).setCellValue(item.getProducto());
                    row.createCell(2).setCellValue(item.getValor());
                    row.createCell(3).setCellValue(item.getDetalles());
                    row.createCell(4).setCellValue(item.getCantidad());
                    row.createCell(5).setCellValue(item.getFechaRegistro());
                    row.createCell(6).setCellValue(item.getType());
                }
            } else {
                Log.e(TAG, "Lista de Items vacía");
            }



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