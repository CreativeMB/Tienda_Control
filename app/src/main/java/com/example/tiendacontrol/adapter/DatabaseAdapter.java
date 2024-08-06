package com.example.tiendacontrol.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendacontrol.R;

import java.util.List;

public class DatabaseAdapter extends RecyclerView.Adapter<DatabaseAdapter.DatabaseViewHolder> {
    private List<String> databaseList;
    private Context context;
    private OnDatabaseClickListener onDatabaseClickListener;

    public DatabaseAdapter(Context context, List<String> databaseList, OnDatabaseClickListener onDatabaseClickListener) {
        this.context = context;
        this.databaseList = databaseList;
        this.onDatabaseClickListener = onDatabaseClickListener;
    }

    @NonNull
    @Override
    public DatabaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_database, parent, false);
        return new DatabaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DatabaseViewHolder holder, int position) {
        String databaseName = databaseList.get(position);
        holder.textViewDatabaseName.setText(databaseName);
        holder.itemView.setOnClickListener(v -> {
            // Evita mostrar muchos toasts al mismo tiempo
            Toast.makeText(context, "Base de datos seleccionada: " + databaseName, Toast.LENGTH_SHORT).show();
            onDatabaseClickListener.onDatabaseClick(databaseName);
        });
    }

    @Override
    public int getItemCount() {
        return databaseList.size();
    }

    public static class DatabaseViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDatabaseName;

        public DatabaseViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDatabaseName = itemView.findViewById(R.id.textViewDatabaseName);
        }
    }

    public interface OnDatabaseClickListener {
        void onDatabaseClick(String databaseName);
    }
}