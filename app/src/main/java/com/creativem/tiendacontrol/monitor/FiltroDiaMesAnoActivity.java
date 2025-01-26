package com.creativem.tiendacontrol.monitor;

import static android.content.ContentValues.TAG;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.creativem.tiendacontrol.R;
import com.creativem.tiendacontrol.adapter.TotalesAdapter;
import com.creativem.tiendacontrol.helper.BdVentas;
import com.creativem.tiendacontrol.helper.PuntoMil;
import com.creativem.tiendacontrol.model.Items;
import com.creativem.tiendacontrol.model.TotalesItem;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import android.widget.CheckBox;

public class FiltroDiaMesAnoActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    // Constantes
    private static final String PREFS_NAME = "TiendaControlPrefs";
    private static final String KEY_CURRENT_DATABASE = "currentDatabase";

    // Variables
    private SharedPreferences sharedPreferences;
    private RecyclerView listaTotales;
    private String currentDatabase;
    private FirebaseAuth mAuth;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final Map<String, List<Items>> allItems = new HashMap<>();
    private CheckBox filterByWeek, filterByMonth, filterByYear;
    private String currentFilter = "Semana";
    private TotalesAdapter totalesAdapter;
    private TextView grandTotalIngresos, grandTotalEgresos, grandTotalDiferencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtro_dia_mes_ano);
        mAuth = FirebaseAuth.getInstance();

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            String userId = user.getUid();
            loadAllDatabases(userId);
        }
        inicializarVistas();
        configurarRecyclerView();
    }
    private void inicializarVistas() {
        listaTotales = findViewById(R.id.listaTotales);
                // Buttons for filter
        filterByWeek = findViewById(R.id.filter_week);
        filterByMonth = findViewById(R.id.filter_month);
        filterByYear = findViewById(R.id.filter_year);

        // Correct initialization of TextViews
        grandTotalIngresos = findViewById(R.id.grand_total_ingresos);
        grandTotalEgresos = findViewById(R.id.grand_total_egresos);
        grandTotalDiferencia = findViewById(R.id.grand_total_diferencia);

        // Inicializar el nuevo TextView
        TextView textViewDatabaseName = findViewById(R.id.text_view_database_name);
        textViewDatabaseName.setText("Todas las bases de datos");

        // Implement Filter Buttons
        filterByWeek.setOnClickListener(v -> {
            currentFilter = "Semana";
            filterByPeriod(currentFilter);
            filterByMonth.setChecked(false);
            filterByYear.setChecked(false);
        });
        filterByMonth.setOnClickListener(v -> {
            currentFilter = "Mes";
            filterByPeriod(currentFilter);
            filterByWeek.setChecked(false);
            filterByYear.setChecked(false);
        });
        filterByYear.setOnClickListener(v -> {
            currentFilter = "Año";
            filterByPeriod(currentFilter);
            filterByMonth.setChecked(false);
            filterByWeek.setChecked(false);
        });
        filterByPeriod(currentFilter);
    }
    private void filterByPeriod(String period){
        if(allItems != null){
            List<TotalesItem> allTotalesItems = new ArrayList<>();
            Log.d(TAG, "Iniciando filtro por " + period);
            for(Map.Entry<String, List<Items>> entry: allItems.entrySet()){
                String databaseName = entry.getKey();
                List<Items> items = entry.getValue();
                if (items != null) {
                    List<TotalesItem> totalesByPeriod = null;
                    Calendar calendar = Calendar.getInstance();
                    Date today = calendar.getTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    switch (period) {
                        case "Semana":
                            totalesByPeriod = filterByWeek(items, today, sdf, databaseName);
                            break;
                        case "Mes":
                            totalesByPeriod = filterByMonth(items, today, sdf, databaseName);
                            break;
                        case "Año":
                            totalesByPeriod = filterByYear(items, today, sdf, databaseName);
                            break;
                        default:
                            Log.w("WARN", "Periodo de filtro no valido");
                    }
                    if(totalesByPeriod != null){
                        allTotalesItems.addAll(totalesByPeriod);
                        Log.d(TAG, "Totales Filtrados por " + period + " en la base de datos " + databaseName + ": " + totalesByPeriod.size() + " items");
                    }else{
                        Log.w(TAG, "No se obtuvieron datos filtrados de base de datos: " + databaseName + " por periodo " + period );
                    }
                }else {
                    Log.e(TAG, "No hay items para filtrar en base de datos: " + databaseName);
                }
            }
            List<TotalesItem> totalesGenerales = calculateGrandTotals(allTotalesItems);
            setGrandTotals(totalesGenerales, grandTotalIngresos, grandTotalEgresos, grandTotalDiferencia);
            if(totalesAdapter != null){
                totalesAdapter.setTotalesItems(allTotalesItems);
            }
            Log.d(TAG, "Cantidad de TotalesItem despues del filtrado: " + allTotalesItems.size());
        }else{
            Log.e(TAG, "No hay bases de datos para filtrar");
        }
    }
    private void setGrandTotals(List<TotalesItem> totalesGenerales, TextView grandTotalIngresos, TextView grandTotalEgresos, TextView grandTotalDiferencia) {
        double totalIngresos = 0;
        double totalEgresos = 0;
        double totalDiferencia = 0;
        for (TotalesItem item : totalesGenerales) {
            totalIngresos += item.getIngresos();
            totalEgresos += item.getEgresos();
            totalDiferencia += item.getDiferencia();
        }
        if (grandTotalIngresos != null && grandTotalEgresos != null && grandTotalDiferencia != null) {
            String ingresosFormatted = PuntoMil.getFormattedNumber((long) totalIngresos);
            String egresosFormatted = PuntoMil.getFormattedNumber((long) Math.abs(totalEgresos)); // Mostrar egresos positivos
            String diferenciaFormatted = PuntoMil.getFormattedNumber((long) totalDiferencia);
            grandTotalIngresos.setText(String.format("$%s", ingresosFormatted));
            grandTotalEgresos.setText(String.format("$%s", egresosFormatted));
            grandTotalDiferencia.setText(String.format("$%s", diferenciaFormatted));
            int colorTexto = totalDiferencia < 0
                    ? ContextCompat.getColor(FiltroDiaMesAnoActivity.this, R.color.colorNegativo)
                    : ContextCompat.getColor(FiltroDiaMesAnoActivity.this, R.color.colorPositivo);
            grandTotalDiferencia.setTextColor(colorTexto);
        } else {
            Log.e(TAG, "Textview para los totales null");
        }
    }
    private List<TotalesItem> filterByWeek(List<Items> items, Date today, SimpleDateFormat sdf, String databaseName){
        List<TotalesItem> totalesItems = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        Date startOfWeek = calendar.getTime();

        Calendar endOfWeekCal = Calendar.getInstance();
        endOfWeekCal.setTime(today);

        while(startOfWeek.before(today) || startOfWeek.equals(today)){
            Date endOfWeek = endOfWeekCal.getTime();
            if(endOfWeekCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || endOfWeekCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
                endOfWeekCal.add(Calendar.DAY_OF_MONTH, 1);
                endOfWeek = endOfWeekCal.getTime();
            }else{
                endOfWeekCal.add(Calendar.DAY_OF_MONTH, 1);
            }
            List<Items> filteredItems = new ArrayList<>();
            for (Items item : items) {
                if (item.date != null) {
                    try {
                        Date itemDate = sdf.parse(item.date);
                        if (itemDate != null && (itemDate.after(startOfWeek) && itemDate.before(endOfWeek))) {
                            filteredItems.add(item);
                        }
                    } catch (ParseException e) {
                        Log.e("ERROR PARSING DATE", "Error al parsear la fecha: " + item.date, e);
                    }
                } else if (item.fecha != null) {
                    try {
                        Date itemDate = sdf.parse(item.fecha);
                        if (itemDate != null && (itemDate.after(startOfWeek) && itemDate.before(endOfWeek))) {
                            filteredItems.add(item);
                        }
                    } catch (ParseException e) {
                        Log.e("ERROR PARSING DATE", "Error al parsear la fecha: " + item.fecha, e);
                    }
                } else {
                    Log.w("WARN", "El item no contiene ninguna fecha");
                }
            }
            if(filteredItems.size() > 0){
                TotalesItem totalesItem = calculateTotals(filteredItems, databaseName).get(0);
                totalesItem.setItemDate("Semana del " + sdf.format(startOfWeek) + " al " + sdf.format(endOfWeek));
                totalesItem.setPeriod("Semana"); //Añadir el periodo a la variable TotalesItem
                totalesItems.add(totalesItem);
            }
            startOfWeek = endOfWeek;
        }
        return totalesItems;
    }
    private List<TotalesItem> filterByMonth(List<Items> items, Date today,  SimpleDateFormat sdf, String databaseName){
        List<TotalesItem> totalesItems = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startOfMonth = calendar.getTime();

        Calendar endOfMonthCal = Calendar.getInstance();
        endOfMonthCal.setTime(today);
        while(startOfMonth.before(today) || startOfMonth.equals(today)){
            Date endOfMonth = endOfMonthCal.getTime();
            if(endOfMonthCal.get(Calendar.DAY_OF_MONTH) == endOfMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)){
                endOfMonthCal.add(Calendar.DAY_OF_MONTH, 1);
                endOfMonth = endOfMonthCal.getTime();
            }else{
                endOfMonthCal.add(Calendar.DAY_OF_MONTH, 1);
            }
            List<Items> filteredItems = new ArrayList<>();
            for (Items item : items) {
                if (item.date != null) {
                    try {
                        Date itemDate = sdf.parse(item.date);
                        if(itemDate != null && (itemDate.after(startOfMonth) && itemDate.before(endOfMonth))){
                            filteredItems.add(item);
                        }
                    }catch(ParseException e){
                        Log.e("ERROR PARSING DATE", "Error al parsear la fecha: " + item.date, e);
                    }
                } else if (item.fecha != null) {
                    try {
                        Date itemDate = sdf.parse(item.fecha);
                        if(itemDate != null && (itemDate.after(startOfMonth) && itemDate.before(endOfMonth))){
                            filteredItems.add(item);
                        }
                    }catch(ParseException e){
                        Log.e("ERROR PARSING DATE", "Error al parsear la fecha: " + item.fecha, e);
                    }
                }else {
                    Log.w("WARN", "El item no contiene ninguna fecha");
                }
            }
            if (filteredItems.size() > 0){
                TotalesItem totalesItem = calculateTotals(filteredItems, databaseName).get(0);
                totalesItem.setItemDate("Mes del " + sdf.format(startOfMonth) + " al " + sdf.format(endOfMonth));
                totalesItem.setPeriod("Mes"); //Añadir el periodo a la variable TotalesItem
                totalesItems.add(totalesItem);
            }
            startOfMonth = endOfMonth;
        }
        return totalesItems;
    }
    private List<TotalesItem> filterByYear(List<Items> items, Date today, SimpleDateFormat sdf, String databaseName){
        List<TotalesItem> totalesItems = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        Date startOfYear = calendar.getTime();

        Calendar endOfYearCal = Calendar.getInstance();
        endOfYearCal.setTime(today);
        while (startOfYear.before(today) || startOfYear.equals(today)){
            Date endOfYear = endOfYearCal.getTime();
            if(endOfYearCal.get(Calendar.DAY_OF_YEAR) == endOfYearCal.getActualMaximum(Calendar.DAY_OF_YEAR)){
                endOfYearCal.add(Calendar.DAY_OF_YEAR, 1);
                endOfYear = endOfYearCal.getTime();
            }else{
                endOfYearCal.add(Calendar.DAY_OF_YEAR, 1);
            }
            List<Items> filteredItems = new ArrayList<>();
            for (Items item : items) {
                if (item.date != null) {
                    try {
                        Date itemDate = sdf.parse(item.date);
                        if (itemDate != null && (itemDate.after(startOfYear) && itemDate.before(endOfYear))) {
                            filteredItems.add(item);
                        }

                    } catch (ParseException e) {
                        Log.e("ERROR PARSING DATE", "Error al parsear la fecha: " + item.date, e);
                    }
                } else if (item.fecha != null) {
                    try {
                        Date itemDate = sdf.parse(item.fecha);
                        if (itemDate != null && (itemDate.after(startOfYear) && itemDate.before(endOfYear))) {
                            filteredItems.add(item);
                        }

                    } catch (ParseException e) {
                        Log.e("ERROR PARSING DATE", "Error al parsear la fecha: " + item.fecha, e);
                    }
                } else {
                    Log.w("WARN", "El item no contiene ninguna fecha");
                }
            }
            if (filteredItems.size() > 0){
                TotalesItem totalesItem = calculateTotals(filteredItems, databaseName).get(0);
                totalesItem.setItemDate("Año del " + sdf.format(startOfYear) + " al " + sdf.format(endOfYear));
                totalesItem.setPeriod("Año"); //Añadir el periodo a la variable TotalesItem
                totalesItems.add(totalesItem);
            }
            startOfYear = endOfYear;
        }
        return totalesItems;
    }
    private List<TotalesItem> calculateTotals(List<Items> items, String databaseName) {
        Map<String, TotalesItem> totalsMap = new HashMap<>();

        for (Items item : items) {
            String itemName = "Total";
            TotalesItem totalesItem = totalsMap.getOrDefault(itemName, new TotalesItem(itemName, 0, 0));

            if (item.getType() != null) {
                if (item.getType().equals("Ingreso")) {
                    totalesItem.setIngresos(totalesItem.getIngresos() + item.getValor());
                } else if (item.getType().equals("Gasto")) {
                    totalesItem.setEgresos(totalesItem.getEgresos() + item.getValor());
                }
                totalsMap.put(itemName, totalesItem);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            if(item.date != null){
                try{
                    Date itemDate = sdf.parse(item.date);
                    if (itemDate != null){
                        totalesItem.setItemDate(sdf.format(itemDate));
                    }

                }catch(Exception e){
                    Log.e(TAG, "Error al obtener la fecha del item: " + item.getProducto(), e );
                }
            }else if (item.fecha != null){
                try{
                    Date itemDate = sdf.parse(item.fecha);
                    if (itemDate != null){
                        totalesItem.setItemDate(sdf.format(itemDate));
                    }
                }catch(Exception e){
                    Log.e(TAG, "Error al obtener la fecha del item: " + item.getProducto(), e );
                }
            }
        }

        List<TotalesItem> totalesItems = new ArrayList<>(totalsMap.values());
        for(TotalesItem item : totalesItems){
            item.setDiferencia(item.getIngresos() + item.getEgresos());
            item.setDatabaseName(databaseName);
        }
        return totalesItems;
    }
    private List<TotalesItem> calculateGrandTotals(List<TotalesItem> totalesItems){
        Map<String, TotalesItem> totalsMap = new HashMap<>();

        for (TotalesItem totalesItem : totalesItems) {
            String itemName = "Total";
            TotalesItem grandTotalItem = totalsMap.getOrDefault(itemName, new TotalesItem(itemName, 0, 0));
            grandTotalItem.setIngresos(grandTotalItem.getIngresos() + totalesItem.getIngresos());
            grandTotalItem.setEgresos(grandTotalItem.getEgresos() + totalesItem.getEgresos());
            grandTotalItem.setDiferencia(grandTotalItem.getDiferencia() + totalesItem.getDiferencia());
            totalsMap.put(itemName, grandTotalItem);
        }

        List<TotalesItem> grandTotales = new ArrayList<>(totalsMap.values());
        return grandTotales;
    }
    private void configurarRecyclerView() {
        listaTotales.setLayoutManager(new LinearLayoutManager(this));
        List<TotalesItem> totalesItems = new ArrayList<>();
        totalesAdapter = new TotalesAdapter(this, totalesItems);
        listaTotales.setAdapter(totalesAdapter);
        Log.d(TAG, "RecyclerView configurado en la inicialización");
    }

    private void loadAllDatabases(String userId) {
        DatabaseReference userDatabasesRef = database.getReference("users").child(userId).child("databases");
        userDatabasesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot databaseSnapshot : snapshot.getChildren()) {
                        String databaseName = databaseSnapshot.getKey();
                        loadItemsForDatabase(userId, databaseName);
                    }
                } else {
                    Log.e(TAG, "No se encontraron bases de datos para el usuario" );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar bases de datos desde Firebase: " + error.getMessage());
            }
        });

    }
    private void loadItemsForDatabase(String userId, String databaseName){
        DatabaseReference databaseRef = database.getReference("users").child(userId).child("databases").child(databaseName);
        BdVentas bdVentas = new BdVentas(this, databaseName, databaseRef);
        bdVentas.setOnDataChangeListener(items -> {
            if(items != null){
                allItems.put(databaseName, items);
                filterByPeriod(currentFilter);
                Log.d(TAG, "Se obtuvieron " + items.size() + " items de la base de datos: " + databaseName);
            }else{
                Log.e(TAG, "No se obtuvieron datos para la base de datos " + databaseName );
            }

        });
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}