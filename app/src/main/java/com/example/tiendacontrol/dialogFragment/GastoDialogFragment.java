package com.example.tiendacontrol.dialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.tiendacontrol.Bd.BdHelper;
import com.example.tiendacontrol.MainActivity;
import com.example.tiendacontrol.R;

public class GastoDialogFragment extends BottomSheetDialogFragment {

    private EditText editProducto, editValor, editDetalles, editCantidad;
    private BdHelper bdHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gasto, container, false);

        editProducto = view.findViewById(R.id.editProducto);
        editValor = view.findViewById(R.id.editValor);
        editDetalles = view.findViewById(R.id.editDetalles);
        editCantidad = view.findViewById(R.id.editCantidad);
        Button btnGasto = view.findViewById(R.id.btnGuarda);

        bdHelper = new BdHelper(requireContext());

        btnGasto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarGasto();
            }
        });

        return view;
    }

    public void guardarGasto() {
        String producto = editProducto.getText().toString().trim();
        String detalles = editDetalles.getText().toString().trim();
        String valorStr = editValor.getText().toString().trim();
        String cantidadStr = editCantidad.getText().toString().trim();

        if (producto.isEmpty() || detalles.isEmpty() || valorStr.isEmpty() || cantidadStr.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double valor;
        int cantidad;

        try {
            valor = Double.parseDouble(valorStr); // Convierte el valor a double

            // Si el valor es positivo, conviértelo a negativo
            if (valor > 0) {
                valor = -valor;
            }

            cantidad = Integer.parseInt(cantidadStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Valor o cantidad no válidos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insertar el gasto en la base de datos
        bdHelper.insertarGasto(producto, valor, detalles, cantidad);

        Toast.makeText(requireContext(), "Gasto guardado correctamente", Toast.LENGTH_SHORT).show();

        // Limpiar los campos después de guardar
        editProducto.setText("");
        editValor.setText("");
        editDetalles.setText("");
        editCantidad.setText("");

        verRegistro();
    }

    private void verRegistro() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        startActivity(intent);
        dismiss(); // Cierra el diálogo después de volver a MainActivity
    }
}
