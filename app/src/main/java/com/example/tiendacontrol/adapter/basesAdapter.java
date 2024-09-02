package com.example.tiendacontrol.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tiendacontrol.R;
import com.example.tiendacontrol.helper.BdVentas;
import com.example.tiendacontrol.helper.PuntoMil;

import java.util.List;

public class basesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_EMPTY = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    public interface OnDatabaseClickListener {
        void onDatabaseClick(String databaseName);
    }

    private Context context;
    private List<String> databaseList;
    private OnDatabaseClickListener listener;

    public basesAdapter(Context context, List<String> databaseList, OnDatabaseClickListener listener) {
        this.context = context;
        this.databaseList = databaseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.itemdatabase, parent, false);
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

            // Obtener instancia de BdVentas para la base de datos actual
            BdVentas bdVentas = new BdVentas(context, databaseName);

            // Obtener los valores para esta base de datos
            double ingresos = bdVentas.obtenerTotalVentas();
            double egresos = bdVentas.obtenerTotalEgresos();
            double diferencia = bdVentas.obtenerDiferencia(); // O calcula diferencia aquí

            // Formatear los valores con PuntoMil
            String ingresosFormatted = PuntoMil.getFormattedNumber((long) ingresos);
            String egresosFormatted = PuntoMil.getFormattedNumber((long) egresos);
            String diferenciaFormatted = PuntoMil.getFormattedNumber((long) diferencia);

            // Mostrar los valores FORMATEADOS en los TextViews
            databaseHolder.textViewDatabaseName.setText(databaseName);
            databaseHolder.textViewIngresos.setText("Ganancia: $" + ingresosFormatted);
            databaseHolder.textViewEgresos.setText("Egresos: $" + egresosFormatted);
            databaseHolder.textViewDiferencia.setText("Ingresos: $" + diferenciaFormatted);
            bdVentas.close();
            databaseHolder.imageViewDatabaseIcon.setImageResource(R.drawable.database);
            databaseHolder.itemView.setOnClickListener(v -> listener.onDatabaseClick(databaseName));
            int colorFondo = R.color.fondoCAr;
            databaseHolder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), colorFondo));
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
            // Asegúrate de que estos IDs coincidan con los de tu layout itemdatabase.xml
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