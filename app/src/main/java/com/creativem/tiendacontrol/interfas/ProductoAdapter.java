package com.creativem.tiendacontrol.interfas;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.creativem.tiendacontrol.R;
import com.creativem.tiendacontrol.helper.PuntoMil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        // Si producto.getFechaHora() ya está en formato correcto (sin segundos) lo mostramos directamente
        holder.nombre.setText(producto.getNombre());

        // Convertir el precio de double a long antes de formatear
        String precioFormateado = PuntoMil.getFormattedNumber((long) producto.getValor());
        holder.precio.setText(precioFormateado);

        // Mostrar la fecha formateada sin segundos
        holder.fechaHora.setText(producto.getFechaHora()); // Asegúrate de que esto es una fecha formateada correctamente

        // Diferenciar productos negativos y positivos
        if (producto.getValor() < 0) {
            holder.precio.setTextColor(context.getResources().getColor(R.color.endColor)); // Rojo
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
            fechaHora = itemView.findViewById(R.id.FechaHoraProducto);
            Editar = itemView.findViewById(R.id.EditarProducto);
            Eliminar = itemView.findViewById(R.id.EliminarProducto);
        }
    }
    public void filtrarPorFecha(String fecha, String tipoFiltro) {
        List<ProductoModel> listaFiltrada = new ArrayList<>();

        SimpleDateFormat dateFormatDia = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dateFormatMes = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat dateFormatAño = new SimpleDateFormat("yyyy", Locale.getDefault());

        for (ProductoModel producto : productoList) {
            if (producto.getFechaHora() != null) {
                try {
                    Date fechaProducto = dateFormatDia.parse(producto.getFechaHora());
                    String fechaProductoFormateada = "";

                    switch (tipoFiltro) {
                        case "Día":
                            fechaProductoFormateada = dateFormatDia.format(fechaProducto);
                            break;
                        case "Semana":
                            Calendar calProducto = Calendar.getInstance();
                            calProducto.setTime(fechaProducto);
                            String[] rangoFechas = fecha.split(" - ");

                            try {
                                Date inicioSemana = dateFormatDia.parse(rangoFechas[0]);
                                Date finSemana = dateFormatDia.parse(rangoFechas[1]);

                                if (fechaProducto.compareTo(inicioSemana) >= 0 && fechaProducto.compareTo(finSemana) <= 0) {
                                    fechaProductoFormateada = fecha;
                                } else {
                                    continue;
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                                continue;
                            }
                            break;
                        case "Mes":
                            fechaProductoFormateada = dateFormatMes.format(fechaProducto);
                            break;
                        case "Año":
                            fechaProductoFormateada = dateFormatAño.format(fechaProducto);
                            break;
                        default:
                            break;
                    }

                    if (fechaProductoFormateada.equals(fecha)) {
                        listaFiltrada.add(producto);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        // Actualizar la lista de productos y notificar cambios
        actualizarLista(listaFiltrada);
    }
    public void actualizarLista(List<ProductoModel> nuevaLista) {
        this.productoList = nuevaLista;
        notifyDataSetChanged();
    }

}
