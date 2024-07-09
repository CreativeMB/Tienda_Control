package com.example.tiendacontrol;



import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendacontrol.Bd.BdVentas;
import com.example.tiendacontrol.adaptadores.ListaVentasAdapter;
import com.example.tiendacontrol.entidades.Ventas;
import com.example.tiendacontrol.exportar.DatabaseExporter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    SearchView txtBuscar;
    RecyclerView listaVentas;
    ArrayList<Ventas> listaArrayVentas;
    FloatingActionButton fabNuevo;
    FloatingActionButton fabGAsto;
    ListaVentasAdapter adapter;
    TextView textVenta, textTotal, textGasto;

    Toolbar toolbar;
    private static final int NUEVA_VENTA = 1;
    private static final int NUEVA_GASTO = 2;
    private static final int MES = 3;
//    private static final int EXPORTAR = 4;
//    private static final int EXPORTAR = R.id.EXPORTAR;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private DatabaseExporter dbExporter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        txtBuscar = findViewById(R.id.txtBuscar);
        listaVentas = findViewById(R.id.listaVentas);
        fabNuevo = findViewById(R.id.favNuevo);
        fabGAsto = findViewById(R.id.favGasto);

        textVenta = findViewById(R.id.textVenta);
        textTotal = findViewById(R.id.textTotal);
        textGasto = findViewById(R.id.textGasto);
        toolbar = findViewById(R.id.toolbar);

        dbExporter = new DatabaseExporter(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tienda Control");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        listaVentas.setLayoutManager(new LinearLayoutManager(this));

        BdVentas bdVentas = new BdVentas(MainActivity.this);

        listaArrayVentas = new ArrayList<>(bdVentas.mostrarVentas());

        adapter = new ListaVentasAdapter(bdVentas.mostrarVentas());
        listaVentas.setAdapter(adapter);


        fabGAsto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nuevoGasto();
            }
        });
        ;

        fabNuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nuevoRegistro();
            }
        });

        txtBuscar.setOnQueryTextListener(this);
        calcularSumaGancias();
        calcularSumaTotalVenta();
        calcularSumaTotalGasto();
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case NUEVA_VENTA:
                nuevoRegistro();
                return true;
            case NUEVA_GASTO:
                nuevoGasto();
                return true;
            case MES:
                // Código para la opción "Mes"
                return true;
            default:
                if (item.getItemId() == R.id.EXPORTAR) { // Usa if para comprobar la ID
                    if (checkPermission()) {
                        exportDatabase();
                    } else {
                        requestPermission();
                    }
                    return true;
                }
                return super.onOptionsItemSelected(item);
        }
    }

    private void nuevoRegistro() {
        Intent intent = new Intent(this, Nuevo.class);
        startActivity(intent);
    }

    private void nuevoGasto() {
        Intent intent = new Intent(this, Gasto.class);
        startActivity(intent);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        adapter.filtrado(s);
        return false;
    }

    private void calcularSumaGancias() {
        double suma = 0.0;
        for (Ventas venta : listaArrayVentas) {
            double valorVenta = venta.getValorAsDouble();
            suma += valorVenta;
            Log.d("SUMA_DEBUG", "Valor de venta: " + valorVenta + ", Suma parcial: " + suma);
        }
        int sumaFormateada = (int) suma;
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        String sumaFormateadaStr = "$" + numberFormat.format(sumaFormateada);

        textTotal.setText(sumaFormateadaStr);
    }

    private void calcularSumaTotalVenta() {
        double suma = 0.0;
        for (Ventas venta : listaArrayVentas) {
            double valorVenta = venta.getValorAsDouble();
            if (valorVenta > 0) {  // Solo sumar si el valor es positivo
                suma += valorVenta;
                Log.d("SUMA_DEBUG", "Valor de venta positivo: " + valorVenta + ", Suma parcial: " + suma);
            }
        }
        int sumaFormateada = (int) suma;
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        String sumaFormateadaStr = "$" + numberFormat.format(sumaFormateada);

        textVenta.setText(sumaFormateadaStr);
    }

    private void calcularSumaTotalGasto() {
        double suma = 0.0;
        for (Ventas venta : listaArrayVentas) {
            double valorVenta = venta.getValorAsDouble();
            if (valorVenta < 0) {  // Solo sumar si el valor es negativo
                suma += valorVenta;
                Log.d("SUMA_DEBUG", "Valor de venta negativo: " + valorVenta + ", Suma parcial: " + suma);
            }
        }
        int sumaFormateada = (int) suma;
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        String sumaFormateadaStr = "$" + numberFormat.format(sumaFormateada);

        textGasto.setText(sumaFormateadaStr);
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportDatabase();
            } else {
                Toast.makeText(this, "Permiso denegado para escribir en almacenamiento externo", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void exportDatabase() {
        Log.d("EXPORT_DEBUG", "Comenzando la exportación...");
        if (dbExporter.exportDatabase()) {
            Log.d("EXPORT_DEBUG", "Base de datos exportada exitosamente");
            Toast.makeText(this, "Base de datos exportada exitosamente", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("EXPORT_DEBUG", "Error al exportar la base de datos");
            Toast.makeText(this, "Error al exportar la base de datos", Toast.LENGTH_SHORT).show();
        }
    }

// ... (resto de tu código)

    // Clase DatabaseExporter (ejemplo)
    public class DatabaseExporter {

        private Context context;

        public DatabaseExporter(Context context) {
            this.context = context;
        }

        public boolean exportDatabase() {
            try {
                // Obtener la base de datos
                BdVentas dbHelper = new BdVentas(context);
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                // Obtener la ruta de la carpeta de archivos externos de la app
                File exportDir = context.getExternalFilesDir(null);
                if (exportDir == null) {
                    Log.e("EXPORT_ERROR", "No se pudo obtener la ruta de la carpeta de archivos externos");
                    return false;
                }

                // Nombre del archivo de exportación
                String fileName = "tienda_control_backup.db";
                File exportFile = new File(exportDir, fileName);

                // Crear el archivo
                if (!exportFile.exists()) {
                    if (!exportFile.createNewFile()) {
                        Log.e("EXPORT_ERROR", "No se pudo crear el archivo de exportación");
                        return false;
                    }
                }

                // Exportar la base de datos
                FileOutputStream outputStream = new FileOutputStream(exportFile);
                Cursor cursor = db.rawQuery("SELECT * FROM " + BdVentas.TABLE_NAME, null);
                if (cursor.moveToFirst()) {
                    // Escribir la estructura de la tabla
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        outputStream.write((cursor.getColumnName(i) + ",").getBytes());
                    }
                    outputStream.write("\n".getBytes());
                    // Escribir los datos de la tabla
                    do {
                        for (int i = 0; i < cursor.getColumnCount(); i++) {
                            outputStream.write((cursor.getString(i) + ",").getBytes());
                        }
                        outputStream.write("\n".getBytes());
                    } while (cursor.moveToNext());
                }
                outputStream.close();
                cursor.close();

                Log.d("EXPORT_DEBUG", "Base de datos exportada a: " + exportFile.getAbsolutePath());
                return true;
            } catch (IOException e) {
                Log.e("EXPORT_ERROR", "Error al exportar la base de datos: " + e.getMessage());
                return false;
            }
        }
    }
}