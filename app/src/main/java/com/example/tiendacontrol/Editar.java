package com.example.tiendacontrol;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tiendacontrol.Bd.BdVentas;
import com.example.tiendacontrol.entidades.Ventas;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Editar extends AppCompatActivity {

    EditText txtProducto, txtValor, txtDetalles, txtCantidad;
    Button btnGuarda;
    FloatingActionButton fabEditar, fabEliminar;
    boolean correcto = false;
    Ventas venta;
    int id = 0;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver);

        txtProducto = findViewById(R.id.txtProducto);
        txtValor = findViewById(R.id.txtValor);
        txtDetalles = findViewById(R.id.txtDetalles);
        txtCantidad = findViewById(R.id.txtCantidad);
        btnGuarda = findViewById(R.id.btnGuarda);
        fabEditar = findViewById(R.id.fabEditar);
        fabEditar.setVisibility(View.INVISIBLE);
        fabEliminar = findViewById(R.id.fabEliminar);
        fabEliminar.setVisibility(View.INVISIBLE);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                id = Integer.parseInt(null);
            } else {
                id = extras.getInt("ID");
            }
        } else {
            id = (int) savedInstanceState.getSerializable("ID");
        }

        final BdVentas bdVentas = new BdVentas(Editar.this);
        venta = bdVentas.verVenta(id);

        if (venta != null) {
            txtProducto.setText(venta.getProducto());
            txtValor.setText(venta.getValor());
            txtDetalles.setText(venta.getDetalles());
            txtCantidad.setText(venta.getCantidad());
        }

        btnGuarda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!txtProducto.getText().toString().equals("") && !txtValor.getText().toString().equals("")) {
                    correcto = bdVentas.editarVenta(id, txtProducto.getText().toString(), txtValor.getText().toString(), txtDetalles.getText().toString(), txtCantidad.getText().toString());

                    if(correcto){
                        Toast.makeText(Editar.this, "REGISTRO MODIFICADO", Toast.LENGTH_LONG).show();
                        verRegistro();

                    } else {
                        Toast.makeText(Editar.this, "ERROR AL MODIFICAR REGISTRO", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(Editar.this, "DEBE LLENAR LOS CAMPOS OBLIGATORIOS", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void verRegistro(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("ID", id);
        startActivity(intent);
    }

}
