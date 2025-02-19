package com.creativem.tiendacontrol.monitor;

import static android.content.ContentValues.TAG;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
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

import com.google.android.material.datepicker.MaterialDatePicker;

public class FiltroDiaMesAnoActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    // Constantes
    private static final String PREFS_NAME = "TiendaControlPrefs";
    private static final String KEY_CURRENT_DATABASE = "currentDatabase";
    private MaterialDatePicker<Pair<Long, Long>> datePicker; //  Usamos Pair<Long,Long> para rango de fechas
    private long startDate, endDate; // Almacenar fechas seleccionadas como longs (milisegundos desde la época)
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
    private TextView selectedDateRangeView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtro_dia_mes_ano);


        datePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Seleccionar rango de fechas")
                .build();

        // Asegúrate de tener un botón en tu layout XML con un ID, por ejemplo: button_select_dates
        findViewById(R.id.button_select_dates).setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), "DATE_PICKER"));

        datePicker.addOnPositiveButtonClickListener(selection -> {
            startDate = selection.first;
            endDate = selection.second;
            if (startDate != 0 && endDate != 0 && startDate <= endDate) {
                filterByCustomDateRange(startDate, endDate);
                actualizarFechaSeleccionada(startDate, endDate); // Update the TextView
            }
        });


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
        selectedDateRangeView = findViewById(R.id.selected_date_range); // Inicialización de la variable
        if (selectedDateRangeView == null) {
        }

        listaTotales = findViewById(R.id.listaTotales);
                // Buttons for filter
        filterByWeek = findViewById(R.id.filter_week);
        filterByMonth = findViewById(R.id.filter_month);
        filterByYear = findViewById(R.id.filter_year);

        // Correct initialization of TextViews
        grandTotalIngresos = findViewById(R.id.grand_total_ingresos);
        grandTotalEgresos = findViewById(R.id.grand_total_egresos);
        grandTotalDiferencia = findViewById(R.id.grand_total_diferencia);

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

    private void filterByCustomDateRange(long startDate, long endDate) {
        if (allItems != null) {
            List<TotalesItem> allTotalesItems = new ArrayList<>();
            SimpleDateFormat sdfColombia = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            sdfColombia.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
            for (Map.Entry<String, List<Items>> entry : allItems.entrySet()) {
                String databaseName = entry.getKey();
                List<Items> items = entry.getValue();
                if (items != null) {
                    List<TotalesItem> totalesByRange = filterByDateRange(items, startDate, endDate, databaseName);
                    if (totalesByRange != null) {
                        allTotalesItems.addAll(totalesByRange);
                        Log.d(TAG, "Totales filtrados por rango de fechas en la base de datos " + databaseName + ": " + totalesByRange.size() + " items");
                    } else {
                        Log.w(TAG, "No se encontraron datos en el rango de fechas para la base de datos: " + databaseName);
                    }
                } else {
                    Log.e(TAG, "No hay items para filtrar en la base de datos: " + databaseName);
                }
            }
            List<TotalesItem> totalesGenerales = calculateGrandTotals(allTotalesItems);
            setGrandTotals(totalesGenerales, grandTotalIngresos, grandTotalEgresos, grandTotalDiferencia);
            if (totalesAdapter != null) {
                totalesAdapter.setTotalesItems(allTotalesItems);
            }
            Log.d(TAG, "Cantidad de TotalesItem después del filtrado: " + allTotalesItems.size());
        } else {
            Log.e(TAG, "No hay bases de datos para filtrar");
        }
    }

    private void actualizarFechaSeleccionada(long startDate, long endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (selectedDateRangeView != null) {
            selectedDateRangeView.setText("Periodo: " + sdf.format(new Date(startDate)) + " - " + sdf.format(new Date(endDate)));
        } else {
            Log.e("Error", "selected_date_range TextView not found.");
        }
    }
    private List<TotalesItem> filterByDateRange(List<Items> items, long startDate, long endDate, String databaseName) {
        List<TotalesItem> totalesItems = new ArrayList<>();
        SimpleDateFormat sdfColombia = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdfColombia.setTimeZone(TimeZone.getTimeZone("America/Bogota"));

        List<Items> filteredItems = new ArrayList<>();
        for (Items item : items) {
            Date itemDate;
            try {
                itemDate = sdfColombia.parse(item.date != null ? item.date : item.fecha);
                if (itemDate != null) {
                    long itemTimeInMillis = itemDate.getTime();
                    if (itemTimeInMillis >= startDate && itemTimeInMillis <= endDate) {
                        filteredItems.add(item);
                    }
                }
            } catch (ParseException e) {
                Log.e("ERROR PARSING DATE", "Error al parsear la fecha: " + (item.date != null ? item.date : item.fecha), e);
            }
        }

        if (filteredItems.size() > 0) {
            TotalesItem totalesItem = calculateTotals(filteredItems, databaseName).get(0);
            Date startDateObj = new Date(startDate);
            Date endDateObj = new Date(endDate);
            totalesItem.setItemDate("Rango: " + sdfColombia.format(startDateObj) + " - " + sdfColombia.format(endDateObj));
            totalesItem.setPeriod("Rango de fechas");
            totalesItems.add(totalesItem);
        }
        return totalesItems;
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
    private List<TotalesItem> filterByWeek(List<Items> items, Date today, SimpleDateFormat sdf, String databaseName) {
        List<TotalesItem> totalesItems = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);

        // Forzar el primer día de la semana a LUNES (2)  Independientemente de la configuración regional
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int diff = Calendar.MONDAY - dayOfWeek;
        if (diff > 0) {
            diff -= 7;
        }
        calendar.add(Calendar.DAY_OF_YEAR, diff);
        Date startOfWeek = calendar.getTime();


        // Calculate endOfWeek precisely (Sunday 11:59 PM)  Using Calendar for precision.
        Calendar endOfWeekCal = Calendar.getInstance();
        endOfWeekCal.setTime(startOfWeek);
        endOfWeekCal.add(Calendar.DAY_OF_YEAR, 6); // Add 6 days to get to Sunday

        // Set time to 11:59 PM
        endOfWeekCal.set(Calendar.HOUR_OF_DAY, 23);
        endOfWeekCal.set(Calendar.MINUTE, 59);
        endOfWeekCal.set(Calendar.SECOND, 59);
        endOfWeekCal.set(Calendar.MILLISECOND, 999);
        Date endOfWeek = endOfWeekCal.getTime();

        // Create SimpleDateFormat with Colombia's time zone
        SimpleDateFormat sdfColombia = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdfColombia.setTimeZone(TimeZone.getTimeZone("America/Bogota"));

        //Ajusta las horas a 00:00:00 para ambos límites para mayor precisión.  Important for consistency.
        calendar.setTime(startOfWeek);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        startOfWeek = calendar.getTime();


        List<Items> filteredItems = new ArrayList<>();
        for (Items item : items) {
            Date itemDate;
            try {
                itemDate = sdfColombia.parse(item.date != null ? item.date : item.fecha); // Use sdfColombia here!
                if (itemDate != null && !itemDate.before(startOfWeek) && !itemDate.after(endOfWeek)) {
                    filteredItems.add(item);
                }
            } catch (ParseException e) {
                Log.e("ERROR PARSING DATE", "Error al parsear la fecha: " + (item.date != null ? item.date : item.fecha), e);
            }
        }

        if (filteredItems.size() > 0) {
            TotalesItem totalesItem = calculateTotals(filteredItems, databaseName).get(0);
            totalesItem.setItemDate("Semana del " + sdfColombia.format(startOfWeek) + " al " + sdfColombia.format(endOfWeek)); // Use sdfColombia here!
            totalesItem.setPeriod("Semana");
            totalesItems.add(totalesItem);
        }

        return totalesItems;
    }
    private List<TotalesItem> filterByMonth(List<Items> items, Date today, SimpleDateFormat sdf, String databaseName) {
        List<TotalesItem> totalesItems = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);

        // Se establece en el primer día del mes
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startOfMonth = calendar.getTime();

        // Calcula el último día del mes con precisión
        Calendar endOfMonthCal = Calendar.getInstance();
        endOfMonthCal.setTime(startOfMonth);
        endOfMonthCal.add(Calendar.MONTH, 1); // Ir al mes siguiente
        endOfMonthCal.add(Calendar.DAY_OF_YEAR, -1); // Restar un día para obtener el último día del mes actual.
        endOfMonthCal.set(Calendar.HOUR_OF_DAY, 23);
        endOfMonthCal.set(Calendar.MINUTE, 59);
        endOfMonthCal.set(Calendar.SECOND, 59);
        endOfMonthCal.set(Calendar.MILLISECOND, 999);
        Date endOfMonth = endOfMonthCal.getTime();

        // Crea SimpleDateFormat con la zona horaria de Colombia
        SimpleDateFormat sdfColombia = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdfColombia.setTimeZone(TimeZone.getTimeZone("America/Bogota"));

        // Ajusta startOfMonth a 00:00:00. Importante para la consistencia.
        calendar.setTime(startOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        startOfMonth = calendar.getTime();

        List<Items> filteredItems = new ArrayList<>();
        for (Items item : items) {
            Date itemDate;
            try {
                itemDate = sdfColombia.parse(item.date != null ? item.date : item.fecha);
                if (itemDate != null && !itemDate.before(startOfMonth) && !itemDate.after(endOfMonth)) {
                    filteredItems.add(item);
                }
            } catch (ParseException e) {
                Log.e("ERROR PARSING DATE", "Error al parsear la fecha: " + (item.date != null ? item.date : item.fecha), e);
            }
        }

        if (filteredItems.size() > 0) {
            TotalesItem totalesItem = calculateTotals(filteredItems, databaseName).get(0);
            totalesItem.setItemDate("Mes del " + sdfColombia.format(startOfMonth) + " al " + sdfColombia.format(endOfMonth));
            totalesItem.setPeriod("Mes");
            totalesItems.add(totalesItem);
        }

        return totalesItems;
    }
    private List<TotalesItem> filterByYear(List<Items> items, Date today, SimpleDateFormat sdf, String databaseName) {
        List<TotalesItem> totalesItems = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);

        // Se establece en el primer día del año
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        Date startOfYear = calendar.getTime();

        // Calcula el último día del año con precisión
        Calendar endOfYearCal = Calendar.getInstance();
        endOfYearCal.setTime(startOfYear);
        endOfYearCal.add(Calendar.YEAR, 1); // Ir al año siguiente
        endOfYearCal.add(Calendar.DAY_OF_YEAR, -1); // Restar un día para obtener el último día del año actual
        endOfYearCal.set(Calendar.HOUR_OF_DAY, 23);
        endOfYearCal.set(Calendar.MINUTE, 59);
        endOfYearCal.set(Calendar.SECOND, 59);
        endOfYearCal.set(Calendar.MILLISECOND, 999);
        Date endOfYear = endOfYearCal.getTime();

        // Crea SimpleDateFormat con la zona horaria de Colombia
        SimpleDateFormat sdfColombia = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdfColombia.setTimeZone(TimeZone.getTimeZone("America/Bogota"));

        // Ajusta startOfYear a 00:00:00
        calendar.setTime(startOfYear);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        startOfYear = calendar.getTime();

        List<Items> filteredItems = new ArrayList<>();
        for (Items item : items) {
            Date itemDate;
            try {
                itemDate = sdfColombia.parse(item.date != null ? item.date : item.fecha);
                if (itemDate != null && !itemDate.before(startOfYear) && !itemDate.after(endOfYear)) {
                    filteredItems.add(item);
                }
            } catch (ParseException e) {
                Log.e("ERROR PARSING DATE", "Error al parsear la fecha: " + (item.date != null ? item.date : item.fecha), e);
            }
        }

        if (filteredItems.size() > 0) {
            TotalesItem totalesItem = calculateTotals(filteredItems, databaseName).get(0);
            totalesItem.setItemDate("Año del " + sdfColombia.format(startOfYear) + " al " + sdfColombia.format(endOfYear));
            totalesItem.setPeriod("Año");
            totalesItems.add(totalesItem);
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