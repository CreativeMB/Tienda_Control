package com.example.tiendacontrol.Bd;

import static com.example.tiendacontrol.Bd.BdHelper.TABLE_VENTAS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.example.tiendacontrol.entidades.Ventas;

import java.util.ArrayList;

public class BdVentas extends BdHelper{

    Context context;

    public BdVentas(@Nullable Context context) {
        super(context);
        this.context = context;
    }

    public long insertarVenta(String nombre, String telefono, String correo_electronico) {

        long id = 0;

        try {
            BdHelper dbHelper = new BdHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("nombre", nombre);
            values.put("telefono", telefono);
            values.put("correo_electronico", correo_electronico);

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

        cursorVentas = db.rawQuery("SELECT * FROM " + TABLE_VENTAS + " ORDER BY nombre ASC", null);

        if (cursorVentas.moveToFirst()) {
            do {
                venta= new Ventas();
                venta.setId(cursorVentas.getInt(0));
                venta.setNombre(cursorVentas.getString(1));
                venta.setTelefono(cursorVentas.getString(2));
                venta.setCorreo_electornico(cursorVentas.getString(3));
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
            venta.setNombre(cursorVentas.getString(1));
            venta.setTelefono(cursorVentas.getString(2));
            venta.setCorreo_electornico(cursorVentas.getString(3));
        }

        cursorVentas.close();

        return venta;
    }

    public boolean editarVenta(int id, String nombre, String telefono, String correo_electronico) {

        boolean correcto = false;

        BdHelper dbHelper = new BdHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            db.execSQL("UPDATE " + TABLE_VENTAS + " SET nombre = '" + nombre + "', telefono = '" + telefono + "', correo_electronico = '" + correo_electronico + "' WHERE id='" + id + "' ");
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

}
