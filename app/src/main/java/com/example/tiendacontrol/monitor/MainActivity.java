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
import com.example.tiendacontrol.model.Items;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, IngresoDialogFragment.OnDataChangedListener, GastoDialogFragment.OnDataChangedListener {
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
    private TextView textVenta, textGanacia, textGasto;
    private ImageView imageViewProfile;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    private ActivityResultLauncher<String[]> requestStoragePermissionLauncher;
    private String currentDatabase;
    private boolean userLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitymain);
        // *** Inicializar SharedPreferences ***
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Obtener nombre de la base de datos del Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("databaseName")) {
            currentDatabase = intent.getStringExtra("databaseName");
            // Guardar en SharedPreferences para uso futuro
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_CURRENT_DATABASE, currentDatabase);
            editor.apply();
        } else {
            currentDatabase = getCurrentDatabaseName();
        }
        // Referencias a vistas
        listaVentas = findViewById(R.id.listaVentas);
        ImageView iconIngreso = findViewById(R.id.ingreso);
        ImageView iconEgreso = findViewById(R.id.egreso);
        textVenta = findViewById(R.id.textVenta);
        textGanacia = findViewById(R.id.textGanacia);
        textGasto = findViewById(R.id.textGasto);
        txtBuscar = findViewById(R.id.txtBuscar);

        ImageView iconLimpiar = findViewById(R.id.borrardados);
        ImageView iconInicio = findViewById(R.id.inicio);

        // Configuración del RecyclerView
        listaVentas.setLayoutManager(new GridLayoutManager(this, 2));
        listaArrayVentas = new ArrayList<>();


        // Inicializar BdVentas
        bdVentas = new BdVentas(this, currentDatabase);
        adapter = new DatosAdapter(this, listaArrayVentas, bdVentas); // Pasa la instancia
        listaVentas.setAdapter(adapter);

        // Inicializar SearchView
        txtBuscar.setOnQueryTextListener(this);

        onDataChanged();

        iconInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Database.class);
                startActivity(intent);
            }
        });

        iconLimpiar.setOnClickListener(view -> {
            confirmarEliminarTodo();
        });

        iconEgreso.setOnClickListener(view -> {
            GastoDialogFragment dialogFragment = new GastoDialogFragment();
            // Crea una nueva instancia de IngresoDialogFragment con el nombre de la base de datos actual
            GastoDialogFragment gastoDialogFragment = GastoDialogFragment.newInstance(currentDatabase);
            gastoDialogFragment.setDataChangedListener(this); // Ahora debería funcionar sin errores
            gastoDialogFragment.show(getSupportFragmentManager(), "GastoDialogFragment");
        });

        iconIngreso.setOnClickListener(view -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            // Crea una nueva instancia de IngresoDialogFragment con el nombre de la base de datos actual
            IngresoDialogFragment ingresoDialogFragment = IngresoDialogFragment.newInstance(currentDatabase);
            ingresoDialogFragment.setDataChangedListener( this); // Asegúrate de esta línea
            ingresoDialogFragment.show(fragmentManager, "ingreso_dialog");
        });
                // Inicializa el lanzador para la solicitud de permisos
        requestStoragePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                isGranted -> {
                    if (isGranted.values().contains(false)) {
                        // Permiso denegado
                        Toast.makeText(this, "Permiso de escritura en almacenamiento externo denegado",
                                Toast.LENGTH_SHORT).show();
                        // Puedes mostrar un mensaje al usuario y/o solicitar el permiso nuevamente
                    } else {
                        // Permiso concedido
                    }
                }
        );

        onDataChanged();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String newDatabase = intent.getStringExtra("databaseName");

        Log.d(TAG, "onNewIntent: Nueva base de datos recibida: " + newDatabase);

        if (newDatabase != null && !newDatabase.equals(currentDatabase)) {
            Log.d(TAG, "onNewIntent: Cambiando base de datos de " + currentDatabase + " a " + newDatabase);
            setCurrentDatabaseName(newDatabase);

            if (bdVentas != null) {
                Log.d(TAG, "onNewIntent: Cerrando bdHelper existente");
                bdVentas.close();
            }

            bdVentas = new BdVentas(this, currentDatabase);

            Log.d(TAG, "onNewIntent: Nueva instancia de BdHelper y BdVentas creada para " + currentDatabase);

            onDataChanged();
        } else {
            Log.d(TAG, "onNewIntent: No se requiere cambio de base de datos");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: Re-inicializando BdHelper y BdVentas para " + currentDatabase);


        bdVentas = new BdVentas(this, currentDatabase);

        if (adapter == null) {
            Log.d(TAG, "onResume: Creando nuevo adaptador y lista");
            listaArrayVentas = new ArrayList<>();
            adapter = new DatosAdapter(this, listaArrayVentas, bdVentas);
            listaVentas.setAdapter(adapter);
        } else {
            Log.d(TAG, "onResume: Adaptador ya existe");
        }

        onDataChanged();
    }

    public void onDataChanged() {
        if (adapter != null) {
            listaArrayVentas.clear();
            listaArrayVentas.addAll(bdVentas.mostrarVentas());
            // ¡Aquí debes llamar al método de ordenación!
            adapter.ordenarPorFecha();
            adapter.setItems(listaArrayVentas);
            adapter.setBdVentas(bdVentas);
            adapter.notifyDataSetChanged(); // Notifica al adaptador sobre el cambio
            Log.d("MainActivity", "RecyclerView actualizado, tamaño de la lista: " + listaArrayVentas.size());
            calcularSumaGanancias();
            calcularSumaTotalVenta();
            calcularSumaTotalGasto();
        }
    }


    // Método para obtener el nombre de la base de datos desde SharedPreferences
    private String getCurrentDatabaseName() {
        return sharedPreferences.getString(KEY_CURRENT_DATABASE, null);
    }

    // Método para actualizar la base de datos actual
    private void setCurrentDatabaseName(String dbName) {
        currentDatabase = dbName;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CURRENT_DATABASE, currentDatabase);
        editor.apply();
    }

    // Este método se llama cuando el usuario envía el texto en el campo de búsqueda.
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    // Este método se llama cada vez que el texto en el campo de búsqueda cambia.
    @Override
    public boolean onQueryTextChange(String newText) {
        // Filtrar el RecyclerView según el texto de búsqueda
        adapter.filtrado(newText);
        return false;
    }

    // Método para cargar la imagen de perfil del usuario desde Firestore
    private void loadProfileImage(String userId) {
        DocumentReference userRef = db.collection("usuarios").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String imageUrl = documentSnapshot.getString("profileImageUrl");

                // Cargar la imagen usando Picasso si la URL de la imagen está disponible
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Picasso.get().load(imageUrl).into(imageViewProfile);
                } else {
                    Toast.makeText(MainActivity.this, "No se encontró la imagen de perfil", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(MainActivity.this, "Error al cargar la imagen de perfil", Toast.LENGTH_SHORT).show();
        });
    }

    // Método para calcular y mostrar la suma de ganancias sin decimales
    private void calcularSumaGanancias() {
        double suma = 0.0;
        Log.d("MainActivity", "calcularSumaGanancias ");
        for (Items venta : listaArrayVentas) {
            double valorVenta = venta.getValorAsDouble();
            suma += valorVenta;

        }

        // Redondear suma a valor entero
        long sumaEntera = Math.round(suma);

        // Formatear la suma como moneda colombiana sin decimales
        String sumaFormateadaStr = NumberFormat.getCurrencyInstance(new Locale("es", "CO")).format(sumaEntera);
        // Eliminar decimales si hay .00
        sumaFormateadaStr = sumaFormateadaStr.replaceAll("[,.]00$", "");

        textGanacia.setText(sumaFormateadaStr);
    }

    // Método para calcular y mostrar la suma total de ventas sin decimales
    private void calcularSumaTotalVenta() {
        double suma = 0.0;
        for (Items venta : listaArrayVentas) {
            double valorVenta = venta.getValorAsDouble();
            if (valorVenta > 0) {
                suma += valorVenta;
            }
        }

        // Redondear suma a valor entero
        long sumaEntera = Math.round(suma);

        // Formatear la suma como moneda colombiana sin decimales
        String sumaFormateadaStr = NumberFormat.getCurrencyInstance(new Locale("es", "CO")).format(sumaEntera);
        // Eliminar decimales si hay .00
        sumaFormateadaStr = sumaFormateadaStr.replaceAll("[,.]00$", "");

        textVenta.setText(sumaFormateadaStr);
    }

    // Método para calcular y mostrar la suma total de icono sin decimales
    private void calcularSumaTotalGasto() {
        double suma = 0.0;
        for (Items venta : listaArrayVentas) {
            double valorVenta = venta.getValorAsDouble();
            if (valorVenta < 0) {
                suma += valorVenta;
            }
        }

        // Asegurarse de que suma sea positiva
        suma = Math.abs(suma);

        // Redondear suma a valor entero
        long sumaEntera = Math.round(suma);

        // Formatear la suma como moneda colombiana sin decimales
        String sumaFormateadaStr = NumberFormat.getCurrencyInstance(new Locale("es", "CO")).format(sumaEntera);
        // Eliminar decimales si hay .00
        sumaFormateadaStr = sumaFormateadaStr.replaceAll("[,.]00$", "");

        textGasto.setText(sumaFormateadaStr);
    }

    // Método para confirmar la eliminación de la base de datos
    public void confirmarEliminarTodo() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Todos los Items")
                .setMessage("¿Estás seguro de que deseas limpiar toda la base de datos no se prodran recuperar en el futuro?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Usa la instancia 'bdVentas' existente en la actividad
                    boolean resultado = bdVentas.eliminarTodo();
                    if (resultado) {
                        Toast.makeText(this, "Base de datos eliminada", Toast.LENGTH_SHORT).show();

                        // 1. Limpia la lista del adaptador
                        listaArrayVentas.clear();

                        // 2. Notifica al adaptador que los datos han cambiado
                        adapter.notifyDataSetChanged();

                        // 3. Actualiza la UI
                        onDataChanged();

                                       } else {
                        Toast.makeText(this, "Error al eliminar la base de datos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .setIcon(R.drawable.eliminar)
                .show();
    }

    @Override
    protected void onDestroy() {
        if (bdVentas != null) {
            bdVentas.close(); // Cierra la base de datos
            bdVentas = null; // Establece bdHelper a null
        }
        super.onDestroy();
    }
}