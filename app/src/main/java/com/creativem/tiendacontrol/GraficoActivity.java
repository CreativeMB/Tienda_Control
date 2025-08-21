package com.creativem.tiendacontrol;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class GraficoActivity extends AppCompatActivity {
    private PieChart pieChartPositivos, pieChartNegativos;
    private LinearLayout legendPositivos, legendNegativos;
    private Spinner spinnerBases;
    private DatabaseReference refEmpresas;
    private String userId = "TU_USER_ID";  // se sobrescribe al loguear
    private String baseSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_grafico);

        // Referencias a los gráficos y leyendas
        pieChartPositivos = findViewById(R.id.pieChartPositivos);
        pieChartNegativos = findViewById(R.id.pieChartNegativos);
        legendPositivos = findViewById(R.id.legendPositivos);
        legendNegativos = findViewById(R.id.legendNegativos);
        spinnerBases = findViewById(R.id.spinnerBases);

        ScrollView scrollView = findViewById(R.id.scrollViewGrafico);
        TextView tvCompartir = findViewById(R.id.tvCompartir);

        tvCompartir.setOnClickListener(v -> compartirScrollComoImagen(scrollView));
        // Usuario actual
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            Toast.makeText(this, "Usuario no logueado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Referencia a Firebase
        refEmpresas = FirebaseDatabase.getInstance()
                .getReference("Empresas")
                .child(userId)
                .child("basededatos");

        cargarBasesDisponibles();
    }

    private void cargarBasesDisponibles() {
        refEmpresas.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> bases = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.getKey() != null) {
                        bases.add(ds.getKey());
                    }
                }

                if (bases.isEmpty()) {
                    Toast.makeText(GraficoActivity.this, "No se encontraron bases disponibles", Toast.LENGTH_SHORT).show();
                    pieChartPositivos.clear();
                    pieChartNegativos.clear();
                    return;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(GraficoActivity.this,
                        android.R.layout.simple_spinner_item, bases);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerBases.setAdapter(adapter);

                baseSeleccionada = bases.get(0);
                cargarDatosGrafico(baseSeleccionada);

                spinnerBases.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        baseSeleccionada = bases.get(position);
                        cargarDatosGrafico(baseSeleccionada);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) { }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GraficoActivity.this, "Error al cargar bases: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarDatosGrafico(String base) {
        DatabaseReference ref = refEmpresas.child(base);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Float> acumulados = new HashMap<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String nombre = ds.child("nombre").getValue(String.class);
                    Object valorObj = ds.child("valor").getValue();
                    float valor = 0f;

                    if (valorObj instanceof Long) {
                        valor = ((Long) valorObj).floatValue();
                    } else if (valorObj instanceof Double) {
                        valor = ((Double) valorObj).floatValue();
                    } else if (valorObj instanceof Integer) {
                        valor = ((Integer) valorObj).floatValue();
                    }

                    if (nombre != null) {
                        acumulados.put(nombre, acumulados.getOrDefault(nombre, 0f) + valor);
                    }
                }

                List<PieEntry> entries = new ArrayList<>();
                for (Map.Entry<String, Float> entry : acumulados.entrySet()) {
                    entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                }

                if (!entries.isEmpty()) {
                    mostrarGraficoTortasSeparados(
                            pieChartPositivos, legendPositivos,
                            pieChartNegativos, legendNegativos,
                            entries
                    );
                } else {
                    pieChartPositivos.clear();
                    pieChartNegativos.clear();
                    Toast.makeText(GraficoActivity.this, "No hay datos en la base seleccionada", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GraficoActivity.this, "Error al cargar datos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarGraficoTortasSeparados(PieChart pieChartPositivos, LinearLayout legendPositivos,
                                               PieChart pieChartNegativos, LinearLayout legendNegativos,
                                               List<PieEntry> entries) {

        List<PieEntry> positivos = new ArrayList<>();
        List<PieEntry> negativos = new ArrayList<>();

        for (PieEntry entry : entries) {
            if (entry.getValue() >= 0) {
                positivos.add(entry);
            } else {
                negativos.add(new PieEntry(Math.abs(entry.getValue()), entry.getLabel())); // valores negativos en positivo
            }
        }

        configurarTorta(pieChartPositivos, legendPositivos, positivos, "Positivos");
        configurarTorta(pieChartNegativos, legendNegativos, negativos, "Negativos");
    }

    private void configurarTorta(PieChart pieChart, LinearLayout legendLayout,
                                 List<PieEntry> entries, String titulo) {
        if (entries.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            legendLayout.setVisibility(View.GONE);
            return;
        }

        pieChart.setVisibility(View.VISIBLE);
        legendLayout.setVisibility(View.VISIBLE);

        PieDataSet dataSet = new PieDataSet(entries, titulo);

        // 🎨 Colores automáticos
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            colors.add(Color.HSVToColor(new float[]{(i * 50) % 360, 1f, 1f}));
        }
        dataSet.setColors(colors);

        // ✅ Solo porcentaje dentro de la torta
        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(false); // sin hueco
        pieChart.setDrawEntryLabels(false); // ❌ no mostrar nombres
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.invalidate();

        // 📌 Leyenda personalizada debajo
        legendLayout.removeAllViews();

        float total = 0;
        for (PieEntry entry : entries) {
            total += entry.getValue();
        }

        // 🔢 Formato de número sin decimales y con separador de miles
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("es", "CO"));
        numberFormat.setMaximumFractionDigits(0);

        for (int i = 0; i < entries.size(); i++) {
            PieEntry entry = entries.get(i);

            float porcentaje = (entry.getValue() / total) * 100f;

            TextView legendItem = new TextView(this);
            legendItem.setTextSize(16f);

            // ▪ nombre | valor con miles | porcentaje con 1 decimal
            String texto = entry.getLabel() + "  | $" +
                    numberFormat.format(entry.getValue()) + "  |  " +
                    String.format(Locale.getDefault(), "%.1f%%", porcentaje);

            // color cuadradito a la izquierda
            SpannableString spannable = new SpannableString("■ " + texto);
            spannable.setSpan(new ForegroundColorSpan(colors.get(i)), 0, 1, 0);

            legendItem.setText(spannable);
            legendLayout.addView(legendItem);
        }
    }
    private void compartirScrollComoImagen(ScrollView scrollView) {
        // Ocultar el botón compartir antes de la captura
        View botonCompartir = findViewById(R.id.tvCompartir);
        botonCompartir.setVisibility(View.GONE);

        // Calcular altura total del ScrollView
        int height = 0;
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            height += scrollView.getChildAt(i).getHeight();
        }

        // Crear bitmap en alta calidad (ARGB_8888)
        Bitmap bitmap = Bitmap.createBitmap(scrollView.getWidth(), height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);

        // Guardar la imagen en un archivo temporal
        try {
            File cachePath = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "shared_images");
            if (!cachePath.exists()) cachePath.mkdirs();
            File file = new File(cachePath, "captura_scroll.png");

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos); // PNG sin pérdida de calidad
            fos.close();

            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);

            // Compartir con intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Compartir imagen"));

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al compartir imagen", Toast.LENGTH_SHORT).show();
        } finally {
            // Volver a mostrar el botón después de la captura
            botonCompartir.setVisibility(View.VISIBLE);
        }
    }


}