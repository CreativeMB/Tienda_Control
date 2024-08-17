package com.example.tiendacontrol.dialogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tiendacontrol.helper.BdVentas;
import com.example.tiendacontrol.helper.ItemManager;
import com.example.tiendacontrol.helper.PuntoMil;
import com.example.tiendacontrol.model.Items;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import com.example.tiendacontrol.R;
import java.util.ArrayList;
import java.util.List;

public class GastoDialogFragment extends BottomSheetDialogFragment {
    // Definición de las variables para los elementos de la interfaz de usuario
    private EditText editProducto, editValor, editDetalles, editCantidad;
    private Spinner spinnerPredefined;
    private Button btnGuarda, btnSavePredefined, btnClearCustom;
    private ItemManager itemManager;
    private String currentDatabase; // Variable para almacenar el nombre de la base de datos actual

    public interface OnDataChangedListener {
        void onDataChanged();
    }

    private OnDataChangedListener dataChangedListener;

    public void setDataChangedListener(OnDataChangedListener listener) {
        this.dataChangedListener = listener;
    }


    // Constructor estático para crear una nueva instancia del fragmento
    public static GastoDialogFragment newInstance(String currentDatabase) {
        GastoDialogFragment fragment = new GastoDialogFragment();
        Bundle args = new Bundle();
        args.putString("databaseName", currentDatabase);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.egreso, container, false);

        // Inicialización de los elementos de la interfaz
        editProducto = view.findViewById(R.id.editProducto);
        editValor = view.findViewById(R.id.editValor);
        editDetalles = view.findViewById(R.id.editDetalles);
        editCantidad = view.findViewById(R.id.editCantidad);
        btnGuarda = view.findViewById(R.id.btnGuarda);
        btnSavePredefined = view.findViewById(R.id.btnSavePredefined);
        spinnerPredefined = view.findViewById(R.id.spinnerPredefined);
        btnClearCustom = view.findViewById(R.id.btnClearCustom);

        // Aplicar el formato con separadores de mil
        PuntoMil.formatNumberWithThousandSeparator(editValor);

        // Inicialización del ItemManager para manejar los ítems
        itemManager = new ItemManager(getContext());

        // Cargar los ítems predefinidos en el spinner
        loadPredefinedItems();

        // Limpiar los campos al iniciar el fragmento
        limpiar();

        // Obtener el nombre de la base de datos actual desde los argumentos
        if (getArguments() != null) {
            currentDatabase = getArguments().getString("databaseName");
        }

        // Configuración de los eventos para los botones
        btnGuarda.setOnClickListener(view1 -> guardarGasto());

        btnSavePredefined.setOnClickListener(view12 -> savePredefinedItem());

        btnClearCustom.setOnClickListener(view13 -> clearCustomItems());

        // Configuración del listener para el spinner
        spinnerPredefined.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Items selectedItem = (Items) parentView.getItemAtPosition(position);
                if (selectedItem != null && !selectedItem.getProducto().equals("Seleccione un ítem")) {
                    editProducto.setText(selectedItem.getProducto());
                    editValor.setText(String.valueOf(selectedItem.getValor()));
                    editDetalles.setText(selectedItem.getDetalles());
                    editCantidad.setText(String.valueOf(selectedItem.getCantidad()));
                } else {
                    limpiar();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // No hacer nada aquí
            }
        });

        return view;
    }

    private void loadPredefinedItems() {
        List<Items> items = itemManager.getItems(); // Obtener ítems predefinidos de la base de datos
        Items placeholderItem = new Items();
        placeholderItem.setProducto("Seleccione un ítem");
        placeholderItem.setValor(0.0);
        placeholderItem.setDetalles("");
        placeholderItem.setCantidad(0);

        List<Items> allItems = new ArrayList<>();
        allItems.add(placeholderItem);
        allItems.addAll(items);

        ArrayAdapter<Items> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, allItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPredefined.setAdapter(adapter);
        spinnerPredefined.setSelection(0);
    }

    public void guardarGasto() {
        String producto = editProducto.getText().toString().trim();
        String valorStr = editValor.getText().toString().trim();
        String detalles = editDetalles.getText().toString().trim();
        String cantidadStr = editCantidad.getText().toString().trim();

        if (producto.isEmpty() || valorStr.isEmpty() || detalles.isEmpty() || cantidadStr.isEmpty()) {
            Toast.makeText(getContext(), "DEBE LLENAR TODOS LOS CAMPOS", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // Eliminar los separadores de mil antes de convertir a número
            valorStr = valorStr.replace(",", "");

            double valor = Double.parseDouble(valorStr);
            int cantidad = Integer.parseInt(cantidadStr);

            if (valor > 0) {
                valor = -valor;
            }

            double total = valor * cantidad;
            BdVentas bdVentas = new BdVentas(getContext(), currentDatabase);
            long id = bdVentas.insertarVenta(producto, total, detalles, cantidad);

            if (id > 0) {
                Toast.makeText(getContext(), "REGISTRO GUARDADO", Toast.LENGTH_SHORT).show();
                limpiar();
                dismiss();
                if (dataChangedListener != null) {
                    dataChangedListener.onDataChanged();
                }
            } else {
                Toast.makeText(getContext(), "ERROR AL GUARDAR REGISTRO", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "VALOR O CANTIDAD NO SON VÁLIDOS", Toast.LENGTH_SHORT).show();
        }
    }

    private void limpiar() {
        editProducto.setText("");
        editValor.setText("");
        editDetalles.setText("");
        editCantidad.setText("");
    }

    public void savePredefinedItem() {
        String producto = editProducto.getText().toString().trim();
        String valorStr = editValor.getText().toString().trim();
        String detalles = editDetalles.getText().toString().trim();
        String cantidadStr = editCantidad.getText().toString().trim();

        if (!producto.isEmpty() && !valorStr.isEmpty() && !detalles.isEmpty() && !cantidadStr.isEmpty()) {
            try {
                double valor = Double.parseDouble(valorStr);
                int cantidad = Integer.parseInt(cantidadStr);
                Items item = new Items();
                item.setProducto(producto);
                item.setValor(valor);
                item.setDetalles(detalles);
                item.setCantidad(cantidad);
                itemManager.saveItem(item);
                Toast.makeText(getContext(), "Ítem guardado", Toast.LENGTH_SHORT).show();
                loadPredefinedItems();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Valor o cantidad no válidos", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getContext(), "Debe llenar todos los campos", Toast.LENGTH_LONG).show();
        }
    }

    public void clearCustomItems() {
        itemManager.removeCustomItems();
        loadPredefinedItems();
        Toast.makeText(requireContext(), "Ítems personalizados eliminados", Toast.LENGTH_SHORT).show();
    }
}