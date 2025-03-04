package com.creativem.tiendacontrol.interfas;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.creativem.tiendacontrol.R;
import com.creativem.tiendacontrol.helper.PuntoMil;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

    private Spinner spinnerFiltro;
    private ProductoAdapter miAdaptador;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mis_datos);


        context = this; // Ahora Android puede instanciarla sin problemas
        productoList = new ArrayList<>();

        miAdaptador = new ProductoAdapter(context, productoList, new ProductoAdapter.OnProductoClickListener() {
            @Override
            public void onEditClick(ProductoModel producto) {

            }

            @Override
            public void onDeleteClick(ProductoModel producto) {

            }

        });



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

        Spinner spinnerFiltro = findViewById(R.id.spinner_filtro);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.filtro_opciones, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltro.setAdapter(adapter);

        spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tipoFiltro = parent.getItemAtPosition(position).toString();
                String fechaSeleccionada = obtenerFechaSegunFiltro(tipoFiltro);
                if (miAdaptador != null) {
                    miAdaptador.filtrarPorFecha(fechaSeleccionada, tipoFiltro);
                } else {
                    Log.e("Filtro", "El adaptador no está inicializado.");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


    }
    private String obtenerFechaSegunFiltro(String tipoFiltro) {
        SimpleDateFormat dateFormatDia = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dateFormatMes = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat dateFormatAño = new SimpleDateFormat("yyyy", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();

        switch (tipoFiltro) {
            case "Día":
                return dateFormatDia.format(calendar.getTime());

            case "Semana":
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                String inicioSemana = dateFormatDia.format(calendar.getTime());
                calendar.add(Calendar.DATE, 6);
                String finSemana = dateFormatDia.format(calendar.getTime());
                return inicioSemana + " - " + finSemana;

            case "Mes":
                return dateFormatMes.format(calendar.getTime());

            case "Año":
                return dateFormatAño.format(calendar.getTime());

            default:
                return "";
        }
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
                double totalIngresos = 0;
                double totalEgresos = 0;

                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        // Intenta convertir cada entrada en un ProductoModel
                        ProductoModel producto = data.getValue(ProductoModel.class);

                        if (producto != null) {
                            productoList.add(0, producto);

                            if (producto.getValor() > 0) {
                                totalIngresos += producto.getValor();
                            } else {
                                totalEgresos += producto.getValor();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace(); // Para depuración, muestra errores en consola
                    }
                }

                double diferencia = totalIngresos + totalEgresos;

                // Actualizar los TextView con los valores calculados
                TextView textIngresos = findViewById(R.id.textIngresos);
                TextView textEgresos = findViewById(R.id.textEgresos);
                TextView textDiferencia = findViewById(R.id.textDiferencia);

                textIngresos.setText("Ingresos: $" + String.format(Locale.getDefault(), "%,.2f", totalIngresos));
                textEgresos.setText("Egresos: $" + String.format(Locale.getDefault(), "%,.2f", Math.abs(totalEgresos)));
                textDiferencia.setText("Diferencia: $" + String.format(Locale.getDefault(), "%,.2f", diferencia));

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

        EditText inputValor = vista.findViewById(R.id.inputValor);
        EditText inputNombre = vista.findViewById(R.id.inputNombre);
        EditText inputNota = vista.findViewById(R.id.inputNota);
        Switch switchTipo = vista.findViewById(R.id.switchTipo);

        inputNombre.setImeOptions(EditorInfo.IME_ACTION_DONE);

        final AlertDialog dialog = builder.create();

        inputNombre.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                guardarActualizarProducto(dialog, inputNombre, inputValor, inputNota, switchTipo, productoExistente);
            }
            return false;
        });

        inputValor.addTextChangedListener(new TextWatcher() {
            private boolean isEditing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) return;
                isEditing = true;

                String originalString = s.toString().replaceAll("[^\\d]", "");
                if (!originalString.isEmpty()) {
                    try {
                        long value = Long.parseLong(originalString);
                        String formattedString = PuntoMil.getFormattedNumber(value); //  Asumiendo que PuntoMil existe
                        inputValor.setText(formattedString);
                        inputValor.setSelection(formattedString.length());
                    } catch (NumberFormatException e) {
                        inputValor.setText("");
                    }
                }
                isEditing = false;
            }
        });

        builder.setPositiveButton(productoExistente == null ? "Guardar" : "Actualizar", (dialogInterface, which) -> {
            guardarActualizarProducto(dialog, inputNombre, inputValor, inputNota, switchTipo, productoExistente);
        });

        builder.setNegativeButton("Cancelar", (dialogInterface, which) -> {}); // No necesita acción aquí

        dialog.show();

        if (productoExistente != null) {
            inputNombre.setText(productoExistente.getNombre());
            long valorEntero = (long) Math.abs(productoExistente.getValor());
            inputValor.setText(String.valueOf(valorEntero));
            inputNota.setText(productoExistente.getNota());
            switchTipo.setChecked(productoExistente.getValor() < 0);
        } else {
            switchTipo.setChecked(false);
        }
    }


    private void guardarActualizarProducto(AlertDialog dialog, EditText inputNombre, EditText inputValor, EditText inputNota, Switch switchTipo, ProductoModel productoExistente) {
        String nombre = inputNombre.getText().toString().trim();
        String nota = inputNota.getText().toString().trim();
        String valorStr = inputValor.getText().toString().trim().replaceAll("[^\\d]", ""); // Solo números

        if (nombre.isEmpty() || valorStr.isEmpty()) {
            Toast.makeText(this, "⚠️ Nombre y valor son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        long valor;
        try {
            valor = Long.parseLong(valorStr); // Convertir directamente sin usar decimales
        } catch (NumberFormatException e) {
            Toast.makeText(this, "⚠️ Valor no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (switchTipo.isChecked()) {
            valor = -Math.abs(valor); // Asegurar que sea negativo si corresponde
        }

        if (productoExistente == null) { // **Creación de producto nuevo**
            String id = firebaseHelper.getDatabaseReference().push().getKey(); // Genera ID único

            if (id != null) {
                String fechaHora = new SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

                ProductoModel nuevoProducto = new ProductoModel(id, nombre, valor, nota, fechaHora);

                firebaseHelper.agregarProducto(id, nuevoProducto, (error, ref) -> {
                    if (error == null) {
                        dialog.dismiss();
                        cargarProductos();
                    } else {
                        Toast.makeText(MisDatos.this, "❌ Error al agregar producto", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "⚠️ No se pudo generar un ID único", Toast.LENGTH_SHORT).show();
            }

        } else { // **Edición de producto existente**
            productoExistente.setNombre(nombre);
            productoExistente.setValor(valor);
            productoExistente.setNota(nota);

            firebaseHelper.editarProducto(
                    productoExistente.getId(),
                    nombre,
                    valor,
                    nota,
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
    }




}
