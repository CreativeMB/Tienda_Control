package com.creativem.tiendacontrol.misdatos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.creativem.tiendacontrol.R;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import androidx.core.util.Pair;


public class MisDatos extends AppCompatActivity implements ProductoAdapter.OnProductoClickListener {
    private RecyclerView recyclerViewProductos;
    private ProductoAdapter adapter;
    private List<ProductoModel> productoList;
    private LinearLayout layoutMensajeVacio;
    private FirebaseHelper firebaseHelper;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "CodePrefs";
    private static final String KEY_CURRENT_DATABASE = "currentDatabase";

    private String userId;
    private String baseDatosSeleccionada;
    private String fechaInicioSeleccionada = null;
    private String fechaFinSeleccionada = null;

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
        layoutMensajeVacio = findViewById(R.id.layoutMensajeVacio);


        recyclerViewProductos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProductos.setAdapter(adapter); // Asignar el adaptador



        TextView  textVentas = findViewById(R.id.Venta); //Get TextView AFTER setContentView
        textVentas.setOnClickListener(v -> mostrarDialogoCrearProducto(null));

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

// Recibes el nombre de la base de datos como antes
        baseDatosSeleccionada = getIntent().getStringExtra("databaseName");

// Aquí podrías guardar el userId para usarlo en la referencia
// (opcional, si quieres guardarlo en SharedPreferences)
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", userId);
        editor.apply();

        if (baseDatosSeleccionada == null || baseDatosSeleccionada.isEmpty()) {
            baseDatosSeleccionada = obtenerBaseDatosSeleccionada();
        }

// Ya luego cuando uses la referencia en Firebase haz:
        DatabaseReference refProductos = FirebaseDatabase.getInstance()
                .getReference("Empresas")
                .child(userId)                 // Aquí usas el userId
                .child("basededatos")
                .child(baseDatosSeleccionada);


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
                    String tipoFiltro = parent.getItemAtPosition(position).toString().trim();
                    Log.d("SpinnerFiltro", "Opción seleccionada: " + tipoFiltro);

                    if (tipoFiltro.equalsIgnoreCase("Fechas")) {
                        Log.d("SpinnerFiltro", "Llamando a mostrarDatePickerRango()");
                        mostrarDatePickerRango();

                        // Restablecer el Spinner después de seleccionar el rango de fechas
                        new Handler().postDelayed(() -> spinnerFiltro.setSelection(0), 500);

                    } else {
                        Log.d("SpinnerFiltro", "Filtrando datos por: " + tipoFiltro);

                        // Obtener fechas según el filtro
                        Pair<String, String> fechas = obtenerFechaSegunFiltro(tipoFiltro);
                        String fechaInicio = fechas.first;
                        String fechaFin = fechas.second; // puede ser null para filtros simples

                        // 🔹 Llamada correcta al método unificado
                        adapter.filtrarPorFecha(fechaInicio, fechaFin, tipoFiltro);

                        // Actualiza visibilidad del RecyclerView
                        actualizarVisibilidadLista();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // No hacer nada si no se selecciona un elemento
                }
            });


        } else {
            Log.e("MisDatos", "spinnerFiltro is NULL! Check your layout file.");
        }

    }

    private void actualizarVisibilidadLista() {
        if (adapter == null) return; // seguridad

        if (adapter.getItemCount() == 0) {
            layoutMensajeVacio.setVisibility(View.VISIBLE);
            recyclerViewProductos.setVisibility(View.GONE);
        } else {
            layoutMensajeVacio.setVisibility(View.GONE);
            recyclerViewProductos.setVisibility(View.VISIBLE);
        }
    }

    public Pair<String, String> obtenerFechaSegunFiltro(String tipoFiltro) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdfBase = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String fechaInicio, fechaFin;

        switch (tipoFiltro) {
            case "Día":
                fechaInicio = fechaFin = sdfBase.format(cal.getTime());
                break;

            case "Semana":
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                fechaInicio = sdfBase.format(cal.getTime());
                cal.add(Calendar.DAY_OF_WEEK, 6);
                fechaFin = sdfBase.format(cal.getTime());
                break;

            case "Mes":
                cal.set(Calendar.DAY_OF_MONTH, 1);
                fechaInicio = sdfBase.format(cal.getTime());
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                fechaFin = sdfBase.format(cal.getTime());
                break;

            case "Año":
                cal.set(Calendar.DAY_OF_YEAR, 1);
                fechaInicio = sdfBase.format(cal.getTime());
                cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR));
                fechaFin = sdfBase.format(cal.getTime());
                break;

            default:
                fechaInicio = fechaFin = sdfBase.format(cal.getTime());
                break;
        }

        return new Pair<>(fechaInicio, fechaFin);
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

                // Filtro por defecto: "Día"
                String tipoFiltro = "Día";
                Pair<String, String> fechas = obtenerFechaSegunFiltro(tipoFiltro);

                String fechaInicio = fechas.first;  // hoy
                String fechaFin = fechas.second;     // normalmente null para "Día"

