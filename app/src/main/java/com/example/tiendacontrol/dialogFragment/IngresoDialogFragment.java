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
import com.example.tiendacontrol.R;
import com.example.tiendacontrol.helper.ItemManager;
import com.example.tiendacontrol.model.Items;
import com.example.tiendacontrol.monitor.MainActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.tiendacontrol.helper.BdVentas;
import java.util.ArrayList;
import java.util.List;

public class IngresoDialogFragment extends BottomSheetDialogFragment {
    // Definición de las variables para los elementos de la interfaz de usuario
    EditText txtProducto, txtValor, txtDetalles, txtCantidad;
    Button btnGuarda, btnSavePredefined, btnClearCustom;
    Spinner spinnerPredefined;
    ItemManager itemManager;

    // Constructor estático para crear una nueva instancia del fragmento
    public static IngresoDialogFragment newInstance() {
        return new IngresoDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.ingreso, container, false);

        // Inicialización de los elementos de la interfaz
        txtProducto = view.findViewById(R.id.txtProducto);
        txtValor = view.findViewById(R.id.txtValor);
        txtDetalles = view.findViewById(R.id.txtDetalles);
        txtCantidad = view.findViewById(R.id.txtCantidad);
        btnGuarda = view.findViewById(R.id.btnGuarda);
        btnSavePredefined = view.findViewById(R.id.btnSavePredefined);
        spinnerPredefined = view.findViewById(R.id.spinnerPredefined);
        btnClearCustom = view.findViewById(R.id.btnClearCustom);

        // Inicialización del ItemManager para manejar los ítems
        itemManager = new ItemManager(getContext());

        // Cargar los ítems predefinidos en el spinner
        loadPredefinedItems();

        // Limpiar los campos al iniciar el fragmento
        limpiar();

        // Configuración de los eventos para los botones
        btnGuarda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarRegistro(); // Guardar el registro
            }
        });

        btnSavePredefined.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePredefinedItem(); // Guardar un ítem predefinido
            }
        });

        btnClearCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCustomItems(); // Limpiar ítems personalizados
            }
        });
                // Configuración del listener para el spinner
        spinnerPredefined.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Obtener el ítem seleccionado del spinner
                Items selectedItem = (Items) parentView.getItemAtPosition(position);
                if (selectedItem != null && !selectedItem.getProducto().equals("Seleccione un ítem")) {
                    // Rellenar los campos con los datos del ítem seleccionado
                    txtProducto.setText(selectedItem.getProducto());
                    txtValor.setText(String.valueOf(selectedItem.getValor()));
                    txtDetalles.setText(selectedItem.getDetalles());
                    txtCantidad.setText(String.valueOf(selectedItem.getCantidad()));
                } else {
                    // Limpiar los campos si se selecciona el ítem de placeholder
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
        // Obtener los ítems predefinidos desde el ItemManager
        List<Items> items = itemManager.getItems();

        // Agregar un ítem de placeholder al inicio
        Items placeholderItem = new Items();
        placeholderItem.setProducto("Seleccione un ítem");
        placeholderItem.setValor(0.0);
        placeholderItem.setDetalles("");
        placeholderItem.setCantidad(0);

        List<Items> allItems = new ArrayList<>();
        allItems.add(placeholderItem);
        allItems.addAll(items);

        // Crear un ArrayAdapter para el spinner
        ArrayAdapter<Items> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, allItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPredefined.setAdapter(adapter);

        // Establecer el ítem de placeholder como seleccionado
        spinnerPredefined.setSelection(0);
    }

    public void savePredefinedItem() {
        // Obtener los datos de los campos
        String producto = txtProducto.getText().toString();
        String valorStr = txtValor.getText().toString();
        String detalles = txtDetalles.getText().toString();
        String cantidadStr = txtCantidad.getText().toString();

        // Verificar que todos los campos estén llenos
        if (!producto.isEmpty() && !valorStr.isEmpty() && !detalles.isEmpty() && !cantidadStr.isEmpty()) {
            try {
                // Convertir los datos a los tipos correctos
                double valor = Double.parseDouble(valorStr);
                int cantidad = Integer.parseInt(cantidadStr);

                // Crear un nuevo ítem y guardarlo
                Items item = new Items();
                item.setProducto(producto);
                item.setValor(valor);
                item.setDetalles(detalles);
                item.setCantidad(cantidad);
                itemManager.saveItem(item);

                // Mostrar un mensaje de éxito y recargar los ítems predefinidos
                Toast.makeText(getContext(), "Ítem guardado", Toast.LENGTH_SHORT).show();
                loadPredefinedItems();
            } catch (NumberFormatException e) {
                // Mostrar un mensaje de error si los valores no son válidos
                Toast.makeText(getContext(), "VALOR O CANTIDAD NO SON VÁLIDOS", Toast.LENGTH_LONG).show();
            }
        } else {
            // Mostrar un mensaje si algún campo está vacío
            Toast.makeText(getContext(), "TODOS LOS CAMPOS DEBEN ESTAR LLENOS", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarRegistro() {
        // Obtener los datos de los campos
        String producto = txtProducto.getText().toString();
        String valorStr = txtValor.getText().toString();
        String detalles = txtDetalles.getText().toString();
        String cantidadStr = txtCantidad.getText().toString();

        // Verificar que todos los campos estén llenos
        if (!producto.isEmpty() && !valorStr.isEmpty() && !detalles.isEmpty() && !cantidadStr.isEmpty()) {
            try {
                // Convertir los datos a los tipos correctos
                double valor = Double.parseDouble(valorStr);
                int cantidad = Integer.parseInt(cantidadStr);
                double total = valor * cantidad; // Calcular el total

                // Crear una instancia de BdVentas y guardar el registro
                BdVentas bdVentas = new BdVentas(getContext());
                long id = bdVentas.insertarVenta(producto, total, detalles, cantidad);

                // Mostrar un mensaje de éxito o error según el resultado
                if (id > 0) {
                    Toast.makeText(getContext(), "REGISTRO GUARDADO", Toast.LENGTH_LONG).show();
                    limpiar();
                    verRegistro();
                    dismiss(); // Cerrar el diálogo
                } else {
                    Toast.makeText(getContext(), "ERROR AL GUARDAR REGISTRO", Toast.LENGTH_LONG).show();
                }
            } catch (NumberFormatException e) {
                // Mostrar un mensaje de error si los valores no son válidos
                Toast.makeText(getContext(), "VALOR O CANTIDAD NO SON VÁLIDOS", Toast.LENGTH_LONG).show();
            }
        } else {
            // Mostrar un mensaje si algún campo está vacío
            Toast.makeText(getContext(), "DEBE LLENAR LOS CAMPOS OBLIGATORIOS", Toast.LENGTH_LONG).show();
        }
    }

    private void limpiar() {
        // Limpiar los campos del formulario
        txtProducto.setText("");
        txtValor.setText("");
        txtDetalles.setText("");
        txtCantidad.setText("");
    }

    private void verRegistro() {
        // Crear una intención para abrir MainActivity
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