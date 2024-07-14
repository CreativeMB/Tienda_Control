package com.example.tiendacontrol;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tiendacontrol.Bd.BdHelper;
import com.example.tiendacontrol.Bd.BdVentas;
import java.text.NumberFormat;
import java.util.Locale;
import com.example.tiendacontrol.adaptadores.ListaVentasAdapter;
import com.example.tiendacontrol.dialogFragment.GastoDialogFragment;
import com.example.tiendacontrol.dropbox.DropboxManager;
import com.example.tiendacontrol.entidades.Ventas;
import com.example.tiendacontrol.login.Login;
import com.example.tiendacontrol.login.PerfilUsuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

// Declaración de variables
    private static final int REQUEST_CODE_PERFIL_USUARIO = 1;
    private SearchView txtBuscar;
    private RecyclerView listaVentas;
    private ArrayList<Ventas> listaArrayVentas;
    private ListaVentasAdapter adapter;
    private FloatingActionButton fabNuevo, fabGasto;
    private TextView textVenta, textTotal, textGasto;
    private Toolbar toolbar;
    private ImageView imageViewProfile;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

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
        fabGasto = findViewById(R.id.favGasto);
        textVenta = findViewById(R.id.textVenta);
        textTotal = findViewById(R.id.textTotal);
        textGasto = findViewById(R.id.textGasto);
        toolbar = findViewById(R.id.toolbar);

        // Configuración de la Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tienda Control");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Configuración del RecyclerView
        listaVentas.setLayoutManager(new LinearLayoutManager(this));

        // Inicialización de la base de datos y el adaptador
        BdVentas bdVentas = new BdVentas(MainActivity.this);
        listaArrayVentas = new ArrayList<>(bdVentas.mostrarVentas());
        adapter = new ListaVentasAdapter(bdVentas.mostrarVentas());
        listaVentas.setAdapter(adapter);

        // Configuración de los botones flotantes
        fabGasto.setOnClickListener(view -> {
            GastoDialogFragment dialogFragment = new GastoDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "GastoDialogFragment");
        });

        fabNuevo.setOnClickListener(view -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            com.example.tiendacontrol.IngresoDialogFragment ingresoDialogFragment = com.example.tiendacontrol.IngresoDialogFragment.newInstance();
            ingresoDialogFragment.show(fragmentManager, "ingreso_dialog");
        });

        // Configuración del SearchView
        txtBuscar.setOnQueryTextListener(this);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Manejo de opciones del menú
        if (id == R.id.exportar_db) {
            // Iniciar el flujo de autenticación y subida a Dropbox
            DropboxManager dropboxManager = DropboxManager.getInstance(this);
            dropboxManager.authenticate();
            return true;
        } else if (id == R.id.nueva_venta) {
            // Mostrar el diálogo de nueva venta
            FragmentManager fragmentManager = getSupportFragmentManager();
            com.example.tiendacontrol.IngresoDialogFragment ingresoDialogFragment = com.example.tiendacontrol.IngresoDialogFragment.newInstance();
            ingresoDialogFragment.show(fragmentManager, "ingreso_dialog");
            return true;
        } else if (id == R.id.nuevo_gasto) {
            // Mostrar el diálogo de nuevo gasto
            GastoDialogFragment dialogFragment = new GastoDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "GastoDialogFragment");
            return true;
        } else if (id == R.id.perfil_usuario) {
            // Ir a la pantalla de perfil de usuario
            Intent intent = new Intent(this, PerfilUsuario.class);
            startActivityForResult(intent, REQUEST_CODE_PERFIL_USUARIO);
            return true;
        } else if (id == R.id.salir) {
            // Dirigir al usuario a la pantalla de inicio de sesión
            dirigirAInicioSesion();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Manejo del resultado de la actividad de perfil de usuario
        if (requestCode == REQUEST_CODE_PERFIL_USUARIO && resultCode == RESULT_OK) {
            // Aquí puedes actualizar la vista de MainActivity si es necesario
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Filtrar el RecyclerView según el texto de búsqueda
        adapter.filtrado(newText);
        return false;
    }

    // Método para dirigir al usuario a la pantalla de inicio de sesión
    private void dirigirAInicioSesion() {
        Intent intent = new Intent(this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAffinity(); // Cierra todas las actividades en la pila de tareas
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
        for (Ventas venta : listaArrayVentas) {
            double valorVenta = venta.getValorAsDouble();
            suma += valorVenta;
        }

        // Redondear suma a valor entero
        long sumaEntera = Math.round(suma);

        // Formatear la suma como moneda colombiana sin decimales
        String sumaFormateadaStr = NumberFormat.getCurrencyInstance(new Locale("es", "CO")).format(sumaEntera);
        // Eliminar decimales si hay .00
        sumaFormateadaStr = sumaFormateadaStr.replaceAll("[,.]00$", "");

        textTotal.setText(sumaFormateadaStr);
    }

    // Método para calcular y mostrar la suma total de ventas sin decimales
    private void calcularSumaTotalVenta() {
        double suma = 0.0;
        for (Ventas venta : listaArrayVentas) {
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
        for (Ventas venta : listaArrayVentas) {
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
}





//    private void exportarBaseDatos() {
//        // Nombre de tu base de datos SQLite
//        String nombreBaseDatos = "MI_contabilidad.db";
//
//        File dbFile = this.getDatabasePath(nombreBaseDatos);
//        if (dbFile.exists()) {
//            // Ejecutar AsyncTask para exportar la base de datos a Dropbox
//            new ExportarBaseDatosTask().execute(nombreBaseDatos);
//        } else {
//            Toast.makeText(MainActivity.this, "Base de datos no encontrada", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    // AsyncTask para exportar la base de datos a Dropbox
//    private class ExportarBaseDatosTask extends AsyncTask<String, Void, Boolean> {
//
//        @Override
//        protected Boolean doInBackground(String... strings) {
//            try {
//                // Llamar al método exportarBaseDatos de DropboxHelper
//                dropboxHelper.exportarBaseDatos(strings[0]);
//                return true; // Éxito
//            } catch (Exception e) {
//                e.printStackTrace();
//                return false; // Error
//            }
//        }
//
//        @Override
//        protected void onPostExecute(Boolean success) {
//            if (success) {
//                Toast.makeText(MainActivity.this, "Base de datos exportada correctamente", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(MainActivity.this, "Error al exportar base de datos", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
