package com.example.tiendacontrol.dialogFragment;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tiendacontrol.helper.ItemManager;
import com.example.tiendacontrol.model.Items;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.tiendacontrol.helper.BdHelper;
import com.example.tiendacontrol.monitor.MainActivity;
import com.example.tiendacontrol.R;
import java.util.ArrayList;
import java.util.List;

public class GastoDialogFragment extends BottomSheetDialogFragment {

    // Definición de variables de vista
    private EditText editProducto, editValor, editDetalles, editCantidad;
    private Spinner spinnerPredefined;
    private BdHelper bdHelper;
    private ItemManager itemManager;
    private Button btnGuarda, btnSavePredefined, btnClearCustom;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla el layout del fragmento
        View view = inflater.inflate(R.layout.egreso, container, false);

        // Inicializa las vistas
        editProducto = view.findViewById(R.id.editProducto);
        editValor = view.findViewById(R.id.editValor);
        editDetalles = view.findViewById(R.id.editDetalles);
        editCantidad = view.findViewById(R.id.editCantidad);
        btnGuarda = view.findViewById(R.id.btnGuarda);
        spinnerPredefined = view.findViewById(R.id.spinnerPredefined);
        btnSavePredefined = view.findViewById(R.id.btnSavePredefined);
        btnClearCustom = view.findViewById(R.id.btnClearCustom); // Asegúrate de tener este botón en tu layout

        // Inicializa los objetos de manejo de base de datos
        bdHelper = new BdHelper(requireContext());
        itemManager = new ItemManager(requireContext());


        // Cargar los ítems predefinidos en el spinner
        loadPredefinedItems();

        // Configura el listener para el botón de guardar gasto
        btnGuarda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarGasto();
            }
        });

        // Configura el listener para el botón de guardar ítem predefinido
        btnSavePredefined.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePredefinedItem();
            }
        });

        // Configura el listener para el botón de eliminar ítems personalizados
        btnClearCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCustomItems();
            }
        });

        final View rootView = view.findViewById(R.id.root_layout); // Cambia esto si es necesario
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                // Ajustar el padding superior según la altura del teclado
                if (keypadHeight > screenHeight * 0.15) { // Si el teclado está visible
                    rootView.setPadding(0, 0, 0, keypadHeight);
                } else {
                    rootView.setPadding(0, 0, 0, 0);
                }
            }
        });

        // Configura el listener para el Spinner de ítems predefinidos
        spinnerPredefined.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Items selectedItem = (Items) parentView.getItemAtPosition(position);
                if (selectedItem != null && !selectedItem.getProducto().equals("Seleccione un ítem")) {
                    // Rellena los campos del formulario con los datos del ítem seleccionado
                    editProducto.setText(selectedItem.getProducto());
                    editValor.setText(String.valueOf(selectedItem.getValor()));
                    editDetalles.setText(selectedItem.getDetalles());
                    editCantidad.setText(String.valueOf(selectedItem.getCantidad()));
                } else {
                    // Limpia los campos si se selecciona el ítem de placeholder
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

    // Método para cargar los ítems predefinidos en el spinner
    private void loadPredefinedItems() {
        List<Items> items = itemManager.getItems(); // Obtener ítems predefinidos de la base de datos
        // Agregar un ítem de placeholder al inicio
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

        // Establecer el ítem de placeholder como seleccionado
        spinnerPredefined.setSelection(0);
    }

    // Método para guardar un nuevo gasto
    public void guardarGasto() {
        String producto = editProducto.getText().toString().trim();
        String detalles = editDetalles.getText().toString().trim();
        String valorStr = editValor.getText().toString().trim();
        String cantidadStr = editCantidad.getText().toString().trim();

        // Verificar que todos los campos estén llenos
        if (producto.isEmpty() || detalles.isEmpty() || valorStr.isEmpty() || cantidadStr.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double valor;
        int cantidad;

        try {
            // Convertir valor y cantidad a los tipos adecuados
            valor = Double.parseDouble(valorStr); // Convertir valor a double
            cantidad = Integer.parseInt(cantidadStr);

            // Si el valor es positivo, convertirlo a negativo
            if (valor > 0) {
                valor = -valor;
            }

            // Calcular el total
            double total = valor * cantidad;

            // Insertar el egreso en la base de datos
            bdHelper.insertarGasto(producto, total, detalles, cantidad);

            Toast.makeText(requireContext(), "Gasto guardado correctamente", Toast.LENGTH_SHORT).show();

            // Limpiar los campos después de guardar
            limpiar();

            verRegistro();
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Valor o cantidad no válidos", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para limpiar los campos del formulario
    private void limpiar() {
        editProducto.setText("");
        editValor.setText("");
        editDetalles.setText("");
        editCantidad.setText("");
    }

    // Método para volver a la actividad principal después de guardar el gasto
    private void verRegistro() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        startActivity(intent);
        dismiss(); // Cerrar el diálogo después de volver a MainActivity
    }

    // Método para guardar un nuevo ítem predefinido
    public void savePredefinedItem() {
        String producto = editProducto.getText().toString().trim();
        String valorStr = editValor.getText().toString().trim();
        String detalles = editDetalles.getText().toString().trim();
        String cantidadStr = editCantidad.getText().toString().trim();

        // Verificar que todos los campos estén llenos
        if (!producto.isEmpty() && !valorStr.isEmpty() && !detalles.isEmpty() && !cantidadStr.isEmpty()) {
            try {
                // Convertir valor y cantidad a los tipos adecuados
                double valor = Double.parseDouble(valorStr);
                int cantidad = Integer.parseInt(cantidadStr);
                Items item = new Items();
                item.setProducto(producto);
                item.setValor(valor);
                item.setDetalles(detalles);
                item.setCantidad(cantidad);
                itemManager.saveItem(item);
                Toast.makeText(getContext(), "Ítem guardado", Toast.LENGTH_SHORT).show();
                loadPredefinedItems(); // Recargar los ítems predefinidos
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "VALOR O CANTIDAD NO SON VÁLIDOS", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getContext(), "TODOS LOS CAMPOS DEBEN ESTAR LLENOS", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para eliminar ítems personalizados
    public void clearCustomItems() {
        // Eliminar ítems personalizados de la base de datos
        itemManager.removeCustomItems();

        // Recargar los ítems predefinidos en el spinner
        loadPredefinedItems();
        // Mostrar un mensaje informativo al usuario
        Toast.makeText(requireContext(), "Ítems personalizados eliminados", Toast.LENGTH_SHORT).show();
    }
}