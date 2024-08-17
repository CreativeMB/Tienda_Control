package com.example.tiendacontrol.helper;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tiendacontrol.model.Items;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpinnerManager {

    private ItemManager itemManager;
    private Context context;
    private Spinner spinnerPredefined;
    private TextView txtProducto;
    private TextView txtValor;
    private TextView txtDetalles;
    private TextView txtCantidad;

    public SpinnerManager(Context context, Spinner spinnerPredefined, TextView txtProducto, TextView txtValor, TextView txtDetalles, TextView txtCantidad) {
        this.context = context;
        this.spinnerPredefined = spinnerPredefined;
        this.txtProducto = txtProducto;
        this.txtValor = txtValor;
        this.txtDetalles = txtDetalles;
        this.txtCantidad = txtCantidad;
        this.itemManager = new ItemManager(context); // Asegúrate de que ItemManager esté correctamente inicializado
    }

    public void loadPredefinedItems() {
        List<Items> items = itemManager.getItems();

        Items placeholderItem = new Items();
        placeholderItem.setProducto("Seleccione un ítem");
        placeholderItem.setValor(0.0);
        placeholderItem.setDetalles("");
        placeholderItem.setCantidad(0);

        List<Items> allItems = new ArrayList<>();
        allItems.add(placeholderItem);
        allItems.addAll(items);

        // Usar un ArrayAdapter personalizado para formatear los valores
        ArrayAdapter<Items> adapter = new ArrayAdapter<Items>(context, android.R.layout.simple_spinner_item, allItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                Items item = getItem(position);
                if (item != null) {
                    String valorFormateado = NumberFormat.getNumberInstance(Locale.getDefault()).format(item.getValor());
                    textView.setText(item.getProducto() + " - " + valorFormateado);
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                Items item = getItem(position);
                if (item != null) {
                    String valorFormateado = NumberFormat.getNumberInstance(Locale.getDefault()).format(item.getValor());
                    textView.setText(item.getProducto() + " - " + valorFormateado);
                }
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPredefined.setAdapter(adapter);

        spinnerPredefined.setSelection(0);
    }

    public void savePredefinedItem() {
        String producto = txtProducto.getText().toString();
        String valorStr = txtValor.getText().toString();
        String detalles = txtDetalles.getText().toString();
        String cantidadStr = txtCantidad.getText().toString();

        // Imprimir los valores para depuración
        Log.d("SpinnerManager", "Valor String: " + valorStr);
        Log.d("SpinnerManager", "Cantidad String: " + cantidadStr);

        if (!producto.isEmpty() && !valorStr.isEmpty() && !detalles.isEmpty() && !cantidadStr.isEmpty()) {
            try {
                // Limpiar valorStr y cantidadStr para asegurar formato correcto
                valorStr = valorStr.replaceAll("[^\\d.,]", ""); // Permitir solo dígitos, punto y coma decimal
                valorStr = valorStr.replaceAll(",", ""); // Eliminar las comas como separadores de miles

                cantidadStr = cantidadStr.replaceAll("[^\\d]", "").trim(); // Permitir solo dígitos

                double valor = Double.parseDouble(valorStr);
                int cantidad = Integer.parseInt(cantidadStr);

                Items item = new Items();
                item.setProducto(producto);
                item.setValor(valor);
                item.setDetalles(detalles);
                item.setCantidad(cantidad);
                itemManager.saveItem(item);

                Toast.makeText(context, "Ítem guardado", Toast.LENGTH_SHORT).show();
                loadPredefinedItems();
            } catch (NumberFormatException e) {
                // Mostrar mensaje de error detallado
                Toast.makeText(context, "VALOR O CANTIDAD NO SON VÁLIDOS: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "TODOS LOS CAMPOS DEBEN ESTAR LLENOS", Toast.LENGTH_SHORT).show();
        }
    }

    public void clearCustomItems() {
        itemManager.removeCustomItems();
        loadPredefinedItems();
        Toast.makeText(context, "Ítems personalizados eliminados", Toast.LENGTH_SHORT).show();
    }
}