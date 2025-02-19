package com.creativem.tiendacontrol.dialogFragment;

import static com.creativem.tiendacontrol.dialogFragment.Utils.formatValor;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class IngresoDialogFragment extends BottomSheetDialogFragment {
    // Definición de las variables para los elementos de la interfaz de usuario
    private static final String ARG_DATABASE_NAME = "databaseName";
    private static final String ARG_DATABASE_PATH = "databasePath";
    private EditText txtProducto, txtValor, txtDetalles, txtCantidad;
    private Button btnGuarda, btnSavePredefined, btnClearCustom;
    private Spinner spinnerPredefined;
    private ItemManager itemManager;
    private String currentDatabase;
    private TextView texEliminar, texGuardar, texGuardarPredefinido;
    private SpinnerManager itemManagerUtil;
    private OnDataChangedListener dataChangedListener;
    private DatabaseReference databaseReference;
    private static final String TAG = "IngresoDialogFragment";
    public interface OnDataChangedListener {
        void onDataChanged();
    }

    public void setDataChangedListener(OnDataChangedListener listener) {
        this.dataChangedListener = listener;
    }


    // Constructor estático para crear una nueva instancia del fragmento
    public static IngresoDialogFragment newInstance(String databaseName, String databasePath) {
        IngresoDialogFragment fragment = new IngresoDialogFragment();
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
        View view = inflater.inflate(R.layout.ingreso, container, false);

        // Inicialización de los elementos de la interfaz
        txtProducto = view.findViewById(R.id.txtProducto);
        txtValor = view.findViewById(R.id.txtValor);
        txtDetalles = view.findViewById(R.id.txtDetalles);
        txtCantidad = view.findViewById(R.id.txtCantidad);
        texGuardar = view.findViewById(R.id.texGuardar);
        texGuardarPredefinido = view.findViewById(R.id.texGuardarPredefinido);
        spinnerPredefined = view.findViewById(R.id.spinnerPredefined);
        texEliminar = view.findViewById(R.id.texEliminar);


        // Aplicar el formato con separadores de mil
        PuntoMil.formatNumberWithThousandSeparator(txtValor);

        // Inicialización del ItemManager para manejar los ítems
        itemManager = new ItemManager(getContext());

        // Limpiar los campos al iniciar el fragmento
        limpiar();

        // Obtener el nombre de la base de datos actual desde los argumentos
        if (getArguments() != null) {
            currentDatabase = getArguments().getString(ARG_DATABASE_NAME);
        }
        // Configuración de los eventos para los botones
        texGuardar.setOnClickListener(view1 -> guardarIngreso());

        txtValor.setOnClickListener(v -> {
            // Verifica si el diálogo ya está visible, para evitar múltiples aperturas
            if (!ControlCalculadora.getInstance().isCalculadoraDialogVisible()) {
                // Muestra el `CalculadoraDialogFragment`
                CalculadoraDialogFragment calculadoraDialog = new CalculadoraDialogFragment();

                // Configura el listener para recibir el valor calculado
                calculadoraDialog.setCalculadoraListener(valorCalculado -> {
                    txtValor.setText(String.valueOf(valorCalculado)); // Establece el valor en el TextView
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

        itemManagerUtil = new SpinnerManager(getContext(), spinnerPredefined, txtProducto, txtValor, txtDetalles, txtCantidad);
        itemManagerUtil.loadPredefinedItems();

        spinnerPredefined.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Evitar el placeholder
                    Items selectedItem = (Items) parent.getItemAtPosition(position);
                    txtValor.setText(formatValor(selectedItem.getValor()));
                    txtProducto.setText(selectedItem.getProducto());
                    txtDetalles.setText(selectedItem.getDetalles());
                    txtCantidad.setText(String.valueOf(selectedItem.getCantidad()));
                } else {
                    limpiar();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se necesita hacer nada aquí
            }
        });
        return view;
    }

    private void guardarIngreso() {
        // Obtener los datos de los campos
        String producto = txtProducto.getText().toString().trim(); // Eliminar espacios en blanco
        String valorStr = txtValor.getText().toString().trim();
        String detalles = txtDetalles.getText().toString().trim();
        String cantidadStr = txtCantidad.getText().toString().trim();

        // Verificar que todos los campos estén llenos
        if (producto.isEmpty() || valorStr.isEmpty() || detalles.isEmpty() || cantidadStr.isEmpty()) {
            Toast.makeText(getContext(), "Todos los campos son Necesarios", Toast.LENGTH_LONG).show();
            return; // Salir del método si hay campos vacíos
        }
        try {
            // Eliminar separadores de miles (por ejemplo, comas o puntos)
            valorStr = valorStr.replace(",", "").replace(".", "");
            cantidadStr = cantidadStr.replace(",", "").replace(".", "");

            // Convertir los datos a los tipos correctos
            double valor = Double.parseDouble(valorStr);
            int cantidad = Integer.parseInt(cantidadStr);
            double total = valor * cantidad;

            // Create date string in Colombia time zone
            SimpleDateFormat sdfColombia = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            sdfColombia.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
            String dateString = sdfColombia.format(new Date()); // Use new Date() for current time

            // Crear un mapa de datos que incluye la marca de tiempo
            Map<String, Object> data = new HashMap<>();
            data.put("producto", producto);
            data.put("valor", total);
            data.put("detalles", detalles);
            data.put("cantidad", cantidad);
            data.put("type", "Ingreso");
            data.put("timestamp", ServerValue.TIMESTAMP);
            data.put("date", dateString);


            DatabaseReference newItemRef = databaseReference.push();
            String key = newItemRef.toString();
            data.put("id", key);


            newItemRef.setValue(data).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Ingreso guardado en Firebase", Toast.LENGTH_SHORT).show();
                    limpiar();
                    dismiss();
                    if (dataChangedListener != null) {
                        dataChangedListener.onDataChanged();
                    }
                } else {
                    Toast.makeText(getContext(), "Error al guardar el ingreso en Firebase", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al guardar el ingreso en Firebase: "+ task.getException());
                }
            });



        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "VALOR O CANTIDAD NO SON VÁLIDOS", Toast.LENGTH_SHORT).show();
        }
    }


    private void limpiar() {
        // Limpiar los campos del formulario
        txtProducto.setText("");
        txtValor.setText("");
        txtDetalles.setText("");
        txtCantidad.setText("");
    }

}
