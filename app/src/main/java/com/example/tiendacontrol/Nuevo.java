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

    EditText txtNombre, txtTelefono, txtCorreoElectronico;
    Button btnGuarda;
    int id = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.nuevo);

        txtNombre = findViewById(R.id.txtNombre);
        txtTelefono = findViewById(R.id.txtTelefono);
        txtCorreoElectronico = findViewById(R.id.txtCorreoElectronico);
        btnGuarda = findViewById(R.id.btnGuarda);

        btnGuarda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!txtNombre.getText().toString().equals("") && !txtTelefono.getText().toString().equals("")) {

                    BdVentas dbContactos = new BdVentas(Nuevo.this);
                    long id = dbContactos.insertarVenta(txtNombre.getText().toString(), txtTelefono.getText().toString(), txtCorreoElectronico.getText().toString());

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
        txtNombre.setText("");
        txtTelefono.setText("");
        txtCorreoElectronico.setText("");

    }
    private void verRegistro(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("ID", id);
        startActivity(intent);
    }
}