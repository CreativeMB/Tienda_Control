package com.example.tiendacontrol.monitor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendacontrol.dialogFragment.MenuDialogFragment;
import com.example.tiendacontrol.dialogFragment.IngresoDialogFragment;

import com.example.tiendacontrol.helper.BaseExporter;
import com.example.tiendacontrol.helper.BdHelper;
import com.example.tiendacontrol.helper.BdVentas;

import java.text.NumberFormat;
import java.util.Locale;

import com.example.tiendacontrol.helper.ExcelExporter;
import com.example.tiendacontrol.R;
import com.example.tiendacontrol.adapter.ListaVentasAdapter;
import com.example.tiendacontrol.dialogFragment.GastoDialogFragment;
import com.example.tiendacontrol.model.Ventas;
import com.example.tiendacontrol.login.Login;
import com.example.tiendacontrol.login.PerfilUsuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    // Declaración de variables
    private static final String DATABASE_NAME = "MI_contabilidad.db";
    private static final int REQUEST_CODE_WRITE_STORAGE_PERMISSION = 100;
    public static final int REQUEST_CODE_PERFIL_USUARIO = 1;
    // Constante para el código de solicitud de permiso de almacenamiento
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 100;
    private SearchView txtBuscar;
    private RecyclerView listaVentas;
    private ArrayList<Ventas> listaArrayVentas;
    private ListaVentasAdapter adapter;
    private FloatingActionButton fabNuevo, fabGasto, fabMenu;
    private TextView textVenta, textTotal, textGasto;
    private Toolbar toolbar;
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
        textTotal = findViewById(R.id.textTotal);
        textGasto = findViewById(R.id.textGasto);
        toolbar = findViewById(R.id.toolbar);

        // Configuración de la Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tienda Control");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Configuración del RecyclerView
        listaVentas.setLayoutManager(new LinearLayoutManager(this));

        baseExporter = new BaseExporter(this);

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
            IngresoDialogFragment ingresoDialogFragment = IngresoDialogFragment.newInstance();
            ingresoDialogFragment.show(fragmentManager, "ingreso_dialog");
        });
        fabMenu.setOnClickListener(view -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            MenuDialogFragment menuDialogFragment = MenuDialogFragment.newInstance();
            menuDialogFragment.show(getSupportFragmentManager(), "MenuDialogFragment");
        });

        // Configuración del SearchView
        txtBuscar.setOnQueryTextListener(this);

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
// finaliza el lanzador para la solicitud de permisos

        // Calcular y mostrar las sumas iniciales
        calcularSumaGanancias();
        calcularSumaTotalVenta();
        calcularSumaTotalGasto();

        // Inicializar el BdHelper
        bdHelper = new BdHelper(this);
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
            // Verificar y solicitar permisos de escritura en almacenamiento externo si es necesario
            if (isStoragePermissionGranted()) {
                baseExporter.exportDatabase(getDatabasePath(BdHelper.DATABASE_NAME).getAbsolutePath());
            } else {
                requestStoragePermissionLauncher.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
            }
            return true;
        } else if (id == R.id.inportar_db) {
            // Verificar y solicitar permisos de escritura en almacenamiento externo si es necesario
            if (isStoragePermissionGranted()) {
                // Obtener la instancia de BaseExporter y llamar al método de importación desde descargas
                BaseExporter baseImporter = new BaseExporter(MainActivity.this);
                baseImporter.importDatabase(BdHelper.DATABASE_NAME);
            } else {
                requestStoragePermissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
            }
            return true;
        } else if (id == R.id.exportar_exel) {
            if (isStoragePermissionGranted()) {
                ExcelExporter.exportToExcel(MainActivity.this);
            } else {
                requestStoragePermissionLauncher.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
            }
            return true;
        } else if (id == R.id.nueva_venta) {
            // Mostrar el diálogo de nueva venta
            FragmentManager fragmentManager = getSupportFragmentManager();
            IngresoDialogFragment ingresoDialogFragment = IngresoDialogFragment.newInstance();
            ingresoDialogFragment.show(fragmentManager, "ingreso_dialog");
            return true;
        } else if (id == R.id.nuevo_gasto) {
            // Mostrar el diálogo de ingreso egreso
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
    // Método para verificar si los permisos de almacenamiento están concedidos
    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                return Environment.isExternalStorageManager();
               // Permiso concedido
            } else {
                // Solicitar permiso MANAGE_EXTERNAL_STORAGE
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                    startActivityForResult(intent, REQUEST_CODE_STORAGE_PERMISSION);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, REQUEST_CODE_STORAGE_PERMISSION);
                }
                return false; // Permiso no concedido aún
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true; // Permiso concedido
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION);
                return false; // Permiso no concedido aún
            }
        } else {
            // Verifica si el permiso ya ha sido concedido
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true; // Permiso concedido
            } else {
                // Si el permiso no ha sido concedido, solicítalo al usuario
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION);
                // El resultado de la solicitud se manejará en onRequestPermissionsResult()
                return false; // Permiso no concedido aún
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos de almacenamiento concedidos", Toast.LENGTH_SHORT).show();
                // Continuar con la lógica de la aplicación
            } else {
                Toast.makeText(this, "Permisos de almacenamiento denegados", Toast.LENGTH_SHORT).show();
            }
        }

    }
