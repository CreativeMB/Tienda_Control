package com.creativem.tiendacontrol.interfas;
import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FirebaseHelper {
    private DatabaseReference databaseReference;

    public FirebaseHelper() {
        databaseReference = FirebaseDatabase.getInstance().getReference("productos");
    }

    // Obtener fecha y hora actual en un solo campo
    private String obtenerFechaHora() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    // Leer productos
    public void obtenerProductos(String baseDatosSeleccionada, ValueEventListener listener) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("bases_de_datos") // Ajusta según tu estructura en Firebase
                .child(baseDatosSeleccionada)
                .child("productos");

        ref.addListenerForSingleValueEvent(listener);
    }


    public DatabaseReference obtenerReferenciaProductos(String baseDatosSeleccionada) {
        return FirebaseDatabase.getInstance().getReference()
                .child("bases_de_datos")  // Asegura que esta estructura sea la correcta
                .child(baseDatosSeleccionada)
                .child("productos");
    }


    public void agregarProducto(String databaseName, String idProducto, ProductoModel producto, DatabaseReference.CompletionListener listener) {
        obtenerReferenciaProductos(databaseName).child(idProducto).setValue(producto, listener);
    }


    // Editar producto (actualiza nombre, precio, nota y fechaHora)
    public void editarProducto(String databaseName, String idProducto, String nombre, double precio, String nota, DatabaseReference.CompletionListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nombre);
        updates.put("precio", precio);
        updates.put("nota", nota);

        obtenerReferenciaProductos(databaseName).child(idProducto).updateChildren(updates, listener);
    }


    // Eliminar producto
    public void eliminarProducto(String baseDatosSeleccionada, String productoId, DatabaseReference.CompletionListener completionListener) {
        DatabaseReference ref = obtenerReferenciaProductos(baseDatosSeleccionada).child(productoId);
        ref.removeValue(completionListener);
    }


}
