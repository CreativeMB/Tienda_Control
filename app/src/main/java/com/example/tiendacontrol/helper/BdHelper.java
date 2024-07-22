package com.example.tiendacontrol.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BdHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "MI_contabilidad.db";
    public static final String TABLE_VENTAS = "Mi_Contabilidad";

    public BdHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_VENTAS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "producto TEXT NOT NULL," +
                "valor INTEGER NOT NULL," +
                "detalles TEXT NOT NULL," +
                "cantidad INTEGER," +
                "fecha_registro TEXT" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE " + TABLE_VENTAS);
        onCreate(sqLiteDatabase);

    }
    // Método para insertar un ingreso registro con valor negativo
    public void insertarGasto(String producto, double total, String detalles, int cantidad) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("producto", producto);
        values.put("valor", total);// Puede ser positivo o negativo
        values.put("detalles", detalles);
        values.put("cantidad", cantidad);
        values.put("fecha_registro", obtenerFechaActual());
        db.insert(TABLE_VENTAS, null, values);
        db.close();
    }
    private String obtenerFechaActual() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
    // Método para cerrar y abrir la base de datos
    public void reopenDatabase() {
        if (getWritableDatabase() != null) {
            getWritableDatabase().close();
            SQLiteDatabase db = getWritableDatabase();
        }
    }
}

