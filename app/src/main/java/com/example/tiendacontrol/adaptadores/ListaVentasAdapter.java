package com.example.tiendacontrol.adaptadores;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendacontrol.R;
import com.example.tiendacontrol.VerActivity;
import com.example.tiendacontrol.entidades.Ventas;

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
        holder.viewProducto.setText(listaVentas.get(position).getProducto());
        holder.viewValor.setText(listaVentas.get(position).getValor());
        holder.viewDetalles.setText(listaVentas.get(position).getDetalles());
        holder.viewCantidad.setText(listaVentas.get(position).getCantidad());
    }

    public void filtrado(final String txtBuscar) {
        int longitud = txtBuscar.length();
        if (longitud == 0) {
            listaVentas.clear();
            listaVentas.addAll(listaOriginal);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                List<Ventas> collecion = listaVentas.stream()
                        .filter(i -> i.getProducto().toLowerCase().contains(txtBuscar.toLowerCase()))
                        .collect(Collectors.toList());
                listaVentas.clear();
                listaVentas.addAll(collecion);
            } else {
                for (Ventas c : listaOriginal) {
                    if (c.getProducto().toLowerCase().contains(txtBuscar.toLowerCase())) {
                        listaVentas.add(c);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listaVentas.size();
    }

    public class ContactoViewHolder extends RecyclerView.ViewHolder {

        TextView viewProducto, viewValor, viewDetalles, viewCantidad;

        public ContactoViewHolder(@NonNull View itemView) {
            super(itemView);

            viewProducto = itemView.findViewById(R.id.viewProducto);
            viewValor = itemView.findViewById(R.id.viewValor);
            viewDetalles = itemView.findViewById(R.id.viewDetalles);
            viewCantidad = itemView.findViewById(R.id.viewCantidad);

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
