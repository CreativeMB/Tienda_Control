package com.example.tiendacontrol.Bd;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class BdHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NOMBRE = "ventas.db";
    public static final String TABLE_VENTAS = "t_ventas";

    public BdHelper(@Nullable Context context) {
        super(context, DATABASE_NOMBRE, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_VENTAS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "producto TEXT NOT NULL," +
                "valor INTEGER NOT NULL," +
                "detalles TEXT NOT NULL," +
                "cantidad INTEGER" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE " + TABLE_VENTAS);
        onCreate(sqLiteDatabase);

    }
    // Método para insertar un nuevo registro con valor negativo
    public void insertarGasto(String producto, double valor, String detalles, int cantidad) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("producto", producto);
        values.put("valor", valor); // Puede ser positivo o negativo
        values.put("detalles", detalles);
        values.put("cantidad", cantidad);
        db.insert(TABLE_VENTAS, null, values);
        db.close();
    }
}
