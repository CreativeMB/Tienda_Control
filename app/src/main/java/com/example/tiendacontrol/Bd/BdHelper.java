package com.example.tiendacontrol.Bd;

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
                "producto TEXT NOT NULL UNIQUE," +
                "valor INTEGER NOT NULL UNIQUE," + // O "valor REAL NOT NULL UNIQUE" si es decimal
                "detalles TEXT NOT NULL UNIQUE," +
                "cantidad INTEGER" +
                ");");
    }
//    @Override
//    public void onCreate(SQLiteDatabase sqLiteDatabase) {
//
//        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_VENTAS + "(" +
//                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
//                "producto TEXT NOT NULL," +
//                "valor TEXT NOT NULL," +
//                "detalles TEXT NOT NULL," +
//                "cantidad TEXT)");
//    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE " + TABLE_VENTAS);
        onCreate(sqLiteDatabase);

    }
}
