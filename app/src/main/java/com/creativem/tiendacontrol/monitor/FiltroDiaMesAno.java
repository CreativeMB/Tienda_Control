package com.creativem.tiendacontrol.monitor;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.creativem.tiendacontrol.R;
import com.creativem.tiendacontrol.adapter.ItemsAdapter;

import com.creativem.tiendacontrol.dialogFragment.FiltroDiaMesAnoDialogFragment;
import com.creativem.tiendacontrol.helper.BdVentas;
import com.creativem.tiendacontrol.helper.PuntoMil;
import com.creativem.tiendacontrol.model.Items;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class FiltroDiaMesAno extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private BdVentas bdVentas;
    private TextView textViewSelectedDateRange;
    private RecyclerView listaFiltro;
    private ItemsAdapter itemsAdapter;
    private Calendar calendar;
    private String startDate, endDate;
    private String currentDatabase;
    private TextView textViewDatabaseName;
    private TextView textViewPositiveSum, textViewNegativeSum, textViewDifference;
    private SearchView txtBuscar;
    private static final String PREFS_NAME = "TiendaControlPrefs";
    private static final String KEY_CURRENT_DATABASE = "currentDatabase";
    private SharedPreferences sharedPreferences;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth;
    private String userId;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filtrodiamesano);

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        // Obtener nombre de la base de datos desde Intent o SharedPreferences
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("databaseName")) {
            currentDatabase = intent.getStringExtra("databaseName");
            setCurrentDatabaseName(currentDatabase);
        } else {
            currentDatabase = getCurrentDatabaseName();
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            userId = user.getUid();
            databaseReference = database.getReference("users").child(userId).child("databases").child(currentDatabase);
        }
        // Inicializar vistas
        textViewSelectedDateRange = findViewById(R.id.text_view_selected_date);
        listaFiltro = findViewById(R.id.listaFiltro);
        textViewPositiveSum = findViewById(R.id.text_view_positive_sum);
        textViewNegativeSum = findViewById(R.id.text_view_negative_sum);
        textViewDifference = findViewById(R.id.text_view_difference);
        txtBuscar = findViewById(R.id.txtBuscar);
        ImageView iconDatabase = findViewById(R.id.database);
        ImageView iconFiltro = findViewById(R.id.filtro);
        calendar = Calendar.getInstance();
        textViewDatabaseName = findViewById(R.id.text_view_database_name);


        // Configuración del RecyclerView
        listaFiltro.setLayoutManager(new GridLayoutManager(this, 2));
        itemsAdapter = new ItemsAdapter(new ArrayList<>());
        listaFiltro.setAdapter(itemsAdapter);

        // Actualizar el TextView con el nombre de la base de datos actual
        textViewDatabaseName.setText("Cuenta: " + currentDatabase);


        // Inicializar SearchView
        txtBuscar.setOnQueryTextListener(this);

        iconDatabase.setOnClickListener(view -> {
            Intent databaseIntent = new Intent(this, BaseDatos.class);
            startActivity(databaseIntent);
        });
        iconFiltro.setOnClickListener(view -> {
            recreate();
        });


        bdVentas = new BdVentas(this, currentDatabase, databaseReference);
        bdVentas.setOnDataChangeListener(items -> {
            filterDates(startDate, endDate);

        });
        // Seleccionar la fecha de inicio
        selectStartDate();

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

            if (bdVentas != null) {
                Log.d(TAG, "onNewIntent: Cerrando bdHelper existente");
                bdVentas.close();
            }
            FirebaseUser user = mAuth.getCurrentUser();
            if(user != null) {
                userId = user.getUid();
                databaseReference = database.getReference("users").child(userId).child("databases").child(newDatabase);
            }
            bdVentas = new BdVentas(this, currentDatabase,databaseReference);
            bdVentas.setOnDataChangeListener(items -> {
                filterDates(startDate, endDate);

            });

            Log.d(TAG, "onNewIntent: Nueva instancia de BdHelper y BdVentas creada para " + currentDatabase);


        } else {
            Log.d(TAG, "onNewIntent: No se requiere cambio de base de datos");
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            userId = user.getUid();
            databaseReference = database.getReference("users").child(userId).child("databases").child(currentDatabase);
        }
        bdVentas = new BdVentas(this, currentDatabase, databaseReference);
        bdVentas.setOnDataChangeListener(items -> {
            filterDates(startDate, endDate);

        });
    }
    @Override
    protected void onDestroy() {
        if (bdVentas != null) {
            bdVentas.close();
            bdVentas = null;
        }
        super.onDestroy();
    }

    private String getCurrentDatabaseName() {
        return sharedPreferences.getString(KEY_CURRENT_DATABASE, null);
    }

    private void setCurrentDatabaseName(String dbName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CURRENT_DATABASE, dbName);
        editor.apply();
        currentDatabase = dbName;
        Log.d(TAG, "Nombre de la base de datos actualizado a: " + currentDatabase);
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (itemsAdapter != null) {
            itemsAdapter.getFilter().filter(newText);
        }
        return false;
    }

    private void selectStartDate() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        FiltroDiaMesAnoDialogFragment startDatePickerDialog = new FiltroDiaMesAnoDialogFragment(FiltroDiaMesAno.this,
                (view, year1, month1, dayOfMonth) -> {
                    String formattedMonth = String.format("%02d", month1 + 1);
                    String formattedDay = String.format("%02d", dayOfMonth);
                    startDate = year1 + "-" + formattedMonth + "-" + formattedDay;

                    // Automáticamente pasa a seleccionar la fecha final
                    selectEndDate();
                }, year, month, day);
        startDatePickerDialog.show();
    }

    private void selectEndDate() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        FiltroDiaMesAnoDialogFragment endDatePickerDialog = new FiltroDiaMesAnoDialogFragment(FiltroDiaMesAno.this,
                (view, year1, month1, dayOfMonth) -> {
                    String formattedMonth = String.format("%02d", month1 + 1);
                    String formattedDay = String.format("%02d", dayOfMonth);
                    endDate = year1 + "-" + formattedMonth + "-" + formattedDay;

                    textViewSelectedDateRange.setText("De: " + startDate + " A: " + endDate);
                    filterDates(startDate, endDate);
                }, year, month, day);
        endDatePickerDialog.show();
    }
    private void filterDates(String startDate, String endDate) {
        if (bdVentas != null) {
            bdVentas.mostrarVentas();

            // Crear un SimpleDateFormat con el formato de fecha adecuado
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            List<Items> filteredItems = new ArrayList<>();


            // Recorrer la lista y agregar los ítems que cumplen con el filtro de fechas
            for(Items item : bdVentas.mostrarVentas()){
                if(item.getTimestamp() != null){
                    try {
                        Date itemDate = new Date(item.getTimestamp());
                        Date start = sdf.parse(startDate);
                        Date end = sdf.parse(endDate);
                        // Comparar las fechas
                        if (itemDate.compareTo(start)>=0 && itemDate.compareTo(end)<=0) {
                            filteredItems.add(item);
                        }

                    }  catch (ParseException e) {
                        Log.e(TAG, "Error al parsear las fechas: " + e.getMessage());
                    }
                }
            }



            itemsAdapter.updateItems(filteredItems);
            itemsAdapter.notifyDataSetChanged();


            if (filteredItems.isEmpty()) {
                showAlertDialog();
            } else {
                Toast.makeText(FiltroDiaMesAno.this, "Filtrando desde " + startDate + " hasta " + endDate, Toast.LENGTH_SHORT).show();
            }
            // Calcula las sumas
            double sumaPositivos = 0;
            double sumaNegativos = 0;
            for (Items item : filteredItems) {
                if (item.getValor() >= 0) {
                    sumaPositivos += item.getValor();
                } else {
                    sumaNegativos += item.getValor();
                }
            }
            // Calcula la diferencia
            double diferencia = sumaPositivos + sumaNegativos;

            // Actualiza los TextView usando PuntoMil
            textViewPositiveSum.setText(String.format("$%s", PuntoMil.getFormattedNumber((long) sumaPositivos)));
            textViewNegativeSum.setText(String.format("$%s", PuntoMil.getFormattedNumber((long) sumaNegativos)));

            // Mostrar diferencia con signo negativo si es necesario
            if (diferencia < 0) {
                textViewDifference.setText(String.format("$-%s", PuntoMil.getFormattedNumber((long) -diferencia)));
            } else {
                textViewDifference.setText(String.format("$%s", PuntoMil.getFormattedNumber((long) diferencia)));
            }
        }
    }
    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sin resultados");
        builder.setMessage("No se encontraron resultados para el rango de fechas seleccionado.");
        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            dialog.dismiss();
            selectStartDate(); // Vuelve a cargar el calendario al aceptar
        });
        builder.show();
    }
}