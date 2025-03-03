package com.creativem.tiendacontrol.interfas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.creativem.tiendacontrol.R;
import com.creativem.tiendacontrol.helper.PuntoMil;
import com.creativem.tiendacontrol.model.ProductoModel;

import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ViewHolder> {
    private Context context;
    private List<ProductoModel> productoList;
    private OnProductoClickListener listener;

    public interface OnProductoClickListener {
        void onEditClick(ProductoModel producto);
        void onDeleteClick(ProductoModel producto);
    }

    public ProductoAdapter(Context context, List<ProductoModel> productoList, OnProductoClickListener listener) {
        this.context = context;
        this.productoList = productoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.productos, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductoModel producto = productoList.get(position);

        // Convertir el precio de double a long antes de formatear
        String precioFormateado = "COP " + PuntoMil.getFormattedNumber((long) producto.getPrecio());

        holder.nombre.setText(producto.getNombre());
        holder.precio.setText(precioFormateado);
        holder.nota.setText(producto.getNota());
        holder.fechaHora.setText("Última edición: " + producto.getFechaHora());

        // Diferenciar productos negativos y positivos
        if (producto.getPrecio() < 0) {
            holder.precio.setTextColor(context.getResources().getColor(R.color.buttonTextColor)); // Rojo
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorFondoNegativo)); // Fondo rojizo
        } else {
            holder.precio.setTextColor(context.getResources().getColor(R.color.Buton)); // Verde
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorFondoPositivo)); // Fondo verdoso
        }

        holder.Editar.setOnClickListener(v -> listener.onEditClick(producto));
        holder.Eliminar.setOnClickListener(v -> listener.onDeleteClick(producto));
    }


    @Override
    public int getItemCount() {
        return productoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, precio, nota, fechaHora, Editar, Eliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.NombreProducto);
            precio = itemView.findViewById(R.id.PrecioProducto);
            nota = itemView.findViewById(R.id.NotaProducto);
            fechaHora = itemView.findViewById(R.id.FechaHoraProducto);
            Editar = itemView.findViewById(R.id.EditarProducto);
            Eliminar = itemView.findViewById(R.id.EliminarProducto);
        }
    }
}
