package com.example.tiendacontrol;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendacontrol.Bd.BdHelper;
import com.example.tiendacontrol.Bd.BdVentas;


import com.example.tiendacontrol.adaptadores.ListaVentasAdapter;
import com.example.tiendacontrol.dialogFragment.GastoDialogFragment;
import com.example.tiendacontrol.dropbox.DropboxManager;
import com.example.tiendacontrol.entidades.Ventas;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

//    private DropboxHelper dropboxHelper;

    SearchView txtBuscar;
    RecyclerView listaVentas;
    ArrayList<Ventas> listaArrayVentas;
    ListaVentasAdapter adapter;
    FloatingActionButton fabNuevo;
    FloatingActionButton fabGasto;
    TextView textVenta, textTotal, textGasto;

    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtBuscar = findViewById(R.id.txtBuscar);
        listaVentas = findViewById(R.id.listaVentas);
        fabNuevo = findViewById(R.id.favNuevo);
        fabGasto = findViewById(R.id.favGasto);
        textVenta = findViewById(R.id.textVenta);
        textTotal = findViewById(R.id.textTotal);
        textGasto = findViewById(R.id.textGasto);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tienda Control");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listaVentas.setLayoutManager(new LinearLayoutManager(this));

        BdVentas bdVentas = new BdVentas(MainActivity.this);
        listaArrayVentas = new ArrayList<>(bdVentas.mostrarVentas());
        adapter = new ListaVentasAdapter(bdVentas.mostrarVentas());
        listaVentas.setAdapter(adapter);

//        dropboxHelper = new DropboxHelper(this);

        fabGasto.setOnClickListener(view -> {
            GastoDialogFragment dialogFragment = new GastoDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "GastoDialogFragment");
        });

        fabNuevo.setOnClickListener(view -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            com.example.tiendacontrol.IngresoDialogFragment ingresoDialogFragment = com.example.tiendacontrol.IngresoDialogFragment.newInstance();
            ingresoDialogFragment.show(fragmentManager, "ingreso_dialog");
        });

        txtBuscar.setOnQueryTextListener(this);

        calcularSumaGanancias();
        calcularSumaTotalVenta();
        calcularSumaTotalGasto();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.exportar_db) {
            // Iniciar el flujo de autenticación y subida a Dropbox
            DropboxManager dropboxManager = DropboxManager.getInstance(this);
            dropboxManager.authenticate();//Inicia la autenticación
            return true;
        } else if (id == R.id.nueva_venta) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            com.example.tiendacontrol.IngresoDialogFragment ingresoDialogFragment = com.example.tiendacontrol.IngresoDialogFragment.newInstance();
            ingresoDialogFragment.show(fragmentManager, "ingreso_dialog");
            return true;
        } else if (id == R.id.nuevo_gasto) {
            GastoDialogFragment dialogFragment = new GastoDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "GastoDialogFragment");
            return true;
        } else if (id == R.id.salir) {
            salirApp();
            return true;

        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.filtrado(newText);
        return false;
    }

    private void calcularSumaGanancias() {
        double suma = 0.0;
        for (Ventas venta : listaArrayVentas) {
            double valorVenta = venta.getValorAsDouble();
            suma += valorVenta;
        }
        int sumaFormateada = (int) suma;
        String sumaFormateadaStr = "$" + sumaFormateada;
        textTotal.setText(sumaFormateadaStr);
    }

    private void calcularSumaTotalVenta() {
        double suma = 0.0;
        for (Ventas venta : listaArrayVentas) {
            double valorVenta = venta.getValorAsDouble();
            if (valorVenta > 0) {
                suma += valorVenta;
            }
        }
        int sumaFormateada = (int) suma;
        String sumaFormateadaStr = "$" + sumaFormateada;
        textVenta.setText(sumaFormateadaStr);
    }

    private void calcularSumaTotalGasto() {
        double suma = 0.0;
        for (Ventas venta : listaArrayVentas) {
            double valorVenta = venta.getValorAsDouble();
            if (valorVenta < 0) {
                suma += valorVenta;
            }
        }
        suma = Math.abs(suma); // Asegurarse de que suma sea positiva

        int sumaFormateada = (int) suma;
        String sumaFormateadaStr;

        if (suma < 0) {
            sumaFormateadaStr = "$" + (-sumaFormateada); // Mostrar el valor positivo sin signo negativo
        } else {
            sumaFormateadaStr = "$" + sumaFormateada;
        }

        Log.d("CalcularSumaTotalGasto", "sumaFormateadaStr: " + sumaFormateadaStr); // Agrega esta línea
        textGasto.setText(sumaFormateadaStr);
    }

    private void salirApp() {
        // Cierra todas las actividades y finaliza la aplicación
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAffinity(); // Cierra todas las actividades en la pila de tareas
        System.exit(0); // Cierra el proceso de la aplicación
    }
}


//    private void exportarBaseDatos() {
//        // Nombre de tu base de datos SQLite
//        String nombreBaseDatos = "MI_contabilidad.db";
//
//        File dbFile = this.getDatabasePath(nombreBaseDatos);
//        if (dbFile.exists()) {
//            // Ejecutar AsyncTask para exportar la base de datos a Dropbox
//            new ExportarBaseDatosTask().execute(nombreBaseDatos);
//        } else {
//            Toast.makeText(MainActivity.this, "Base de datos no encontrada", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    // AsyncTask para exportar la base de datos a Dropbox
//    private class ExportarBaseDatosTask extends AsyncTask<String, Void, Boolean> {
//
//        @Override
//        protected Boolean doInBackground(String... strings) {
//            try {
//                // Llamar al método exportarBaseDatos de DropboxHelper
//                dropboxHelper.exportarBaseDatos(strings[0]);
//                return true; // Éxito
//            } catch (Exception e) {
//                e.printStackTrace();
//                return false; // Error
//            }
//        }
//
//        @Override
//        protected void onPostExecute(Boolean success) {
//            if (success) {
//                Toast.makeText(MainActivity.this, "Base de datos exportada correctamente", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(MainActivity.this, "Error al exportar base de datos", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
