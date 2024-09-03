package com.example.tiendacontrol.monitor;

import static android.content.ContentValues.TAG;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendacontrol.adapter.DatosAdapter;
import com.example.tiendacontrol.dialogFragment.IngresoDialogFragment;

import com.example.tiendacontrol.helper.BdVentas;
import java.text.NumberFormat;
import java.util.Locale;
import com.example.tiendacontrol.R;
import com.example.tiendacontrol.dialogFragment.GastoDialogFragment;
import com.example.tiendacontrol.helper.PuntoMil;
import com.example.tiendacontrol.model.Items;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, IngresoDialogFragment.OnDataChangedListener, GastoDialogFragment.OnDataChangedListener, DatosAdapter.OnDataChangedListener {
    // Constantes
    private static final String PREFS_NAME = "TiendaControlPrefs";
    private static final String KEY_CURRENT_DATABASE = "currentDatabase";

    // Variables
    private DatosAdapter adapter;
    private BdVentas bdVentas;
    private SharedPreferences sharedPreferences;
    private SearchView txtBuscar;
    private RecyclerView listaVentas;
    private ArrayList<Items> listaArrayVentas;
    private FloatingActionButton fabNuevo, fabGasto, fabMenu;
    private TextView textIngresos, textEgresos, textDiferencia;
    private ActivityResultLauncher<String[]> requestStoragePermissionLauncher;
    private String currentDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitymain);

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Obtener nombre de la base de datos desde Intent o SharedPreferences
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("databaseName")) {
            currentDatabase = intent.getStringExtra("databaseName");
            guardarNombreBaseDeDatos(currentDatabase);
        } else {
            currentDatabase = obtenerNombreBaseDeDatos();
        }

        // Inicializar BdVentas
        bdVentas = new BdVentas(this, currentDatabase);

        // Inicializar vistas
        inicializarVistas();

        // Configurar RecyclerView
        configurarRecyclerView();

        // Configurar SearchView
        txtBuscar.setOnQueryTextListener(this);

        // Inicializar ActivityResultLauncher para permisos
        inicializarLauncherPermisos();

        // Actualizar UI
        onDataChanged();

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

        iconInicio.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, Database.class)));
        iconLimpiar.setOnClickListener(view -> confirmarEliminarTodo());
        iconEgreso.setOnClickListener(view -> mostrarGastoDialogFragment());
        iconIngreso.setOnClickListener(view -> mostrarIngresoDialogFragment());


    }

    private void configurarRecyclerView() {
        listaVentas.setLayoutManager(new GridLayoutManager(this, 2));
        listaArrayVentas = new ArrayList<>();
        adapter = new DatosAdapter(this, listaArrayVentas, bdVentas, this);
        listaVentas.setAdapter(adapter);
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
        GastoDialogFragment gastoDialogFragment = GastoDialogFragment.newInstance(currentDatabase);
        gastoDialogFragment.setDataChangedListener(this);
        gastoDialogFragment.show(getSupportFragmentManager(), "GastoDialogFragment");
    }

    private void mostrarIngresoDialogFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        IngresoDialogFragment ingresoDialogFragment = IngresoDialogFragment.newInstance(currentDatabase);
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
        bdVentas = new BdVentas(this, currentDatabase);
        onDataChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mostrarValores();
        onDataChanged();
        bdVentas = new BdVentas(this, currentDatabase);
        if (adapter == null) {
            listaArrayVentas = new ArrayList<>();
            adapter = new DatosAdapter(this, listaArrayVentas, bdVentas, this);
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
        adapter.filtrado(newText);
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

    @Override
    public void onDataChanged() {
        if (adapter != null) {
            listaArrayVentas.clear();
            listaArrayVentas.addAll(bdVentas.mostrarVentas());
            adapter.ordenarPorFecha();
            adapter.setItems(listaArrayVentas);
            adapter.setBdVentas(bdVentas);

            adapter.notifyDataSetChanged();
            mostrarValores();
            Log.d(TAG, "RecyclerView actualizado, tamaño de la lista: " + listaArrayVentas.size());
        }
    }

    private void confirmarEliminarTodo() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Todos los Items")
                .setMessage("¿Estás seguro de que deseas limpiar toda la base de datos no se podrán recuperar en el futuro?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    boolean resultado = bdVentas.eliminarTodo();
                    if (resultado) {
                        Toast.makeText(this, "Base de datos eliminada", Toast.LENGTH_SHORT).show();
                        listaArrayVentas.clear();
                        adapter.notifyDataSetChanged();
                        mostrarValores();
                        onDataChanged();
                    } else {
                        Toast.makeText(this, "Error al eliminar la base de datos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .setIcon(R.drawable.eliminar)
                .show();
    }

    private void mostrarValores() {
        // Obtener los valores de ingresos, egresos y diferencia
        double ingresos = bdVentas.obtenerTotalVentas();
        double egresos = bdVentas.obtenerTotalEgresos();
        double diferencia = bdVentas.obtenerDiferencia();

        // Convertir a long y formatear con punto de mil
        String ingresosFormatted = PuntoMil.getFormattedNumber((long) ingresos);
        String egresosFormatted = PuntoMil.getFormattedNumber((long) egresos);
        String diferenciaFormatted = PuntoMil.getFormattedNumber((long) diferencia);

        // Mostrar los valores en los TextView
        textIngresos.setText(String.format("$%s", ingresosFormatted));
        textEgresos.setText(String.format("$%s", egresosFormatted));
        textDiferencia.setText(String.format("$%s", diferenciaFormatted));
    }

}