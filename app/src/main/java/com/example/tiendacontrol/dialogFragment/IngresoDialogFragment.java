package com.example.tiendacontrol.dialogFragment;

import static java.util.TimeZone.LONG;

import android.content.Intent;
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

import com.example.tiendacontrol.R;
import com.example.tiendacontrol.helper.ItemManager;
import com.example.tiendacontrol.model.Items;
import com.example.tiendacontrol.monitor.MainActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.tiendacontrol.helper.BdVentas;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IngresoDialogFragment extends BottomSheetDialogFragment {
    EditText txtProducto, txtValor, txtDetalles, txtCantidad;
    Button btnGuarda, btnSavePredefined, btnClearCustom;
    Spinner spinnerPredefined;
    ItemManager itemManager;

    private GastoDialogFragment gastoDialogFragment;
    public static IngresoDialogFragment newInstance() {
        return new IngresoDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ingreso, container, false);

        txtProducto = view.findViewById(R.id.txtProducto);
        txtValor = view.findViewById(R.id.txtValor);
        txtDetalles = view.findViewById(R.id.txtDetalles);
        txtCantidad = view.findViewById(R.id.txtCantidad);
        btnGuarda = view.findViewById(R.id.btnGuarda);
        btnSavePredefined = view.findViewById(R.id.btnSavePredefined);
        spinnerPredefined = view.findViewById(R.id.spinnerPredefined);
        btnClearCustom = view.findViewById(R.id.btnClearCustom);

        itemManager = new ItemManager(getContext());
        loadPredefinedItems();
        gastoDialogFragment = new GastoDialogFragment();
        // Clear fields when the view is created
        limpiar();

        btnGuarda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarRegistro();
            }
        });

        btnSavePredefined.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePredefinedItem();
            }
        });
        btnClearCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCustomItems();
            }
        });

        spinnerPredefined.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Items selectedItem = (Items) parentView.getItemAtPosition(position);
                if (selectedItem != null && !selectedItem.getProducto().equals("Seleccione un ítem")) {
                    txtProducto.setText(selectedItem.getProducto());
                    txtValor.setText(String.valueOf(selectedItem.getValor()));
                    txtDetalles.setText(selectedItem.getDetalles());
                    txtCantidad.setText(String.valueOf(selectedItem.getCantidad()));
                } else {
                    // Clear fields if placeholder is selected
                    limpiar();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });

        return view;
    }

    private void loadPredefinedItems() {
        List<Items> items = itemManager.getItems();
        // Add a placeholder item at the beginning
        Items placeholderItem = new Items();
        placeholderItem.setProducto("Seleccione un ítem");
        placeholderItem.setValor(0.0);
        placeholderItem.setDetalles("");
        placeholderItem.setCantidad(0);

        List<Items> allItems = new ArrayList<>();
        allItems.add(placeholderItem);
        allItems.addAll(items);

        ArrayAdapter<Items> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, allItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPredefined.setAdapter(adapter);

        // Set the placeholder item as selected
        spinnerPredefined.setSelection(0);
    }

    public void savePredefinedItem() {
        String producto = txtProducto.getText().toString();
        String valorStr = txtValor.getText().toString();
        String detalles = txtDetalles.getText().toString();
        String cantidadStr = txtCantidad.getText().toString();

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
                Toast.makeText(getContext(), "VALOR O CANTIDAD NO SON VÁLIDOS", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getContext(), "TODOS LOS CAMPOS DEBEN ESTAR LLENOS", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarRegistro() {
        String producto = txtProducto.getText().toString();
        String valorStr = txtValor.getText().toString();
        String detalles = txtDetalles.getText().toString();
        String cantidadStr = txtCantidad.getText().toString();

        if (!producto.isEmpty() && !valorStr.isEmpty() && !detalles.isEmpty() && !cantidadStr.isEmpty()) {
            try {
                double valor = Double.parseDouble(valorStr);
                int cantidad = Integer.parseInt(cantidadStr);

                BdVentas bdVentas = new BdVentas(getContext());
                long id = bdVentas.insertarVenta(producto, valor, detalles, cantidad);

                if (id > 0) {
                    Toast.makeText(getContext(), "REGISTRO GUARDADO", Toast.LENGTH_LONG).show();
                    limpiar();
                    verRegistro();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "ERROR AL GUARDAR REGISTRO", Toast.LENGTH_LONG).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "VALOR O CANTIDAD NO SON VÁLIDOS", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getContext(), "DEBE LLENAR LOS CAMPOS OBLIGATORIOS", Toast.LENGTH_LONG).show();
        }
    }

    private void limpiar() {
        txtProducto.setText("");
        txtValor.setText("");
        txtDetalles.setText("");
        txtCantidad.setText("");
    }

    private void verRegistro() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    public void clearCustomItems() {
        // Eliminar ítems personalizados de la base de datos
        itemManager.removeCustomItems();

        // Recargar los ítems predefinidos en el spinner
        loadPredefinedItems();
        // Mostrar un mensaje informativo al usuario
        Toast.makeText(requireContext(), "Ítems personalizados eliminados", Toast.LENGTH_SHORT).show();
    }
}
