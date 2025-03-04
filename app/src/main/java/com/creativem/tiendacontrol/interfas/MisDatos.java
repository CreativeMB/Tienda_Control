package com.creativem.tiendacontrol.interfas;

import android.app.Activity;
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
//    private Context context;
    public static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mis_datos);

        context = this;
        productoList = new ArrayList<>();

        adapter = new ProductoAdapter(context, productoList, this); // Initialize adapter here

        recyclerViewProductos = findViewById(R.id.recyclerViewProductos);
        recyclerViewProductos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProductos.setAdapter(adapter); // Set adapter to RecyclerView

        TextView  textVentas = findViewById(R.id.Venta); //Get TextView AFTER setContentView
        textVentas.setOnClickListener(v -> mostrarDialogoCrearProducto(null));

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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

        spinnerFiltro = findViewById(R.id.spinner_filtro); //Get Spinner AFTER setContentView
        if (spinnerFiltro != null) {
            ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                    this, R.array.filtro_opciones, android.R.layout.simple_spinner_item);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerFiltro.setAdapter(spinnerAdapter);
            spinnerFiltro.setSelection(0); // Set default selection (Día)

            spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String tipoFiltro = parent.getItemAtPosition(position).toString();
                    adapter.filtrarPorFecha(obtenerFechaSegunFiltro(tipoFiltro), tipoFiltro); // Update adapter filter
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        } else {
            Log.e("MisDatos", "spinnerFiltro is NULL! Check your layout file.");
        }
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

                // Carga los datos de Firebase (sin cambios)
                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        ProductoModel producto = data.getValue(ProductoModel.class);
                        if (producto != null) {
                            productoList.add(producto);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Si el adaptador es nulo, lo inicializamos
                if (adapter == null) {
                    adapter = new ProductoAdapter(MisDatos.this, productoList, MisDatos.this);
                    recyclerViewProductos.setAdapter(adapter);
                }

                // Aplica el filtro por defecto al cargar los productos
                String tipoFiltro = "Día"; // Este es el filtro por defecto, pero puedes cambiarlo según lo necesites.
                String fechaFiltro = obtenerFechaSegunFiltro(tipoFiltro); // Este método te da la fecha correcta para el filtro

                // Aplica el filtro sobre la lista de productos cargados
                adapter.filtrarPorFecha(fechaFiltro, tipoFiltro);

                // Calcula los totales después de aplicar el filtro
                calcularTotales(adapter.getFilteredProductList());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MisDatos.this, "❌ Error al cargar productos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("cargarProductos", "Firebase error: " + error.getMessage());
            }
        });
    }

    public static void calcularTotales(List<ProductoModel> listaFiltrada) {
        // Usamos un arreglo para almacenar los totales, de forma que puedan modificarse
        final double[] totalIngresos = {0};  // Usamos un arreglo con un solo elemento
        final double[] totalEgresos = {0};   // Usamos un arreglo con un solo elemento
        final double[] diferencia = {0};     // Usamos un arreglo con un solo elemento

        // Calcular los totales de ingresos y egresos
        for (ProductoModel producto : listaFiltrada) {
            if (producto.getValor() > 0) {
                totalIngresos[0] += producto.getValor();
            } else {
                totalEgresos[0] += producto.getValor();
            }
        }

        diferencia[0] = totalIngresos[0] + totalEgresos[0];

        // Asegurarse de que la actualización de la UI se haga en el hilo principal
        // Usar runOnUiThread si no estás en el hilo principal
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Actualiza los TextViews con los totales
                    TextView textIngresos = ((Activity) context).findViewById(R.id.textIngresos);
                    TextView textEgresos = ((Activity) context).findViewById(R.id.textEgresos);
                    TextView textDiferencia = ((Activity) context).findViewById(R.id.textDiferencia);

                    if (textIngresos != null && textEgresos != null && textDiferencia != null) {
                        textIngresos.setText("Ingresos: $" + String.format(Locale.getDefault(), "%,.2f", totalIngresos[0]));
                        textEgresos.setText("Egresos: $" + String.format(Locale.getDefault(), "%,.2f", Math.abs(totalEgresos[0])));
                        textDiferencia.setText("Diferencia: $" + String.format(Locale.getDefault(), "%,.2f", diferencia[0]));
                    } else {
                        Log.e("calcularTotales", "One or more TextViews not found!");
                    }
                }
            });
        }
    }

    private void mostrarDialogoCrearProducto(final ProductoModel productoExistente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(productoExistente == null ? "" : "");

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
                        productoList.add(0, nuevoProducto);
                        adapter.notifyItemInserted(0);
                        adapter.filtrarPorFecha(obtenerFechaSegunFiltro(adapter.getCurrentFilter()), adapter.getCurrentFilter()); //Vuelve a aplicar el filtro
                        MisDatos.calcularTotales(adapter.getFilteredProductList()); //Recalcula los totales
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
