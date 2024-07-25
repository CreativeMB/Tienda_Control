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
import com.example.tiendacontrol.adapter.BaseDatosAdapter;
import com.example.tiendacontrol.dialogFragment.MenuDialogFragment;
import com.example.tiendacontrol.helper.BdHelper;
import com.example.tiendacontrol.model.Items;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class IngresoTotal extends AppCompatActivity {



    private RecyclerView recyclerPositivos;
    private BaseDatosAdapter adapterPositivos;
    private ArrayList<Items> listaArrayVentas;
    private TextView textVenta;
    private FloatingActionButton fabMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ingreso_total); // Asegúrate de que el nombre del layout sea correcto

        recyclerPositivos = findViewById(R.id.recyclerPositivos);
        textVenta = findViewById(R.id.textPositivo);
        fabMenu = findViewById(R.id.fabMenu);

        // Configurar el RecyclerView
        recyclerPositivos.setLayoutManager(new LinearLayoutManager(this));

        // Obtener la lista original de ventas
        listaArrayVentas = obtenerListaVentas();

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

}
