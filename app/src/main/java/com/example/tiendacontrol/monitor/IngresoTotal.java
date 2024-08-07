package com.example.tiendacontrol.monitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tiendacontrol.R;
import com.example.tiendacontrol.adapter.BaseDatosAdapter;
import com.example.tiendacontrol.dialogFragment.MenuDialogFragment;
import com.example.tiendacontrol.helper.BdHelper;
import com.example.tiendacontrol.model.Items;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class IngresoTotal extends AppCompatActivity {
    private BdHelper bdHelper;
    private RecyclerView recyclerPositivos;
    private BaseDatosAdapter adapterPositivos;
    private ArrayList<Items> listaArrayVentas;
    private TextView textVenta;
    private FloatingActionButton fabMenu;
    private String currentDatabase; // Variable para almacenar el nombre de la base de datos
    private static final String PREFS_NAME = "TiendaControlPrefs";
    private static final String KEY_CURRENT_DATABASE = "currentDatabase";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ingreso_total); // Asegúrate de que el nombre del layout sea correcto

        recyclerPositivos = findViewById(R.id.recyclerPositivos);
        textVenta = findViewById(R.id.textPositivo);
        fabMenu = findViewById(R.id.fabMenu);

        // Configurar el RecyclerView
        recyclerPositivos.setLayoutManager(new GridLayoutManager(this, 2));

        // Inicializar la base de datos
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentDatabase = sharedPreferences.getString(KEY_CURRENT_DATABASE, "");
        bdHelper = new BdHelper(this, currentDatabase);

        // Obtener la lista original de ventas
        listaArrayVentas = obtenerListaVentas(bdHelper);

        // Filtrar elementos positivos
        ArrayList<Items> listaPositivos = new ArrayList<>();
        for (Items venta : listaArrayVentas) {
            if (venta.getValorAsDouble() >= 0) {
                listaPositivos.add(venta);
            }
        }

        // Configurar el adaptador
        adapterPositivos = new BaseDatosAdapter(listaPositivos);
        recyclerPositivos.setAdapter(adapterPositivos);

        fabMenu.setOnClickListener(view -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            MenuDialogFragment menuDialogFragment = MenuDialogFragment.newInstance();
            menuDialogFragment.show(fragmentManager, "servicios_dialog");
        });

        // Calcular y mostrar la suma total de ventas
        calcularSumaTotalVenta();
    }

    private ArrayList<Items> obtenerListaVentas(BdHelper bdHelper) {
        ArrayList<Items> listaVentas = new ArrayList<>();
        // Obtener la fecha de inicio y fin para la consulta (puedes ajustar esto según tus necesidades)
        String startDate = "2024-01-01"; // Ejemplo de fecha de inicio
        String endDate = "2024-12-31";   // Ejemplo de fecha de fin

        // Obtener los resultados filtrados por fechas
        listaVentas = (ArrayList<Items>) bdHelper.getItemsByDates(startDate, endDate);

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
}