package com.example.tiendacontrol.monitor;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendacontrol.R;
import com.example.tiendacontrol.adapter.BaseDatosAdapter;

import com.example.tiendacontrol.dialogFragment.MenuDialogFragment;
import com.example.tiendacontrol.helper.BdHelper;
import com.example.tiendacontrol.model.Items;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FiltroDiaMesAno extends AppCompatActivity {
    private FloatingActionButton fabMenu;
    private BdHelper bdHelper;
    private RecyclerView recyclerView;
    private BaseDatosAdapter ventasAdapter;
    private TextView textViewSelectedDate;
    private TextView textViewPositiveSum, textViewNegativeSum, textViewDifference;
    private Button buttonSelectDate, buttonFilter;
    private RadioGroup radioGroupFilter;
    private Calendar calendar;
    private String selectedDate;
    private DecimalFormat decimalFormat;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filtro_dia_mes_ano);

        bdHelper = new BdHelper(this);
        recyclerView = findViewById(R.id.recycler_view_results);
        textViewSelectedDate = findViewById(R.id.text_view_selected_date);
        buttonSelectDate = findViewById(R.id.button_select_date);
        buttonFilter = findViewById(R.id.button_filter);
        radioGroupFilter = findViewById(R.id.radio_group_filter);
        textViewPositiveSum = findViewById(R.id.text_view_positive_sum);
        textViewNegativeSum = findViewById(R.id.text_view_negative_sum);
        textViewDifference = findViewById(R.id.text_view_difference);

        fabMenu = findViewById(R.id.fabMenu);
        calendar = Calendar.getInstance();
        decimalFormat = new DecimalFormat("#,###");

        // Configuración del RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ventasAdapter = new BaseDatosAdapter(new ArrayList<>());
        recyclerView.setAdapter(ventasAdapter);

// Configura el RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ventasAdapter = new BaseDatosAdapter(new ArrayList<>());
        recyclerView.setAdapter(ventasAdapter);

// Deshabilitar clics en el RecyclerView específico
        recyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                return false; // Permitir el desplazamiento
            }
            return true; // Ignorar los clics
        });

        fabMenu.setOnClickListener(view -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            MenuDialogFragment menuDialogFragment = MenuDialogFragment.newInstance();
            menuDialogFragment.show(fragmentManager, "servicios_dialog");


        });

        buttonSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(FiltroDiaMesAno.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // Asegurarse de que el mes y el día tengan dos dígitos
                                String formattedMonth = String.format("%02d", month + 1);
                                String formattedDay = String.format("%02d", dayOfMonth);
                                selectedDate = year + "-" + formattedMonth + "-" + formattedDay;
                                textViewSelectedDate.setText(selectedDate);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        buttonFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedDate == null || selectedDate.isEmpty()) {
                    Toast.makeText(FiltroDiaMesAno.this, "Por favor, selecciona una fecha", Toast.LENGTH_SHORT).show();
                    return;
                }

                int selectedFilterId = radioGroupFilter.getCheckedRadioButtonId();
                String filterType = null;

                if (selectedFilterId == R.id.radio_day) {
                    filterType = "day";
                } else if (selectedFilterId == R.id.radio_week) {
                    filterType = "week";
                } else if (selectedFilterId == R.id.radio_month) {
                    filterType = "month";
                } else if (selectedFilterId == R.id.radio_year) {
                    filterType = "year";
                } else {
                    Toast.makeText(FiltroDiaMesAno.this, "Selecciona un tipo de filtro", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Items> filteredItems = null;

                if ("day".equals(filterType)) {
                    filteredItems = bdHelper.getResultsByDay(selectedDate);
                } else if ("week".equals(filterType)) {
                    filteredItems = bdHelper.getResultsByWeek(selectedDate);
                } else if ("month".equals(filterType)) {
                    filteredItems = bdHelper.getResultsByMonth(selectedDate);
                } else if ("year".equals(filterType)) {
                    filteredItems = bdHelper.getResultsByYear(selectedDate);
                }
                if (filteredItems != null && !filteredItems.isEmpty()) {
                    // Actualizar el adaptador con los ítems filtrados
                    ventasAdapter.setItems(new ArrayList<>(filteredItems));

                    // Calcular sumas positivas y negativas
                    double positiveSum = 0;
                    double negativeSum = 0;
                    for (Items item : filteredItems) {
                        double valor = item.getValor();
                        if (valor >= 0) {
                            positiveSum += valor;
                        } else {
                            negativeSum += Math.abs(valor); // Convertir el valor negativo a positivo
                        }
                    }

                double difference = positiveSum - negativeSum; // La diferencia será la suma de positivos y negativos

                    // Mostrar resultados
                    textViewPositiveSum.setText("$" + decimalFormat.format(positiveSum));
                    textViewNegativeSum.setText("$" + decimalFormat.format(negativeSum));
                    textViewDifference.setText("$" + decimalFormat.format(difference));
                } else {
                    Toast.makeText(FiltroDiaMesAno.this, "No se encontraron resultados", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
