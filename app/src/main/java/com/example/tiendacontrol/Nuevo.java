package com.example.tiendacontrol;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tiendacontrol.Bd.BdVentas;

public class Nuevo extends AppCompatActivity {

    EditText txtProducto, txtValor, txtDetalles, txtCantidad;
    Button btnGuarda;
    int id = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.nuevo);

        txtProducto = findViewById(R.id.txtProducto);
        txtValor = findViewById(R.id.txtValor);
        txtDetalles = findViewById(R.id.txtDetalles);
        txtCantidad = findViewById(R.id.txtCantidad);
        btnGuarda = findViewById(R.id.btnGuarda);

        btnGuarda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!txtProducto.getText().toString().equals("") && !txtValor.getText().toString().equals("") && !txtDetalles.getText().toString().equals("") && !txtCantidad.getText().toString().equals("")) {

                    BdVentas bdVentas = new BdVentas(Nuevo.this);
                    long id = bdVentas.insertarVenta(txtProducto.getText().toString(), txtValor.getText().toString(), txtDetalles.getText().toString(), txtCantidad.getText().toString());

                    if (id > 0) {
                        Toast.makeText(Nuevo.this, "REGISTRO GUARDADO", Toast.LENGTH_LONG).show();
                        limpiar();
                        verRegistro();
                    } else {
                        Toast.makeText(Nuevo.this, "ERROR AL GUARDAR REGISTRO", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(Nuevo.this, "DEBE LLENAR LOS CAMPOS OBLIGATORIOS", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void limpiar() {
        txtProducto.setText("");
        txtValor.setText("");
        txtDetalles.setText("");
        txtCantidad.setText("");

    }
    private void verRegistro(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("ID", id);
        startActivity(intent);
    }
}