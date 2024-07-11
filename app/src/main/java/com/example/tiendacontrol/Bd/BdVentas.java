package com.example.tiendacontrol.Bd;

import static com.example.tiendacontrol.Bd.BdHelper.TABLE_VENTAS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.example.tiendacontrol.entidades.Ventas;

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

    public long insertarVenta(String producto, String valor, String detalles, String cantidad) {

        long id = 0;

        try {
            BdHelper dbHelper = new BdHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("producto", producto);
            values.put("valor", valor);
            values.put("detalles", detalles);
            values.put("cantidad", cantidad);
            values.put("fecha_registro", obtenerFechaActual());
            id = db.insert(TABLE_VENTAS, null, values);
        } catch (Exception ex) {
            ex.toString();
        }

        return id;
    }

    public ArrayList<Ventas> mostrarVentas() {

        BdHelper dbHelper = new BdHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Ventas> listaVentas = new ArrayList<>();
        Ventas venta;
        Cursor cursorVentas;

        cursorVentas = db.rawQuery("SELECT * FROM " + TABLE_VENTAS + " ORDER BY producto ASC", null);

        if (cursorVentas.moveToFirst()) {
            do {
                venta= new Ventas();
                venta.setId(cursorVentas.getInt(0));
                venta.setProducto(cursorVentas.getString(1));
                venta.setValor(cursorVentas.getString(2));
                venta.setDetalles(cursorVentas.getString(3));
                venta.setCantidad(cursorVentas.getString(4));
                venta.setFechaRegistro(cursorVentas.getString(5));
                listaVentas.add(venta);
            } while (cursorVentas.moveToNext());
        }

        cursorVentas.close();

        return listaVentas;
    }

    public Ventas verVenta(int id) {

        BdHelper dbHelper = new BdHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Ventas venta = null;
        Cursor cursorVentas;

        cursorVentas = db.rawQuery("SELECT * FROM " + TABLE_VENTAS + " WHERE id = " + id + " LIMIT 1", null);

        if (cursorVentas.moveToFirst()) {
            venta= new Ventas();
            venta.setId(cursorVentas.getInt(0));
            venta.setProducto(cursorVentas.getString(1));
            venta.setValor(cursorVentas.getString(2));
            venta.setDetalles(cursorVentas.getString(3));
            venta.setCantidad(cursorVentas.getString(4));
        }

        cursorVentas.close();

        return venta;
    }

    public boolean editarVenta(int id, String producto, String valor, String detalles, String cantidad) {

        boolean correcto = false;

        BdHelper dbHelper = new BdHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            db.execSQL("UPDATE " + TABLE_VENTAS + " SET producto = '" + producto + "', valor = '" + valor + "', detalles = '" + detalles + "', cantidad = '" + cantidad + "' WHERE id='" + id + "' ");
            correcto = true;
        } catch (Exception ex) {
            ex.toString();
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
            db.execSQL("DELETE FROM " + TABLE_VENTAS + " WHERE id = '" + id + "'");
            correcto = true;
        } catch (Exception ex) {
            ex.toString();
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