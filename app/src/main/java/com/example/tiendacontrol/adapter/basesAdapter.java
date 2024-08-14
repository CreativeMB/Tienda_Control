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
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendacontrol.R;

import java.util.List;

public class basesAdapter extends RecyclerView.Adapter<basesAdapter.DatabaseViewHolder> {
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
    public DatabaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.itemdatabase, parent, false);
        return new DatabaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DatabaseViewHolder holder, int position) {
        String databaseName = databaseList.get(position);
        holder.textViewDatabaseName.setText(databaseName);

        // Usa una imagen por defecto para los ítems
        holder.imageViewDatabaseIcon.setImageResource(R.drawable.database);

        holder.itemView.setOnClickListener(v -> listener.onDatabaseClick(databaseName));
        int colorFondo = R.color.fondoCAr; // Usar el color definido en colors.xml
        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), colorFondo));

    }

    @Override
    public int getItemCount() {
        return databaseList.size();
    }

    public static class DatabaseViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDatabaseName;
        CardView cardView;
        ImageView imageViewDatabaseIcon;

        public DatabaseViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDatabaseName = itemView.findViewById(R.id.textViewDatabaseName);
            imageViewDatabaseIcon = itemView.findViewById(R.id.imageViewDatabase);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}