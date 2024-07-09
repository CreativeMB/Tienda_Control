//package com.example.tiendacontrol;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.tiendacontrol.Bd.BdHelper;
//
//public class Gasto extends AppCompatActivity {
//
//    private EditText editProducto, editValor, editDetalles, editCantidad;
//    private BdHelper bdHelper;
//    Button btnGasto;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.gasto);
//
//        editProducto = findViewById(R.id.editProducto);
//        editValor = findViewById(R.id.editValor);
//        editDetalles = findViewById(R.id.editDetalles);
//        editCantidad = findViewById(R.id.editCantidad);
//        btnGasto = findViewById(R.id.btnGuarda);
//        bdHelper = new BdHelper(this);
//
//        btnGasto.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                guardarGasto();
//            }
//        });
//    }
//
//    public void guardarGasto() {
//        String producto = editProducto.getText().toString().trim();
//        String detalles = editDetalles.getText().toString().trim();
//        String valorStr = editValor.getText().toString().trim();
//        String cantidadStr = editCantidad.getText().toString().trim();
//
//        if (producto.isEmpty() || detalles.isEmpty() || valorStr.isEmpty() || cantidadStr.isEmpty()) {
//            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        double valor;
//        int cantidad;
//
//        try {
//            valor = Double.parseDouble(valorStr); // Convierte el valor a double
//
//            // Si el valor es positivo, conviértelo a negativo
//            if (valor > 0) {
//                valor = -valor;
//            }
//
//            cantidad = Integer.parseInt(cantidadStr);
//        } catch (NumberFormatException e) {
//            Toast.makeText(this, "Valor o cantidad no válidos", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Insertar el gasto en la base de datos
//        bdHelper.insertarGasto(producto, valor, detalles, cantidad);
//
//        Toast.makeText(this, "Gasto guardado correctamente", Toast.LENGTH_SHORT).show();
//
//        // Limpiar los campos después de guardar
//        editProducto.setText("");
//        editValor.setText("");
//        editDetalles.setText("");
//        editCantidad.setText("");
//
//        verRegistro();
//    }
//
//    private void verRegistro() {
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
//        finish(); // Opcional: cierra esta actividad después de volver a MainActivity
//    }
//}
