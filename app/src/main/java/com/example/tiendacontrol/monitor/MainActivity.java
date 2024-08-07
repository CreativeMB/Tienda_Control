package com.example.tiendacontrol.monitor;

import static android.content.ContentValues.TAG;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendacontrol.dialogFragment.IngresoDialogFragment;
import com.example.tiendacontrol.dialogFragment.MenuDialogFragment;
import com.example.tiendacontrol.helper.BaseExporter;
import com.example.tiendacontrol.helper.BdHelper;
import com.example.tiendacontrol.helper.BdVentas;
import java.text.NumberFormat;
import java.util.Locale;
import com.example.tiendacontrol.R;
import com.example.tiendacontrol.adapter.BaseDatosAdapter;
import com.example.tiendacontrol.dialogFragment.GastoDialogFragment;
import com.example.tiendacontrol.model.Items;
import com.example.tiendacontrol.login.PerfilUsuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, MenuDialogFragment.MainActivityListener, IngresoDialogFragment.OnDataChangedListener, GastoDialogFragment.OnDataChangedListener {
    // Declaración de variables
    private static final String PREFS_NAME = "TiendaControlPrefs";
    private static final String KEY_CURRENT_DATABASE = "currentDatabase";
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 100;

    private SharedPreferences sharedPreferences;

    private SearchView txtBuscar;
    private RecyclerView listaVentas;
    private ArrayList<Items> listaArrayVentas;
    private BaseDatosAdapter adapter;
    private FloatingActionButton fabNuevo, fabGasto, fabMenu;
    private TextView textVenta, textGanacia, textGasto;
    private ImageView imageViewProfile;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    private BdHelper bdHelper;
    private ActivityResultLauncher<String[]> requestStoragePermissionLauncher;
    private String currentDatabase; // Variable para almacenar el nombre de la base de datos
    private boolean userLoggedIn; // Flag para indicar si el usuario está autenticado
    private BdVentas bdVentas; // Declaración de la variable BdVentas
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialización de Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // Inicializar SharedPreferences (dentro del método onCreate)
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentDatabase = getCurrentDatabaseName();
// 1. Obtén el nombre de la base de datos actual de SharedPreferences
        currentDatabase = sharedPreferences.getString(KEY_CURRENT_DATABASE, null);

        // Referencias a vistas
        imageViewProfile = findViewById(R.id.imageViewProfile);
        listaVentas = findViewById(R.id.listaVentas);
        fabNuevo = findViewById(R.id.favNuevo);
        fabMenu = findViewById(R.id.fabMenu);
        fabGasto = findViewById(R.id.favGasto);
        textVenta = findViewById(R.id.textVenta);
        textGanacia = findViewById(R.id.textGanacia);
        textGasto = findViewById(R.id.textGasto);
        txtBuscar = findViewById(R.id.txtBuscar);

        // Configuración del RecyclerView
        listaVentas.setLayoutManager(new GridLayoutManager(this, 2));

        // Inicializar el adaptador AQUÍ, solo una vez
        listaArrayVentas = new ArrayList<>();
        adapter = new BaseDatosAdapter(listaArrayVentas); // Pasar la lista vacía al adaptador
        listaVentas.setAdapter(adapter);



        // Verificar si el Intent contiene un nombre de base de datos
        Intent intent = getIntent();
        if (intent.hasExtra("databaseName")) {
            currentDatabase = intent.getStringExtra("databaseName");
        } else {
            currentDatabase = sharedPreferences.getString(KEY_CURRENT_DATABASE, "nombre_por_defecto.db");
        }
        // Crea una instancia de BdHelper (solo una vez)
        bdHelper = new BdHelper(this, currentDatabase);
        bdVentas = new BdVentas(this, currentDatabase);

        // Crear una instancia de BaseExporter
        BaseExporter exporter = new BaseExporter(this, this, currentDatabase);


        // Cargar la imagen de perfil del usuario si ya está autenticado
        cargarimperfil();

// Inicializar SearchView
        txtBuscar.setOnQueryTextListener(this);
        // Configuración de los botones flotantes

        fabGasto.setOnClickListener(view -> {
            GastoDialogFragment dialogFragment = new GastoDialogFragment();
            // Crea una nueva instancia de IngresoDialogFragment con el nombre de la base de datos actual
            GastoDialogFragment gastoDialogFragment = GastoDialogFragment.newInstance(currentDatabase);
            gastoDialogFragment.setDataChangedListener(this); // Ahora debería funcionar sin errores
            gastoDialogFragment.show(getSupportFragmentManager(), "GastoDialogFragment");
        });

        fabNuevo.setOnClickListener(view -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            // Crea una nueva instancia de IngresoDialogFragment con el nombre de la base de datos actual
            IngresoDialogFragment ingresoDialogFragment = IngresoDialogFragment.newInstance(currentDatabase);
            ingresoDialogFragment.setDataChangedListener( this); // Asegúrate de esta línea
            ingresoDialogFragment.show(fragmentManager, "ingreso_dialog");
        });

        fabMenu.setOnClickListener(view -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            MenuDialogFragment menuDialogFragment = new MenuDialogFragment();
            menuDialogFragment.setListener(this);
            menuDialogFragment.show(fragmentManager, "MenuDialogFragment");
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
                        // Realiza la operación que requería el permiso
                    }
                }
        );


        // Configurar OnClickListener para abrir Negativo
        textGasto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EgresoTotal.class);
                startActivity(intent);
            }
        });

        // Configurar OnClickListener para abrir Positivo
        textVenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, IngresoTotal.class);
                startActivity(intent);
            }
        });

        // Configurar OnClickListener para abrir Ganancia
        textGanacia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, IngresoEgreso.class);
                startActivity(intent);
            }
        });

        // Configurar OnClickListener para el ImageView de perfil
        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verifica si el usuario está autenticado antes de abrir la actividad
                if (mAuth.getCurrentUser() != null) {
                    Intent intent = new Intent(MainActivity.this, PerfilUsuario.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Debes estar autenticado para acceder a este perfil", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

            if (bdHelper != null) {
                Log.d(TAG, "onNewIntent: Cerrando bdHelper existente");
                bdHelper.close();
            }

            bdHelper = new BdHelper(this, currentDatabase);
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

        bdHelper = new BdHelper(this, currentDatabase);
        bdVentas = new BdVentas(this, currentDatabase);

        if (adapter == null) {
            Log.d(TAG, "onResume: Creando nuevo adaptador y lista");
            listaArrayVentas = new ArrayList<>();
            adapter = new BaseDatosAdapter(listaArrayVentas);
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

            adapter.notifyDataSetChanged(); // Notifica al adaptador sobre el cambio
            Log.d("MainActivity", "RecyclerView actualizado, tamaño de la lista: " + listaArrayVentas.size());
            calcularSumaGanancias();
            calcularSumaTotalVenta();
            calcularSumaTotalGasto();
        }
    }

    private void cargarimperfil() {

        if (userLoggedIn) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                userId = user.getUid();
                loadProfileImage(userId);
                imageViewProfile.setVisibility(View.VISIBLE);
            }
        } else {
            imageViewProfile.setVisibility(View.GONE);
        }
    }

    // Método para obtener el nombre de la base de datos desde SharedPreferences
    private String getCurrentDatabaseName() {
        return sharedPreferences.getString(KEY_CURRENT_DATABASE, "nombre_por_defecto.db");
    }

    // Método para actualizar la base de datos actual
    private void setCurrentDatabaseName(String dbName) {
        currentDatabase = dbName;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CURRENT_DATABASE, currentDatabase);
        editor.apply();
    }
    @Override
    protected void onStart() {

        super.onStart();
               FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userLoggedIn = true;
            // Ya hay un usuario autenticado, actualiza el estado y la imagen de perfil
            userId = user.getUid();
            loadProfileImage(userId);
            imageViewProfile.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Autenticado", Toast.LENGTH_LONG).show();
        } else {
            // No hay un usuario autenticado, muestra el mensaje "No autenticado"
            userLoggedIn = false;
            imageViewProfile.setVisibility(View.GONE);
            Toast.makeText(this, "No autenticado", Toast.LENGTH_LONG).show();
        }
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

    // Método para calcular y mostrar la suma total de gastos sin decimales
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
                .setTitle("Eliminar base de datos")
                .setMessage("¿Estás seguro de que deseas eliminar toda la base de datos?")
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

                        // 4. Opcional: Reinicia la actividad para una actualización completa
                        // Intent intent = getIntent();
                        // finish();
                        // startActivity(intent);
                    } else {
                        Toast.makeText(this, "Error al eliminar la base de datos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .setIcon(R.drawable.baseline_delete_forever_24)
                .show();
    }

    @Override
    protected void onDestroy() {
        if (bdHelper != null) {
            bdHelper.close(); // Cierra la base de datos
            bdHelper = null; // Establece bdHelper a null
        }
        super.onDestroy();
    }
}