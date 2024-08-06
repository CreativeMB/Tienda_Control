package com.example.tiendacontrol.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class BaseDatosAdapter extends RecyclerView.Adapter<BaseDatosAdapter.ContactoViewHolder>{
    private List<Items> itemsList; // Lista interna del adaptador
    private ArrayList<Items> listaVentas; // Lista de ventas
    private HashSet<Items> conjuntoOriginal; // Conjunto para filtrados eficientes
    private List<Items> items;
    // Constructor del adaptador que inicializa las listas
    public BaseDatosAdapter(ArrayList<Items> listaVentas) {
        this.listaVentas = listaVentas; // Asigna la lista recibida
        this.conjuntoOriginal = new HashSet<>(listaVentas); // Crea el HashSet
        this.itemsList = new ArrayList<>(listaVentas); // Copia la lista
        ordenarPorFecha();
    }

    // Método para inflar la vista del item
    @NonNull
    @Override
    public ContactoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lista_item, parent, false);
        return new ContactoViewHolder(view);
    }

    // Método para enlazar los datos de un item con la vista
    @Override
    public void onBindViewHolder(@NonNull ContactoViewHolder holder, int position) {
        Items venta = listaVentas.get(position);

        // Asignar datos a los TextViews
        holder.viewProducto.setText(venta.getProducto());
        double valor = venta.getValorAsDouble();
        holder.viewValor.setText(formatoNumerico(Math.abs(valor)));
        holder.viewValor.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                valor < 0 ? R.color.colorNegativo : R.color.colorPositivo));

        holder.viewDetalles.setText(venta.getDetalles());
        holder.viewCantidad.setText(String.valueOf(venta.getCantidad()));
        holder.viewFecha.setText(venta.getFechaRegistro());

        // Cambiar el color de fondo del item según el valor
        holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(),
                valor < 0 ? R.color.colorFondoNegativo : R.color.colorFondoPositivo));
    }

    // Método para filtrar la lista de ventas según el texto ingresado
    public void filtrado(final String txtBuscar) {
        listaVentas.clear();
        if (txtBuscar.isEmpty()) {
            listaVentas.addAll(conjuntoOriginal);
        } else {
            String buscarMinusculas = txtBuscar.toLowerCase();
            for (Items venta : conjuntoOriginal) {
                if (venta.getProducto().toLowerCase().contains(buscarMinusculas)) {
                    listaVentas.add(venta);
                }
            }
        }
        ordenarPorFecha(); // Asegúrate de volver a ordenar después de filtrar
        notifyDataSetChanged();
    }

    // Método que devuelve la cantidad de items en la lista
    @Override
    public int getItemCount() {
        return listaVentas.size();
    }

    // Método para formatear los valores numéricos con separador de miles
    public String formatoNumerico(double valor) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return "$" + df.format(valor);
    }

    public void setItems(List<Items> ventas) {
        this.items = ventas;
        notifyDataSetChanged();
    }

    // Clase interna para el ViewHolder de cada item en el RecyclerView
    public static class ContactoViewHolder extends RecyclerView.ViewHolder {
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
                    intent.putExtra("ID", getAdapterPosition());
                    context.startActivity(intent);
                }
            });
        }
    }

    // Método para actualizar los items en el adaptador
    public void setItems(ArrayList<Items> items) {
        conjuntoOriginal.clear(); // Limpiar el conjunto
        conjuntoOriginal.addAll(items); // Añadir todos los nuevos elementos

        listaVentas.clear(); // Limpiar la lista
        listaVentas.addAll(conjuntoOriginal); // Añadir todos los elementos del conjunto

        ordenarPorFecha();  // Ordenar la lista por fecha
        notifyDataSetChanged();  // Notificar al adaptador una vez, al final
    }

    // Método para ordenar los items por fecha
    public void ordenarPorFecha() {
        Collections.sort(listaVentas, new Comparator<Items>() {
            @Override
            public int compare(Items item1, Items item2) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                try {
                    Date date1 = sdf.parse(item1.getFechaRegistro());
                    Date date2 = sdf.parse(item2.getFechaRegistro());

                    // Manejar casos donde alguna fecha es nula
                    if (date1 == null && date2 == null) {
                        return 0;
                    } else if (date1 == null) {
                        return 1; // date1 es "menor" si es nula
                    } else if (date2 == null) {
                        return -1; // date2 es "menor" si es nula
                    } else {
                        return date2.compareTo(date1);  // Orden descendente
                    }
                } catch (ParseException e) {
                    Log.e("BaseDatosAdapter", "Error al analizar fechas: " + e.getMessage());
                    return 0;
                }
            }
        });
    }

    // Método para limpiar los datos del adaptador
    public void clearData() {
        if (itemsList != null) {
            itemsList.clear();
            notifyDataSetChanged();
        }
    }
}