package com.example.tiendacontrol.monitor;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.tiendacontrol.dialogFragment.MenuDialogFragment;
import com.example.tiendacontrol.helper.BdVentas;
import com.example.tiendacontrol.R;
import com.example.tiendacontrol.dialogFragment.EditarDialogFragment;
import com.example.tiendacontrol.model.Items;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class VerActivity extends AppCompatActivity {
    EditText txtProducto, txtValor, txtDetalles, txtCantidad;
    FloatingActionButton fabEditar, fabEliminar, fabMenu;
    Button btnGuarda;
    Items venta;
    int id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver);

        txtProducto = findViewById(R.id.txtProducto);
        txtValor = findViewById(R.id.txtValor);
        txtDetalles = findViewById(R.id.txtDetalles);
        txtCantidad = findViewById(R.id.txtCantidad);
        fabEditar = findViewById(R.id.fabEditar);
        fabEliminar = findViewById(R.id.fabEliminar);
        btnGuarda = findViewById(R.id.btnGuarda);
        btnGuarda.setVisibility(View.INVISIBLE);
        fabMenu = findViewById(R.id.fabMenu);



        fabMenu.setOnClickListener(view -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            MenuDialogFragment menuDialogFragment = MenuDialogFragment.newInstance();
            menuDialogFragment.show(fragmentManager, "servicios_dialog");
        });

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                id = -1; // Cambiar a un valor de ID predeterminado o manejar el caso en que no se proporciona ID
            } else {
                id = extras.getInt("ID");
            }
        } else {
            id = (int) savedInstanceState.getSerializable("ID");
        }

        final BdVentas bdVentas = new BdVentas(VerActivity.this);
        venta = bdVentas.verVenta(id);

        if (venta != null) {
            txtProducto.setText(venta.getProducto());
            // Formatear valor para eliminar decimales y signo negativo
            double valor = venta.getValor();
            if (valor < 0) {
                valor = -valor; // Eliminar signo negativo
            }
            txtValor.setText(String.format("%.0f", valor));
            txtDetalles.setText(venta.getDetalles());
            txtCantidad.setText(String.valueOf(venta.getCantidad())); // Asegúrate de que getCantidad devuelve int o String
            txtProducto.setInputType(InputType.TYPE_NULL);
            txtValor.setInputType(InputType.TYPE_NULL);
            txtDetalles.setInputType(InputType.TYPE_NULL);
            txtCantidad.setInputType(InputType.TYPE_NULL);
        }

        fabEditar.setOnClickListener(view -> {
            // Mostrar EditarDialogFragment al hacer clic en fabEditar
            EditarDialogFragment dialogFragment = EditarDialogFragment.newInstance(id);
            dialogFragment.show(getSupportFragmentManager(), "EditarDialogFragment");
        });

        fabEliminar.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(VerActivity.this);
            builder.setMessage("¿Desea eliminar Item?")
                    .setPositiveButton("SI", (dialogInterface, i) -> {
                        if (bdVentas.eliminarVenta(id)) {
                            lista();
                        }
                    })
                    .setNegativeButton("NO", (dialogInterface, i) -> {
                        // No hacer nada si se selecciona NO
                    }).show();
        });
    }

    private void lista() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}