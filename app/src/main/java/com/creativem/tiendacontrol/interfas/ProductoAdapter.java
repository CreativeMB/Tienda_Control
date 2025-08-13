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
    public void filtrarPorFecha(String fechaFiltro, String tipoFiltro, @Nullable String fechaFin) {
        List<ProductoModel> listaFiltrada = new ArrayList<>();

        // Formatos de fecha
        SimpleDateFormat dateFormatDia = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dateFormatMes = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat dateFormatAño = new SimpleDateFormat("yyyy", Locale.getDefault());
        SimpleDateFormat dateFormatSemana = new SimpleDateFormat("yyyy-'W'ww", Locale.getDefault());
        SimpleDateFormat dateFormatBD = new SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault()); // Formato en BD
        SimpleDateFormat dateFormatBDComparacion = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // Solo fecha sin hora

        try {
            Date fechaFiltroDate = null;
            Date fechaFinDate = null;

            switch (tipoFiltro) {
                case "Día":
                    fechaFiltroDate = dateFormatDia.parse(fechaFiltro);
                    break;
                case "Mes":
                    fechaFiltroDate = dateFormatMes.parse(fechaFiltro);
                    break;
                case "Año":
                    fechaFiltroDate = dateFormatAño.parse(fechaFiltro);
                    break;
                case "Semana":
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dateFormatDia.parse(fechaFiltro));
                    int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
                    int year = cal.get(Calendar.YEAR);
                    fechaFiltroDate = dateFormatSemana.parse(year + "-W" + weekOfYear);
                    break;
                case "Rango de Fechas":
                    if (fechaFiltro == null || fechaFiltro.isEmpty() || fechaFin == null || fechaFin.isEmpty()) {
                        Toast.makeText(context, "Debe seleccionar un rango de fechas válido.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    fechaFiltroDate = dateFormatDia.parse(fechaFiltro);
                    fechaFinDate = dateFormatDia.parse(fechaFin);
                    break;
                default:
                    return;
            }

            for (ProductoModel producto : productoList) {
                try {
                    Date fechaProducto = dateFormatBD.parse(producto.getFechaHora());
                    Date fechaProductoComparacion = dateFormatBDComparacion.parse(producto.getFechaHora());

                    if (fechaProducto == null || fechaProductoComparacion == null) continue;

                    if (tipoFiltro.equals("Rango de Fechas")) {
                        if (fechaFinDate != null &&
                                fechaProductoComparacion.compareTo(fechaFiltroDate) >= 0 &&
                                fechaProductoComparacion.compareTo(fechaFinDate) <= 0) {
                            listaFiltrada.add(producto);
                        }
                    } else {
                        if (cumpleFiltro(producto, fechaFiltroDate, tipoFiltro)) {
                            listaFiltrada.add(producto);
                        }
                    }
                } catch (ParseException e) {
                    Log.e("filtrarPorFecha", "Error al parsear fecha del producto: " + producto.getFechaHora());
                }
            }

            // Ordenar por fecha más reciente
            listaFiltrada.sort((p1, p2) -> {
                try {
                    Date fecha1 = dateFormatBD.parse(p1.getFechaHora());
                    Date fecha2 = dateFormatBD.parse(p2.getFechaHora());
                    return fecha2.compareTo(fecha1); // Orden descendente
                } catch (ParseException e) {
                    return 0;
                }
            });

            // Actualizar lista
            filteredProductList.clear();
            filteredProductList.addAll(listaFiltrada);
            notifyDataSetChanged();
            MisDatos.calcularTotales(filteredProductList);

            Log.d("Filtro", "Filtrado aplicado. Elementos encontrados: " + filteredProductList.size());

        } catch (ParseException e) {
            Log.e("FiltrarPorFecha", "Error al parsear la fecha del filtro: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private boolean cumpleRangoFechas(ProductoModel producto, Date fechaInicio, Date fechaFin) {
        SimpleDateFormat dateFormatBD = new SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault());


        try {
            Date fechaProducto = dateFormatBD.parse(producto.getFechaHora());
            if (fechaProducto == null) return false;

            // Validación correcta del rango de fechas
            return (fechaProducto.equals(fechaInicio) || fechaProducto.after(fechaInicio)) &&
                    (fechaProducto.equals(fechaFin) || fechaProducto.before(fechaFin));
        } catch (ParseException e) {
            Log.e("FiltrarPorFecha", "Error al comparar fechas: " + e.getMessage());
            return false;
        }
    }

    private boolean cumpleFiltro(ProductoModel producto, Date fechaFiltroDate, String tipoFiltro) {
        SimpleDateFormat dateFormatDia = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dateFormatMes = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat dateFormatAño = new SimpleDateFormat("yyyy", Locale.getDefault());
        SimpleDateFormat dateFormatSemana = new SimpleDateFormat("yyyy-'W'ww", Locale.getDefault());
        SimpleDateFormat dateFormatBD = new SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault());


        try {
            String fechaProductoStr = producto.getFechaHora();
            if (fechaProductoStr == null || fechaProductoStr.isEmpty()) return false;

            Date fechaProducto = dateFormatBD.parse(fechaProductoStr);
            if (fechaProducto == null) return false;

            switch (tipoFiltro) {
                case "Día":
                    return dateFormatDia.format(fechaProducto).equals(dateFormatDia.format(fechaFiltroDate));
                case "Mes":
                    return dateFormatMes.format(fechaProducto).equals(dateFormatMes.format(fechaFiltroDate));
                case "Año":
                    return dateFormatAño.format(fechaProducto).equals(dateFormatAño.format(fechaFiltroDate));
                case "Semana":
                    return dateFormatSemana.format(fechaProducto).equals(dateFormatSemana.format(fechaFiltroDate));
                default:
                    return false;
            }
        } catch (ParseException e) {
            Log.e("cumpleFiltro", "Error al parsear fecha: " + e.getMessage());
            return false;
        }
    }

    public String getCurrentFilter() { // Añade este método getter
        return currentFilter;
    }
    public List<ProductoModel> getFilteredProductList() {
        return filteredProductList;
    }
}