package com.creativem.tiendacontrol.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.creativem.tiendacontrol.R;
import com.creativem.tiendacontrol.helper.PuntoMil;
import com.creativem.tiendacontrol.model.TotalesItem;

import java.util.List;

public class TotalesAdapter extends RecyclerView.Adapter<TotalesAdapter.ViewHolder> {
    private Context context;
    private List<TotalesItem> totalesItems;
    public TotalesAdapter(Context context, List<TotalesItem> totalesItems) {
        this.context = context;
        this.totalesItems = totalesItems;
    }
    public void setTotalesItems(List<TotalesItem> totalesItems) {
        this.totalesItems = totalesItems;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_totales, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TotalesItem totalesItem = totalesItems.get(position);
        // Formatear los valores
        String ingresosFormatted = PuntoMil.getFormattedNumber((long) totalesItem.getIngresos());
        String egresosFormatted = PuntoMil.getFormattedNumber((long) Math.abs(totalesItem.getEgresos())); // Mostrar egresos positivos
        String diferenciaFormatted = PuntoMil.getFormattedNumber((long) totalesItem.getDiferencia());
        holder.itemIngresos.setText(String.format("$%s", ingresosFormatted));
        holder.itemEgresos.setText(String.format("$%s", egresosFormatted));
        holder.itemDiferencia.setText(String.format("$%s", diferenciaFormatted));
        holder.itemDatabase.setText(totalesItem.getDatabaseName() != null ? totalesItem.getDatabaseName() : "Total");
        if(totalesItem.getItemDate() != null){
            holder.itemDate.setText("Periodo: " + totalesItem.getItemDate());
        }else{
            holder.itemDate.setText("Periodo: -");
        }
        holder.itemName.setText(totalesItem.getPeriod() != null ? totalesItem.getPeriod() : "");

        int colorTexto = totalesItem.getDiferencia() < 0
                ? ContextCompat.getColor(context, R.color.colorNegativo)
                : ContextCompat.getColor(context, R.color.colorPositivo);
        holder.itemDiferencia.setTextColor(colorTexto);

    }

    @Override
    public int getItemCount() {
        return totalesItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView itemName, itemIngresos, itemEgresos, itemDiferencia, itemDatabase, itemDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemIngresos = itemView.findViewById(R.id.item_ingresos);
            itemEgresos = itemView.findViewById(R.id.item_egresos);
            itemDiferencia = itemView.findViewById(R.id.item_diferencia);
            itemDatabase = itemView.findViewById(R.id.item_database);
            itemDate = itemView.findViewById(R.id.item_date);
        }
    }
}