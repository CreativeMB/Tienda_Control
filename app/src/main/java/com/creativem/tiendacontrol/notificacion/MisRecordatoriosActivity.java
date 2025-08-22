package com.creativem.tiendacontrol.notificacion;

import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log; // Importar Log
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.creativem.tiendacontrol.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MisRecordatoriosActivity extends AppCompatActivity {

    private static final String TAG = "AlarmScheduler"; // TAG para Logcat

    private RecyclerView recyclerView;
    private RecordatorioAdapter adapter;
    private List<RecordatorioModel> listaRecordatorios;

    private EditText etNombre;
    private Spinner spFrecuencia;
    private Button btnAgregar;

    private ActivityResultLauncher<String> requestNotificationPermission;
    private ActivityResultLauncher<Intent> requestExactAlarmPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_recordatorios);
        Log.d(TAG, "onCreate: Activity started.");

        recyclerView = findViewById(R.id.recyclerViewRecordatorios);
        etNombre = findViewById(R.id.etNombreRecordatorio);
        spFrecuencia = findViewById(R.id.spFrecuencia);
        btnAgregar = findViewById(R.id.btnAgregarRecordatorio);

        requestNotificationPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Permiso de notificación concedido.");
                        Toast.makeText(this, "Permiso de notificación concedido.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "Permiso de notificación NO concedido.");
                        Toast.makeText(this, "Las notificaciones pueden no mostrarse. Concede el permiso en Ajustes > Aplicaciones > [Tu App] > Notificaciones.", Toast.LENGTH_LONG).show();
                    }
                }
        );

        requestExactAlarmPermission = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "Regreso de la configuración de permisos de alarmas exactas.");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
                            Log.d(TAG, "Permiso de alarmas exactas concedido por el usuario.");
                            Toast.makeText(this, "Permiso de alarmas exactas concedido.", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w(TAG, "Permiso de alarmas exactas NO concedido por el usuario.");
                            Toast.makeText(this, "Permiso de alarmas exactas NO concedido. Las notificaciones pueden retrasarse.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Solicitando permiso POST_NOTIFICATIONS.");
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                Log.d(TAG, "Permiso POST_NOTIFICATIONS ya concedido.");
            }
        }

        listaRecordatorios = PrefsHelper.cargarLista(this);
        Log.d(TAG, "Lista de recordatorios cargada: " + listaRecordatorios.size() + " elementos.");

        adapter = new RecordatorioAdapter(this, listaRecordatorios, new RecordatorioAdapter.OnRecordatorioListener() {
            @Override
            public void onEliminar(RecordatorioModel recordatorio) {
                Log.d(TAG, "onEliminar: Eliminando recordatorio con ID " + recordatorio.getId());
                eliminarRecordatorio(recordatorio);
            }

            @Override
            public void onSwitchChange(RecordatorioModel recordatorio, boolean activo) {
                Log.d(TAG, "onSwitchChange: Recordatorio ID " + recordatorio.getId() + " cambió a activo=" + activo);
                recordatorio.setActivo(activo);
                PrefsHelper.guardarLista(MisRecordatoriosActivity.this, listaRecordatorios);

                if (activo) {
                    checkAndScheduleAlarm(recordatorio);
                } else {
                    AlarmScheduler.cancelAlarm(MisRecordatoriosActivity.this, recordatorio);
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnAgregar.setOnClickListener(v -> {
            Log.d(TAG, "Botón Agregar Recordatorio clicado.");
            mostrarTimePicker();
        });
    }

    private void mostrarTimePicker() {
        String nombre = etNombre.getText().toString().trim();
        String frecuencia = spFrecuencia.getSelectedItem().toString();

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingrese un nombre", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "mostrarTimePicker: Nombre de recordatorio vacío.");
            return;
        }
        Log.d(TAG, "mostrarTimePicker: Nombre='" + nombre + "', Frecuencia='" + frecuencia + "'");

        Calendar calendar = Calendar.getInstance();
        int horaActual = calendar.get(Calendar.HOUR_OF_DAY);
        int minutoActual = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    Calendar selectedTime = Calendar.getInstance();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);
                    String horaTexto = sdf.format(selectedTime.getTime());
                    Log.d(TAG, "TimePicker: Hora seleccionada (24h) " + hourOfDay + ":" + minute + ", Formateada: " + horaTexto);

                    RecordatorioModel tempRecordatorio = new RecordatorioModel(0, nombre, horaTexto, frecuencia, true, 0);
                    long initialTriggerMillis = AlarmScheduler.calculateNextTriggerTime(tempRecordatorio);
                    Log.d(TAG, "TimePicker: InitialTriggerMillis calculado: " + initialTriggerMillis + " (" + new SimpleDateFormat("dd/MM HH:mm").format(initialTriggerMillis) + ")");

                    if(initialTriggerMillis == -1){
                        Toast.makeText(this, "La hora seleccionada ya pasó y el recordatorio es 'Una vez'. No se agregó.", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "TimePicker: No se agregó recordatorio 'Una vez' porque la hora ya pasó.");
                        return;
                    }

                    RecordatorioModel recordatorio = new RecordatorioModel(
                            (int) System.currentTimeMillis(),
                            nombre,
                            horaTexto,
                            frecuencia,
                            true,
                            initialTriggerMillis
                    );
                    Log.d(TAG, "TimePicker: Nuevo Recordatorio creado con ID " + recordatorio.getId());

                    listaRecordatorios.add(recordatorio);
                    adapter.notifyItemInserted(listaRecordatorios.size() - 1);
                    PrefsHelper.guardarLista(this, listaRecordatorios);
                    Log.d(TAG, "TimePicker: Recordatorio guardado y lista actualizada.");

                    checkAndScheduleAlarm(recordatorio);

                    etNombre.setText("");
                    spFrecuencia.setSelection(0);

                    Toast.makeText(this, "Recordatorio agregado", Toast.LENGTH_SHORT).show();
                },
                horaActual,
                minutoActual,
                false
        );

        timePicker.show();
    }

    private void checkAndScheduleAlarm(RecordatorioModel recordatorio) {
        Log.d(TAG, "checkAndScheduleAlarm: Verificando permisos para recordatorio ID " + recordatorio.getId());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "checkAndScheduleAlarm: Permiso SCHEDULE_EXACT_ALARM NO concedido. Mostrando diálogo.");
                showExactAlarmPermissionDialog();
                return;
            } else {
                Log.d(TAG, "checkAndScheduleAlarm: Permiso SCHEDULE_EXACT_ALARM concedido (o no requerido en esta API).");
            }
        } else {
            Log.d(TAG, "checkAndScheduleAlarm: Versión de Android < S, SCHEDULE_EXACT_ALARM no requerido.");
        }
        AlarmScheduler.scheduleAlarm(this, recordatorio);
    }

    private void showExactAlarmPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permiso de Alarmas Exactas")
                .setMessage("Tu aplicación necesita el permiso de 'Alarmas y recordatorios' para que los recordatorios funcionen a la hora exacta. Por favor, actívalo en la configuración de la aplicación.")
                .setPositiveButton("Ir a Ajustes", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    requestExactAlarmPermission.launch(intent);
                    Log.d(TAG, "showExactAlarmPermissionDialog: Lanzando Intent para ajustes de permiso de alarma.");
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    Toast.makeText(this, "Las alarmas pueden no ser exactas sin este permiso.", Toast.LENGTH_LONG).show();
                    Log.w(TAG, "showExactAlarmPermissionDialog: Usuario canceló la solicitud de permiso de alarma.");
                })
                .show();
    }

    private void eliminarRecordatorio(RecordatorioModel recordatorio) {
        int index = listaRecordatorios.indexOf(recordatorio);
        if (index != -1) {
            listaRecordatorios.remove(index);
            adapter.notifyItemRemoved(index);
            PrefsHelper.guardarLista(this, listaRecordatorios);
            AlarmScheduler.cancelAlarm(this, recordatorio);
            Log.d(TAG, "eliminarRecordatorio: Recordatorio ID " + recordatorio.getId() + " eliminado y alarma cancelada.");
            Toast.makeText(this, "Recordatorio eliminado", Toast.LENGTH_SHORT).show();
        } else {
            Log.w(TAG, "eliminarRecordatorio: No se encontró el recordatorio con ID " + recordatorio.getId());
        }
    }
}