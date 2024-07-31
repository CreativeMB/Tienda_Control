package com.example.tiendacontrol.adapter;

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
import com.example.tiendacontrol.monitor.VerActivity;
import com.example.tiendacontrol.model.Items;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

public class BaseDatosAdapter extends RecyclerView.Adapter<BaseDatosAdapter.ContactoViewHolder>{
    // Lista de ventas y lista original para realizar filtrados
    ArrayList<Items> listaVentas;
    ArrayList<Items> listaOriginal;

    // Constructor del adaptador que inicializa las listas
    public BaseDatosAdapter(ArrayList<Items> listaVentas) {
        this.listaVentas = listaVentas;
        listaOriginal = new ArrayList<>();
        listaOriginal.addAll(listaVentas);
    }

    // Método para inflar la vista del item
    @NonNull
    @Override
    public ContactoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lista_item, null, false);
        return new ContactoViewHolder(view);
    }

    // Método para enlazar los datos de un item con la vista
    @Override
    public void onBindViewHolder(@NonNull ContactoViewHolder holder, int position) {
        Items venta = listaVentas.get(position);

        // Asignar datos a los TextViews
        holder.viewProducto.setText(venta.getProducto());
        double valor = venta.getValorAsDouble();
        holder.viewValor.setText(formatoNumerico(Math.abs(valor))); // Mostrar el valor positivo formateado
        holder.viewValor.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                valor < 0 ? R.color.colorNegativo : R.color.colorPositivo));

        holder.viewDetalles.setText(venta.getDetalles());
        holder.viewCantidad.setText(String.valueOf(venta.getCantidad())); // Convertir int a String
        holder.viewFecha.setText(venta.getFechaRegistro());

        // Cambiar el color de fondo del item según el valor
        holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(),
                valor < 0 ? R.color.colorFondoNegativo : R.color.colorFondoPositivo));
    }

    // Método para filtrar la lista de ventas según el texto ingresado
    public void filtrado(final String txtBuscar) {
        if (txtBuscar.isEmpty()) {
            listaVentas.clear();
            listaVentas.addAll(listaOriginal);
        } else {
            listaVentas.clear();
            for (Items venta : listaOriginal) {
                if (venta.getProducto().toLowerCase().contains(txtBuscar.toLowerCase())) {
                    listaVentas.add(venta);
                }
            }
        }
        notifyDataSetChanged(); // Notificar cambios al adaptador
    }

    // Método que devuelve la cantidad de items en la lista
    @Override
    public int getItemCount() {
        return listaVentas.size();
    }

    // Método para formatear los valores numéricos con separador de miles
    public String formatoNumerico(double valor) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.'); // Punto como separador de miles
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return "$" + df.format(valor);
    }

    // Clase interna para el ViewHolder de cada item en el RecyclerView
    public class ContactoViewHolder extends RecyclerView.ViewHolder {

        TextView viewProducto, viewValor, viewDetalles, viewCantidad, viewFecha;

        // Constructor del ViewHolder que inicializa los TextViews
        public ContactoViewHolder(@NonNull View itemView) {
            super(itemView);

            viewProducto = itemView.findViewById(R.id.viewProducto);
            viewValor = itemView.findViewById(R.id.viewValor);
            viewDetalles = itemView.findViewById(R.id.viewDetalles);
            viewCantidad = itemView.findViewById(R.id.viewCantidad);
            viewFecha = itemView.findViewById(R.id.viewFecha);

            // Configuración del click listener para cada item
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
    // Método para actualizar los elementos de la lista
    public void setItems(ArrayList<Items> items) {
        listaVentas.clear();
        listaVentas.addAll(items);
        notifyDataSetChanged();
    }

}