package com.creativem.tiendacontrol.interfas;

import com.creativem.tiendacontrol.model.ProductoModel;
import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FirebaseHelper {
    private DatabaseReference databaseReference;

    public FirebaseHelper(String userId, String newDatabase) {
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("databases")
                .child(newDatabase)
                .child("productos");
    }

    // Obtener fecha y hora actual
    private String obtenerFechaHora() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    // Obtener referencia de productos
    public DatabaseReference obtenerReferenciaProductos() {
        return databaseReference;
    }

    // Leer productos
    public void obtenerProductos(ValueEventListener listener) {
        databaseReference.addListenerForSingleValueEvent(listener);
    }

    // Agregar producto
    public void agregarProducto(String idProducto, ProductoModel producto, DatabaseReference.CompletionListener listener) {
        databaseReference.child(idProducto).setValue(producto, listener);
    }

    // Editar producto
    public void editarProducto(String idProducto, String nombre, double precio, String nota, DatabaseReference.CompletionListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nombre);
        updates.put("precio", precio);
        updates.put("nota", nota);
        updates.put("fechaHora", obtenerFechaHora());

        databaseReference.child(idProducto).updateChildren(updates, listener);
    }

    // Eliminar producto
    public void eliminarProducto(String idProducto, DatabaseReference.CompletionListener completionListener) {
        databaseReference.child(idProducto).removeValue(completionListener);
    }
    public DatabaseReference getDatabaseReference() {
        return databaseReference; // Asegúrate de que databaseReference está inicializado correctamente
    }

}