// Llamada al adaptador unificado
                adapter.filtrarPorFecha(fechaInicio, fechaFin, tipoFiltro);

                actualizarVisibilidadLista();


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
        builder.setTitle(productoExistente == null ? "Nuevo Producto" : "Editar Producto");

        View vista = getLayoutInflater().inflate(R.layout.productos_nuevos, null);
        builder.setView(vista);

        EditText inputValor = vista.findViewById(R.id.inputValor);
        EditText inputNombre = vista.findViewById(R.id.inputNombre);
        TextView tvSugerencia = vista.findViewById(R.id.tvSugerencia);
        EditText inputNota = vista.findViewById(R.id.inputNota);
        TextView inputGuardar = vista.findViewById(R.id.inputGuardar);
        Switch switchTipo = vista.findViewById(R.id.switchTipo);

        inputNombre.setImeOptions(EditorInfo.IME_ACTION_DONE);
        final AlertDialog dialog = builder.create();

        // Configurar autocompletado inline
        setupAutocompleteInline(inputNombre, tvSugerencia);

        // Guardar con botón
        inputGuardar.setOnClickListener(v -> {
            String textoFinal = inputNombre.getText().toString();

            // Si hay sugerencia visible y empieza con el texto del usuario, usar la sugerencia
            if (tvSugerencia.getVisibility() == View.VISIBLE &&
                    tvSugerencia.getText().toString().toLowerCase().startsWith(textoFinal.toLowerCase())) {
                textoFinal = tvSugerencia.getText().toString();
            }

            // Reemplazar el EditText con la sugerencia aceptada
            inputNombre.setText(textoFinal);
            inputNombre.setSelection(textoFinal.length());

            // Llamar a tu método de guardar o actualizar
            guardarActualizarProducto(dialog, inputNombre, inputValor, inputNota, switchTipo, productoExistente);
        });

        // Guardar con teclado físico (Enter / DONE)
        inputNombre.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Igual que el botón: aceptar sugerencia si existe
                String textoFinal = inputNombre.getText().toString();
                if (tvSugerencia.getVisibility() == View.VISIBLE &&
                        tvSugerencia.getText().toString().toLowerCase().startsWith(textoFinal.toLowerCase())) {
                    textoFinal = tvSugerencia.getText().toString();
                }
                inputNombre.setText(textoFinal);
                inputNombre.setSelection(textoFinal.length());

                guardarActualizarProducto(dialog, inputNombre, inputValor, inputNota, switchTipo, productoExistente);
                return true;
            }
            return false;
        });

        // Formatear valor con separadores de miles
        inputValor.addTextChangedListener(new TextWatcher() {
            private boolean isEditing = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) return;
                isEditing = true;

                String originalString = s.toString().replaceAll("[^\\d]", "");
                if (!originalString.isEmpty()) {
                    try {
                        long value = Long.parseLong(originalString);
                        String formattedString = PuntoMil.getFormattedNumber(value);
                        inputValor.setText(formattedString);
                        inputValor.setSelection(formattedString.length());
                    } catch (NumberFormatException e) {
                        inputValor.setText("");
                    }
                }

                isEditing = false;
            }
        });

        builder.setPositiveButton(productoExistente == null ? "Guardar" : "Actualizar",
                (dialogInterface, which) -> {
                    // Al usar el botón nativo de AlertDialog también aceptamos la sugerencia
                    String textoFinal = inputNombre.getText().toString();
                    if (tvSugerencia.getVisibility() == View.VISIBLE &&
                            tvSugerencia.getText().toString().toLowerCase().startsWith(textoFinal.toLowerCase())) {
                        textoFinal = tvSugerencia.getText().toString();
                    }
                    inputNombre.setText(textoFinal);
                    inputNombre.setSelection(textoFinal.length());

                    guardarActualizarProducto(dialog, inputNombre, inputValor, inputNota, switchTipo, productoExistente);
                });

        builder.setNegativeButton("Cancelar", (dialogInterface, which) -> {});

        dialog.show();

        // Rellenar si estamos editando
        if (productoExistente != null) {
            inputNombre.setText(productoExistente.getNombre());
            inputValor.setText(String.valueOf(Math.abs((long) productoExistente.getValor())));
            inputNota.setText(productoExistente.getNota());
            switchTipo.setChecked(productoExistente.getValor() < 0);
        } else {
            switchTipo.setChecked(false);
        }
    }

    // ----------------------------
