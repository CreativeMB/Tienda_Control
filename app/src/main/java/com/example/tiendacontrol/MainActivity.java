package com.example.tiendacontrol;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendacontrol.Bd.BdVentas;
import com.example.tiendacontrol.adaptadores.ListaVentasAdapter;
import com.example.tiendacontrol.entidades.Ventas;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
        });;

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

}