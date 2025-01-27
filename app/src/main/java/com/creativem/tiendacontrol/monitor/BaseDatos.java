package com.creativem.tiendacontrol.monitor;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.creativem.tiendacontrol.Login;
import com.creativem.tiendacontrol.R;
import com.creativem.tiendacontrol.SessionManager;
import com.creativem.tiendacontrol.adapter.BasesAdapter;
import com.creativem.tiendacontrol.helper.ExcelExporter;
import com.creativem.tiendacontrol.model.AnimacionInicio;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class BaseDatos extends AppCompatActivity implements BasesAdapter.OnDatabaseClickListener {

    private static final String LOGIN_STATUS = "loginStatus";
    private static final String PREFS_NAME = "CodePrefs";
    private static final String TAG = "BaseDatos";
    private int completedTasks = 0;
    private int totalTasks = 0;
    private Uri fileUri;
    private List<String> databaseNames = new ArrayList<>();
    private boolean allTasksCompleted = false;
    private FirebaseAuth mAuth;
    private GoogleSignInClient gso;
    private SessionManager sessionManager;
    private static final int REQUEST_CODE_EXACT_ALARM = 1;
    private Calendar selectedTime;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private RecyclerView recyclerViewDatabases;
    private SharedPreferences sharedPreferences;
    private TextView navHeaderName, navHeaderEmail, navHeaderPerson, navHeaderAddress,
            navHeaderCity, navHeaderCountry, navHeaderPhone;
    private ImageView navHeaderImage;

    private static final String KEY_CURRENT_DATABASE = "currentDatabase";
    private List<String> databaseList;
    private BasesAdapter adapter;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private String databasePath;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basedatos);
        mAuth = FirebaseAuth.getInstance();
        // Configurar Google Sign-In
        GoogleSignInOptions gsoOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        gso = GoogleSignIn.getClient(this, gsoOptions);

        sessionManager = new SessionManager(this);

        ImageView iconRecordatorio = findViewById(R.id.recordatorio);
        iconRecordatorio.setOnClickListener(view -> showTimePickerDialog());

        ImageView iconCreateDatabase = findViewById(R.id.database);
        iconCreateDatabase.setOnClickListener(v -> showDatabaseNameDialog());

        ImageView iconDonacion = findViewById(R.id.donacion);
        ImageView imageManual = findViewById(R.id.manual);

        recyclerViewDatabases = findViewById(R.id.recyclerViewDatabases);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        configurarRecyclerViewDatabases();


        iconDonacion.setOnClickListener(view -> {
            Intent databaseIntent = new Intent(this, Donar.class);
            startActivity(databaseIntent);
        });

        imageManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // URL al que quieres dirigir al usuario
                String url = "https://www.floristerialoslirios.com/tienda-control";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        // Inicializa los componentes del layout
        ImageView iconMenu = findViewById(R.id.menu);
        drawerLayout = findViewById(R.id.drawerLayout);
        navView = findViewById(R.id.nav_view);
        // Obtener el header del navigation view
        View headerView = navView.getHeaderView(0);
        // Referencias a los componentes del diseño del header
        navHeaderName = headerView.findViewById(R.id.nav_empresa);
        navHeaderImage = headerView.findViewById(R.id.nav_logo);
        navHeaderEmail = headerView.findViewById(R.id.nav_header_email);
        navHeaderPerson = headerView.findViewById(R.id.nav_nombre);
        navHeaderAddress = headerView.findViewById(R.id.nav_direcion);
        navHeaderCity = headerView.findViewById(R.id.nav_ciudad);
        navHeaderCountry = headerView.findViewById(R.id.nav_pais);
        navHeaderPhone = headerView.findViewById(R.id.nav_telefono);

        // Cargar los datos del usuario
        obtenerDatosEmpresa();

        iconMenu.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(navView)) {
                drawerLayout.closeDrawer(navView);
            } else {
                drawerLayout.openDrawer(navView);
            }
        });
        // Configurar el Listener para NavigationView
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                try {
                    if (id == R.id.loslirios) {
                        Intent intent = new Intent(BaseDatos.this, LosLirios.class);
                        startActivity(intent);
                    } else if (id == R.id.codeItem) {
                        // Acción para Código
                        Intent intent = new Intent(BaseDatos.this, EdicionPin.class);
                        startActivity(intent);
                    } else if (id == R.id.donaItem) {
                        // Acción para Donar
                        Intent intent = new Intent(BaseDatos.this, Donar.class);
                        startActivity(intent);
                    } else if (id == R.id.manual) {
                        String url = "https://www.floristerialoslirios.com/tienda-control";
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        return true;
                    } else if (id == R.id.contabilidad) {
                        // Acción para Donar
                        Intent intent = new Intent(BaseDatos.this, FiltroDiaMesAnoActivity.class);
                        startActivity(intent);
                    } else if (id == R.id.exel) {
                        exportAllDatabasesSequentially();
                    }
                    else if (id == R.id.salirItem) {
                        mAuth.signOut();
                        gso.signOut().addOnCompleteListener(task -> {
                            sessionManager.setLoggedIn(false);
                            Intent intent = new Intent(BaseDatos.this, Login.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(BaseDatos.this, "Ocurrió un error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                // Cerrar el menú después de la selección
                drawerLayout.closeDrawers();
                return true;
            }
        });


        // Recarga la lista de bace de datos disponibles en le carpeta de la apliccion
        loadDatabases();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            Toast.makeText(this, "No se encuentra logueado el usuario", Toast.LENGTH_SHORT).show();
        }
    }

    private void configurarRecyclerViewDatabases() { // Renamed for clarity
        int orientation = getResources().getConfiguration().orientation;
        RecyclerView.LayoutManager layoutManager;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutManager = new LinearLayoutManager(this); // Vertical by default
        } else { // Landscape orientation
            layoutManager = new GridLayoutManager(this, calculateNoOfColumns());
        }

        recyclerViewDatabases.setLayoutManager(layoutManager); // Use recyclerViewDatabases here

        databaseList = new ArrayList<>();
        adapter = new BasesAdapter(this, databaseList, this);
        recyclerViewDatabases.setAdapter(adapter);
    }

    private int calculateNoOfColumns() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        float columnWidth = 350; // Desired column width in dp (adjust as needed)
        return (int) (dpWidth / columnWidth);
    }

    private void obtenerDatosEmpresa() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Log.e(TAG, "Error: Usuario no autenticado.");
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        Log.d(TAG, "UserID: " + userId);


        String email = user.getEmail();
        if (email != null && navHeaderEmail != null) {
            navHeaderEmail.setText(email);
        }

        // Cargar foto de perfil (si existe)
        Uri photoUrl = user.getPhotoUrl();
        if(photoUrl!= null) {
            Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.icono)
                    .error(R.drawable.icono)
                    .into(navHeaderImage);
        } else {
            // Si no hay foto de perfil, establecer la imagen por defecto
            navHeaderImage.setImageResource(R.drawable.icono);
            Log.w(TAG, "No se encontró foto de perfil para el usuario.");
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Empresas");
        Query query = databaseReference.orderByChild("userId").equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot empresaSnapshot : snapshot.getChildren()) {
                        String nombreEmpresa = empresaSnapshot.child("nombreEmpresa").getValue(String.class);
                        String nombrePersona = empresaSnapshot.child("nombrePersona").getValue(String.class);
                        String direccion = empresaSnapshot.child("direccion").getValue(String.class);
                        String ciudad = empresaSnapshot.child("ciudad").getValue(String.class);
                        String pais = empresaSnapshot.child("pais").getValue(String.class);
                        String telefono = empresaSnapshot.child("telefono").getValue(String.class);

                        if (navHeaderName != null) navHeaderName.setText(nombreEmpresa != null ? nombreEmpresa : "");
                        if (navHeaderPerson != null) navHeaderPerson.setText(nombrePersona != null ? nombrePersona : "");
                        if (navHeaderAddress != null) navHeaderAddress.setText(direccion != null ? direccion : "");
                        if (navHeaderCity != null) navHeaderCity.setText(ciudad != null ? ciudad : "");
                        if (navHeaderCountry != null) navHeaderCountry.setText(pais != null ? pais : "");
                        if (navHeaderPhone != null) navHeaderPhone.setText(telefono != null ? telefono : "");
                    }
                } else {
                    Log.w(TAG, "No se encontraron empresas registradas para el usuario.");
                    Toast.makeText(BaseDatos.this, "No se encontraron empresas registradas.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al obtener datos: " + error.getMessage());
                Toast.makeText(BaseDatos.this, "Error al obtener datos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showDatabaseNameDialog() {
        final EditText input = new EditText(this);
        input.setHint("Nombre de la base de datos (sin espacios ni emojis)");

        new AlertDialog.Builder(this)
                .setTitle("Crear Base de Datos")
                .setMessage("Ingrese el nombre para la nueva base de datos (sin espacios ni emojis):")
                .setView(input)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String databaseName = input.getText().toString().trim();
                    if (isValidDatabaseName(databaseName)) {
                        checkAndCreateDatabase(databaseName);
                    } else {
                        showToast("El nombre no debe contener espacios ni emojis.");
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private boolean isValidDatabaseName(String databaseName) {
        //Expresión regular para detectar espacios o emojis
        return !databaseName.isEmpty() && !databaseName.contains(" ") && !containsEmoji(databaseName);
    }

    private boolean containsEmoji(String text) {
        //Expresión regular para detectar emojis
        return text.matches(".*[\\p{Emoji}].*");
    }

    private void checkAndCreateDatabase(String databaseName) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Log.e(TAG, "Usuario no autenticado");
            showToast("Usuario no autenticado");
            return;
        }

        // Create date string in Colombia time zone
        SimpleDateFormat sdfColombia = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdfColombia.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
        String dateString = sdfColombia.format(new Date()); // Use new Date() for current time


        DatabaseReference userDatabasesRef = database.getReference("users").child(userId).child("databases");
        Map<String,Object> databaseData = new HashMap<>();
        databaseData.put("timestamp", ServerValue.TIMESTAMP);
        databaseData.put("fechaCreacion", dateString);
        userDatabasesRef.child(databaseName).setValue(databaseData).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                showToast("Base de datos creada en Firebase");
                Log.d(TAG, "Base de datos creada en Firebase");
                loadDatabases();
            } else {
                showToast("Error al crear base de datos en Firebase: " + task.getException());
                Log.e(TAG, "Error al crear base de datos en Firebase: " + task.getException());
            }
        });


    }


    private void loadDatabases() {
        databaseList.clear();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Log.e(TAG, "Usuario no autenticado");
            showToast("Usuario no autenticado");
            return;
        }

        String userId = user.getUid();
        DatabaseReference userDatabasesRef = database.getReference("users").child(userId).child("databases");
        userDatabasesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                databaseList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot databaseSnapshot : snapshot.getChildren()) {
                        String databaseName = databaseSnapshot.getKey();
                        databaseList.add(databaseName);
                        Log.d(TAG, "Base de datos encontrada en Firebase: "+ databaseName);
                    }
                } else {
                    showToast("No se encontraron bases de datos");
                    Log.d(TAG, "No se encontraron bases de datos para el usuario");
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Error al cargar bases de datos: " + error.getMessage());
                Log.e(TAG, "Error al cargar bases de datos: " + error.getMessage());
            }
        });

    }


    private void showDatabaseExistsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Base de datos existente")
                .setMessage("La base de datos ya está creada.")
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDatabaseClick(String databaseName) {
        if (databaseName != null && !databaseName.isEmpty()) {
            showDatabaseOptionsDialog(databaseName);
        } else {
            showToast("Nombre de base de datos inválido");
        }
    }

    private void showDatabaseOptionsDialog(String databaseName) {
        // Inflar el diseño personalizado
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.menubasedatos, null);

        // Encontrar los botones y elementos en el diseño inflado
        TextView btnEditar = dialogView.findViewById(R.id.btnEditar);
        TextView btnEliminar = dialogView.findViewById(R.id.btnEliminar);

        // Crear el AlertDialog con el diseño inflado
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.TransparentDialogTheme)
                .setView(dialogView)
                .create();


        // Configurar los eventos de clic
        btnEditar.setOnClickListener(v -> {
            editDatabase(databaseName);
            dialog.dismiss();
        });

        btnEliminar.setOnClickListener(v -> {
            confirmDeleteDatabase(databaseName);
            dialog.dismiss();
        });

        // Mostrar el diálogo
        dialog.show();
    }
    private void exportAllDatabasesSequentially() {
        if (userId != null) {
            Log.d(TAG, "Iniciando exportación secuencial de todas las bases de datos");
            DatabaseReference userDatabasesRef = database.getReference("users").child(userId).child("databases");
            userDatabasesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        databaseNames.clear(); // Limpiar la lista antes de añadir nuevos nombres
                        for (DataSnapshot databaseSnapshot : snapshot.getChildren()) {
                            String databaseName = databaseSnapshot.getKey();
                            if (databaseName != null && !databaseName.isEmpty()) { //Comprobación de null y vacío
                                databaseNames.add(databaseName);
                            }
                        }
                        if (!databaseNames.isEmpty()) { //Comprobación de lista vacía
                            XSSFWorkbook workbook = new XSSFWorkbook();
                            exportDatabasesSequentially(workbook);
                        } else {
                            Log.e(TAG, "No se encontraron bases de datos para el usuario");
                            Toast.makeText(BaseDatos.this, "No se encontraron bases de datos", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "No se encontraron bases de datos para el usuario");
                        Toast.makeText(BaseDatos.this, "No se encontraron bases de datos", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error al obtener la referencia a la base de datos en firebase " + error.getMessage());
                    Toast.makeText(BaseDatos.this, "Error al obtener bases de datos", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "Error: userId es null en exportAllDatabasesSequentially");
            Toast.makeText(this, "Error: No se pudo obtener el ID del usuario", Toast.LENGTH_SHORT).show();
        }
    }


    private void exportDatabasesSequentially(XSSFWorkbook workbook) {
        if (databaseNames.isEmpty()) {
            Log.d(TAG, "Proceso de exportación secuencial completado");
            //Compartir el archivo SOLO CUANDO SE TERMINE TODO
            String fileName = generateFileName("TodasLasBasesDeDatos");
            File tempDir = getCacheDir();
            File excelFile = null;
            FileOutputStream outputStream = null;
            try {
                excelFile = new File(tempDir, fileName + ".xlsx");
                outputStream = new FileOutputStream(excelFile);
                workbook.write(outputStream);
                Uri fileUri = FileProvider.getUriForFile(BaseDatos.this,
                        getApplicationContext().getPackageName() + ".provider",
                        excelFile);
                shareExcelFile(fileUri);
            } catch (IOException e) {
                Log.e(TAG, "Error al crear o escribir el archivo: " + e.getMessage());
                Toast.makeText(this, "Error al generar el archivo", Toast.LENGTH_SHORT).show();
            } finally {
                closeOutputStream(outputStream);
                closeWorkbook(workbook);
            }
            return;
        }

        String databaseName = databaseNames.remove(0);
        DatabaseReference userDatabasesRef = database.getReference("users").child(userId).child("databases");
        userDatabasesRef.child(databaseName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String databasePath = snapshot.getRef().toString();
                    ExcelExporter exporter = new ExcelExporter(BaseDatos.this, databaseName, databasePath);
                    exporter.exportToExcel(workbook, new ExcelExporter.OnCompleteListener() {
                        @Override
                        public void onComplete(Uri fileUri) {
                            exportDatabasesSequentially(workbook);
                        }
                    });
                } else {
                    Log.e(TAG, "No se encuentra la referencia para " + databaseName);
                    exportDatabasesSequentially(workbook);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al obtener la referencia a la base de datos en firebase " + error.getMessage());
                exportDatabasesSequentially(workbook);
            }
        });
    }

    private void shareExcelFile(Uri fileUri) {
        if (fileUri != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Compartir Excel"));
        } else {
            Log.e(TAG, "Error: fileUri es null en shareExcelFile");
            Toast.makeText(this, "Error al compartir el archivo", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateFileName(String baseName) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return baseName + "_" + timeStamp;
    }

    // Métodos auxiliares para cerrar streams y workbook de forma segura
    private void closeOutputStream(FileOutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error al cerrar el outputStream: " + e.getMessage());
            }
        }
    }

    private void closeWorkbook(XSSFWorkbook workbook) {
        if (workbook != null) {
            try {
                workbook.close();
            } catch (IOException e) {
                Log.e(TAG, "Error al cerrar el workbook: " + e.getMessage());
            }
        }
    }

    private void editDatabase(String databaseName) {
        Log.d(TAG, "editDatabase() ejecutado con databaseName: " + databaseName);
        if (databaseName != null && !databaseName.isEmpty()) {
            closeCurrentDatabase();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_CURRENT_DATABASE, databaseName);
            editor.putBoolean("KEY_DATABASE_SELECTED", true);
            editor.apply();
            Log.d(TAG, "Nombre de la base de datos guardado en SharedPreferences: " + databaseName);
            showToast("Base de datos actual: " + databaseName);

            // Abre la base de datos en la actividad correspondiente
            Intent intent = new Intent(BaseDatos.this, DatosDatos.class);
            intent.putExtra("databaseName", databaseName);
            startActivity(intent);
        } else {
            showToast("Nombre de base de datos inválido");
        }
    }

    private void editDatabase2(String databaseName) {
        if (databaseName != null && !databaseName.isEmpty()) {
            closeCurrentDatabase();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_CURRENT_DATABASE, databaseName);
            editor.putBoolean("KEY_DATABASE_SELECTED", true);
            editor.apply();
            showToast("Base de datos actual: " + databaseName);

            // Abre la base de datos en la actividad correspondiente
            Intent intent = new Intent(BaseDatos.this, DatosDatos.class);
            intent.putExtra("databaseName", databaseName);
            startActivity(intent);
        } else {
            showToast("Nombre de base de datos inválido");
        }
    }

    private void confirmDeleteDatabase(String databaseName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar la base de datos " + databaseName + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    deleteCustomDatabase(databaseName);
                    loadDatabases();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if (positiveButton != null) {
            positiveButton.setTextColor(getResources().getColor(R.color.colorNegativo));
        }

        if (negativeButton != null) {
            negativeButton.setTextColor(getResources().getColor(R.color.colorPositivo));
        }
    }

    public void deleteCustomDatabase(String databaseName) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Log.e(TAG, "Usuario no autenticado");
            showToast("Usuario no autenticado");
            return;
        }

        String userId = user.getUid();
        DatabaseReference userDatabasesRef = database.getReference("users").child(userId).child("databases");

        userDatabasesRef.child(databaseName).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showToast("Base de datos eliminada de Firebase");
                Log.d(TAG, "Base de datos eliminada en Firebase");
                loadDatabases();
            } else {
                showToast("Error al eliminar base de datos en Firebase: " + task.getException());
                Log.e(TAG, "Error al eliminar base de datos en Firebase: " + task.getException());
            }
        });
    }
    private void closeCurrentDatabase() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_CURRENT_DATABASE);
        editor.putBoolean("KEY_DATABASE_SELECTED", false);
        editor.apply();
    }

    private interface OnStoragePermissionResultListener {
        void onPermissionResult(boolean granted);
    }

    private void requestStoragePermission(OnStoragePermissionResultListener listener) {

    }
    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Bogota"));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText("Selecciona la hora para Recordatorio diario")
                .build();

        timePicker.addOnPositiveButtonClickListener(dialog -> {
            int hourOfDay = timePicker.getHour();
            int minuteOfHour = timePicker.getMinute();

            TimeZone bogotaTimeZone = TimeZone.getTimeZone("America/Bogota");
            selectedTime = Calendar.getInstance(bogotaTimeZone);
            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedTime.set(Calendar.MINUTE, minuteOfHour);
            selectedTime.set(Calendar.SECOND, 0);

            scheduleNotification(selectedTime);
        });

        timePicker.show(getSupportFragmentManager(), "time_picker");
    }
    private void scheduleNotification(Calendar selectedTime) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, Recordatorio.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && canScheduleExactAlarms()) {
                alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        selectedTime.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,  // Intervalo de un día
                        pendingIntent
                );

                Toast.makeText(this, "Recordatorio diario guardado para las " + formatTime(selectedTime), Toast.LENGTH_SHORT).show();
            } else {
                // Solicitar permiso para alarmas exactas
                Intent permissionIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(permissionIntent);

                Toast.makeText(this, "Debes conceder permiso para guardar el recordatorio diario", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (alarmManager != null) {
                alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        selectedTime.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,  // Intervalo de un día
                        pendingIntent
                );

                Toast.makeText(this, "Recordatorio diario guardado para las " + formatTime(selectedTime), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private String formatTime(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
        return sdf.format(calendar.getTime());
    }

    private boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
                // Verificar permisos de notificación en Android 13 y superior
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        if (notificationManager.areNotificationsEnabled()) {
                            return true;
                        } else {
                            openAppSettings();
                            return false;
                        }
                    }
                } else {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        Toast.makeText(this, "Revisa las notificaciones están habilitadas", Toast.LENGTH_SHORT).show();
        intent.setData(uri);
        startActivity(intent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EXACT_ALARM) {
            if (resultCode == RESULT_OK) {
                // Permiso concedido, reintentar programar la alarma
                scheduleNotification(selectedTime);
            } else {
                // Permiso denegado, informar al usuario
                Toast.makeText(this, "Se requiere permiso para programar la alarma", Toast.LENGTH_SHORT).show();
            }
        }
    }
}