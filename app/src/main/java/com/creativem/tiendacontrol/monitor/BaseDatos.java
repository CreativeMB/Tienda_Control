package com.creativem.tiendacontrol.monitor;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.creativem.tiendacontrol.helper.BdVentas;
import com.creativem.tiendacontrol.R;
import com.creativem.tiendacontrol.adapter.BasesAdapter;
import com.creativem.tiendacontrol.helper.ExcelExporter;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class BaseDatos extends AppCompatActivity implements BasesAdapter.OnDatabaseClickListener {
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1;
    private static final int REQUEST_CODE_EXACT_ALARM = 1;
    private static final String DATABASE_SUBFOLDER = "MisBasesDeDatos"; // Nombre de la subcarpeta
    private Calendar selectedTime;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private RecyclerView recyclerViewDatabases;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_CURRENT_DATABASE = "currentDatabase";
    private List<String> databaseList;
    private BasesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basedatos);
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
        adapter = new BasesAdapter(this, databaseList, this);
        recyclerViewDatabases.setAdapter(adapter);


        iconDonacion.setOnClickListener(view -> {
            Intent databaseIntent = new Intent(this, Donar.class);
            startActivity(databaseIntent);
        });

        imageManual.setOnClickListener(v -> {
            // URL al que quieres dirigir al usuario
            String url = "https://www.floristerialoslirios.com/tienda-control";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
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
                    Toast.makeText(BaseDatos.this, "Ocurrió un error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                // Cerrar el menú después de la selección
                drawerLayout.closeDrawers();

                return true;
            }
        });

        // Carga las bases de datos desde el almacenamiento interno
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
                        createDatabase(databaseName);
                    } else {
                        showToast("Ingrese un nombre de base de datos");
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Crea la base de datos en el almacenamiento interno, dentro de la subcarpeta
    private void createDatabase(String databaseName) {
        // Usa el método getDatabasePath de la clase BdVentas
        File dbFile = new File(BdVentas.getDatabaseFilePath(this, databaseName));

        if (dbFile.exists()) {
            showDatabaseExistsDialog();
        } else {
            try {
                // Crea un archivo vacío para la base de datos
                FileOutputStream out = new FileOutputStream(dbFile);
                out.close();

                showToast("Base de datos '" + databaseName + "' creada correctamente");
                loadDatabases();
            } catch (IOException e) {
                Log.e("BaseDatos", "Error al crear la base de datos", e);
                showToast("Error al crear la base de datos");
            }
        }
    }

    private void loadDatabases() {
        databaseList.clear();

        // Obtiene el directorio de bases de datos de la aplicación
        File databaseDir = getDatabasePath("dummy.db").getParentFile();

        // Obtiene la subcarpeta
        File subfolder = new File(databaseDir, DATABASE_SUBFOLDER);

        if (subfolder.exists() && subfolder.isDirectory()) {
            File[] dbFiles = subfolder.listFiles();
            if (dbFiles != null) {
                for (File file : dbFiles) {
                    if (file.isFile() && file.getName().endsWith(".db")) {
                        String fileNameWithoutExtension = file.getName().replace(".db", "");
                        databaseList.add(fileNameWithoutExtension);
                    }
                }
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
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.menubasedatos, null);

        TextView btnEditar = dialogView.findViewById(R.id.btnEditar);
        TextView btnEliminar = dialogView.findViewById(R.id.btnEliminar);
        TextView btnExportar = dialogView.findViewById(R.id.btnExportar);
        TextView tvContabilidad = dialogView.findViewById(R.id.btnContabilidad);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.TransparentDialogTheme)
                .setView(dialogView)
                .create();

        btnEditar.setOnClickListener(v -> {
            editDatabase(databaseName);
            dialog.dismiss();
        });

        btnEliminar.setOnClickListener(v -> {
            confirmDeleteDatabase(databaseName);
            dialog.dismiss();
        });

        btnExportar.setOnClickListener(v -> {
            showProVersionDialog();
            dialog.dismiss();
        });

        tvContabilidad.setOnClickListener(v -> {
            editDatabase2(databaseName);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void editDatabase(String databaseName) {
        Log.d("BaseDatos", "editDatabase() ejecutado con databaseName: " + databaseName);
        if (databaseName != null && !databaseName.isEmpty()) {
            closeCurrentDatabase();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_CURRENT_DATABASE, databaseName);
            editor.putBoolean("KEY_DATABASE_SELECTED", true);
            editor.apply();
            Log.d("BaseDatos", "Nombre de la base de datos guardado en SharedPreferences: " + databaseName);
            showToast("Base de datos actual: " + databaseName);

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

            Intent intent = new Intent(BaseDatos.this, FiltroDiaMesAno.class);
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

    // Elimina la base de datos del almacenamiento interno
    public void deleteCustomDatabase(String databaseName) {
        // Construye la ruta del archivo dentro de la subcarpeta
        File dbFile = new File(BdVentas.getDatabaseFilePath(this, databaseName));

        boolean deleted = dbFile.delete();
        if (deleted) {
            showToast("Base de datos '" + databaseName + "' eliminada correctamente");
        } else {
            showToast("Error al eliminar la base de datos '" + databaseName + "'");
        }
        loadDatabases();
    }

    private void closeCurrentDatabase() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_CURRENT_DATABASE);
        editor.putBoolean("KEY_DATABASE_SELECTED", false);
        editor.apply();
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
    private void showProVersionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Función Pro")
                .setMessage("La exportación a Excel solo está disponible en la versión Pro de la aplicación.")
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .show();
    }
}