//    public void requestWriteStoragePermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    != PackageManager.PERMISSION_GRANTED) {
//                // No se ha concedido el permiso, se solicita al usuario
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        REQUEST_CODE_WRITE_STORAGE_PERMISSION);
//            } else {
//                // Permiso ya concedido, realiza la operación que requiere el permiso
//                Toast.makeText(this, "Permiso de escritura en almacenamiento externo concedido",
//                        Toast.LENGTH_SHORT).show();
//                // Aquí puedes realizar la operación que necesitabas el permiso
//            }
//        } else {
//            // Versiones anteriores a Marshmallow, el permiso se considera concedido
//            Toast.makeText(this, "Permiso de escritura en almacenamiento externo concedido",
//                    Toast.LENGTH_SHORT).show();
//            // Aquí puedes realizar la operación que necesitabas el permiso
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "Permisos de almacenamiento concedidos", Toast.LENGTH_SHORT).show();
                    // Continuar con la lógica de la aplicación
                } else {
                    Toast.makeText(this, "Permisos de almacenamiento denegados", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    public BaseExporter getDatabaseManager() {
        return baseExporter;
    }
}

//    // Método para exportar la base de datos SQLite
//    private void exportDatabase() {
//        Log.d(TAG, "Iniciando exportación de la base de datos.");
//
//        // Obtener la ruta de la base de datos original
//        File dbFile = getDatabasePath(BdHelper.DATABASE_NAME);
//        Log.d(TAG, "Ruta de la base de datos original: " + dbFile.getAbsolutePath());
//
//        // Define la ruta de destino para la exportación en la carpeta de descargas
//        File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        File exportFile = new File(exportDir, BdHelper.DATABASE_NAME);
//        Log.d(TAG, "Ruta de destino para la exportación: " + exportFile.getAbsolutePath());
//
//        try {
//            // Copia el archivo de la base de datos a la nueva ubicación
//            copyFile(dbFile, exportFile);
//            Log.d(TAG, "Base de datos exportada exitosamente a: " + exportFile.getAbsolutePath());
//
//            // Mostrar mensaje al usuario con la ruta de descarga
//            Toast.makeText(this, "Base de datos exportada correctamente a " + exportFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
//
//            // Puedes realizar otras acciones después de la exportación exitosa si es necesario
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.e(TAG, "Error al exportar la base de datos: " + e.getMessage());
//            // Manejar cualquier excepción que pueda ocurrir durante la copia del archivo
//
//            // Mostrar mensaje de error al usuario si es necesario
//            Toast.makeText(this, "Error al exportar la base de datos", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    // Método para copiar archivos
//    private void copyFile(File sourceFile, File destFile) throws IOException {
//        FileChannel sourceChannel = null;
//        FileChannel destChannel = null;
//
//        try {
//            sourceChannel = new FileInputStream(sourceFile).getChannel();
//            destChannel = new FileOutputStream(destFile).getChannel();
//            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
//        } finally {
//            if (sourceChannel != null) {
//                sourceChannel.close();
//            }
//            if (destChannel != null) {
//                destChannel.close();
//            }
//        }
//        if (!sourceFile.exists()) {
//            throw new IOException("Archivo de origen no existe: " + sourceFile.getAbsolutePath());
//        }
//
//        try (InputStream inputStream = new FileInputStream(sourceFile);
//             OutputStream outputStream = new FileOutputStream(destFile)) {
//
//            byte[] buffer = new byte[1024];
//            int length;
//            while ((length = inputStream.read(buffer)) > 0) {
//                outputStream.write(buffer, 0, length);
//            }
//
//        } catch (IOException e) {
//            throw new IOException("Error al copiar archivo: " + e.getMessage());
//        }
//    }
        // Método para verificar y solicitar permisos de escritura en almacenamiento externo


//    private void importarBaseDeDatos() {
//        Log.d(TAG, "Iniciando importación de la base de datos.");
//
//        // Ruta del archivo exportado en la carpeta de descargas
//        File archivoImportado = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), BdHelper.DATABASE_NAME);
//        Log.d(TAG, "Ruta del archivo a importar: " + archivoImportado.getAbsolutePath());
//
//        // Obtener la ruta de la base de datos actual
//        File dbFile = getDatabasePath(BdHelper.DATABASE_NAME);
//
//        try {
//            // Copiar el archivo exportado a la ubicación de la base de datos actual
//            copyFile(archivoImportado, dbFile);
//            Log.d(TAG, "Base de datos importada exitosamente desde: " + archivoImportado.getAbsolutePath());
//
//            // Mostrar mensaje al usuario
//            Toast.makeText(this, "Base de datos importada correctamente", Toast.LENGTH_SHORT).show();
//
//            // Actualizar la lista de ventas u otra operación necesaria después de la importación
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.e(TAG, "Error al importar la base de datos: " + e.getMessage());
//            // Manejar cualquier excepción que pueda ocurrir durante la copia del archivo
//
//            // Mostrar mensaje de error al usuario si es necesario
//            Toast.makeText(this, "Error al importar (MI_contabilidad.db no esta descargas)", Toast.LENGTH_LONG).show();
//        }
//    }

