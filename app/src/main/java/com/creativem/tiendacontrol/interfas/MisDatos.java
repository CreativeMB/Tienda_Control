package com.creativem.tiendacontrol.interfas;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

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

    private static final String PREFS_NAME = "CodePrefs";
    private static final String KEY_CURRENT_DATABASE = "currentDatabase";
    private SharedPreferences sharedPreferences;

    private String baseDatosSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mis_datos);

        recyclerViewProductos = findViewById(R.id.recyclerViewProductos);
        recyclerViewProductos.setLayoutManager(new LinearLayoutManager(this));

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        firebaseHelper = new FirebaseHelper();
        productoList = new ArrayList<>();

        // Obtener la base de datos seleccionada del Intent o SharedPreferences
        baseDatosSeleccionada = getIntent().getStringExtra("databaseName");
        if (baseDatosSeleccionada == null || baseDatosSeleccionada.isEmpty()) {
            baseDatosSeleccionada = obtenerBaseDatosSeleccionada();
        }

        if (baseDatosSeleccionada.isEmpty()) {
            Toast.makeText(this, "⚠️ No hay base de datos seleccionada.", Toast.LENGTH_LONG).show();
        } else {
            guardarBaseDatosSeleccionada(baseDatosSeleccionada);
            cargarProductos();
        }

        // Configurar el botón "Ventas" para abrir el diálogo
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
        if (baseDatosSeleccionada.isEmpty()) {
            Toast.makeText(this, "⚠️ No hay base de datos seleccionada.", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseHelper.eliminarProducto(baseDatosSeleccionada, producto.getId(), (error, ref) -> {
            if (error == null) {
                Toast.makeText(this, "✅ Producto eliminado", Toast.LENGTH_SHORT).show();
                cargarProductos();
            } else {
                Toast.makeText(this, "❌ Error al eliminar producto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarProductos() {
        Log.d("BaseDatos", "Base de datos seleccionada: " + baseDatosSeleccionada);

        if (baseDatosSeleccionada.isEmpty()) {
            Toast.makeText(this, "⚠️ No hay base de datos seleccionada.", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseHelper.obtenerProductos(baseDatosSeleccionada, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productoList.clear();
                Log.d("BaseDatos", "Productos encontrados: " + snapshot.getChildrenCount());

                for (DataSnapshot data : snapshot.getChildren()) {
                    ProductoModel producto = data.getValue(ProductoModel.class);
                    Log.d("BaseDatos", "Producto cargado: " + producto.getNombre()); // Asegúrate de que `getNombre()` existe
                    productoList.add(producto);
                }

                if (adapter == null) {
                    adapter = new ProductoAdapter(MisDatos.this, productoList, MisDatos.this);
                    recyclerViewProductos.setAdapter(adapter);
                    Log.d("BaseDatos", "Adapter asignado al RecyclerView");
                } else {
                    adapter.notifyDataSetChanged();
                    Log.d("BaseDatos", "Adapter actualizado con nuevos datos");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("BaseDatos", "Error al cargar productos: " + error.getMessage());
                Toast.makeText(MisDatos.this, "❌ Error al cargar productos", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void mostrarDialogoCrearProducto(final ProductoModel productoExistente) {
        if (baseDatosSeleccionada.isEmpty()) {
            Toast.makeText(this, "⚠️ No hay base de datos seleccionada.", Toast.LENGTH_SHORT).show();
            return;
        }

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

        builder.setPositiveButton(productoExistente == null ? "Guardar" : "Actualizar", (dialog, which) -> {
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

            if (productoExistente == null) {
                String idProducto = firebaseHelper.obtenerReferenciaProductos(baseDatosSeleccionada).push().getKey();
                String fechaHoraActual = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                ProductoModel nuevoProducto = new ProductoModel(idProducto, nombre, precio, nota, fechaHoraActual);

                firebaseHelper.agregarProducto(baseDatosSeleccionada, idProducto, nuevoProducto, (error, ref) -> {
                    if (error == null) {
                        cargarProductos();
                        Toast.makeText(this, "✅ Producto guardado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "❌ Error al guardar producto", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                firebaseHelper.editarProducto(baseDatosSeleccionada, productoExistente.getId(), nombre, precio, nota, (error, ref) -> {
                    if (error == null) {
                        cargarProductos();
                        Toast.makeText(this, "✅ Producto actualizado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "❌ Error al actualizar producto", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
}
