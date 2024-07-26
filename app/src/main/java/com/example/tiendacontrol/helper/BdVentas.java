package com.example.tiendacontrol.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.Nullable;
import com.example.tiendacontrol.model.Items;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BdVentas extends BdHelper{
    Context context;

    public BdVentas(@Nullable Context context) {
        super(context);
        this.context = context;
    }

    // Método para insertar una venta en la base de datos
    public long insertarVenta(String producto, double total, String detalles, int cantidad) {
        long id = 0;

        try {
            // Crear una instancia de BdHelper y obtener una base de datos de escritura
            BdHelper dbHelper = new BdHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Crear un ContentValues para almacenar los datos de la venta
            ContentValues values = new ContentValues();
            values.put("producto", producto);
            values.put("valor", total); // Guarda el total calculado (puede ser positivo o negativo)
            values.put("detalles", detalles);
            values.put("cantidad", cantidad);
            values.put("fecha_registro", obtenerFechaActual()); // Obtener la fecha actual

            // Insertar los datos en la tabla y obtener el ID de la fila insertada
            id = db.insert(TABLE_VENTAS, null, values);
        } catch (Exception ex) {
            ex.printStackTrace(); // Imprimir el error si ocurre una excepción
        }

        return id; // Retornar el ID de la fila insertada
    }

    // Método para mostrar todas las ventas de la base de datos
    public ArrayList<Items> mostrarVentas() {
        BdHelper dbHelper = new BdHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Items> listaVentas = new ArrayList<>();
        Items venta;
        Cursor cursorVentas;

        // Consultar todas las ventas y ordenarlas por el nombre del producto en orden ascendente
        cursorVentas = db.rawQuery("SELECT * FROM " + TABLE_VENTAS + " ORDER BY producto ASC", null);

        if (cursorVentas.moveToFirst()) {
            do {
                venta = new Items();
                // Obtener los datos de la venta desde el cursor
                venta.setId(cursorVentas.getInt(0));
                venta.setProducto(cursorVentas.getString(1));
                venta.setValor(cursorVentas.getDouble(2)); // Cambiar a double
                venta.setDetalles(cursorVentas.getString(3));
                venta.setCantidad(cursorVentas.getInt(4)); // Cambiar a int
                venta.setFechaRegistro(cursorVentas.getString(5));
                // Añadir la venta a la lista
                listaVentas.add(venta);
            } while (cursorVentas.moveToNext());
        }

        cursorVentas.close(); // Cerrar el cursor

        return listaVentas; // Retornar la lista de ventas
    }

    // Método para ver una venta específica por ID
    public Items verVenta(int id) {
        BdHelper dbHelper = new BdHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Items venta = null;
        Cursor cursorVentas;

        // Consultar la venta con el ID específico
        cursorVentas = db.rawQuery("SELECT * FROM " + TABLE_VENTAS + " WHERE id = " + id + " LIMIT 1", null);

        if (cursorVentas.moveToFirst()) {
            venta = new Items();
            // Obtener los datos de la venta desde el cursor
            venta.setId(cursorVentas.getInt(0));
            venta.setProducto(cursorVentas.getString(1));
            venta.setValor(cursorVentas.getDouble(2)); // Cambiar a double
            venta.setDetalles(cursorVentas.getString(3));
            venta.setCantidad(cursorVentas.getInt(4)); // Cambiar a int
        }

        cursorVentas.close(); // Cerrar el cursor

        return venta; // Retornar la venta
    }

    // Método para editar una venta existente
    public boolean editarVenta(int id, String producto, double total, String detalles, int cantidad) {
        boolean correcto = false;
        BdHelper bdHelper = new BdHelper(context);
        SQLiteDatabase db = bdHelper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("producto", producto);
            values.put("valor", total); // Asegúrate de que esta columna puede manejar valores negativos
            values.put("detalles", detalles);
            values.put("cantidad", cantidad);

            // Actualizar la venta con el ID específico
            int rowsAffected = db.update(TABLE_VENTAS, values, "id = ?", new String[]{String.valueOf(id)});
            correcto = rowsAffected > 0; // Verificar si la actualización fue exitosa

        } catch (Exception ex) {
            ex.printStackTrace(); // Imprimir el error si ocurre una excepción
            correcto = false;
        } finally {
            db.close(); // Cerrar la base de datos
        }

        return correcto; // Retornar si la edición fue exitosa o no
    }
    // Método para eliminar una venta existente
    public boolean eliminarVenta(int id) {
        boolean correcto = false;

        BdHelper dbHelper = new BdHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            // Eliminar la venta con el ID específico
            int rowsAffected = db.delete(TABLE_VENTAS, "id = ?", new String[]{String.valueOf(id)});
            correcto = rowsAffected > 0; // Verificar si la eliminación fue exitosa
        } catch (Exception ex) {
            ex.printStackTrace(); // Imprimir el error si ocurre una excepción
            correcto = false;
        } finally {
            db.close(); // Cerrar la base de datos
        }

        return correcto; // Retornar si la eliminación fue exitosa o no
    }

    // Método para obtener la fecha y hora actual en formato específico
    private String obtenerFechaActual() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}