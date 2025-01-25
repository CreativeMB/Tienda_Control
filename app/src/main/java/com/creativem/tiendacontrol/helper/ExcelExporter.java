package com.creativem.tiendacontrol.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.FileProvider;
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
    private  String databaseName;
    private DatabaseReference databaseReference;
    private Context context;
    private static int taskCount = 0;

    public ExcelExporter(Context context, String databaseName, String databasePath) {
        this.context = context;
        this.databaseName = databaseName;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReferenceFromUrl(databasePath);
        Log.d(TAG, "ExcelExporter creado para la base de datos: " + databaseName + " con la ruta: " + databasePath + " Task Number: " + taskCount);
    }

    // Método público para iniciar la exportación a Excel
    public void exportToExcel(XSSFWorkbook workbook, OnCompleteListener listener) {
        Log.d(TAG, "Iniciando exportación a Excel para: " + databaseName + " Task Number: " + taskCount);
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Exportando en Excel...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        new ExportTask(context, progressDialog,workbook,databaseName,databaseReference,listener).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }
    public interface OnCompleteListener {
        void onComplete(Uri fileUri);
    }

    private static class ExportTask extends AsyncTask<Void, Void, Uri> {
        private  Context context;
        private  ProgressDialog progressDialog;
        private  ArrayList<Items> itemsList;
        private  OnCompleteListener listener;
        private XSSFWorkbook workbook;
        private String databaseName;
        private DatabaseReference databaseReference;


        ExportTask(Context context, ProgressDialog progressDialog, XSSFWorkbook workbook, String databaseName, DatabaseReference databaseReference, OnCompleteListener listener) {
            this.context = context;
            this.progressDialog = progressDialog;
            this.workbook = workbook;
            this.databaseName = databaseName;
            this.databaseReference = databaseReference;
            this.listener = listener;
            itemsList = new ArrayList<>();
            Log.d(TAG, "ExportTask creado para la base de datos: " + databaseName+ " Task Number: " + taskCount );
        }

        @Override
        protected Uri doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground() iniciado para la base de datos: " + databaseName+ " Task Number: " + (++taskCount));
            try {
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange() iniciado para la base de datos: " + databaseName+ " Task Number: " + taskCount);
                        //Ahora iteramos en los hijos de la base de datos
                        for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                            Log.d(TAG, "Procesando itemSnapshot: " + itemSnapshot.getKey() + " en la base de datos: " + databaseName+ " Task Number: " + taskCount);
                            if (itemSnapshot.hasChildren() && !itemSnapshot.getKey().equals("fechaCreacion") && !itemSnapshot.getKey().equals("timestamp")) {
                                Items item = itemSnapshot.getValue(Items.class);
                                if (item != null) {
                                    itemsList.add(item);
                                    Log.d(TAG, "Item agregado a la lista: " + item.getProducto() + " en la base de datos: " + databaseName+ " Task Number: " + taskCount );
                                }else {
                                    Log.e(TAG, "No se pudo convertir a item el snapshot con Id: " + itemSnapshot.getKey() + " en la base de datos: " + databaseName+ " Task Number: " + taskCount);
                                }
                            }else {
                                Log.w(TAG, "Snapshot sin hijos o Metadata, ignorando: " + itemSnapshot.getKey() + " en la base de datos: " + databaseName+ " Task Number: " + taskCount);
                            }
                        }
                        Log.d(TAG, "onDataChange() finalizado para la base de datos: " + databaseName + ", Items encontrados: " + itemsList.size()+ " Task Number: " + taskCount);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Error al obtener data de firebase para la base de datos: " + databaseName+ " Task Number: " + taskCount, databaseError.toException());
                    }
                });


                // Esperar a que la data se obtenga
                while(itemsList.isEmpty()){
                    Log.d(TAG, "Esperando datos de Firebase para la base de datos: " + databaseName+ " Task Number: " + taskCount);
                    Thread.sleep(20);
                }
                Log.d(TAG, "Datos obtenidos de Firebase para la base de datos: " + databaseName + ", Items encontrados: " + itemsList.size() + " Task Number: " + taskCount);

            } catch (InterruptedException e) {
                Log.e(TAG,"Error en Thread sleep: "+e.getMessage() + " en la base de datos: " + databaseName+ " Task Number: " + taskCount);
                return null;
            }

            //Obtenemos la hoja o creamos una nueva
            Sheet sheet = workbook.getSheet(databaseName);
            if(sheet == null){
                sheet = workbook.createSheet(databaseName);
                Log.d(TAG, "Creando hoja de Excel para la base de datos: " + databaseName+ " Task Number: " + taskCount);
                // Escribir los encabezados en la primera fila
                Row headerRow = sheet.createRow(0);
                // Asegúrate de que los nombres de las columnas coincidan con los campos de tu clase Item
                String[] headers = {"Producto", "Valor", "Detalles", "Cantidad", "Fecha","Tipo"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                }
            } else{
                Log.d(TAG, "Obteniendo la hoja existente de excel para la base de datos: " + databaseName + " Task Number: " + taskCount);
            }
            int rowNum = sheet.getLastRowNum() + 1;
            if(!itemsList.isEmpty()) {
                // Escribir los datos obtenidos de Firebase
                Log.d(TAG, "Escribiendo datos al Excel para la base de datos: " + databaseName+ " Task Number: " + taskCount);
                for (Items item : itemsList) {
                    Row row = sheet.createRow(rowNum++);
                    // Escribir los datos del Item en las celdas de la fila
                    row.createCell(0).setCellValue(item.getProducto());
                    row.createCell(1).setCellValue(item.getValor());
                    row.createCell(2).setCellValue(item.getDetalles());
                    row.createCell(3).setCellValue(item.getCantidad());
                    row.createCell(4).setCellValue(item.getDate());
                    row.createCell(5).setCellValue(item.getType());
                    Log.d(TAG, "Escribiendo item: " + item.getProducto() + " en excel para la base de datos: " + databaseName+ " Task Number: " + taskCount);
                }
                Log.d(TAG, "Finalizando escritura en Excel para la base de datos: " + databaseName+ " Task Number: " + taskCount);
            } else {
                Log.e(TAG, "Lista de Items vacía para la base de datos: " + databaseName+ " Task Number: " + taskCount);
            }


            // Generar un nombre de archivo basado en el nombre de la base de datos
            // y una marca de tiempo
            String fileName = generateFileName("TodasLasBasesDeDatos");
            Log.d(TAG, "Generando nombre de archivo Excel para la base de datos: " + databaseName+ " Task Number: " + taskCount);

            // Obtener el directorio temporal de la app
            File tempDir = context.getCacheDir();
            File excelFile = null;
            FileOutputStream outputStream = null;
            try{
                excelFile = new File(tempDir, fileName + ".xlsx");
                outputStream = new FileOutputStream(excelFile);
                Log.d(TAG, "Escribiendo el excel en la ruta: " + excelFile.getAbsolutePath() + " para la base de datos: " + databaseName+ " Task Number: " + taskCount);
                workbook.write(outputStream);
                Log.d(TAG, "Escritura completada en la ruta: " + excelFile.getAbsolutePath() + " para la base de datos: " + databaseName+ " Task Number: " + taskCount);
            } catch (IOException e){
                Log.e(TAG, "Error al crear o escribir el archivo: " + e.getMessage() + " para la base de datos: " + databaseName+ " Task Number: " + taskCount);
                return null;
            }finally {
                if(outputStream != null){
                    try{
                        outputStream.close();
                        Log.d(TAG, "Cerrando outputStream para la base de datos: " + databaseName+ " Task Number: " + taskCount);
                    }catch(IOException e){
                        Log.e(TAG, "Error al cerrar el outputStream: " + e.getMessage() + " para la base de datos: " + databaseName+ " Task Number: " + taskCount);
                    }
                }
            }

            //Usar FileProvider para obtener el Uri para compartir
            if(excelFile != null) {
                Log.d(TAG, "Obteniendo Uri con FileProvider para la base de datos: " + databaseName + " en la ruta: " + excelFile.getAbsolutePath()+ " Task Number: " + taskCount);
                return FileProvider.getUriForFile(context,
                        context.getApplicationContext().getPackageName() + ".provider",
                        excelFile);
            }
            Log.e(TAG, "Error al obtener Uri con FileProvider para la base de datos: " + databaseName+ " Task Number: " + taskCount);
            return null;
        }

        @Override
        protected void onPostExecute(Uri fileUri) {
            progressDialog.dismiss();
            if (fileUri != null) {
                ExcelExporter exporter = new ExcelExporter(context, databaseName, databaseReference.toString());
                exporter.shareExcelFile(fileUri);
                Log.d(TAG, "Exportación exitosa, compartiendo excel para la base de datos: " + databaseName+ " Task Number: " + taskCount);
                if (listener != null) {
                    listener.onComplete(fileUri);
                }
            } else {
                Toast.makeText(context, "Error al exportar a Excel", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error al exportar a Excel para la base de datos: " + databaseName+ " Task Number: " + taskCount);
                if (listener != null) {
                    listener.onComplete(null);
                }
            }
        }
        public void setOnCompleteListener(OnCompleteListener listener) {
            this.listener = listener;
        }
    }

    private void shareExcelFile(Uri fileUri) {
        Log.d(TAG, "Compartiendo archivo excel para la base de datos: " + databaseName);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(shareIntent, "Compartir Excel"));
    }

    private static String generateFileName(String baseName) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return baseName + "_" + timeStamp;
    }
}