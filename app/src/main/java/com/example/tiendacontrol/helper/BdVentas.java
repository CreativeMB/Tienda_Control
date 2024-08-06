package com.example.tiendacontrol.helper;

import static android.app.PendingIntent.getActivity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.tiendacontrol.model.Items;
import com.example.tiendacontrol.monitor.MainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BdVentas  {
    private Context context;
    private BdHelper bdHelper; // Instancia de BdHelper
    private SQLiteDatabase database;
    public BdVentas(Context context, String databaseName) {
        this.context = context;
        // Inicializa bdHelper solo una vez
        if (bdHelper == null) {
            bdHelper = new BdHelper(context, databaseName);
        }
    }
    // Método para insertar una venta en la base de datos
    public long insertarVenta(String producto, double total, String detalles, int cantidad) {
        long id = 0;
        SQLiteDatabase db = null;
        try {
            // Obtener una base de datos de escritura
            db = bdHelper.getWritableDatabase();

            // Crear un ContentValues para almacenar los datos de la venta
            ContentValues values = new ContentValues();
            values.put("producto", producto);
            values.put("valor", total); // Guarda el total calculado (puede ser positivo o negativo)
            values.put("detalles", detalles);
            values.put("cantidad", cantidad);
            values.put("fecha_registro", obtenerFechaActual()); // Obtener la fecha actual

            // Insertar los datos en la tabla y obtener el ID de la fila insertada
            id = db.insert(BdHelper.TABLE_VENTAS, null, values);
        } catch (Exception ex) {
            ex.printStackTrace(); // Imprimir el error si ocurre una excepción
        } finally {
            if (db != null && db.isOpen()) {
                db.close(); // Cerrar la base de datos
            }
        }
        return id; // Retornar el ID de la fila insertada
    }

    // Método para mostrar todas las ventas de la base de datos
    public ArrayList<Items> mostrarVentas() {
        ArrayList<Items> listaVentas = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursorVentas = null;

        try {
            db = bdHelper.getReadableDatabase();
            Log.d("Database", "Abriendo base de datos: " + db.getPath());

            cursorVentas = db.rawQuery("SELECT * FROM " + BdHelper.TABLE_VENTAS + " ORDER BY producto ASC", null);

            if (cursorVentas.moveToFirst()) {
                do {
                    Items venta = new Items();
                    venta.setId(cursorVentas.getInt(0));
                    venta.setProducto(cursorVentas.getString(1));
                    venta.setValor(cursorVentas.getDouble(2));
                    venta.setDetalles(cursorVentas.getString(3));
                    venta.setCantidad(cursorVentas.getInt(4));
                    venta.setFechaRegistro(cursorVentas.getString(5));
                    listaVentas.add(venta);
                } while (cursorVentas.moveToNext());
            } else {
                Log.d("Database", "La base de datos está vacía");
                listaVentas = new ArrayList<>();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursorVentas != null) {
                cursorVentas.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return listaVentas;
    }
    // Cierra la base de datos en el método close()
    public void close() {
        if (bdHelper != null) {
            bdHelper.close();
        }
    }
    // Método para ver una venta específica por ID
    public Items verVenta(int id) {
        Items venta = null;
        SQLiteDatabase db = null;
        Cursor cursorVentas = null;

        try {
            // Obtener una base de datos de lectura
            db = bdHelper.getReadableDatabase();

            // Consultar la venta con el ID específico
            cursorVentas = db.rawQuery("SELECT * FROM " + BdHelper.TABLE_VENTAS + " WHERE id = ? LIMIT 1", new String[]{String.valueOf(id)});

            // Verificar si el cursor contiene al menos un registro
            if (cursorVentas.moveToFirst()) {
                venta = new Items();
                // Obtener los datos de la venta desde el cursor
                venta.setId(cursorVentas.getInt(cursorVentas.getColumnIndex("id"))); // Usar el nombre de columna si es posible
                venta.setProducto(cursorVentas.getString(cursorVentas.getColumnIndex("producto")));
                venta.setValor(cursorVentas.getDouble(cursorVentas.getColumnIndex("valor")));
                venta.setDetalles(cursorVentas.getString(cursorVentas.getColumnIndex("detalles")));
                venta.setCantidad(cursorVentas.getInt(cursorVentas.getColumnIndex("cantidad")));
                venta.setFechaRegistro(cursorVentas.getString(cursorVentas.getColumnIndex("fechaRegistro"))); // Verifica el nombre de columna
            }
        } catch (Exception ex) {
            ex.printStackTrace(); // Imprimir el error si ocurre una excepción
        } finally {
            if (cursorVentas != null) {
                cursorVentas.close(); // Cerrar el cursor
            }
            if (db != null && db.isOpen()) {
                db.close(); // Cerrar la base de datos
            }
        }

        return venta; // Retornar la venta
    }

    // Método para editar una venta existente
    public boolean editarVenta(int id, String producto, double valor, String detalles, int cantidad) {
        boolean correcto = false;
        SQLiteDatabase db = null;

        try {
            // Obtener una base de datos de escritura
            db = bdHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("producto", producto);
            values.put("valor", valor); // Asegúrate de que esta columna puede manejar valores negativos
            values.put("detalles", detalles);
            values.put("cantidad", cantidad);

            // Actualizar la venta con el ID específico
            int rowsAffected = db.update(BdHelper.TABLE_VENTAS, values, "id = ?", new String[]{String.valueOf(id)});
            correcto = rowsAffected > 0; // Verificar si la actualización fue exitosa

        } catch (SQLException e) {
            e.printStackTrace(); // Imprimir el error si ocurre una excepción SQL
        } catch (Exception e) {
            e.printStackTrace(); // Imprimir el error si ocurre una excepción general
        } finally {
            if (db != null && db.isOpen()) {
                db.close(); // Cerrar la base de datos
            }
        }

        return correcto; // Retornar si la edición fue exitosa o no
    }

    // Método para eliminar una venta existente
    public boolean eliminarVenta(int id) {
        boolean correcto = false;
        SQLiteDatabase db = null;

        try {
            // Obtener una base de datos de escritura
            db = bdHelper.getWritableDatabase();

            // Eliminar la venta con el ID específico
            int rowsAffected = db.delete(BdHelper.TABLE_VENTAS, "id = ?", new String[]{String.valueOf(id)});
            correcto = rowsAffected > 0; // Verificar si la eliminación fue exitosa
        } catch (Exception ex) {
            ex.printStackTrace(); // Imprimir el error si ocurre una excepción
            correcto = false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close(); // Cerrar la base de datos
            }
        }

        return correcto; // Retornar si la eliminación fue exitosa o no
    }

    // Método para obtener la fecha y hora actual en formato específico
    private String obtenerFechaActual() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    // Método para eliminar todas las filas de la tabla de ventas
    public boolean eliminarTodo() {
        SQLiteDatabase db = null;
        try {
            db = bdHelper.getWritableDatabase();
            db.execSQL("DELETE FROM " + BdHelper.TABLE_VENTAS);
            return true;
        } catch (Exception e) {
            Log.e("BdVentas", "Error al eliminar las filas", e);
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
}