package com.example.tiendacontrol.monitor;

import static android.app.PendingIntent.getActivity;
import static android.content.ContentValues.TAG;

import static androidx.core.content.ContentProviderCompat.requireContext;

import static java.security.AccessController.getContext;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendacontrol.R;
import com.example.tiendacontrol.adapter.ItemAdapter;
import com.example.tiendacontrol.dialogFragment.MenuDialogFragment;

import com.example.tiendacontrol.helper.BdVentas;
import com.example.tiendacontrol.model.Items;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;


public class FiltroDiaMesAno extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private BdVentas bdVentas;
    private FloatingActionButton fabMenu;
    private TextView textViewSelectedDateRange;
    private RecyclerView listaFiltro, recyclerView;
    private ItemAdapter itemAdapter; // Usa solo itemAdapter
    private Calendar calendar;
    private String startDate, endDate;
    private String currentDatabase;

    private TextView textViewPositiveSum, textViewNegativeSum, textViewDifference;
    private SearchView txtBuscar;
    private static final String PREFS_NAME = "TiendaControlPrefs";
    private static final String KEY_CURRENT_DATABASE = "currentDatabase";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filtro_dia_mes_ano);
        // Inicializar SharedPreferences (dentro del método onCreate)
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentDatabase = getCurrentDatabaseName();
        currentDatabase = sharedPreferences.getString(KEY_CURRENT_DATABASE, null);
        // Inicializar vistas
        textViewSelectedDateRange = findViewById(R.id.text_view_selected_date);
        listaFiltro = findViewById(R.id.listaFiltro);
        textViewPositiveSum = findViewById(R.id.text_view_positive_sum);
        textViewNegativeSum = findViewById(R.id.text_view_negative_sum);
        textViewDifference = findViewById(R.id.text_view_difference);
        txtBuscar = findViewById(R.id.txtBuscar);
        ImageView iconDatabase = findViewById(R.id.database);
        calendar = Calendar.getInstance();

//        // Configuración del RecyclerView
        itemAdapter = new ItemAdapter(new ArrayList<>()); // Inicializa con una lista vacía
        listaFiltro.setLayoutManager(new GridLayoutManager(this, 2));
        listaFiltro.setAdapter(itemAdapter); // Configura el RecyclerView con itemAdapter

        // Verificar si el Intent contiene un nombre de base de datos
        Intent intent = getIntent();
        if (intent.hasExtra("databaseName")) {
            currentDatabase = intent.getStringExtra("databaseName");
        } else {
            currentDatabase = sharedPreferences.getString(KEY_CURRENT_DATABASE, "nombre_por_defecto.db");
        }

        // Crea una instancia de BdHelper (solo una vez)
        bdVentas = new BdVentas(this, currentDatabase);

        // Inicializar SearchView

        txtBuscar.setOnQueryTextListener(this);

        iconDatabase.setOnClickListener(view -> {
            Intent databaseIntent = new Intent(this, Database.class);
            startActivity(databaseIntent);
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


            bdVentas = new BdVentas(this, currentDatabase);

            Log.d(TAG, "onNewIntent: Nueva instancia de BdHelper y BdVentas creada para " + currentDatabase);

//            onDataChanged();
        } else {
            Log.d(TAG, "onNewIntent: No se requiere cambio de base de datos");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: Re-inicializando BdHelper y BdVentas para " + currentDatabase);


        bdVentas = new BdVentas(this, currentDatabase);

    }

    private String getCurrentDatabaseName() {
        return sharedPreferences.getString(KEY_CURRENT_DATABASE, "nombre_por_defecto.db");
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
        itemAdapter.getFilter().filter(newText);
        return false;
    }

    private void selectStartDate() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog startDatePickerDialog = new DatePickerDialog(FiltroDiaMesAno.this,
                (view, year1, month1, dayOfMonth) -> {
                    String formattedMonth = String.format("%02d", month1 + 1);
                    String formattedDay = String.format("%02d", dayOfMonth);
                    startDate = year1 + "-" + formattedMonth + "-" + formattedDay;
                    selectEndDate();
                }, year, month, day);
        startDatePickerDialog.show();
    }

    private void selectEndDate() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog endDatePickerDialog = new DatePickerDialog(FiltroDiaMesAno.this,
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
        List<Items> filteredItems = bdVentas.getItemsByDates(startDate, endDate);
        // Actualiza el adaptador con los datos filtrados
        itemAdapter.updateItems(filteredItems);

        // IMPORTANTE: Notifica al adaptador sobre el cambio
        itemAdapter.notifyDataSetChanged();

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

        // Actualiza los TextView
        textViewPositiveSum.setText(formatoNumerico(sumaPositivos));
        textViewNegativeSum.setText(formatoNumerico(sumaNegativos));
        textViewDifference.setText(formatoNumerico(sumaPositivos + sumaNegativos));
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

    public String formatoNumerico(double valor) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.'); // Punto como separador de miles
        DecimalFormat df = new DecimalFormat("#,###;#,###", symbols);
        return "$" + df.format(valor);
    }
}