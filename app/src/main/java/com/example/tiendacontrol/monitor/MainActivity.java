package com.example.tiendacontrol.monitor;
import static androidx.core.content.ContentProviderCompat.requireContext;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
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

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, MenuDialogFragment.MainActivityListener {
    // Declaración de variables
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 100;
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
    private BaseExporter baseExporter;
    private BaseExporter baseInporter;
    private ActivityResultLauncher<String[]> requestStoragePermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialización de Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Referencias a vistas
        imageViewProfile = findViewById(R.id.imageViewProfile);
        txtBuscar = findViewById(R.id.txtBuscar);
        listaVentas = findViewById(R.id.listaVentas);
        fabNuevo = findViewById(R.id.favNuevo);
        fabMenu = findViewById(R.id.fabMenu);
        fabGasto = findViewById(R.id.favGasto);
        textVenta = findViewById(R.id.textVenta);
        textGanacia = findViewById(R.id.textGanacia);
        textGasto = findViewById(R.id.textGasto);

        // Configuración del RecyclerView
        listaVentas.setLayoutManager(new LinearLayoutManager(this));
        baseExporter = new BaseExporter(this);

        // Inicializar SearchView
        txtBuscar = findViewById(R.id.txtBuscar);

        // Configurar el listener para el SearchView
        txtBuscar.setOnQueryTextListener(this);

        // Inicialización de la base de datos y el adaptador
        BdVentas bdVentas = new BdVentas(MainActivity.this);
        listaArrayVentas = new ArrayList<>(bdVentas.mostrarVentas());
        adapter = new BaseDatosAdapter(bdVentas.mostrarVentas());
        listaVentas.setAdapter(adapter);


        // Inicializar el BdHelper
        bdHelper = new BdHelper(this);

        // Configuración de los botones flotantes
        fabGasto.setOnClickListener(view -> {
            GastoDialogFragment dialogFragment = new GastoDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "GastoDialogFragment");
        });

        fabNuevo.setOnClickListener(view -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            IngresoDialogFragment ingresoDialogFragment = IngresoDialogFragment.newInstance();
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

        // Calcular y mostrar las sumas iniciales
        calcularSumaGanancias();
        calcularSumaTotalVenta();
        calcularSumaTotalGasto();

        // Obtener el usuario actual de Firebase Authentication
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();

            // Cargar la imagen de perfil del usuario
            loadProfileImage(userId);
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }
        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PerfilUsuario.class);
                startActivity(intent);
            }
        });

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
    }
    //Este método se llama cuando el usuario envía el texto en el campo de búsqueda.
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
    //Este método se llama cada vez que el texto en el campo de búsqueda cambia.
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
        // Crear y mostrar el diálogo
        new AlertDialog.Builder(this)
                .setTitle("Eliminar base de datos")
                .setMessage("¿Estás seguro de que deseas eliminar toda la base de datos?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    BdVentas bdVentas = new BdVentas(this); // Crear instancia de BdVentas
                    boolean resultado = bdVentas.eliminarTodo(); // Llamar al método para eliminar datos
                    if (resultado) {
                        Toast.makeText(this, "Base de datos eliminada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al eliminar la base de datos", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Acción al cancelar
                })
                .setIcon(R.drawable.baseline_delete_forever_24)
                .show();
    }
//    // Método para verificar si el permiso de escritura en almacenamiento externo está concedido
//    private boolean isStoragePermissionGranted() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            return Environment.isExternalStorageManager();
//        } else {
//            return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    == PackageManager.PERMISSION_GRANTED;
//        }
//    }
//
//    // Método para solicitar el permiso de escritura en almacenamiento externo
//    private void requestStoragePermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            // Solicitar permiso MANAGE_EXTERNAL_STORAGE
//            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
//            intent.addCategory(Intent.CATEGORY_DEFAULT);
//            intent.setData(Uri.parse("package:" + getPackageName()));
//            startActivityForResult(intent, REQUEST_CODE_STORAGE_PERMISSION);
//        } else {
//            // Solicitar permiso WRITE_EXTERNAL_STORAGE
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    REQUEST_CODE_STORAGE_PERMISSION);
//        }
//    }

//    // Método para manejar el resultado de la solicitud de permisos
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permiso concedido
//                Toast.makeText(this, "Permiso de almacenamiento concedido", Toast.LENGTH_SHORT).show();
//                // Realiza la operación que requería el permiso
//            } else {
//                // Permiso denegado
//                Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
}