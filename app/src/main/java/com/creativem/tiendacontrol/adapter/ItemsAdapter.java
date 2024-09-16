package com.creativem.tiendacontrol.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.creativem.tiendacontrol.R;
import com.creativem.tiendacontrol.model.Items;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> implements Filterable {
    private List<Items> itemList;
    private List<Items> itemListFull;

    public ItemsAdapter(List<Items> itemList) {
        this.itemList = itemList;
        this.itemListFull = new ArrayList<>(itemList); // Inicializa la lista completa para el filtrado
    }

    public void updateItems(List<Items> newItems) {
        this.itemList = newItems;
        this.itemListFull = new ArrayList<>(newItems); // Actualiza la lista completa para el filtrado
        notifyDataSetChanged(); // Actualiza el RecyclerView
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemdatosdatos, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Items item = itemList.get(position);
        holder.viewProducto.setText(item.getProducto());
        holder.viewValor.setText(formatoNumerico(Math.abs(item.getValorAsDouble())));

//        holder.viewValor.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
//                item.getValorAsDouble() < 0 ? R.color.colorNegativo : R.color.colorPositivo));
        holder.viewFecha.setText(item.getFechaRegistro());
        holder.viewDetalles.setText(item.getDetalles());
        holder.viewCantidad.setText(String.valueOf(item.getCantidad()));

        // Cambiar el color de fondo del CardView
        int colorFondo = item.getValorAsDouble() < 0 ? R.color.colorFondoNegativo : R.color.colorFondoPositivo;
        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), colorFondo));
        // Asignar la imagen en función del valor
        int iconoFondo = item.getValorAsDouble() < 0 ? R.drawable.egreso : R.drawable.ingreso;
        holder. imageItems.setImageResource(iconoFondo);

    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public Filter getFilter() {
        return itemFilter;
    }

    private Filter itemFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Items> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(itemListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Items item : itemListFull) {
                    if (item.getProducto().toLowerCase().contains(filterPattern) ||
                            item.getDetalles().toLowerCase().contains(filterPattern) ||
                            item.getFechaRegistro().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            itemList.clear();
            itemList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView viewProducto, viewValor, viewDetalles, viewCantidad, viewFecha;
        ImageView imageItems;
        CardView cardView;
        public ViewHolder(View itemView) {
            super(itemView);
            viewProducto = itemView.findViewById(R.id.viewProducto);
            viewValor = itemView.findViewById(R.id.viewValor);
            viewDetalles = itemView.findViewById(R.id.viewDetalles);
            viewCantidad = itemView.findViewById(R.id.viewCantidad);
            viewFecha = itemView.findViewById(R.id.viewFecha);
            cardView = itemView.findViewById(R.id.cardView);
            imageItems = itemView.findViewById(R.id. imageItems);
        }
    }

    public String formatoNumerico(double valor) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.'); // Punto como separador de miles
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return "$" + df.format(valor);
    }
}