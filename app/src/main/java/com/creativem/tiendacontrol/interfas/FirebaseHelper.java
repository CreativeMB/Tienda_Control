package com.creativem.tiendacontrol.interfas;

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
                .child(newDatabase);
    }

    // Obtener fecha y hora actual
    public String obtenerFechaHora() {
        return new SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
    }

    // Obtener referencia de la base de datos del usuario
    public DatabaseReference obtenerReferenciaProductos() {
        return databaseReference;
    }

    // Leer datos
    public void obtenerProductos(ValueEventListener listener) {
        databaseReference.addListenerForSingleValueEvent(listener);
    }

    // Agregar producto
    public void agregarProducto(String idProducto, ProductoModel producto, DatabaseReference.CompletionListener listener) {
        databaseReference.child(idProducto).setValue(producto, listener);
    }

    // Editar producto
    public void editarProducto(String idProducto, String nombre, double valor, String nota, DatabaseReference.CompletionListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nombre);
        updates.put("valor", valor);  // 🔹 Se cambió "precio" por "valor"
        updates.put("nota", nota);
        updates.put("fechaHora", obtenerFechaHora());

        databaseReference.child(idProducto).updateChildren(updates, listener);
    }

    // Eliminar producto
    public void eliminarProducto(String idProducto, DatabaseReference.CompletionListener completionListener) {
        databaseReference.child(idProducto).removeValue(completionListener);
    }

    // Obtener referencia de la base de datos
    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }
}
