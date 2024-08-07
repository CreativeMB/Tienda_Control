package com.example.tiendacontrol.monitor;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendacontrol.R;
import com.example.tiendacontrol.adapter.ItemAdapter;
import com.example.tiendacontrol.dialogFragment.MenuDialogFragment;
import com.example.tiendacontrol.helper.BdHelper;
import com.example.tiendacontrol.model.Items;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;


public class FiltroDiaMesAno extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private FloatingActionButton fabMenu;
    private TextView textViewSelectedDateRange;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private Calendar calendar;
    private String startDate, endDate;
    private BdHelper bdHelper; // Instancia de BdHelper
    private TextView textViewPositiveSum, textViewNegativeSum, textViewDifference;
    private SearchView txtBuscar;
    private String currentDatabase; // Variable para almacenar el nombre de la base de datos
    private static final String PREFS_NAME = "TiendaControlPrefs";
    private static final String KEY_CURRENT_DATABASE = "currentDatabase";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filtro_dia_mes_ano);

        textViewSelectedDateRange = findViewById(R.id.text_view_selected_date);
        recyclerView = findViewById(R.id.recycler_view_results);
        textViewPositiveSum = findViewById(R.id.text_view_positive_sum);
        textViewNegativeSum = findViewById(R.id.text_view_negative_sum);
        textViewDifference = findViewById(R.id.text_view_difference);
        fabMenu = findViewById(R.id.fabMenu);
        calendar = Calendar.getInstance();

        // Inicializar la base de datos
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentDatabase = sharedPreferences.getString(KEY_CURRENT_DATABASE, "");
        bdHelper = new BdHelper(this, currentDatabase);

        // Inicializar SearchView
        txtBuscar = findViewById(R.id.txtBuscar);
        // Configurar el listener para el SearchView
        txtBuscar.setOnQueryTextListener(this);

        // Inicializar el adaptador y el RecyclerView
        itemAdapter = new ItemAdapter(new ArrayList<>()); // Inicializa con una lista vacía
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(itemAdapter);

        fabMenu.setOnClickListener(view -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            MenuDialogFragment menuDialogFragment = MenuDialogFragment.newInstance();
            menuDialogFragment.show(fragmentManager, "servicios_dialog");
        });

        selectStartDate();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Filtrar el RecyclerView según el texto de búsqueda
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
        List<Items> filteredItems = bdHelper.getItemsByDates(startDate, endDate); // Obtén los datos de la base de datos
        itemAdapter.updateItems(filteredItems); // Actualiza el adaptador con los datos filtrados

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