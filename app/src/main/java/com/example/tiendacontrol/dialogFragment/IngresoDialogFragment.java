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

import com.example.tiendacontrol.R;
import com.example.tiendacontrol.monitor.MainActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.tiendacontrol.helper.BdVentas;

public class IngresoDialogFragment extends BottomSheetDialogFragment {

    EditText txtProducto, txtValor, txtDetalles, txtCantidad;
    Button btnGuarda;

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

        btnGuarda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!txtProducto.getText().toString().equals("") && !txtValor.getText().toString().equals("") && !txtDetalles.getText().toString().equals("") && !txtCantidad.getText().toString().equals("")) {

                    BdVentas bdVentas = new BdVentas(getContext());
                    long id = bdVentas.insertarVenta(txtProducto.getText().toString(), txtValor.getText().toString(), txtDetalles.getText().toString(), txtCantidad.getText().toString());

                    if (id > 0) {
                        Toast.makeText(getContext(), "REGISTRO GUARDADO", Toast.LENGTH_LONG).show();
                        limpiar();
                        verRegistro();
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), "ERROR AL GUARDAR REGISTRO", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "DEBE LLENAR LOS CAMPOS OBLIGATORIOS", Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
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
}
