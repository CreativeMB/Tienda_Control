package com.creativem.tiendacontrol.dialogFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.creativem.tiendacontrol.R;
import com.creativem.tiendacontrol.helper.ItemManager;
import com.creativem.tiendacontrol.helper.SpinnerManager;
import com.creativem.tiendacontrol.helper.PuntoMil;
import com.creativem.tiendacontrol.model.ControlCalculadora;
import com.creativem.tiendacontrol.model.Items;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;


import java.util.HashMap;
import java.util.Map;

public class GastoDialogFragment extends BottomSheetDialogFragment {
    // Definición de las variables para los elementos de la interfaz de usuario
    private static final String ARG_DATABASE_NAME = "databaseName";
    private static final String ARG_DATABASE_PATH = "databasePath";
    private EditText editProducto, editValor, editDetalles, editCantidad;
    private Spinner spinnerPredefined;
    private ItemManager itemManager;
    private String currentDatabase;
    private SpinnerManager itemManagerUtil;
    private TextView texEliminar, texGuardar, texGuardarPredefinido;
    private OnDataChangedListener dataChangedListener;
    private DatabaseReference databaseReference;
    private static final String TAG = "GastoDialogFragment";

    public interface OnDataChangedListener {
        void onDataChanged();
    }

    public void setDataChangedListener(OnDataChangedListener listener) {
        this.dataChangedListener = listener;
    }
    public static GastoDialogFragment newInstance(String databaseName, String databasePath) {
        GastoDialogFragment fragment = new GastoDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATABASE_NAME, databaseName);
        args.putString(ARG_DATABASE_PATH, databasePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentDatabase = getArguments().getString(ARG_DATABASE_NAME);
            String databasePath = getArguments().getString(ARG_DATABASE_PATH);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            databaseReference = database.getReferenceFromUrl(databasePath);
        }
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
        texGuardar = view.findViewById(R.id.texGuardar);
        texGuardarPredefinido = view.findViewById(R.id.texGuardarPredefinido);
        spinnerPredefined = view.findViewById(R.id.spinnerPredefined);
        texEliminar = view.findViewById(R.id.texEliminar);

        // Aplicar el formato con separadores de mil
        PuntoMil.formatNumberWithThousandSeparator(editValor);

        // Inicialización del ItemManager para manejar los ítems
        itemManager = new ItemManager(getContext());


        // Limpiar los campos al iniciar el fragmento
        limpiar();

        // Obtener el nombre de la base de datos actual desde los argumentos
        if (getArguments() != null) {
            currentDatabase = getArguments().getString(ARG_DATABASE_NAME);
        }
        // Configuración de los eventos para los botones
        texGuardar.setOnClickListener(view1 -> guardarEgreso());

        editValor.setOnClickListener(v -> {
            // Verifica si el diálogo ya está visible, para evitar múltiples aperturas
            if (!ControlCalculadora.getInstance().isCalculadoraDialogVisible()) {
                // Muestra el `CalculadoraDialogFragment`
                CalculadoraDialogFragment calculadoraDialog = new CalculadoraDialogFragment();

                // Configura el listener para recibir el valor calculado
                calculadoraDialog.setCalculadoraListener(valorCalculado -> {
                    editValor.setText(String.valueOf(valorCalculado)); // Establece el valor en el TextView
                });

                // Marca el diálogo como visible globalmente
                ControlCalculadora.getInstance().setCalculadoraDialogVisible(true);

                // Muestra el diálogo usando `getParentFragmentManager()` ya que estás dentro de un fragmento
                calculadoraDialog.show(getParentFragmentManager(), "calculadoraDialog");
            }
        });

        texGuardarPredefinido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemManagerUtil.savePredefinedItem(); // Guardar un ítem predefinido
            }
        });

        texEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemManagerUtil.removeSelectedItem(); // Limpiar ítems personalizados
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
            Toast.makeText(getContext(), "Todos los campos son Necesarios", Toast.LENGTH_LONG).show();
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

            // Crear un mapa de datos que incluye la marca de tiempo
            Map<String, Object> data = new HashMap<>();
            data.put("producto", producto);
            data.put("valor", total); // Guardar el total NEGATIVO
            data.put("detalles", detalles);
            data.put("cantidad", cantidad);
            data.put("type", "Gasto");
            data.put("timestamp", ServerValue.TIMESTAMP);

            DatabaseReference newItemRef = databaseReference.push();
            String key = newItemRef.toString();
            data.put("id", key);


            newItemRef.setValue(data).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Gasto guardado en Firebase", Toast.LENGTH_SHORT).show();
                    limpiar();
                    dismiss();
                    if (dataChangedListener != null) {
                        dataChangedListener.onDataChanged();
                    }
                } else {
                    Toast.makeText(getContext(), "Error al guardar el gasto en Firebase", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al guardar el gasto en Firebase: "+ task.getException());
                }
            });

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