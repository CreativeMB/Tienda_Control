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

    public long insertarVenta(String producto, double valor, String detalles, int cantidad) {
        long id = 0;

        try {
            BdHelper dbHelper = new BdHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("producto", producto);
            values.put("valor", valor); // Guardar como double
            values.put("detalles", detalles);
            values.put("cantidad", cantidad); // Cambiar a int
            values.put("fecha_registro", obtenerFechaActual());
            id = db.insert(TABLE_VENTAS, null, values);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return id;
    }

    public ArrayList<Items> mostrarVentas() {
        BdHelper dbHelper = new BdHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Items> listaVentas = new ArrayList<>();
        Items venta;
        Cursor cursorVentas;

        cursorVentas = db.rawQuery("SELECT * FROM " + TABLE_VENTAS + " ORDER BY producto ASC", null);

        if (cursorVentas.moveToFirst()) {
            do {
                venta = new Items();
                venta.setId(cursorVentas.getInt(0));
                venta.setProducto(cursorVentas.getString(1));
                venta.setValor(cursorVentas.getDouble(2)); // Cambiar a double
                venta.setDetalles(cursorVentas.getString(3));
                venta.setCantidad(cursorVentas.getInt(4)); // Cambiar a int
                venta.setFechaRegistro(cursorVentas.getString(5));
                listaVentas.add(venta);
            } while (cursorVentas.moveToNext());
        }

        cursorVentas.close();

        return listaVentas;
    }

    public Items verVenta(int id) {
        BdHelper dbHelper = new BdHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Items venta = null;
        Cursor cursorVentas;

        cursorVentas = db.rawQuery("SELECT * FROM " + TABLE_VENTAS + " WHERE id = " + id + " LIMIT 1", null);

        if (cursorVentas.moveToFirst()) {
            venta = new Items();
            venta.setId(cursorVentas.getInt(0));
            venta.setProducto(cursorVentas.getString(1));
            venta.setValor(cursorVentas.getDouble(2)); // Cambiar a double
            venta.setDetalles(cursorVentas.getString(3));
            venta.setCantidad(cursorVentas.getInt(4)); // Cambiar a int
        }

        cursorVentas.close();

        return venta;
    }

    public boolean editarVenta(int id, String producto, double valor, String detalles, int cantidad) {
        boolean correcto = false;
        BdHelper bdHelper = new BdHelper(context);
        SQLiteDatabase db = bdHelper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("producto", producto);
            values.put("valor", valor); // Cambiar a double
            values.put("detalles", detalles);
            values.put("cantidad", cantidad); // Cambiar a int

            int rowsAffected = db.update(TABLE_VENTAS, values, "id = ?", new String[]{String.valueOf(id)});
            correcto = rowsAffected > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            correcto = false;
        } finally {
            db.close();
        }

        return correcto;
    }

    public boolean eliminarVenta(int id) {
        boolean correcto = false;

        BdHelper dbHelper = new BdHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            int rowsAffected = db.delete(TABLE_VENTAS, "id = ?", new String[]{String.valueOf(id)});
            correcto = rowsAffected > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            correcto = false;
        } finally {
            db.close();
        }

        return correcto;
    }

    private String obtenerFechaActual() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}