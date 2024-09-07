package com.example.tiendacontrol.dialogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.tiendacontrol.helper.SpinnerManager;
import com.example.tiendacontrol.helper.BdVentas;
import com.example.tiendacontrol.helper.ItemManager;
import com.example.tiendacontrol.helper.PuntoMil;
import com.example.tiendacontrol.model.Items;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.tiendacontrol.R;

public class GastoDialogFragment extends BottomSheetDialogFragment {
    // Definición de las variables para los elementos de la interfaz de usuario
    private EditText editProducto, editValor, editDetalles, editCantidad;
    private Spinner spinnerPredefined;
    private Button btnGuarda, btnSavePredefined, btnClearCustom;
    private ItemManager itemManager;
    private String currentDatabase; // Variable para almacenar el nombre de la base de datos actual
    private SpinnerManager itemManagerUtil;

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


        // Limpiar los campos al iniciar el fragmento
        limpiar();

        // Obtener el nombre de la base de datos actual desde los argumentos
        if (getArguments() != null) {
            currentDatabase = getArguments().getString("databaseName");
        }
        // Configuración de los eventos para los botones
        btnGuarda.setOnClickListener(view1 -> guardarEgreso());

        editValor.setOnClickListener(v -> {
            // Muestra una calculadora personalizada
            CalculadoraDialogFragment calculadoraDialog = new CalculadoraDialogFragment();
            calculadoraDialog.setCalculadoraListener(valorCalculado -> {
                editValor.setText(String.valueOf(valorCalculado));
            });
            calculadoraDialog.show(getParentFragmentManager(), "calculadoraDialog");
        });

        btnSavePredefined.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemManagerUtil.savePredefinedItem(); // Guardar un ítem predefinido
            }
        });

        btnClearCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemManagerUtil.clearCustomItems(); // Limpiar ítems personalizados
            }
        });
        itemManagerUtil = new SpinnerManager(getContext(), spinnerPredefined,  editProducto, editValor, editDetalles, editCantidad);
        itemManagerUtil.loadPredefinedItems();

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

    public void guardarEgreso() {
        // Obtener los datos de los campos
        String producto = editProducto.getText().toString().trim();
        String valorStr = editValor.getText().toString().trim();
        String detalles = editDetalles.getText().toString().trim();
        String cantidadStr = editCantidad.getText().toString().trim();

        // Verificar que todos los campos estén llenos
        if (producto.isEmpty() || valorStr.isEmpty() || detalles.isEmpty() || cantidadStr.isEmpty()) {
            Toast.makeText(getContext(), "DEBE LLENAR TODOS LOS CAMPOS", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // Eliminar cualquier carácter no numérico antes de la conversión
            // Primero eliminar los puntos de mil, luego convertir a número
            valorStr = valorStr.replaceAll("[.,]", ""); // Eliminar puntos y comas, si es necesario
            cantidadStr = cantidadStr.replaceAll("[^\\d]", ""); // Mantener solo dígitos

            // Convertir los datos a los tipos correctos
            double valor = Double.parseDouble(valorStr);
            int cantidad = Integer.parseInt(cantidadStr);

            // Hacer que el total sea negativo para egresos
            double total = -1 * valor * cantidad; // Multiplicar por -1 para hacerlo negativo

            // Guardar el registro en la base de datos
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

}