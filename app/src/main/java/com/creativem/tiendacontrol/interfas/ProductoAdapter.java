package com.creativem.tiendacontrol.interfas;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.creativem.tiendacontrol.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ViewHolder> {
    private Context context;
    private List<ProductoModel> productoList; //Lista original
    private List<ProductoModel> filteredProductList; //Lista filtrada para mostrar
    private OnProductoClickListener listener;
    private String currentFilter = "Día"; // Filtro por defecto

    public interface OnProductoClickListener {
        void onEditClick(ProductoModel producto);
        void onDeleteClick(ProductoModel producto);
    }

    public ProductoAdapter(Context context, List<ProductoModel> productoList, OnProductoClickListener listener) {
        this.context = context;
        this.productoList = productoList;
        this.filteredProductList = new ArrayList<>(productoList); // Copia de la lista original
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
        ProductoModel producto = filteredProductList.get(position);

        holder.nombre.setText(producto.getNombre());
        holder.nombre.setSelected(true); // Activa marquee

        String precioFormateado = PuntoMil.getFormattedNumber((long) producto.getValor());
        holder.precio.setText(precioFormateado);
        holder.precio.setSelected(true); // Activa marquee

        holder.fechaHora.setText(producto.getFechaHora());
        holder.fechaHora.setSelected(true); // Activa marquee

        if (producto.getValor() < 0) {
            holder.precio.setTextColor(context.getResources().getColor(R.color.endColor));
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorFondoNegativo));
        } else {
            holder.precio.setTextColor(context.getResources().getColor(R.color.Buton));
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorFondoPositivo));
        }

        holder.itemView.setOnClickListener(v -> listener.onEditClick(producto));

        holder.Eliminar.setOnClickListener(v -> listener.onDeleteClick(producto));
    }

    @Override
    public int getItemCount() {
        return filteredProductList.size(); // Usar la lista filtrada
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, precio, nota, fechaHora, Editar, Eliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.NombreProducto);
            precio = itemView.findViewById(R.id.PrecioProducto);
            fechaHora = itemView.findViewById(R.id.FechaHoraProducto);
            Eliminar = itemView.findViewById(R.id.EliminarProducto);
        }
    }
    public void filtrarPorFecha(String fechaInicioStr, @Nullable String fechaFinStr, String tipoFiltro) {
        SimpleDateFormat sdfDB = new SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault());

        try {
            // Parseamos fechaInicio y fechaFin en formato completo "yyyy-MM-dd"
            SimpleDateFormat sdfBase = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date fechaInicio = sdfBase.parse(fechaInicioStr);
            Date fechaFin = fechaFinStr != null ? sdfBase.parse(fechaFinStr) : fechaInicio;

            if (fechaInicio == null) return;

            List<ProductoModel> listaFiltrada = new ArrayList<>();

            // Formatos para cada tipo de filtro
            SimpleDateFormat sdfDia = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat sdfSemana = new SimpleDateFormat("yyyy-'W'ww", Locale.getDefault());
            SimpleDateFormat sdfMes = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            SimpleDateFormat sdfAño = new SimpleDateFormat("yyyy", Locale.getDefault());

            for (ProductoModel producto : productoList) {
                if (producto.getFechaHora() == null || producto.getFechaHora().isEmpty()) continue;

                Date fechaProducto = sdfDB.parse(producto.getFechaHora());
                if (fechaProducto == null) continue;

                boolean cumple = false;

                switch (tipoFiltro) {
                    case "Día":
                        cumple = sdfDia.format(fechaProducto).equals(sdfDia.format(fechaInicio));
                        break;

                    case "Semana":
                        cumple = sdfSemana.format(fechaProducto).equals(sdfSemana.format(fechaInicio));
                        break;

                    case "Mes":
                        cumple = sdfMes.format(fechaProducto).equals(sdfMes.format(fechaInicio));
                        break;

                    case "Año":
                        cumple = sdfAño.format(fechaProducto).equals(sdfAño.format(fechaInicio));
                        break;

                    case "Rango de Fechas":
                        cumple = !fechaProducto.before(fechaInicio) && !fechaProducto.after(fechaFin);
                        break;
                }

                if (cumple) listaFiltrada.add(producto);
            }

            // Orden descendente por fecha
            listaFiltrada.sort((p1, p2) -> {
                try {
                    Date d1 = sdfDB.parse(p1.getFechaHora());
                    Date d2 = sdfDB.parse(p2.getFechaHora());
                    return d2.compareTo(d1);
                } catch (Exception e) {
                    return 0;
                }
            });

            filteredProductList.clear();
            filteredProductList.addAll(listaFiltrada);
            notifyDataSetChanged();

            MisDatos.calcularTotales(filteredProductList);

        } catch (Exception e) {
            Log.e("ProductoAdapter", "Error filtrando: " + e.getMessage());
        }
    }

    public String getCurrentFilter() { // Añade este método getter
        return currentFilter;
    }
    public List<ProductoModel> getFilteredProductList() {
        return filteredProductList;
    }
}