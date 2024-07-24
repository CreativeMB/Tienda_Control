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

    // Versión actual de la base de datos
    private static final int DATABASE_VERSION = 2;
    // Nombre del archivo de la base de datos
    public static final String DATABASE_NAME = "MI_contabilidad.db";
    // Nombre de la tabla en la base de datos
    public static final String TABLE_VENTAS = "Mi_Contabilidad";

    // Constructor de la clase BdHelper
    public BdHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Método llamado cuando se crea la base de datos por primera vez
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Crear la tabla con las columnas especificadas
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_VENTAS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "producto TEXT NOT NULL," +
                "valor INTEGER NOT NULL," +
                "detalles TEXT NOT NULL," +
                "cantidad INTEGER," +
                "fecha_registro TEXT" +
                ");");
    }

    // Método llamado cuando se actualiza la base de datos (cambio de versión)
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Eliminar la tabla si existe
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_VENTAS);
        // Crear una nueva tabla
        onCreate(sqLiteDatabase);
    }

    // Método para insertar un gasto en la base de datos
    public void insertarGasto(String producto, double total, String detalles, int cantidad) {
        SQLiteDatabase db = this.getWritableDatabase(); // Obtener acceso de escritura a la base de datos
        ContentValues values = new ContentValues();
        values.put("producto", producto); // Insertar el nombre del producto
        values.put("valor", total); // Insertar el valor del producto (puede ser positivo o negativo)
        values.put("detalles", detalles); // Insertar detalles adicionales
        values.put("cantidad", cantidad); // Insertar la cantidad del producto
        values.put("fecha_registro", obtenerFechaActual()); // Insertar la fecha y hora actual
        db.insert(TABLE_VENTAS, null, values); // Insertar los valores en la tabla
        db.close(); // Cerrar la base de datos
    }

    // Método para obtener la fecha y hora actual en formato específico
    private String obtenerFechaActual() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    // Método para cerrar y reabrir la base de datos
    public void reopenDatabase() {
        if (getWritableDatabase() != null) {
            getWritableDatabase().close(); // Cerrar la base de datos si está abierta
            SQLiteDatabase db = getWritableDatabase(); // Reabrir la base de datos
        }
    }
}