// Autocompletado inline
// ----------------------------
    private void setupAutocompleteInline(EditText inputNombre, TextView tvSugerencia) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Empresas")
                .child(userId)
                .child("basededatos")
                .child(baseDatosSeleccionada);

        Set<String> nombresUnicos = new HashSet<>();

        // Traer nombres únicos de Firebase
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nombresUnicos.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String nombre = ds.child("nombre").getValue(String.class);
                    if (nombre != null && !nombre.isEmpty()) {
                        nombresUnicos.add(nombre);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error al leer nombres únicos", error.toException());
            }
        });

        inputNombre.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String userInput = s.toString().trim();
                String suggestion = "";

                // Buscar la primera coincidencia
                for (String nombre : nombresUnicos) {
                    if (nombre.toLowerCase().startsWith(userInput.toLowerCase()) && !nombre.equalsIgnoreCase(userInput)) {
                        suggestion = nombre;
                        break;
                    }
                }

                if (!suggestion.isEmpty() && !userInput.isEmpty()) {
                    // Mostrar la parte que falta en gris
                    tvSugerencia.setText(userInput + suggestion.substring(userInput.length()));
                    tvSugerencia.setVisibility(View.VISIBLE);
                } else {
                    tvSugerencia.setText("");
                    tvSugerencia.setVisibility(View.INVISIBLE);
                }
            }
        });

        // Para aceptar la sugerencia al presionar TAB o ENTER (opcional)
        inputNombre.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_TAB || keyCode == KeyEvent.KEYCODE_ENTER) &&
                    tvSugerencia.getVisibility() == View.VISIBLE) {
                inputNombre.setText(tvSugerencia.getText());
                inputNombre.setSelection(inputNombre.getText().length());
                tvSugerencia.setVisibility(View.INVISIBLE);
                return true;
            }
            return false;
        });
    }
    // ----------------------------
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

                        // ✅ Mantener el filtro activo
                        Pair<String, String> fechas = obtenerFechaSegunFiltro(adapter.getCurrentFilter());
                        String fechaInicio = fechas.first;
                        String fechaFin = fechas.second;

                        adapter.filtrarPorFecha(fechaInicio, fechaFin, adapter.getCurrentFilter());
                        MisDatos.calcularTotales(adapter.getFilteredProductList());
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
    private void mostrarDatePickerRango() {
        Log.d("DatePicker", "mostrarDatePickerRango() llamado");

        // Crear el selector de rango de fechas
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder
                .dateRangePicker()
                .setTitleText("Seleccionar Rango de Fechas")
                .build();

        // Mostrar el selector
        picker.show(getSupportFragmentManager(), "RangoFechaPicker");

        // Listener al confirmar selección
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null && selection.first != null && selection.second != null) {
                // Crear formato de fecha en zona horaria UTC
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                // Convertir timestamps directamente a fecha
                String fechaInicio = sdf.format(new Date(selection.first));
                String fechaFin = sdf.format(new Date(selection.second));

                Log.d("DatePicker", "Rango seleccionado corregido: " + fechaInicio + " - " + fechaFin);
                Toast.makeText(this, "Rango: " + fechaInicio + " - " + fechaFin, Toast.LENGTH_LONG).show();

                // Filtrar en adaptador
                adapter.filtrarPorFecha(fechaInicio, fechaFin, "Fechas");
                actualizarVisibilidadLista();
            } else {
                Log.e("DatePicker", "Error: Selección de fechas inválida");
            }
        });


    }




}
