package com.creativem.tiendacontrol.monitor;

import static android.content.ContentValues.TAG;
import static java.security.AccessController.getContext;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.creativem.tiendacontrol.adapter.DatosAdapter;

import com.creativem.tiendacontrol.helper.BdVentas;
import com.creativem.tiendacontrol.R;

import com.creativem.tiendacontrol.helper.PuntoMil;
import com.creativem.tiendacontrol.helper.SpinnerManager;
import com.creativem.tiendacontrol.model.Items;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


public class DatosDatos extends AppCompatActivity implements SearchView.OnQueryTextListener, DatosAdapter.OnDataChangedListener, BdVentas.OnDataChangeListener {
    // Constantes
    private static final String PREFS_NAME = "TiendaControlPrefs";
    private static final String KEY_CURRENT_DATABASE = "currentDatabase";
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private Spinner spinnerFiltro;
    private String filtroActual = "Día";
    private DatosAdapter adapter;
    private BdVentas bdVentas;
    private SharedPreferences sharedPreferences;
    private SearchView txtBuscar;
    private RecyclerView listaVentas;
    private TextView textIngresos, textEgresos, textDiferencia;
    private String currentDatabase;
    private TextView textViewDatabaseName;
    private FirebaseAuth mAuth;
    private String userId;
    private DatabaseReference databaseReference;
    private boolean datosCargados = false;
    private FloatingActionButton fabNuevo, fabGasto, fabMenu;
    private ActivityResultLauncher<String[]> requestStoragePermissionLauncher;
    private ArrayList<Items> listaArrayVentas; // Mantén esta variable
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.datosdatos);
        mAuth = FirebaseAuth.getInstance();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("databaseName")) {
            currentDatabase = intent.getStringExtra("databaseName");
            guardarNombreBaseDeDatos(currentDatabase);
        } else {
            currentDatabase = obtenerNombreBaseDeDatos();
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            userId = user.getUid();
            databaseReference = database.getReference("users").child(userId).child("databases").child(currentDatabase);
        }

        bdVentas = new BdVentas(this, currentDatabase, databaseReference);
        bdVentas.setOnDataChangeListener(this);
        bdVentas.cargarDatos();

        inicializarVistas();
        configurarRecyclerView();

        spinnerFiltro = findViewById(R.id.spinner_filtro);
        ArrayAdapter<CharSequence> adapterFiltro = ArrayAdapter.createFromResource(this, R.array.filtro_opciones, android.R.layout.simple_spinner_item);
        spinnerFiltro.setAdapter(adapterFiltro); // ¡Configura el adaptador PRIMERO!

        spinnerFiltro.setSelection(0);
        filtroActual = "Día";


        spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                filtroActual = parent.getItemAtPosition(position).toString();
                aplicarFiltro();
                actualizarTotales();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        textViewDatabaseName.setText("Cuenta: " + currentDatabase);
        txtBuscar.setOnQueryTextListener(this);
        inicializarLauncherPermisos();


    }


    private void aplicarFiltro() {
        SimpleDateFormat dateFormat;
        String fechaActual;
        Calendar calendar = Calendar.getInstance();

        switch (filtroActual) {
            case "Día":
                dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                fechaActual = dateFormat.format(calendar.getTime());
                break;
            case "Semana":
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek()); // Inicio de la semana
                Date inicioSemana = calendar.getTime();
                calendar.add(Calendar.DAY_OF_YEAR, 6); // Fin de la semana
                Date finSemana = calendar.getTime();
                dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                fechaActual = dateFormat.format(inicioSemana) + " - " + dateFormat.format(finSemana); // Rango de fechas
                break;
            case "Mes":
                dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                fechaActual = dateFormat.format(calendar.getTime());

                break;
            case "Año":
                dateFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
                fechaActual = dateFormat.format(calendar.getTime());
                break;
            default:
                dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                fechaActual = dateFormat.format(calendar.getTime());
                break;

        }

        if (adapter != null) {
            adapter.filtrarPorFecha(fechaActual, filtroActual);
        }
    }

    @Override
    public void onDataChange(ArrayList<Items> items) {
        Log.d(TAG, "📌 onDataChange() - Recibidos " + items.size() + " elementos");

        runOnUiThread(() -> {
            if (!datosCargados || adapter == null) {
                adapter = new DatosAdapter(this, new ArrayList<>(items), this);
                listaVentas.setAdapter(adapter);
                datosCargados = true;
                Log.d(TAG, "✅ Adapter creado y asignado");
            } else {
                adapter.setItems(new ArrayList<>(items));
                adapter.notifyDataSetChanged();
                Log.d(TAG, "🔄 Datos actualizados en el adaptador");
            }

            spinnerFiltro.setSelection(0);
            filtroActual = "Día";
            aplicarFiltro();
            actualizarTotales();
        });
    }

    @Override
    public void onDataChanged() {
        Log.d(TAG, "DatosDatos - onDataChanged (DatosAdapter): Actualizando totales");

    }

    private void inicializarVistas() {
        listaVentas = findViewById(R.id.listaVentas);
        ImageView iconIngreso = findViewById(R.id.ingreso);
        ImageView iconEgreso = findViewById(R.id.egreso);
        textIngresos = findViewById(R.id.textIngresos);
        textEgresos = findViewById(R.id.textEgresos);
        textDiferencia = findViewById(R.id.textDiferencia);
        txtBuscar = findViewById(R.id.txtBuscar);
        ImageView iconLimpiar = findViewById(R.id.borrardados);
        ImageView iconInicio = findViewById(R.id.inicio);
        textViewDatabaseName = findViewById(R.id.text_view_database_name);

        // Evitar que el SearchView tenga foco
        txtBuscar.clearFocus();
        txtBuscar.setFocusable(false);
        txtBuscar.setFocusableInTouchMode(true);

        // Forzar el foco en el icono de ingreso
        iconIngreso.setFocusable(true);
        iconIngreso.setFocusableInTouchMode(true);
        iconIngreso.requestFocus();

        iconInicio.setOnClickListener(view -> startActivity(new Intent(DatosDatos.this, BaseDatos.class)));
        iconLimpiar.setOnClickListener(view -> confirmarEliminarTodo());
        iconEgreso.setOnClickListener(view -> mostrarEgresoDialog());
        iconIngreso.setOnClickListener(view -> mostrarIngresoDialogo());
    }

    public void mostrarIngresoDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
               // Inflar el layout personalizado
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.ingreso, null);
        builder.setView(dialogView);

        // Inicialización de los elementos de la interfaz
        EditText txtProducto = dialogView.findViewById(R.id.txtProducto);
        EditText txtValor = dialogView.findViewById(R.id.txtValor);
        EditText txtDetalles = dialogView.findViewById(R.id.txtDetalles);
        EditText txtCantidad = dialogView.findViewById(R.id.txtCantidad);
        TextView texGuardar = dialogView.findViewById(R.id.texGuardar);
        TextView texGuardarPredefinido = dialogView.findViewById(R.id.texGuardarPredefinido);
        Spinner spinnerPredefined = dialogView.findViewById(R.id.spinnerPredefined);
        TextView texEliminar = dialogView.findViewById(R.id.texEliminar);

        // Aplicar el formato con separadores de mil
        PuntoMil.formatNumberWithThousandSeparator(txtValor);

        // Configurar campos numéricos
        txtValor.setInputType(InputType.TYPE_CLASS_NUMBER);
        txtCantidad.setInputType(InputType.TYPE_CLASS_NUMBER);

        // Crear el objeto SpinnerManager
        SpinnerManager spinnerManager = new SpinnerManager(this, spinnerPredefined, txtProducto, txtValor, txtDetalles, txtCantidad);
        spinnerManager.loadPredefinedItems();

        // Escuchar selección de Spinner y llenar campos automáticamente
        spinnerPredefined.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Items selectedItem = (Items) parent.getItemAtPosition(position);
                if (selectedItem != null && position != 0) { // Evitar "Seleccione un ítem"
                    txtProducto.setText(selectedItem.getProducto());
                    txtValor.setText(String.valueOf(selectedItem.getValor()));
                    txtDetalles.setText(selectedItem.getDetalles());
                    txtCantidad.setText(String.valueOf(selectedItem.getCantidad()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Crear y mostrar el AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Acción del botón "Guardar"
        texGuardar.setOnClickListener(v -> {
            String producto = txtProducto.getText().toString().trim();
            String valorStr = txtValor.getText().toString().trim();
            String detalles = txtDetalles.getText().toString().trim();
            String cantidadStr = txtCantidad.getText().toString().trim();
            String categoriaSeleccionada = spinnerPredefined.getSelectedItem().toString();

            // Verificar que todos los campos estén llenos
            if (producto.isEmpty() || valorStr.isEmpty() || detalles.isEmpty() || cantidadStr.isEmpty()) {
                Toast.makeText(this, "Todos los campos son Necesarios", Toast.LENGTH_LONG).show();
                return; // Salir del método si hay campos vacíos
            }
            try {
                // Eliminar separadores de miles (por ejemplo, comas o puntos)
                valorStr = valorStr.replace(",", "").replace(".", "");
                cantidadStr = cantidadStr.replace(",", "").replace(".", "");

                // Convertir los datos a los tipos correctos
                double valor = Double.parseDouble(valorStr);
                int cantidad = Integer.parseInt(cantidadStr);
                double total = valor * cantidad;

                // Obtener fecha con formato colombiano
                SimpleDateFormat sdfColombia = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                sdfColombia.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
                String dateString = sdfColombia.format(new Date());

                // Crear un mapa de datos que incluye la marca de tiempo
                Map<String, Object> data = new HashMap<>();
                data.put("producto", producto);
                data.put("valor", total);
                data.put("detalles", detalles);
                data.put("cantidad", cantidad);
                data.put("type", "Ingreso");
                data.put("timestamp", ServerValue.TIMESTAMP);
                data.put("date", dateString);


                DatabaseReference newItemRef = databaseReference.push();
                String key = newItemRef.toString();
                data.put("id", key);

                // Guardar en Firebase con el ID generado
                newItemRef.setValue(data).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        Toast.makeText(this, "Ingreso guardado", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Ingrese valores numéricos válidos", Toast.LENGTH_SHORT).show();
            }
        });

        // Acción del botón "Guardar Predefinido"
        texGuardarPredefinido.setOnClickListener(v -> {
            spinnerManager.savePredefinedItem();
            Toast.makeText(v.getContext(), "Guardado como predefinido", Toast.LENGTH_SHORT).show();
            spinnerManager.loadPredefinedItems(); // Recargar lista en el Spinner
        });

        // Acción del botón "Eliminar"
        texEliminar.setOnClickListener(v -> {
            if (spinnerPredefined.getSelectedItemPosition() > 0) { // Verificar que no sea el primer ítem
                spinnerManager.removeSelectedItem(); // Eliminar el ítem
                Toast.makeText(v.getContext(), "Ítem eliminado correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(v.getContext(), "Seleccione un ítem válido para eliminar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void mostrarEgresoDialog() {
        // Crear un AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nuevo Egreso");

        // Inflar el layout personalizado
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.egreso, null);  // Asegúrate de usar el layout adecuado
        builder.setView(dialogView);

        // Inicialización de los elementos de la interfaz
        EditText editProducto = dialogView.findViewById(R.id.editProducto);
        EditText editValor = dialogView.findViewById(R.id.editValor);
        EditText editDetalles = dialogView.findViewById(R.id.editDetalles);
        EditText editCantidad = dialogView.findViewById(R.id.editCantidad);
        Spinner spinnerPredefined = dialogView.findViewById(R.id.spinnerPredefined);
        TextView texGuardar = dialogView.findViewById(R.id.texGuardar);
        TextView texGuardarPredefinido = dialogView.findViewById(R.id.texGuardarPredefinido);
        TextView texEliminar = dialogView.findViewById(R.id.texEliminar);

        // Aplicar el formato con separadores de mil
        PuntoMil.formatNumberWithThousandSeparator(editValor);

        // Configuración del SpinnerManager
        SpinnerManager spinnerManager = new SpinnerManager(this, spinnerPredefined, editProducto, editValor, editDetalles, editCantidad);
        spinnerManager.loadPredefinedItems();

        // Configurar el evento para la selección del Spinner
        spinnerPredefined.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Evitar "Seleccione un ítem"
                    Items selectedItem = (Items) parent.getItemAtPosition(position);
                    editProducto.setText(selectedItem.getProducto());
                    editValor.setText(String.valueOf(selectedItem.getValor()));
                    editDetalles.setText(selectedItem.getDetalles());
                    editCantidad.setText(String.valueOf(selectedItem.getCantidad()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Crear el AlertDialog
        AlertDialog dialog = builder.create();  // Aquí se crea el dialog

        // Mostrar el diálogo
        dialog.show();

        texGuardar.setOnClickListener(v -> {
            String producto = editProducto.getText().toString().trim();
            String valorStr = editValor.getText().toString().trim();
            String detalles = editDetalles.getText().toString().trim();
            String cantidadStr = editCantidad.getText().toString().trim();

            // Verificar si los campos están vacíos
            if (producto.isEmpty() || valorStr.isEmpty() || detalles.isEmpty() || cantidadStr.isEmpty()) {
                Toast.makeText(this, "Todos los campos son Necesarios", Toast.LENGTH_LONG).show();
                return; // Salir si hay campos vacíos
            }

            try {
                // Eliminar separadores de miles
                valorStr = valorStr.replace(",", "").replace(".", "");
                cantidadStr = cantidadStr.replace(",", "").replace(".", "");

                // Convertir los valores
                double valor = Double.parseDouble(valorStr);
                int cantidad = Integer.parseInt(cantidadStr);

                // El valor total se multiplica por -1 para los egresos (debe ser negativo)
                double total = -valor * cantidad;  // Aquí se pone el signo negativo

                // Obtener fecha con formato colombiano
                SimpleDateFormat sdfColombia = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                sdfColombia.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
                String dateString = sdfColombia.format(new Date());

                // Crear el mapa de datos
                Map<String, Object> data = new HashMap<>();
                data.put("producto", producto);
                data.put("valor", total);  // Aquí se guarda el valor negativo
                data.put("detalles", detalles);
                data.put("cantidad", cantidad);
                data.put("type", "Gasto");  // Para asegurarse de que es un egreso
                data.put("timestamp", ServerValue.TIMESTAMP);
                data.put("date", dateString);

                // Referencia a la base de datos de Firebase
                DatabaseReference newItemRef = databaseReference.push();
                String key = newItemRef.toString();
                data.put("id", key);

                // Guardar el gasto en Firebase
                newItemRef.setValue(data).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Gasto guardado", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();  // Ahora puedes cerrar el diálogo correctamente
                    } else {
                        Toast.makeText(this, "Error al guardar el gasto", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Ingrese valores numéricos válidos", Toast.LENGTH_SHORT).show();
            }
        });

        // Acción para guardar un gasto como predefinido
        texGuardarPredefinido.setOnClickListener(v -> {
            spinnerManager.savePredefinedItem();
            Toast.makeText(v.getContext(), "Guardado como predefinido", Toast.LENGTH_SHORT).show();
            spinnerManager.loadPredefinedItems(); // Recargar lista en el Spinner
        });

        // Acción para eliminar el ítem predefinido
        texEliminar.setOnClickListener(v -> {
            if (spinnerPredefined.getSelectedItemPosition() > 0) {
                spinnerManager.removeSelectedItem();
                Toast.makeText(v.getContext(), "Ítem eliminado correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(v.getContext(), "Seleccione un ítem válido para eliminar", Toast.LENGTH_SHORT).show();
            }
        });
    }
       private void configurarRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        listaVentas.setLayoutManager(layoutManager);

    }
    private void inicializarLauncherPermisos() {
        requestStoragePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                isGranted -> {
                    if (isGranted.values().contains(false)) {
                        Toast.makeText(this, "Permiso de escritura en almacenamiento externo denegado", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String newDatabase = intent.getStringExtra("databaseName");

        if (newDatabase != null && !newDatabase.equals(currentDatabase)) {
            actualizarBaseDeDatos(newDatabase);

        }
    }

    private void actualizarBaseDeDatos(String newDatabase) {
        guardarNombreBaseDeDatos(newDatabase);
        if (bdVentas != null) {
            bdVentas.close();
        }
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            databaseReference = database.getReference("users").child(userId).child("databases").child(newDatabase);
        }
        bdVentas = new BdVentas(this, newDatabase, databaseReference);
        bdVentas.setOnDataChangeListener(items -> {
            if (adapter != null) {
                adapter.setItems(items);
                actualizarTotales();
            }
        });

        textViewDatabaseName.setText("Cuenta: " + newDatabase);
    }


    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            databaseReference = database.getReference("users").child(userId).child("databases").child(currentDatabase);
        }
        if (bdVentas == null) {
            bdVentas = new BdVentas(this, currentDatabase, databaseReference);
            bdVentas.setOnDataChangeListener(items -> {
                if (adapter != null) {
                    adapter.setItems(items);
                    actualizarTotales();
                }
            });
        }


        if (adapter == null) {
            listaArrayVentas = new ArrayList<>();
            adapter = new DatosAdapter(this, listaArrayVentas,  this);
            listaVentas.setAdapter(adapter);
        }

    }
    @Override
    protected void onDestroy() {
        if (bdVentas != null) {
            bdVentas.close();
            bdVentas = null;
        }
        super.onDestroy();
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (adapter != null) {
            adapter.filtrado(newText);
        }
        return false;
    }

    private String obtenerNombreBaseDeDatos() {
        return sharedPreferences.getString(KEY_CURRENT_DATABASE, null);
    }

    private void guardarNombreBaseDeDatos(String dbName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CURRENT_DATABASE, dbName);
        editor.apply();
    }
    private void actualizarTotales() {
        if (adapter != null) { // Verificar que el adaptador no sea nulo
            List<Items> listaFiltrada = adapter.getItemsList(); // Obtener la lista filtrada del adaptador
            double ingresos = 0;
            double egresos = 0;

            for (Items item : listaFiltrada) { // Iterar sobre la lista filtrada
                if (item.getType() != null) {
                    if (item.getType().equals("Ingreso")) {
                        ingresos += item.getValor();
                    } else if (item.getType().equals("Gasto")) {
                        egresos += item.getValor(); // Los egresos ya deberían ser negativos
                    }
                }
            }

            double diferencia = ingresos + egresos;

            // Formatear los valores
            String ingresosFormatted = PuntoMil.getFormattedNumber((long) ingresos);
            String egresosFormatted = PuntoMil.getFormattedNumber((long) Math.abs(egresos)); // Mostrar egresos como positivos
            String diferenciaFormatted = PuntoMil.getFormattedNumber((long) diferencia);

            // Actualizar los TextViews
            textIngresos.setText(String.format("$%s", ingresosFormatted));
            textEgresos.setText(String.format("$%s", egresosFormatted));
            textDiferencia.setText(String.format("$%s", diferenciaFormatted));

            // Cambiar el color del texto de la diferencia
            int colorTexto = diferencia < 0 ? ContextCompat.getColor(this, R.color.colorNegativo) : ContextCompat.getColor(this, R.color.colorPositivo);
            textDiferencia.setTextColor(colorTexto);


        }
    }
    private void confirmarEliminarTodo() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Todos los Items")
                .setMessage("¿Estás seguro de que deseas limpiar toda la base de datos no se podrán recuperar en el futuro?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    boolean resultado = bdVentas.eliminarTodo();
                    if(resultado){
                        Toast.makeText(this, "Base de datos eliminada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al eliminar la base de datos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .setIcon(R.drawable.eliminar)
                .show();
    }
}