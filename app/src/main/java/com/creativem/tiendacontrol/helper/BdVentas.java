package com.creativem.tiendacontrol.helper;

import android.content.Context;
import android.util.Log;
import com.creativem.tiendacontrol.model.Items;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

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
        cargarDatos();

    }



    public void close() {
        // No es necesario cerrar nada con Firebase
    }

    private void cargarDatos() {
        if (databaseReference != null) {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    itemsList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Items item = snapshot.getValue(Items.class);
                        if(item != null){
                            itemsList.add(item);
                        }
                    }
                    if(onDataChangeListener != null){
                        onDataChangeListener.onDataChange(itemsList);
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Error al cargar datos desde Firebase: " + databaseError.getMessage());
                }
            });
        } else {
            Log.e(TAG,"databaseReference null");
        }
    }


    public ArrayList<Items> mostrarVentas() {
        return itemsList;
    }


    public double obtenerTotalVentas() {
        double total = 0;
        if (itemsList != null) {
            for (Items items : itemsList) {
                if (items.getType().equals("Ingreso"))
                    total += items.getValor();
            }
        }
        return total;
    }

    public double obtenerTotalEgresos() {
        double total = 0;
        if (itemsList != null) {
            for (Items items : itemsList) {
                if (items.getType().equals("Gasto"))
                    total += items.getValor();
            }
        }
        return total;
    }


    public String obtenerDiferencia() {
        double diferencia = obtenerTotalVentas() - obtenerTotalEgresos();
        return PuntoMil.getFormattedNumber((long) diferencia);
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