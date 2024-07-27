package com.example.tiendacontrol.dialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.tiendacontrol.helper.BdVentas;
import com.example.tiendacontrol.monitor.MainActivity;
import com.example.tiendacontrol.R;
import com.example.tiendacontrol.model.Items;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EditarDialogFragment extends DialogFragment {
    // Definición de variables de vista
    EditText txtProducto, txtValor, txtDetalles, txtCantidad;
    FloatingActionButton fabEditar, fabEliminar, fabMenu;
    Button btnGuarda;
    boolean correcto = false;
    Items venta;
    int id = 0;

    public static EditarDialogFragment newInstance(int id) {
        EditarDialogFragment fragment = new EditarDialogFragment();
        Bundle args = new Bundle();
        args.putInt("ID", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_ver, container, false);

        txtProducto = view.findViewById(R.id.txtProducto);
        txtValor = view.findViewById(R.id.txtValor);
        txtDetalles = view.findViewById(R.id.txtDetalles);
        txtCantidad = view.findViewById(R.id.txtCantidad);
        btnGuarda = view.findViewById(R.id.btnGuarda);

        fabEditar = view.findViewById(R.id.fabEditar);
        fabEliminar = view.findViewById(R.id.fabEliminar);
        fabMenu = view.findViewById(R.id.fabMenu);

        fabEditar.setVisibility(View.INVISIBLE);
        fabEliminar.setVisibility(View.INVISIBLE);
        fabMenu.setVisibility(View.INVISIBLE);

        if (getArguments() != null) {
            id = getArguments().getInt("ID");
        }

        final BdVentas bdVentas = new BdVentas(requireContext());
        venta = bdVentas.verVenta(id);

        if (venta != null) {
            txtProducto.setText(venta.getProducto());
            double valor = venta.getValor();
            txtValor.setText(String.valueOf(valor));
            txtDetalles.setText(venta.getDetalles());
            txtCantidad.setText(String.valueOf(venta.getCantidad())); // Convertir int a String
        }

        txtValor.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se necesita implementar este método
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No se necesita implementar este método
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) {
                    return;
                }

                isUpdating = true;

                String str = s.toString();
                if (!str.startsWith("-") && venta.getValor() < 0) {
                    // Si el valor debería ser negativo pero el usuario ha eliminado el signo, mostrar un mensaje
                    Toast.makeText(requireContext(), "El valor debe ser negativo", Toast.LENGTH_SHORT).show();
                    Log.d("EditarDialogFragment", "El valor debe ser negativo");

                    // Restaurar el signo negativo
                    str = "-" + str.replace("-", "");
                    txtValor.setText(str);
                    txtValor.setSelection(txtValor.getText().length());
                }

                isUpdating = false;
            }
        });

        btnGuarda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String producto = txtProducto.getText().toString().trim();
                String valorStr = txtValor.getText().toString().trim();
                String detalles = txtDetalles.getText().toString().trim();
                String cantidadStr = txtCantidad.getText().toString().trim();

                if (producto.isEmpty() || valorStr.isEmpty() || cantidadStr.isEmpty()) {
                    Toast.makeText(requireContext(), "DEBE LLENAR LOS CAMPOS OBLIGATORIOS", Toast.LENGTH_LONG).show();
                    return;
                }

                double valor;
                int cantidad;

                try {
                    valor = Double.parseDouble(valorStr);
                    if (venta.getValor() < 0 && valor > 0) {
                        Toast.makeText(requireContext(), "El valor debe ser negativo", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    cantidad = Integer.parseInt(cantidadStr);

                    double total = valor * cantidad;

                    correcto = bdVentas.editarVenta(id, producto, total, detalles, cantidad);

                    if (correcto) {
                        Toast.makeText(requireContext(), "REGISTRO MODIFICADO", Toast.LENGTH_LONG).show();
                        verRegistro();
                        dismiss();
                    } else {
                        Toast.makeText(requireContext(), "ERROR AL MODIFICAR REGISTRO", Toast.LENGTH_LONG).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "VALOR O CANTIDAD NO SON VÁLIDOS", Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }

    private void verRegistro() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.putExtra("ID", id);
        startActivity(intent);
    }
}