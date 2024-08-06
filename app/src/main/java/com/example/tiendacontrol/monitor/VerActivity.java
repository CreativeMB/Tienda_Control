package com.example.tiendacontrol.monitor;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
    int id = -1;
    String currentDatabase;

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
            if (extras != null) {
                id = extras.getInt("ID", -1);
                currentDatabase = extras.getString("databaseName");
            }
        } else {
            id = savedInstanceState.getInt("ID", -1);
            currentDatabase = savedInstanceState.getString("databaseName");
        }

        Log.d("VerActivity", "onCreate() - ID recibido: " + id);
        Log.d("VerActivity", "onCreate() - Base de datos: " + currentDatabase);

        if (id == -1 || currentDatabase == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("No se pudo recuperar los datos del ítem.")
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .show();
            return;
        }

        final BdVentas bdVentas = new BdVentas(VerActivity.this, currentDatabase);
        venta = bdVentas.verVenta(id);

        if (venta != null) {
            txtProducto.setText(venta.getProducto());
            double valor = venta.getValor();
            txtValor.setText(String.valueOf(valor));
            txtDetalles.setText(venta.getDetalles());
            txtCantidad.setText(String.valueOf(venta.getCantidad()));

            txtProducto.setInputType(InputType.TYPE_NULL);
            txtValor.setInputType(InputType.TYPE_NULL);
            txtDetalles.setInputType(InputType.TYPE_NULL);
            txtCantidad.setInputType(InputType.TYPE_NULL);
        } else {
            Log.e("VerActivity", "Error: venta con ID " + id + " no encontrada.");
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("No se encontraron los datos del ítem.")
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .show();
        }

        fabEditar.setOnClickListener(view -> {
            EditarDialogFragment dialogFragment = EditarDialogFragment.newInstance(id, currentDatabase);
            dialogFragment.show(getSupportFragmentManager(), "EditarDialogFragment");
        });

        fabEliminar.setOnClickListener(view -> {
            new AlertDialog.Builder(VerActivity.this)
                    .setMessage("¿Desea eliminar el ítem?")
                    .setPositiveButton("SI", (dialogInterface, i) -> {
                        if (bdVentas.eliminarVenta(id)) {
                            lista();
                        } else {
                            new AlertDialog.Builder(VerActivity.this)
                                    .setTitle("Error")
                                    .setMessage("No se pudo eliminar el ítem.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    })
                    .setNegativeButton("NO", null)
                    .show();
        });
    }

    private void lista() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("databaseName", currentDatabase);
        startActivity(intent);
        finish();
    }
}