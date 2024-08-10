package com.example.tiendacontrol.monitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tiendacontrol.R;
import com.example.tiendacontrol.adapter.DatosAdapter;
import com.example.tiendacontrol.dialogFragment.MenuDialogFragment;

import com.example.tiendacontrol.helper.BdVentas;
import com.example.tiendacontrol.model.Items;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IngresoEgreso extends AppCompatActivity {
    private RecyclerView recyclerPositivos, recyclerNegativos;
    private DatosAdapter adapterPositivos, adapterNegativos;
    private ArrayList<Items> listaArrayVentas;
    private TextView textVenta, textGasto;
    private FloatingActionButton fabMenu;
    private BdVentas bdVentas;
    private String currentDatabase; // Variable para almacenar el nombre de la base de datos
    private static final String PREFS_NAME = "TiendaControlPrefs";
    private static final String KEY_CURRENT_DATABASE = "currentDatabase";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ingreso_egreso);

        recyclerPositivos = findViewById(R.id.recyclerPositivos);
        recyclerNegativos = findViewById(R.id.recyclerNegativos);
        textVenta = findViewById(R.id.textPositivo);
        textGasto = findViewById(R.id.textNegativo);
        fabMenu = findViewById(R.id.fabMenu);

        // Inicializar la base de datos
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentDatabase = sharedPreferences.getString(KEY_CURRENT_DATABASE, "");
        bdVentas = new BdVentas(this, currentDatabase);

        // Configurar los RecyclerViews
        recyclerPositivos.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerNegativos.setLayoutManager(new GridLayoutManager(this, 2));

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
               adapterNegativos = new DatosAdapter(this, listaPositivos, bdVentas);
        adapterNegativos = new DatosAdapter(this, listaNegativos, bdVentas);
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
        // Usa el método de BdHelper para obtener todas las ventas
        List<Items> ventas = bdVentas.getItemsByDates("2023-01-01", "2024-12-31");
        return new ArrayList<>(ventas); // Convierte la lista a ArrayList si es necesario
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