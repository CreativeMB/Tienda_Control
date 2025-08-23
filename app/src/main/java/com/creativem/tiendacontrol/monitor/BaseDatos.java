package com.creativem.tiendacontrol.monitor;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.creativem.tiendacontrol.notificacion.MisRecordatorios;
import com.creativem.tiendacontrol.R;
import com.creativem.tiendacontrol.exel.ExcelExporter;
import com.creativem.tiendacontrol.misdatos.MisDatos;

import com.creativem.tiendacontrol.pin.EdicionPin;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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

public class BaseDatos extends AppCompatActivity implements BasesAdapter.OnDatabaseClickListener {

    private static final String PREFS_NAME = "CodePrefs";
    private static final String TAG = "BaseDatos";
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
//        iconRecordatorio.setOnClickListener(view -> showTimePickerDialog());

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
        iconRecordatorio.setOnClickListener(view -> {
            Intent databaseIntent = new Intent(this, MisRecordatorios.class);
            startActivity(databaseIntent);
        });

        imageManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Acción graficos
                Intent intent = new Intent(BaseDatos.this, GraficoActivity.class);
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
                        String url = "https://creativem.carrd.co/";
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        return true;
                    } else if (id == R.id.graficos) {
                        // Acción graficos
                        Intent intent = new Intent(BaseDatos.this, GraficoActivity.class);
                        startActivity(intent);
                    } else if (id == R.id.exel) {
                        descargarYExportarDatos();
                        return true;
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
    private void descargarYExportarDatos() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Empresas")
                .child(userId)
                .child("basededatos");


        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, List<Map<String, Object>>> datosPorBases = new HashMap<>();

                for (DataSnapshot dbSnapshot : snapshot.getChildren()) {
                    String nombreBase = dbSnapshot.getKey(); // Nombre para la hoja Excel
                    List<Map<String, Object>> listaRegistros = new ArrayList<>();

                    for (DataSnapshot itemSnapshot : dbSnapshot.getChildren()) {
                        Map<String, Object> registro = new HashMap<>();
                        registro.put("fechaHora", itemSnapshot.child("fechaHora").getValue(String.class));
                        registro.put("id", itemSnapshot.child("id").getValue(String.class)); // Si no lo usas en Excel puedes eliminarlo
                        registro.put("nombre", itemSnapshot.child("nombre").getValue(String.class));
                        registro.put("nota", itemSnapshot.child("nota").getValue(String.class));
                        registro.put("valor", itemSnapshot.child("valor").getValue(Double.class));
                        listaRegistros.add(registro);
                    }

                    datosPorBases.put(nombreBase, listaRegistros);
                }

                try {
                    // Crear nombre con fecha y hora actual
                    String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new java.util.Date());
                    String fileName = "Datos_" + timestamp;

                    // Exportar y compartir el Excel con todas las bases en hojas separadas
                    File excelFile = ExcelExporter.exportToExcel(BaseDatos.this, datosPorBases, fileName);
                    ExcelExporter.shareExcel(BaseDatos.this, excelFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(BaseDatos.this, "Error al exportar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BaseDatos.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void configurarRecyclerViewDatabases() { // Renamed for clarity
        int orientation = getResources().getConfiguration().orientation;
        RecyclerView.LayoutManager layoutManager;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutManager = new LinearLayoutManager(this); // Vertical by default
        } else { // Landscape orientation
            layoutManager = new GridLayoutManager(this, calculateNoOfColumns());
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showToast("Usuario no autenticado");
            return;
        }
        String userId = user.getUid();

        databaseList = new ArrayList<>();
        adapter = new BasesAdapter(
                this,
                databaseList,
                databaseName -> { // OnDatabaseClickListener
                    Intent intent = new Intent(BaseDatos.this, MisDatos.class);
                    intent.putExtra("databaseName", databaseName);
                    startActivity(intent);
                },
                databaseName -> { // OnDeleteClickListener
                    confirmDeleteDatabase(databaseName);
                }
        );


        recyclerViewDatabases.setLayoutManager(layoutManager);
        recyclerViewDatabases.setAdapter(adapter);
    }

    private void confirmDeleteDatabase(String databaseName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar la base de datos " + databaseName + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    deleteCustomDatabase(databaseName);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Cambiar colores de botones si quieres
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if (positiveButton != null) {
            positiveButton.setTextColor(getResources().getColor(R.color.colorNegativo));
        }

        if (negativeButton != null) {
            negativeButton.setTextColor(getResources().getColor(R.color.colorPositivo));
        }
    }

    private void deleteCustomDatabase(String databaseName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        DatabaseReference refToDelete = FirebaseDatabase.getInstance()
                .getReference("Empresas")
                .child(userId)
                .child("basededatos")
                .child(databaseName);

        refToDelete.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Base de datos eliminada", Toast.LENGTH_SHORT).show();
                    loadDatabases();  // Recargar lista tras borrar
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
        if (text == null || text.isEmpty()) return false;

        // Expresión regular con rangos Unicode de emojis comunes
        String emojiPattern =
                ".*[" +
                        "\u203C-\u3299" +     // Símbolos varios
                        "\uD83C\uDC04" +     // Mahjong tile
                        "\uD83C\uD000-\uD83D\uDFFF" + // Emojis en bloques de símbolos y pictogramas
                        "\uD83E\uDD00-\uD83E\uDDFF" + // Emojis más recientes
                        "\uD83E\uDE00-\uD83E\uDEFF" + // Más emojis recientes
                        "]+.*";

        return text.matches(emojiPattern);
    }
    private void checkAndCreateDatabase(String databaseName) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Log.e(TAG, "Usuario no autenticado");
            showToast("Usuario no autenticado");
            return;
        }

        String userId = user.getUid();

        // Crear fecha en zona horaria de Colombia
        SimpleDateFormat sdfColombia = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdfColombia.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
        String dateString = sdfColombia.format(new Date());

        DatabaseReference userDatabasesRef = FirebaseDatabase.getInstance()
                .getReference("Empresas")
                .child(userId)
                .child("basededatos");

        Map<String, Object> databaseData = new HashMap<>();
        databaseData.put("timestamp", ServerValue.TIMESTAMP);
        databaseData.put("fechaCreacion", dateString);

        userDatabasesRef.child(databaseName).setValue(databaseData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showToast("Base de datos creada en Firebase");
                Log.d(TAG, "Base de datos creada en Firebase");
                loadDatabases();
            } else {
                showToast("Error al crear base de datos en Firebase: " + task.getException());
                Log.e(TAG, "Error al crear base de datos en Firebase: ", task.getException());
            }
        });
    }



    private void loadDatabases() {
        databaseList.clear();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.e(TAG, "Usuario no autenticado");
            showToast("Usuario no autenticado");
            return;
        }

        String userId = user.getUid();

        // Cambiamos la referencia para que apunte a la estructura Empresas/{userId}/basededatos
        DatabaseReference userDatabasesRef = database.getReference("Empresas")
                .child(userId)
                .child("basededatos");

        userDatabasesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                databaseList.clear();
                List<Pair<String, Long>> tempDatabaseList = new ArrayList<>();

                if (snapshot.exists()) {
                    for (DataSnapshot databaseSnapshot : snapshot.getChildren()) {
                        String databaseName = databaseSnapshot.getKey();
                        Long timestamp = databaseSnapshot.child("timestamp").getValue(Long.class);

                        if (timestamp == null) timestamp = 0L; // Si no hay timestamp, poner 0 por defecto
                        tempDatabaseList.add(new Pair<>(databaseName, timestamp));

                        Log.d(TAG, "Base de datos encontrada en Firebase: " + databaseName + " | Timestamp: " + timestamp);
                    }

                    // Ordenar por timestamp (de más reciente a más antigua)
                    Collections.sort(tempDatabaseList, (db1, db2) -> Long.compare(db2.second, db1.second));

                    // Agregar los nombres de las bases de datos en orden
                    for (Pair<String, Long> database : tempDatabaseList) {
                        databaseList.add(database.first);
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

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDatabaseClick(String databaseName) {
        if (databaseName != null && !databaseName.isEmpty()) {
//            showDatabaseOptionsDialog(databaseName);
        } else {
            showToast("Nombre de base de datos inválido");
        }
    }


//    private void closeCurrentDatabase() {
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.remove(KEY_CURRENT_DATABASE);
//        editor.putBoolean("KEY_DATABASE_SELECTED", false);
//        editor.apply();
//    }
//
//    private void showTimePickerDialog() {
//        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Bogota"));
//        int hour = calendar.get(Calendar.HOUR_OF_DAY);
//        int minute = calendar.get(Calendar.MINUTE);
//
//        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
//                .setTimeFormat(TimeFormat.CLOCK_12H)
//                .setHour(hour)
//                .setMinute(minute)
//                .setTitleText("Selecciona la hora para Recordatorio diario")
//                .build();
//
//        timePicker.addOnPositiveButtonClickListener(dialog -> {
//            int hourOfDay = timePicker.getHour();
//            int minuteOfHour = timePicker.getMinute();
//
//            TimeZone bogotaTimeZone = TimeZone.getTimeZone("America/Bogota");
//            selectedTime = Calendar.getInstance(bogotaTimeZone);
//            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
//            selectedTime.set(Calendar.MINUTE, minuteOfHour);
//            selectedTime.set(Calendar.SECOND, 0);
//
//            scheduleNotification(selectedTime);
//        });
//
//        timePicker.show(getSupportFragmentManager(), "time_picker");
//    }
//    private void scheduleNotification(Calendar selectedTime) {
//        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//
//        Intent intent = new Intent(this, Recordatorio.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                this,
//                0,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//        );
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (alarmManager != null && canScheduleExactAlarms()) {
//                alarmManager.setRepeating(
//                        AlarmManager.RTC_WAKEUP,
//                        selectedTime.getTimeInMillis(),
//                        AlarmManager.INTERVAL_DAY,  // Intervalo de un día
//                        pendingIntent
//                );
//
//                Toast.makeText(this, "Recordatorio diario guardado para las " + formatTime(selectedTime), Toast.LENGTH_SHORT).show();
//            } else {
//                // Solicitar permiso para alarmas exactas
//                Intent permissionIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
//                permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(permissionIntent);
//
//                Toast.makeText(this, "Debes conceder permiso para guardar el recordatorio diario", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            if (alarmManager != null) {
//                alarmManager.setRepeating(
//                        AlarmManager.RTC_WAKEUP,
//                        selectedTime.getTimeInMillis(),
//                        AlarmManager.INTERVAL_DAY,  // Intervalo de un día
//                        pendingIntent
//                );
//
//                Toast.makeText(this, "Recordatorio diario guardado para las " + formatTime(selectedTime), Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//    private String formatTime(Calendar calendar) {
//        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//        sdf.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
//        return sdf.format(calendar.getTime());
//    }
//
//    private boolean canScheduleExactAlarms() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//            if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
//                // Verificar permisos de notificación en Android 13 y superior
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                    if (notificationManager != null) {
//                        if (notificationManager.areNotificationsEnabled()) {
//                            return true;
//                        } else {
//                            openAppSettings();
//                            return false;
//                        }
//                    }
//                } else {
//                    return true;
//                }
//            }
//            return false;
//        }
//        return true;
//    }
//    private void openAppSettings() {
//        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//        Uri uri = Uri.fromParts("package", getPackageName(), null);
//        Toast.makeText(this, "Revisa las notificaciones están habilitadas", Toast.LENGTH_SHORT).show();
//        intent.setData(uri);
//        startActivity(intent);
//    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE_EXACT_ALARM) {
//            if (resultCode == RESULT_OK) {
//                // Permiso concedido, reintentar programar la alarma
//                scheduleNotification(selectedTime);
//            } else {
//                // Permiso denegado, informar al usuario
//                Toast.makeText(this, "Se requiere permiso para programar la alarma", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
}