package com.creativem.tiendacontrol.monitor;

import static android.content.ContentValues.TAG;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.creativem.tiendacontrol.adapter.DatosAdapter;
import com.creativem.tiendacontrol.dialogFragment.IngresoDialogFragment;
import com.creativem.tiendacontrol.helper.BdVentas;
import com.creativem.tiendacontrol.R;
import com.creativem.tiendacontrol.dialogFragment.GastoDialogFragment;
import com.creativem.tiendacontrol.helper.PuntoMil;
import com.creativem.tiendacontrol.model.Items;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DatosDatos extends AppCompatActivity implements SearchView.OnQueryTextListener, IngresoDialogFragment.OnDataChangedListener, GastoDialogFragment.OnDataChangedListener, DatosAdapter.OnDataChangedListener, BdVentas.OnDataChangeListener {
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
        adapterFiltro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltro.setAdapter(adapterFiltro);

        spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                filtroActual = parent.getItemAtPosition(position).toString();
                aplicarFiltro();
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
                adapter = new DatosAdapter(this, new ArrayList<>(items), this); // Copia la lista aquí también
                listaVentas.setAdapter(adapter);
                datosCargados = true;
                Log.d(TAG, "✅ Adapter creado y asignado");
            } else {
                adapter.setItems(items);
                // aplicarFiltro() - Si es necesario, llámalo antes de notifyDataSetChanged()
                adapter.notifyDataSetChanged();
                Log.d(TAG, "🔄 Datos actualizados en el adaptador");
            }

            actualizarTotales(); // Llama después de actualizar el adaptador
        });
    }

    @Override
    public void onDataChanged() {
        Log.d(TAG, "DatosDatos - onDataChanged (DatosAdapter): Actualizando totales");
        actualizarTotales();
        aplicarFiltro();
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
        iconEgreso.setOnClickListener(view -> mostrarGastoDialogFragment());
        iconIngreso.setOnClickListener(view -> mostrarIngresoDialogFragment());
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

    private void mostrarGastoDialogFragment() {
        GastoDialogFragment gastoDialogFragment = GastoDialogFragment.newInstance(currentDatabase,databaseReference.toString());
        gastoDialogFragment.setDataChangedListener(this);
        gastoDialogFragment.show(getSupportFragmentManager(), "GastoDialogFragment");
    }

    private void mostrarIngresoDialogFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        IngresoDialogFragment ingresoDialogFragment = IngresoDialogFragment.newInstance(currentDatabase,databaseReference.toString());
        ingresoDialogFragment.setDataChangedListener(this);
        ingresoDialogFragment.show(fragmentManager, "ingreso_dialog");
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
        if (bdVentas != null) {
            // Obtener valores desde la base de datos
            double ingresos = bdVentas.obtenerTotalVentas();
            double egresos = bdVentas.obtenerTotalEgresos(); // Los egresos deben incluir el signo negativo
            double diferencia = ingresos + egresos; // Calcular correctamente la diferencia

            // Formatear los valores
            String ingresosFormatted = PuntoMil.getFormattedNumber((long) ingresos);
            String egresosFormatted = PuntoMil.getFormattedNumber((long) Math.abs(egresos)); // Mostrar egresos positivos
            String diferenciaFormatted = PuntoMil.getFormattedNumber((long) diferencia);

            // Actualizar los TextViews
            textIngresos.setText(String.format("$%s", ingresosFormatted));
            textEgresos.setText(String.format("$%s", egresosFormatted));
            textDiferencia.setText(String.format("$%s", diferenciaFormatted));

            // Cambiar el color del texto según el valor de la diferencia
            int colorTexto = diferencia < 0
                    ? ContextCompat.getColor(DatosDatos.this, R.color.colorNegativo)
                    : ContextCompat.getColor(DatosDatos.this, R.color.colorPositivo);
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