package com.example.tiendacontrol.monitor;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tiendacontrol.R;
import com.example.tiendacontrol.adapter.ListaVentasAdapter;
import com.example.tiendacontrol.dialogFragment.MenuDialogFragment;
import com.example.tiendacontrol.helper.BdHelper;
import com.example.tiendacontrol.model.Items;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class IngresoEgreso extends AppCompatActivity {
        private RecyclerView recyclerPositivos, recyclerNegativos;
        private ListaVentasAdapter adapterPositivos, adapterNegativos;
        private ArrayList<Items> listaArrayVentas;
        private TextView textVenta, textGasto;
    private FloatingActionButton  fabMenu;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.ingreso_egreso);

            recyclerPositivos = findViewById(R.id.recyclerPositivos);
            recyclerNegativos = findViewById(R.id.recyclerNegativos);
            textVenta = findViewById(R.id.textPositivo);
            textGasto = findViewById(R.id.textNegativo);
            fabMenu = findViewById(R.id.fabMenu);
            // Configurar los RecyclerViews
            recyclerPositivos.setLayoutManager(new LinearLayoutManager(this));
            recyclerNegativos.setLayoutManager(new LinearLayoutManager(this));

            // Obtener la lista original de ventas
            listaArrayVentas = obtenerListaVentas();

            // Filtrar elementos positivos y negativos
            ArrayList<Items> listaPositivos = new ArrayList<>();
            ArrayList<Items> listaNegativos = new ArrayList<>();

            for (Items venta : listaArrayVentas) {
                if (venta.getValorAsDouble() >= 0) {
                    listaPositivos.add(venta);
                } else {
                    listaNegativos.add(venta);
                }
            }

            // Configurar los adaptadores
            adapterPositivos = new ListaVentasAdapter(listaPositivos);
            adapterNegativos = new ListaVentasAdapter(listaNegativos);

            // Asignar los adaptadores a los RecyclerViews
            recyclerPositivos.setAdapter(adapterPositivos);
            recyclerNegativos.setAdapter(adapterNegativos);
            fabMenu.setOnClickListener(view -> {
                FragmentManager fragmentManager = getSupportFragmentManager();
                MenuDialogFragment menuDialogFragment = MenuDialogFragment.newInstance();
                menuDialogFragment.show(fragmentManager, "servicios_dialog");
            });

            // Calcular y mostrar las sumas totales
            calcularSumaTotalVenta();
            calcularSumaTotalGasto();
        }

        private ArrayList<Items> obtenerListaVentas() {
            ArrayList<Items> listaVentas = new ArrayList<>();
            BdHelper bdHelper = new BdHelper(this);
            SQLiteDatabase db = bdHelper.getReadableDatabase();

            Cursor cursor = db.rawQuery("SELECT * FROM " + BdHelper.TABLE_VENTAS, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Items venta = new Items();
                    venta.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    venta.setProducto(cursor.getString(cursor.getColumnIndex("producto")));
                    venta.setValor(cursor.getDouble(cursor.getColumnIndex("valor"))); // Utilizar setValor con double
                    venta.setDetalles(cursor.getString(cursor.getColumnIndex("detalles")));
                    venta.setCantidad(cursor.getInt(cursor.getColumnIndex("cantidad")));
                    venta.setFechaRegistro(cursor.getString(cursor.getColumnIndex("fecha_registro")));

                    listaVentas.add(venta);
                }
                cursor.close();
            }
            db.close();

            return listaVentas;
        }

        private void calcularSumaTotalVenta() {
            double suma = 0.0;
            for (Items venta : listaArrayVentas) {
                double valorVenta = venta.getValorAsDouble();
                if (valorVenta > 0) {
                    suma += valorVenta;
                }
            }

            // Redondear suma a valor entero
            long sumaEntera = Math.round(suma);

            // Formatear la suma como moneda colombiana sin decimales
            String sumaFormateadaStr = NumberFormat.getCurrencyInstance(new Locale("es", "CO")).format(sumaEntera);
            // Eliminar decimales si hay .00
            sumaFormateadaStr = sumaFormateadaStr.replaceAll("[,.]00$", "");

            textVenta.setText(sumaFormateadaStr);
        }

        private void calcularSumaTotalGasto() {
            double suma = 0.0;
            for (Items venta : listaArrayVentas) {
                double valorVenta = venta.getValorAsDouble();
                if (valorVenta < 0) {
                    suma += valorVenta;
                }
            }

            // Asegurarse de que suma sea positiva
            suma = Math.abs(suma);

            // Redondear suma a valor entero
            long sumaEntera = Math.round(suma);

            // Formatear la suma como moneda colombiana sin decimales
            String sumaFormateadaStr = NumberFormat.getCurrencyInstance(new Locale("es", "CO")).format(sumaEntera);
            // Eliminar decimales si hay .00
            sumaFormateadaStr = sumaFormateadaStr.replaceAll("[,.]00$", "");

            textGasto.setText(sumaFormateadaStr);
        }
    }