package com.example.tiendacontrol.monitor;
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
    EditText txtProducto, txtValor, txtDetalles, txtCantidad; // Campos de texto para mostrar los detalles del ítem
    FloatingActionButton fabEditar, fabEliminar, fabMenu; // Botones flotantes para editar, eliminar y abrir el menú
    Button btnGuarda; // Botón para guardar, que está oculto en esta actividad
    Items venta; // Objeto para almacenar la venta que se va a mostrar
    int id = 0; // ID de la venta que se va a mostrar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver); // Establece el diseño de la actividad

        // Inicializa las vistas
        txtProducto = findViewById(R.id.txtProducto);
        txtValor = findViewById(R.id.txtValor);
        txtDetalles = findViewById(R.id.txtDetalles);
        txtCantidad = findViewById(R.id.txtCantidad);
        fabEditar = findViewById(R.id.fabEditar);
        fabEliminar = findViewById(R.id.fabEliminar);
        btnGuarda = findViewById(R.id.btnGuarda);
        btnGuarda.setVisibility(View.INVISIBLE); // Oculta el botón de guardar
        fabMenu = findViewById(R.id.fabMenu);

        // Configura el botón flotante para mostrar el menú cuando se hace clic
        fabMenu.setOnClickListener(view -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            MenuDialogFragment menuDialogFragment = MenuDialogFragment.newInstance();
            menuDialogFragment.show(fragmentManager, "servicios_dialog");
        });

        // Obtiene el ID del ítem desde el Intent o el estado guardado
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                id = -1; // Asigna un valor predeterminado en caso de que no se proporcione el ID
            } else {
                id = extras.getInt("ID");
            }
        } else {
            id = (int) savedInstanceState.getSerializable("ID");
        }

        // Crea una instancia de BdVentas y obtiene la venta con el ID proporcionado
        final BdVentas bdVentas = new BdVentas(VerActivity.this);
        venta = bdVentas.verVenta(id);

        // Si la venta existe, muestra los detalles en los campos de texto
        if (venta != null) {
            txtProducto.setText(venta.getProducto());
            // Formatea el valor para eliminar decimales y el signo negativo
            double valor = venta.getValor();
            if (valor < 0) {
                valor = -valor; // Elimina el signo negativo
            }
            txtValor.setText(String.format("%.0f", valor));
            txtDetalles.setText(venta.getDetalles());
            txtCantidad.setText(String.valueOf(venta.getCantidad())); // Asegúrate de que getCantidad devuelve int o String
            txtProducto.setInputType(InputType.TYPE_NULL); // Hace que los campos de texto sean solo lectura
            txtValor.setInputType(InputType.TYPE_NULL);
            txtDetalles.setInputType(InputType.TYPE_NULL);
            txtCantidad.setInputType(InputType.TYPE_NULL);
        }

        // Configura el botón flotante de edición para mostrar el EditarDialogFragment cuando se hace clic
        fabEditar.setOnClickListener(view -> {
            EditarDialogFragment dialogFragment = EditarDialogFragment.newInstance(id);
            dialogFragment.show(getSupportFragmentManager(), "EditarDialogFragment");
        });

        // Configura el botón flotante de eliminación para mostrar un diálogo de confirmación al hacer clic
        fabEliminar.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(VerActivity.this);
            builder.setMessage("¿Desea eliminar Item?")
                    .setPositiveButton("SI", (dialogInterface, i) -> {
                        if (bdVentas.eliminarVenta(id)) {
                            lista(); // Llama al método lista si la venta se elimina correctamente
                        }
                    })
                    .setNegativeButton("NO", (dialogInterface, i) -> {
                        // No hacer nada si se selecciona NO
                    }).show();
        });
    }

    // Método para iniciar la actividad MainActivity
    private void lista() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}