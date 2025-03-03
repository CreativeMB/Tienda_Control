package com.creativem.tiendacontrol.interfas;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.creativem.tiendacontrol.R;
import com.creativem.tiendacontrol.model.ProductoModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MisDatos extends AppCompatActivity implements ProductoAdapter.OnProductoClickListener {
    private RecyclerView recyclerViewProductos;
    private ProductoAdapter adapter;
    private List<ProductoModel> productoList;
    private FirebaseHelper firebaseHelper;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "CodePrefs";
    private static final String KEY_CURRENT_DATABASE = "currentDatabase";

    private String userId;
    private String baseDatosSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mis_datos);

        recyclerViewProductos = findViewById(R.id.recyclerViewProductos);
        recyclerViewProductos.setLayoutManager(new LinearLayoutManager(this));

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        productoList = new ArrayList<>();

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        baseDatosSeleccionada = getIntent().getStringExtra("databaseName");
        if (baseDatosSeleccionada == null || baseDatosSeleccionada.isEmpty()) {
            baseDatosSeleccionada = obtenerBaseDatosSeleccionada();
        }

        if (baseDatosSeleccionada.isEmpty()) {
            Toast.makeText(this, "⚠️ No hay base de datos seleccionada.", Toast.LENGTH_LONG).show();
        } else {
            guardarBaseDatosSeleccionada(baseDatosSeleccionada);
            firebaseHelper = new FirebaseHelper(userId, baseDatosSeleccionada);
            cargarProductos();
        }

        TextView textVentas = findViewById(R.id.Venta);
        textVentas.setOnClickListener(v -> mostrarDialogoCrearProducto(null));
    }

    private void guardarBaseDatosSeleccionada(String nombreBase) {
        sharedPreferences.edit().putString(KEY_CURRENT_DATABASE, nombreBase).apply();
    }

    private String obtenerBaseDatosSeleccionada() {
        return sharedPreferences.getString(KEY_CURRENT_DATABASE, "");
    }

    @Override
    public void onEditClick(ProductoModel producto) {
        mostrarDialogoCrearProducto(producto);
    }

    @Override
    public void onDeleteClick(ProductoModel producto) {
        firebaseHelper.eliminarProducto(producto.getId(), (error, ref) -> {
            if (error == null) {
                Toast.makeText(MisDatos.this, "✅ Producto eliminado", Toast.LENGTH_SHORT).show();
                cargarProductos();
            } else {
                Toast.makeText(MisDatos.this, "❌ Error al eliminar producto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarProductos() {
        firebaseHelper.obtenerProductos(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productoList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ProductoModel producto = data.getValue(ProductoModel.class);
                    if (producto != null) {
                        productoList.add(producto);
                    }
                }
                if (adapter == null) {
                    adapter = new ProductoAdapter(MisDatos.this, productoList, MisDatos.this);
                    recyclerViewProductos.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MisDatos.this, "❌ Error al cargar productos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoCrearProducto(final ProductoModel productoExistente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(productoExistente == null ? "Nuevo Producto" : "Editar Producto");

        View vista = getLayoutInflater().inflate(R.layout.productos_nuevos, null);
        builder.setView(vista);

        EditText inputNombre = vista.findViewById(R.id.inputNombre);
        EditText inputPrecio = vista.findViewById(R.id.inputPrecio);
        EditText inputNota = vista.findViewById(R.id.inputNota);
        Switch switchTipo = vista.findViewById(R.id.switchTipo);

        if (productoExistente != null) {
            inputNombre.setText(productoExistente.getNombre());
            inputPrecio.setText(String.valueOf(Math.abs(productoExistente.getPrecio())));
            inputNota.setText(productoExistente.getNota());
            switchTipo.setChecked(productoExistente.getPrecio() < 0);
        } else {
            switchTipo.setChecked(false);
        }

        builder.setPositiveButton(productoExistente == null ? "Guardar" : "Actualizar", null);
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nombre = inputNombre.getText().toString().trim();
            String precioStr = inputPrecio.getText().toString().trim();
            String nota = inputNota.getText().toString().trim();

            if (nombre.isEmpty() || precioStr.isEmpty()) {
                Toast.makeText(this, "⚠️ Nombre y precio son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            double precio = Double.parseDouble(precioStr);
            if (switchTipo.isChecked()) {
                precio = -Math.abs(precio);
            }

            if (productoExistente == null) { // **Creación de producto nuevo**
                String id = firebaseHelper.getDatabaseReference().push().getKey(); // Genera ID único

                if (id != null) { // Verifica que el ID no sea null
                    ProductoModel nuevoProducto = new ProductoModel(id, nombre, precio, nota, obtenerFechaHoraActual());

                    firebaseHelper.agregarProducto(id, nuevoProducto, (error, ref) -> {
                        if (error == null) {
                            dialog.dismiss();
                            cargarProductos(); // Refresca la lista de productos
                        } else {
                            Toast.makeText(MisDatos.this, "❌ Error al agregar producto", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "⚠️ No se pudo generar un ID único", Toast.LENGTH_SHORT).show();
                }

            } else { // **Edición de producto existente**
                firebaseHelper.editarProducto(
                        productoExistente.getId(),
                        productoExistente.getNombre(),
                        productoExistente.getPrecio(),
                        productoExistente.getNota(),
                        (error, ref) -> {
                            if (error == null) {
                                dialog.dismiss();
                                cargarProductos();
                            } else {
                                Toast.makeText(MisDatos.this, "❌ Error al actualizar producto", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }
        });
    }

    private String obtenerFechaHoraActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

}
