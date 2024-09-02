package com.example.tiendacontrol.monitor;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendacontrol.R;
import com.example.tiendacontrol.adapter.basesAdapter;
import com.example.tiendacontrol.helper.ExcelExporter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Database extends AppCompatActivity implements basesAdapter.OnDatabaseClickListener {
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1;
    private static final int PICK_IMAGE_REQUEST = 1;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private RecyclerView recyclerViewDatabases;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_CURRENT_DATABASE = "currentDatabase";
    private List<String> databaseList;
    private basesAdapter adapter;
    private OnStoragePermissionResultListener storagePermissionResultListener;

    private final ActivityResultLauncher<Intent> manageAllFilesPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        if (storagePermissionResultListener != null) {
                            storagePermissionResultListener.onPermissionResult(true);
                            // Recarga la lista de bace de datos disponibles en le carpeta de la apliccion
                            loadDatabases();

                        }
                    } else {
                        if (storagePermissionResultListener != null) {
                            storagePermissionResultListener.onPermissionResult(false);
                        }
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> requestWritePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) {
                    if (storagePermissionResultListener != null) {
                        storagePermissionResultListener.onPermissionResult(true);
                    }
                } else {
                    if (storagePermissionResultListener != null) {
                        storagePermissionResultListener.onPermissionResult(false);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.database);
        ImageView imageManual = findViewById(R.id.manual);

        ImageView iconRecordatorio = findViewById(R.id.recordatorio);
        iconRecordatorio.setOnClickListener(view -> showTimePickerDialog());

        ImageView iconCreateDatabase = findViewById(R.id.database);
        iconCreateDatabase.setOnClickListener(v -> showDatabaseNameDialog());

        ImageView iconDonacion = findViewById(R.id.donacion);

        recyclerViewDatabases = findViewById(R.id.recyclerViewDatabases);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        recyclerViewDatabases.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        databaseList = new ArrayList<>();
        adapter = new basesAdapter(this, databaseList,this);
        recyclerViewDatabases.setAdapter(adapter);


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
                    if (id == R.id.inicioItem) {
                        // Acción para Inicio
                        Intent intent = new Intent(Database.this, Database.class);
                        startActivity(intent);
                    } else if (id == R.id.codeItem) {
                        // Acción para Código
                        Intent intent = new Intent(Database.this, SetCode.class);
                        startActivity(intent);
                    } else if (id == R.id.donaItem) {
                        // Acción para Donar
                        Intent intent = new Intent(Database.this, Donar.class);
                        startActivity(intent);
                    } else if (id == R.id.manual)  {
                        // URL al que quieres dirigir al usuario
                        String url = "https://www.floristerialoslirios.com/tienda-control";
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        return true;
                    } else if (id == R.id.salirItem) {
                        // Acción para Salir
                        finishAffinity();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(Database.this, "Ocurrió un error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                // Cerrar el menú después de la selección
                drawerLayout.closeDrawers();

                return true;
            }
        });

        // Solicita permisos cuando se inicia la actividad
        requestStoragePermission(granted -> {
            if (granted) {
                // Permiso concedido, puedes proceder con otras acciones si es necesario

            } else {
                showToast("Permiso de almacenamiento denegado");
            }
        });
        // Recarga la lista de bace de datos disponibles en le carpeta de la apliccion
        loadDatabases();
    }

    private void showDatabaseNameDialog() {
        final EditText input = new EditText(this);
        input.setHint("Nombre de la base de datos");

        new AlertDialog.Builder(this)
                .setTitle("Crear Base de Datos")
                .setMessage("Ingrese el nombre para la nueva base de datos:")
                .setView(input)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String databaseName = input.getText().toString().trim();
                    if (!databaseName.isEmpty()) {
                        checkAndCreateDatabase(databaseName);
                    } else {
                        showToast("Ingrese un nombre de base de datos");
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void checkAndCreateDatabase(String databaseName) {
        File documentsFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "TiendaControl");
        if (!documentsFolder.exists() && !documentsFolder.mkdirs()) {
            showToast("Error al crear la carpeta de documentos");
            return;
        }

        File dbFile = new File(documentsFolder, databaseName + ".db");
        if (dbFile.exists()) {
            showDatabaseExistsDialog();
        } else {
            try (FileOutputStream out = new FileOutputStream(dbFile)) {
                out.write(new byte[0]);
                showToast("Guardada en: " + dbFile.getAbsolutePath());
                loadDatabases();

            } catch (IOException e) {
                showToast("Error al crear la base de datos: " + e.getMessage());
                Log.e("Database", "Error al crear la base de datos: " + e.getMessage());
            }
        }
    }

    private void loadDatabases() {
        databaseList.clear();
        File documentsFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "TiendaControl");
        Log.d("Database", "Directorio de documentos: " + documentsFolder.getAbsolutePath());
        if (documentsFolder.isDirectory()) {
            File[] dbFiles = documentsFolder.listFiles();
            if (dbFiles != null) {
                for (File file : dbFiles) {
                    Log.d("Database", "Archivo encontrado: " + file.getName());
                    if (file.isFile() && file.getName().endsWith(".db")) {
                        String fileNameWithoutExtension = file.getName().replace(".db", "");
                        databaseList.add(fileNameWithoutExtension);
                    }
                }
            } else {
                showToast("No se encontraron bases de datos");
            }
        }
        adapter.notifyDataSetChanged();
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
        View dialogView = inflater.inflate(R.layout.menudatabase, null);

        // Encontrar los botones y elementos en el diseño inflado
        Button btnEditar = dialogView.findViewById(R.id.btnEditar);
        Button btnEliminar = dialogView.findViewById(R.id.btnEliminar);
        Button btnExportar = dialogView.findViewById(R.id.btnExportar);
        TextView tvContabilidad = dialogView.findViewById(R.id.btnContabilidad);

        // Crear el AlertDialog con el diseño inflado
        AlertDialog dialog = new AlertDialog.Builder(this)
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

        btnExportar.setOnClickListener(v -> {
            new ExcelExporter(databaseName).exportToExcel(this);
            dialog.dismiss();
        });

        tvContabilidad.setOnClickListener(v -> {
            editDatabase2(databaseName);
            dialog.dismiss();
        });

        // Mostrar el diálogo
        dialog.show();
    }

    private void editDatabase(String databaseName) {
        Log.d("Database", "editDatabase() ejecutado con databaseName: " + databaseName);
        if (databaseName != null && !databaseName.isEmpty()) {
            closeCurrentDatabase();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_CURRENT_DATABASE, databaseName);
            editor.putBoolean("KEY_DATABASE_SELECTED", true);
            editor.apply();
            Log.d("Database", "Nombre de la base de datos guardado en SharedPreferences: " + databaseName);
            showToast("Base de datos actual: " + databaseName);

            // Abre la base de datos en la actividad correspondiente
            Intent intent = new Intent(Database.this, MainActivity.class);
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
            Intent intent = new Intent(Database.this, FiltroDiaMesAno.class);
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
        File internalDbFile = getDatabasePath(databaseName + ".db");
        boolean internalDeleted = internalDbFile.delete();

        File externalDbFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "TiendaControl/" + databaseName + ".db");
        boolean externalDeleted = externalDbFile.delete();

        if (internalDeleted && externalDeleted) {
            showToast("Base de datos " + databaseName + " eliminada correctamente");
        } else if (internalDeleted) {
            showToast("Base de datos interna " + databaseName + " eliminada correctamente");
        } else if (externalDeleted) {
            showToast("Base de datos externa " + databaseName + " eliminada correctamente");
        } else {
            showToast("Error al eliminar la base de datos " + databaseName);
        }

        loadDatabases();
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
        this.storagePermissionResultListener = listener;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                listener.onPermissionResult(true);

            } else {
                showToast("Tienes que dar permisos para comenzar");
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                manageAllFilesPermissionLauncher.launch(intent);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                listener.onPermissionResult(true);

            } else {
                requestWritePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    // Guardar la hora seleccionada
                    Calendar selectedTime = Calendar.getInstance();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minuteOfHour);
                    selectedTime.set(Calendar.SECOND, 0);

                    // Programar la notificación
                    scheduleNotification(selectedTime);
                }, hour, minute, true);

        timePickerDialog.show();
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

                Toast.makeText(this, "Recordatorio diario guardado para las " + selectedTime.get(Calendar.HOUR_OF_DAY) + ":" + selectedTime.get(Calendar.MINUTE), Toast.LENGTH_SHORT).show();
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

                Toast.makeText(this, "Recordatorio diario guardado para las " + selectedTime.get(Calendar.HOUR_OF_DAY) + ":" + selectedTime.get(Calendar.MINUTE), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        showToast("Revisa las notificaciones están habilitadas");
        intent.setData(uri);
        startActivity(intent);

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
                            // Aquí podrías verificar si las burbujas están habilitadas, pero no se puede hacer directamente.
                            return true;
                        } else {
                            // Abrir la configuración de notificaciones
                            openAppSettings();
                            return false;
                        }
                    }
                } else {

                    // Para versiones anteriores a Android 13, siempre se considera que las notificaciones están habilitadas
                    return true;
                }
            }
            return false;
        }
        // Para versiones anteriores a Android 12, siempre devolver verdadero
        return true;
    }
}