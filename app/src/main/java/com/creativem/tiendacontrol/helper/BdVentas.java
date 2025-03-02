package com.creativem.tiendacontrol.helper;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.creativem.tiendacontrol.model.Items;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class BdVentas {
    private static final String TAG = "BdVentas";
    private String currentDatabase;
    private DatabaseReference databaseReference;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private ArrayList<Items> itemsList = new ArrayList<>();
    private OnDataChangeListener onDataChangeListener;


    public interface OnDataChangeListener {
        void onDataChange(ArrayList<Items> items);
    }

    public void setOnDataChangeListener(OnDataChangeListener listener) {
        this.onDataChangeListener = listener;
    }

    public BdVentas(Context context, String currentDatabase, DatabaseReference databaseReference) {
        this.currentDatabase = currentDatabase;
        this.databaseReference = databaseReference;
    }
    public void close() {
        // No es necesario cerrar nada con Firebase
    }
    public void cargarDatos() {
        if (databaseReference != null) {
            databaseReference.addValueEventListener(new ValueEventListener() { // ⚡ Escucha cambios en tiempo real
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    itemsList.clear();
                    if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // Ignora nodos de metadatos como timestamp, fechaCreacion, etc.
                            if (snapshot.getKey().equals("timestamp") || snapshot.getKey().equals("fechaCreacion") || snapshot.getKey().equals("databaseName")) {
                                continue;
                            }

                            try {
                                Items item = snapshot.getValue(Items.class);
                                if (item != null) {
                                    long timestamp = snapshot.child("timestamp").getValue(Long.class);
                                    Date fecha = new Date(timestamp);
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                    String fechaString = sdf.format(fecha);
                                    item.setFecha(fechaString);
                                    itemsList.add(item);
                                    Log.d(TAG, "✅ Item cargado: " + item.toString());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "⚠️ Error al convertir un item: " + e.getMessage(), e);
                            }
                        }
                    } else {
                        Log.d(TAG, "❌ No se encontraron datos o no hay hijos en la referencia.");
                    }

                    // 🔄 Notificar cambio de datos
                    if (onDataChangeListener != null) {
                        Log.d(TAG, "📌 BdVentas - onDataChange: Llamando a onDataChangeListener con " + itemsList.size() + " items");
                        onDataChangeListener.onDataChange(itemsList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "❌ Error al cargar datos: " + databaseError.getMessage(), databaseError.toException());
                }
            });
        } else {
            Log.e(TAG, "⚠️ databaseReference es null");
        }
    }


    public ArrayList<Items> mostrarVentas() {
        return itemsList;
    }


    public double obtenerTotalVentas() {
        double total = 0;
        if (itemsList != null) {
            for (Items items : itemsList) {
                if (items.getType() != null && items.getType().equals("Ingreso"))
                    total += items.getValor();
            }
        }
        return total;
    }

    public double obtenerTotalEgresos() {
        double total = 0;
        if (itemsList != null) {
            for (Items items : itemsList) {
                if (items.getType() != null && items.getType().equals("Gasto"))
                    total += items.getValor();
            }
        }
        return total;
    }

    public boolean eliminarTodo() {
        if (databaseReference != null) {
            databaseReference.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Todos los items eliminados de firebase");
                    itemsList.clear();
                    if(onDataChangeListener != null){
                        onDataChangeListener.onDataChange(itemsList);
                    }
                } else {
                    Log.e(TAG, "Error al eliminar todos los items de firebase: " + task.getException());
                }
            });
            return true;
        } else {
            Log.e(TAG, "databaseReference is null, cannot delete all items.");
            return false;
        }
    }
}