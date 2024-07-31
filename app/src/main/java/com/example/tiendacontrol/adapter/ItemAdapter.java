package com.example.tiendacontrol.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendacontrol.R;
import com.example.tiendacontrol.model.Items;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private List<Items> itemList;

    public ItemAdapter(List<Items> itemList) {
        this.itemList = itemList;
    }

    public void updateItems(List<Items> newItems) {
        this.itemList = newItems;
        notifyDataSetChanged(); // Actualiza el RecyclerView
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lista_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Items venta = itemList.get(position);
        Items item = itemList.get(position);
        holder.viewProducto.setText(item.getProducto()); // Asegúrate de que "producto" sea el nombre correcto del campo en la clase Items
        holder.viewValor.setText(String.valueOf(item.getValor())); // Convierte el valor a String antes de establecer el texto
        holder.viewFecha.setText(item.getFechaRegistro());
        holder.viewDetalles.setText(item.getDetalles());
        holder.viewCantidad.setText(String.valueOf(item.getCantidad())); // Convierte la cantidad a String antes de establecer el texto

        // Asignar datos a los TextViews
        holder.viewProducto.setText(venta.getProducto());
        double valor = venta.getValorAsDouble();
        holder.viewValor.setText(formatoNumerico(Math.abs(valor)));
        holder.viewValor.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                valor < 0 ? R.color.colorNegativo : R.color.colorPositivo));

        // Cambiar el color de fondo del item según el valor
        holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(),
                valor < 0 ? R.color.colorFondoNegativo : R.color.colorFondoPositivo));

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView viewProducto, viewValor, viewDetalles, viewCantidad, viewFecha;

        public ViewHolder(View itemView) {
            super(itemView);
            viewProducto = itemView.findViewById(R.id.viewProducto);
            viewValor = itemView.findViewById(R.id.viewValor);
            viewDetalles = itemView.findViewById(R.id.viewDetalles);
            viewCantidad = itemView.findViewById(R.id.viewCantidad);
            viewFecha = itemView.findViewById(R.id.viewFecha);
        }
    }
    public String formatoNumerico(double valor) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.'); // Punto como separador de miles
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return "$" + df.format(valor);
    }
}