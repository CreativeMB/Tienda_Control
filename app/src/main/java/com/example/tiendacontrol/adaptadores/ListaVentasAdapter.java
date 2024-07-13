package com.example.tiendacontrol.adaptadores;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendacontrol.R;
import com.example.tiendacontrol.VerActivity;
import com.example.tiendacontrol.entidades.Ventas;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListaVentasAdapter extends RecyclerView.Adapter<ListaVentasAdapter.ContactoViewHolder>{


    ArrayList<Ventas> listaVentas;
    ArrayList<Ventas> listaOriginal;


    public ListaVentasAdapter(ArrayList<Ventas> listaVentas) {
        this.listaVentas = listaVentas;
        listaOriginal = new ArrayList<>();
        listaOriginal.addAll(listaVentas);
    }

    @NonNull
    @Override
    public ContactoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lista_item_ventas, null, false);
        return new ContactoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactoViewHolder holder, int position) {
        Ventas venta = listaVentas.get(position);

        holder.viewProducto.setText(venta.getProducto());
        double valor = venta.getValorAsDouble();

        holder.viewValor.setText(formatoNumerico(Math.abs(valor))); // Mostrar el valor positivo formateado
        holder.viewValor.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                valor < 0 ? R.color.colorNegativo : R.color.colorPositivo));

        holder.viewDetalles.setText(venta.getDetalles());
        holder.viewCantidad.setText(venta.getCantidad());
        holder.viewFecha.setText(venta.getFechaRegistro());

        holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(),
                valor < 0 ? R.color.colorFondoNegativo : R.color.colorFondoPositivo));
    }

    public void filtrado(final String txtBuscar) {
        if (txtBuscar.isEmpty()) {
            listaVentas.clear();
            listaVentas.addAll(listaOriginal);
        } else {
            listaVentas.clear();
            for (Ventas venta : listaOriginal) {
                if (venta.getProducto().toLowerCase().contains(txtBuscar.toLowerCase())) {
                    listaVentas.add(venta);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listaVentas.size();
    }

    private String formatoNumerico(double valor) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.'); // Punto como separador de miles
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return "$" + df.format(valor);
    }

    public class ContactoViewHolder extends RecyclerView.ViewHolder {

        TextView viewProducto, viewValor, viewDetalles, viewCantidad, viewFecha;

        public ContactoViewHolder(@NonNull View itemView) {
            super(itemView);

            viewProducto = itemView.findViewById(R.id.viewProducto);
            viewValor = itemView.findViewById(R.id.viewValor);
            viewDetalles = itemView.findViewById(R.id.viewDetalles);
            viewCantidad = itemView.findViewById(R.id.viewCantidad);
            viewFecha = itemView.findViewById(R.id.viewFecha);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, VerActivity.class);
                    intent.putExtra("ID", listaVentas.get(getAdapterPosition()).getId());
                    context.startActivity(intent);
                }
            });
        }
    }
}