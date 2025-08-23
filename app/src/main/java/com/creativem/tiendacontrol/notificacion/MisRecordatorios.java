package com.creativem.tiendacontrol.notificacion;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log; // Importar Log
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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

public class MisRecordatorios extends AppCompatActivity {

    private static final String TAG = "MisRecordatorios"; // TAG corregido

    private RecyclerView recyclerView;
    private RecordatorioAdapter adapter;
    private List<RecordatorioModel> listaRecordatorios;

    private EditText etNombre;
    private Spinner spFrecuencia;
    private TextView texAgregar;

    private ActivityResultLauncher<String> requestNotificationPermission;
    private ActivityResultLauncher<Intent> requestExactAlarmPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mis_recordatorios);
        Log.d(TAG, "onCreate: Activity started.");

        recyclerView = findViewById(R.id.recyclerViewRecordatorios);
        etNombre = findViewById(R.id.etNombreRecordatorio);
        spFrecuencia = findViewById(R.id.spFrecuencia);
        texAgregar = findViewById(R.id.btnAgregarRecordatorio);

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
                PrefsHelper.guardarLista(MisRecordatorios.this, listaRecordatorios);

                if (activo) {
                    checkAndScheduleAlarm(recordatorio);
                } else {
                    AlarmScheduler.cancelAlarm(MisRecordatorios.this, recordatorio);
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        texAgregar.setOnClickListener(v -> {
            Log.d(TAG, "Botón Agregar Recordatorio clicado.");
            mostrarTimePicker();
        });
    }

    private void mostrarTimePicker() {
        String nombre = etNombre.getText().toString().trim();
        String frecuencia = spFrecuencia.getSelectedItem().toString(); // Ej: "Diario", "Semanal", "Mensual", "Fecha", "Una vez"

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingrese un nombre", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "mostrarTimePicker: Nombre de recordatorio vacío.");
            return;
        }
        Log.d(TAG, "mostrarTimePicker: Nombre='" + nombre + "', Frecuencia='" + frecuencia + "'");

        Calendar calendarActual = Calendar.getInstance(); // Calendar para la hora actual del TimePicker
        int horaActual = calendarActual.get(Calendar.HOUR_OF_DAY);
        int minutoActual = calendarActual.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    Calendar fechaHoraSeleccionada = Calendar.getInstance();
                    fechaHoraSeleccionada.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    fechaHoraSeleccionada.set(Calendar.MINUTE, minute);
                    fechaHoraSeleccionada.set(Calendar.SECOND, 0);
                    fechaHoraSeleccionada.set(Calendar.MILLISECOND, 0);

                    // Si la frecuencia es "Fecha" (one-time date) o "Una vez" (one-time date and time)
                    if (frecuencia.equalsIgnoreCase("Fecha") || frecuencia.equalsIgnoreCase("Una vez")) {
                        DatePickerDialog datePicker = new DatePickerDialog(
                                this,
                                (dp, year, month, dayOfMonth) -> {
                                    fechaHoraSeleccionada.set(Calendar.YEAR, year);
                                    fechaHoraSeleccionada.set(Calendar.MONTH, month);
                                    fechaHoraSeleccionada.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                    // Formatear la fecha y hora completa para mostrar en la lista
                                    SimpleDateFormat sdfCompleta = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
                                    String fechaHoraTexto = sdfCompleta.format(fechaHoraSeleccionada.getTime());
                                    Log.d(TAG, "DatePicker: Fecha y hora seleccionada para 'Fecha'/'Una vez': " + fechaHoraTexto);

                                    // Para "Fecha" y "Una vez", el triggerMillis es la fecha y hora exacta seleccionada.
                                    // Comprobar si ya pasó. Si es así, advertir y no agregar.
                                    if (fechaHoraSeleccionada.getTimeInMillis() <= System.currentTimeMillis()) {
                                        Toast.makeText(this, "La fecha y hora seleccionada ya pasó. No se puede programar.", Toast.LENGTH_LONG).show();
                                        Log.w(TAG, "mostrarTimePicker: No se agregó recordatorio 'Fecha'/'Una vez' porque la fecha/hora ya pasó.");
                                        return; // No agregar recordatorio si ya pasó la fecha
                                    }

                                    agregarRecordatorio(nombre, frecuencia, fechaHoraSeleccionada.getTimeInMillis(), fechaHoraTexto);
                                },
                                calendarActual.get(Calendar.YEAR), // Usar calendarActual para la fecha inicial del DatePicker
                                calendarActual.get(Calendar.MONTH),
                                calendarActual.get(Calendar.DAY_OF_MONTH)
                        );
                        datePicker.show();
                    } else {
                        // Para "Diario", "Semanal", "Mensual"
                        long triggerMillis = calcularProximoTrigger(fechaHoraSeleccionada, frecuencia);
                        SimpleDateFormat sdfHora = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                        String horaTexto = sdfHora.format(fechaHoraSeleccionada.getTime()); // Solo la hora para el display string
                        Log.d(TAG, "TimePicker: Hora seleccionada para repetitivo: " + horaTexto);

                        agregarRecordatorio(nombre, frecuencia, triggerMillis, horaTexto);
                    }
                },
                horaActual,
                minutoActual,
                false // false = TimePicker en formato AM/PM
        );

        timePicker.show();
    }

    /**
     * Calcula el próximo trigger para recordatorios repetitivos (Diario, Semanal, Mensual).
     * Toma la fecha y hora seleccionada (del TimePicker) como base y la avanza si ya pasó hoy.
     * @param fechaHoraBase Calendar con la hora seleccionada para el día actual.
     * @param frecuencia La frecuencia de repetición ("Diario", "Semanal", "Mensual").
     * @return El tiempo en milisegundos para la próxima ocurrencia de la alarma.
     */
    private long calcularProximoTrigger(Calendar fechaHoraBase, String frecuencia) {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.SECOND, 0); // Normalizar segundos y milisegundos para una comparación justa
        now.set(Calendar.MILLISECOND, 0);

        // Si la hora y fecha base ya han pasado o es el mismo minuto que ahora,
        // avanzamos a la próxima ocurrencia para asegurar que siempre sea en el futuro.
        if (fechaHoraBase.getTimeInMillis() <= now.getTimeInMillis()) {
            Log.d(TAG, "calcularProximoTrigger: Hora base (" + new SimpleDateFormat("dd/MM HH:mm:ss").format(fechaHoraBase.getTime()) + ") ya pasó o es igual a la actual (" + new SimpleDateFormat("dd/MM HH:mm:ss").format(now.getTime()) + "). Ajustando para la próxima ocurrencia.");
            switch (frecuencia) {
                case "Diario":
                    fechaHoraBase.add(Calendar.DAY_OF_YEAR, 1);
                    break;
                case "Semanal":
                    fechaHoraBase.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case "Mensual":
                    fechaHoraBase.add(Calendar.MONTH, 1);
                    break;
                // "Fecha" y "Una vez" no llegan a este método.
                default:
                    Log.e(TAG, "calcularProximoTrigger: Frecuencia desconocida para recordatorio repetitivo: " + frecuencia);
                    fechaHoraBase.add(Calendar.DAY_OF_YEAR, 1); // Fallback seguro
                    break;
            }
        }
        Log.d(TAG, "calcularProximoTrigger: Próximo trigger calculado (inicial): "
                + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault()).format(fechaHoraBase.getTimeInMillis())
                + " para frecuencia: " + frecuencia);
        return fechaHoraBase.getTimeInMillis();
    }

    private void agregarRecordatorio(String nombre, String frecuencia, long triggerMillis, String displayTexto) {
        RecordatorioModel recordatorio = new RecordatorioModel(
                (int) System.currentTimeMillis(), // Generar un ID único
                nombre,
                displayTexto, // Esto puede ser solo la hora, o la fecha y hora completa
                frecuencia,
                true,
                triggerMillis // Ya contiene el tiempo calculado para la primera alarma
        );
        Log.d(TAG, "agregarRecordatorio: Agregando Recordatorio ID " + recordatorio.getId() + ", Frecuencia: " + frecuencia + ", Display: " + displayTexto + ", Trigger: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(triggerMillis));

        listaRecordatorios.add(recordatorio);
        adapter.notifyItemInserted(listaRecordatorios.size() - 1);
        PrefsHelper.guardarLista(this, listaRecordatorios);

        checkAndScheduleAlarm(recordatorio);

        etNombre.setText("");
        spFrecuencia.setSelection(0);

        Toast.makeText(this, "Recordatorio agregado", Toast.LENGTH_SHORT).show();
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
    public void mostrarDialogoEditarRecordatorio(final RecordatorioModel recordatorio) {
        // Inflar el layout (usa el mismo estilo que para crear, pero con IDs *_Editar)
        LayoutInflater inflater = LayoutInflater.from(this);
        View vista = inflater.inflate(R.layout.editar_recordatorio, null);

        EditText etNombre = vista.findViewById(R.id.etNombreRecordatorioEditar);
        Spinner spFrecuencia = vista.findViewById(R.id.spFrecuenciaEditar);
        TextView texActualizar = vista.findViewById(R.id.btnActualizarRecordatorio);

        // Prellenar datos actuales
        etNombre.setText(recordatorio.getTitulo());

        // Adaptador del spinner
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(
                this, R.array.frecuencias, android.R.layout.simple_spinner_item);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFrecuencia.setAdapter(adapterSpinner);

        // Seleccionar la frecuencia actual
        int pos = adapterSpinner.getPosition(recordatorio.getRepeticion());
        if (pos >= 0) spFrecuencia.setSelection(pos);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(vista)
                .create();

        texActualizar.setOnClickListener(v -> {
            String nuevoNombre = etNombre.getText().toString().trim();
            String nuevaFrecuencia = spFrecuencia.getSelectedItem().toString();

            if (nuevoNombre.isEmpty()) {
                Toast.makeText(this, "Ingrese un nombre", Toast.LENGTH_SHORT).show();
                return;
            }

            // Abrir el TimePicker como cuando se crea
            Calendar calendarActual = Calendar.getInstance();
            int horaActual = calendarActual.get(Calendar.HOUR_OF_DAY);
            int minutoActual = calendarActual.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        Calendar fechaHoraSeleccionada = Calendar.getInstance();
                        fechaHoraSeleccionada.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        fechaHoraSeleccionada.set(Calendar.MINUTE, minute);
                        fechaHoraSeleccionada.set(Calendar.SECOND, 0);
                        fechaHoraSeleccionada.set(Calendar.MILLISECOND, 0);

                        if (nuevaFrecuencia.equalsIgnoreCase("Fecha") || nuevaFrecuencia.equalsIgnoreCase("Una vez")) {
                            // Si es fecha o una vez → abrir DatePicker
                            DatePickerDialog datePicker = new DatePickerDialog(
                                    this,
                                    (dp, year, month, dayOfMonth) -> {
                                        fechaHoraSeleccionada.set(Calendar.YEAR, year);
                                        fechaHoraSeleccionada.set(Calendar.MONTH, month);
                                        fechaHoraSeleccionada.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                        if (fechaHoraSeleccionada.getTimeInMillis() <= System.currentTimeMillis()) {
                                            Toast.makeText(this, "La fecha/hora ya pasó", Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        // Actualizar recordatorio
                                        actualizarRecordatorio(recordatorio, nuevoNombre, nuevaFrecuencia, fechaHoraSeleccionada);
                                        dialog.dismiss();
                                    },
                                    calendarActual.get(Calendar.YEAR),
                                    calendarActual.get(Calendar.MONTH),
                                    calendarActual.get(Calendar.DAY_OF_MONTH)
                            );
                            datePicker.show();
                        } else {
                            // Para Diario, Semanal, Mensual
                            long triggerMillis = calcularProximoTrigger(fechaHoraSeleccionada, nuevaFrecuencia);
                            actualizarRecordatorio(recordatorio, nuevoNombre, nuevaFrecuencia, fechaHoraSeleccionada);
                            dialog.dismiss();
                        }
                    },
                    horaActual, minutoActual, false
            );
            timePicker.show();
        });

        dialog.show();
    }

   public void actualizarRecordatorio(RecordatorioModel recordatorio, String nombre, String frecuencia, Calendar fechaHoraSeleccionada) {
        SimpleDateFormat sdfDisplay;

        if (frecuencia.equalsIgnoreCase("Fecha") || frecuencia.equalsIgnoreCase("Una vez")) {
            sdfDisplay = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
        } else {
            sdfDisplay = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        }

        String displayTexto = sdfDisplay.format(fechaHoraSeleccionada.getTime());

        recordatorio.setTitulo(nombre);
        recordatorio.setRepeticion(frecuencia);
        recordatorio.setHora(displayTexto); // aquí guardas lo visible
        recordatorio.setActivo(true);
        recordatorio.setTriggerTime(fechaHoraSeleccionada.getTimeInMillis());

        PrefsHelper.guardarLista(this, listaRecordatorios);
        adapter.notifyDataSetChanged();

        AlarmScheduler.cancelAlarm(this, recordatorio);
        checkAndScheduleAlarm(recordatorio);

        Toast.makeText(this, "Recordatorio actualizado", Toast.LENGTH_SHORT).show();
    }



}