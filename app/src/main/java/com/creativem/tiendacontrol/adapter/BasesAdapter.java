package com.creativem.tiendacontrol.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.creativem.tiendacontrol.R;
import com.creativem.tiendacontrol.helper.PuntoMil;
import com.creativem.tiendacontrol.model.Items;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class BasesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_EMPTY = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    public interface OnDatabaseClickListener {
        void onDatabaseClick(String databaseName);
    }

    private Context context;
    private List<String> databaseList;
    private OnDatabaseClickListener listener;
    private static final String TAG = "BasesAdapter";
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();


    public BasesAdapter(Context context, List<String> databaseList, OnDatabaseClickListener listener) {
        this.context = context;
        this.databaseList = databaseList;
        this.listener = listener;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.itembasedatos, parent, false);
            return new DatabaseViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.baseinicio, parent, false);
            return new EmptyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DatabaseViewHolder) {
            DatabaseViewHolder databaseHolder = (DatabaseViewHolder) holder;
            String databaseName = databaseList.get(position);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user == null) {
                Log.e(TAG,"Usuario no autenticado");
                Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = user.getUid();
            DatabaseReference databaseReference = database.getReference("users").child(userId).child("databases").child(databaseName);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    double ingresos = 0;
                    double egresos = 0;

                    for (DataSnapshot itemsSnapshot : snapshot.getChildren()) {
                        Items item = itemsSnapshot.getValue(Items.class);

                        if (item != null) {
                            if ("Ingreso".equals(item.getType())) {
                                ingresos += item.getValor(); // Los ingresos se suman normalmente
                            } else if ("Gasto".equals(item.getType())) {
                                egresos += item.getValor(); // Los egresos ya son negativos
                            }
                        }
                    }

                    Log.d("BasesAdapter", "Ingresos sin formato: " + ingresos);
                    Log.d("BasesAdapter", "Egresos sin formato: " + egresos);

                    // Calcula correctamente la diferencia
                    double diferencia = ingresos + egresos; // Sumamos egresos porque ya son negativos

                    // Formateamos los valores
                    String ingresosFormatted = PuntoMil.getFormattedNumber((long) ingresos);
                    String egresosFormatted = PuntoMil.getFormattedNumber((long) Math.abs(egresos)); // Mostramos egresos como positivos
                    String diferenciaFormateada = PuntoMil.getFormattedNumber((long) diferencia);

                    Log.d("BasesAdapter", "Ingresos formateados: " + ingresosFormatted);
                    Log.d("BasesAdapter", "Egresos formateados: " + egresosFormatted);
                    Log.d("BasesAdapter", "Diferencia: " + diferenciaFormateada);

                    // Mostrar los valores en los TextViews
                    databaseHolder.textViewDatabaseName.setText(databaseName);
                    databaseHolder.textViewIngresos.setText("Ingresos: $" + ingresosFormatted);
                    databaseHolder.textViewEgresos.setText("Egresos: $" + egresosFormatted);
                    databaseHolder.textViewDiferencia.setText("Ganancia: $" + diferenciaFormateada);

                    // Cambiar el color del texto basado en el valor de la diferencia
                    int colorTexto = diferencia < 0
                            ? ContextCompat.getColor(holder.itemView.getContext(), R.color.colorNegativo)
                            : ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPositivo);
                    databaseHolder.textViewDiferencia.setTextColor(colorTexto);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error al obtener data de firebase", error.toException());
                    Toast.makeText(context, "Error al obtener data de firebase", Toast.LENGTH_SHORT).show();
                }
            });
            databaseHolder.imageViewDatabaseIcon.setImageResource(R.drawable.database);
            databaseHolder.itemView.setOnClickListener(v -> listener.onDatabaseClick(databaseName));

        } else if (holder instanceof EmptyViewHolder) {
            // No es necesario hacer nada aquí, ya que la vista EmptyViewHolder se configura en el layout XML.
        }
    }

    @Override
    public int getItemCount() {
        return databaseList.isEmpty() ? 1 : databaseList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return databaseList.isEmpty() ? VIEW_TYPE_EMPTY : VIEW_TYPE_ITEM;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    // La vista vacía (posición 0 cuando la lista está vacía) ocupa ambas columnas
                    return getItemViewType(position) == VIEW_TYPE_EMPTY ? 2 : 1;
                }
            });
        }
    }

    public static class DatabaseViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDatabaseName;
        TextView textViewIngresos;
        TextView textViewEgresos;
        TextView textViewDiferencia;
        CardView cardView;
        ImageView imageViewDatabaseIcon;

        public DatabaseViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDatabaseName = itemView.findViewById(R.id.textViewDatabaseName);
            // Asegúrate de que estos IDs coincidan con los de tu layout itembasedatos.xml
            textViewIngresos = itemView.findViewById(R.id.textViewIngresos);
            textViewEgresos = itemView.findViewById(R.id.textViewEgresos);
            textViewDiferencia = itemView.findViewById(R.id.textViewDiferencia);
            imageViewDatabaseIcon = itemView.findViewById(R.id.imageViewDatabase);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }
}