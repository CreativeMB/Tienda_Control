package com.example.tiendacontrol;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendacontrol.Bd.BdVentas;
import com.example.tiendacontrol.adaptadores.ListaVentasAdapter;
import com.example.tiendacontrol.entidades.Ventas;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    SearchView txtBuscar;
    RecyclerView listaVentas;
    ArrayList<Ventas> listaArrayVentas;
    FloatingActionButton fabNuevo;
    ListaVentasAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        txtBuscar = findViewById(R.id.txtBuscar);
        listaVentas = findViewById(R.id.listaVentas);
        fabNuevo = findViewById(R.id.favNuevo);
        listaVentas.setLayoutManager(new LinearLayoutManager(this));

       BdVentas dbContactos = new BdVentas(MainActivity.this);

        listaArrayVentas = new ArrayList<>();

        adapter = new ListaVentasAdapter(dbContactos.mostrarVentas());
        listaVentas.setAdapter(adapter);

        fabNuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nuevoRegistro();
            }
        });

        txtBuscar.setOnQueryTextListener(this);
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_principal, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menuNuevo:
//                nuevoRegistro();
//                return true;
//
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    private void nuevoRegistro(){
        Intent intent = new Intent(this, Nuevo.class);
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


}