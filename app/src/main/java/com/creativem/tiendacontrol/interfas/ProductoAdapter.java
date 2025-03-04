package com.creativem.tiendacontrol.interfas;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.util.Log;
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
        ProductoModel producto = filteredProductList.get(position); // Usar la lista filtrada

        holder.nombre.setText(producto.getNombre());

        String precioFormateado = PuntoMil.getFormattedNumber((long) producto.getValor());
        holder.precio.setText(precioFormateado);

        holder.fechaHora.setText(producto.getFechaHora());

        if (producto.getValor() < 0) {
            holder.precio.setTextColor(context.getResources().getColor(R.color.endColor));
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorFondoNegativo));
        } else {
            holder.precio.setTextColor(context.getResources().getColor(R.color.Buton));
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorFondoPositivo));
        }

        holder.Editar.setOnClickListener(v -> listener.onEditClick(producto));
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
            Editar = itemView.findViewById(R.id.EditarProducto);
            Eliminar = itemView.findViewById(R.id.EliminarProducto);
        }
    }

    public void filtrarPorFecha(String fechaFiltro, String tipoFiltro) {
        List<ProductoModel> listaFiltrada = new ArrayList<>();
        SimpleDateFormat dateFormatDia = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dateFormatMes = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat dateFormatAño = new SimpleDateFormat("yyyy", Locale.getDefault());
        SimpleDateFormat dateFormatSemana = new SimpleDateFormat("yyyy-'W'ww", Locale.getDefault());
        SimpleDateFormat dateFormatBD = new SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date fechaFiltroDate;
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
                    //Handle week filter separately
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dateFormatDia.parse(fechaFiltro.substring(0,10))); //Parse start date
                    int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
                    int year = cal.get(Calendar.YEAR);
                    fechaFiltroDate = dateFormatSemana.parse(year + "-W" + weekOfYear); //Construct week date
                    break;
                default:
                    return; // Filtro inválido
            }

            for (ProductoModel producto : productoList) {
                if (cumpleFiltro(producto, fechaFiltroDate, tipoFiltro)) {
                    listaFiltrada.add(producto);
                }

            }
            // Ordena la lista filtrada por fecha de publicación (más reciente primero)
            listaFiltrada.sort((p1, p2) -> {
                try {
                    Date fecha1 = dateFormatBD.parse(p1.getFechaHora());
                    Date fecha2 = dateFormatBD.parse(p2.getFechaHora());
                    return fecha2.compareTo(fecha1); // Ordena descendentemente (más reciente primero)
                } catch (ParseException e) {
                    Log.e("filtrarPorFecha", "Error al ordenar por fecha: " + e.getMessage());
                    return 0; // En caso de error, mantiene el orden original
                }
            });
            filteredProductList.clear();
            filteredProductList.addAll(listaFiltrada);
            notifyDataSetChanged();
            MisDatos.calcularTotales(filteredProductList);

        } catch (ParseException e) {
            Log.e("FiltrarPorFecha", "Error al parsear la fecha del filtro: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Corrección en el método cumpleFiltro
    private boolean cumpleFiltro(ProductoModel producto, Date fechaFiltroDate, String tipoFiltro) {
        SimpleDateFormat dateFormatDia = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dateFormatMes = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat dateFormatAño = new SimpleDateFormat("yyyy", Locale.getDefault());
        SimpleDateFormat dateFormatSemana = new SimpleDateFormat("yyyy-'W'ww", Locale.getDefault());
        SimpleDateFormat dateFormatBD = new SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault()); // Formato de tu base de datos

        try {
            String fechaProductoStr = producto.getFechaHora();
            if (fechaProductoStr == null || fechaProductoStr.isEmpty()) return false;

            Date fechaProducto;
            if (fechaProductoStr.contains(" - ")) { // Rango de fechas
                String[] fechas = fechaProductoStr.split(" - ");
                if (fechas.length == 2) {
                    Date fechaInicio = dateFormatDia.parse(fechas[0].trim());
                    Date fechaFin = dateFormatDia.parse(fechas[1].trim());
                    Calendar calFiltro = Calendar.getInstance();
                    calFiltro.setTime(fechaFiltroDate);
                    Calendar calInicio = Calendar.getInstance();
                    calInicio.setTime(fechaInicio);
                    Calendar calFin = Calendar.getInstance();
                    calFin.setTime(fechaFin);
                    return !calFiltro.before(calInicio) && !calFiltro.after(calFin);

                } else {
                    return false;
                }
            } else {
                fechaProducto = dateFormatBD.parse(fechaProductoStr);
            }

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