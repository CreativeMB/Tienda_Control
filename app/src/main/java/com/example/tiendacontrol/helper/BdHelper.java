package com.example.tiendacontrol.helper;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.tiendacontrol.model.Items;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BdHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    public static final String TABLE_VENTAS = "Mi_Contabilidad";
    private static BdHelper instance;
    private SQLiteDatabase database;

    public BdHelper(@Nullable Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
    }
    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        if (database == null || !database.isOpen()) {
            database = super.getWritableDatabase();
        }
        return database;
    }

    // Método para cerrar la base de datos
    @Override
    public void close() {
        super.close();
    }


    public static synchronized BdHelper getInstance(Context context, String databaseName) {
        if (instance == null) {
            instance = new BdHelper(context.getApplicationContext(), databaseName);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_VENTAS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "producto TEXT NOT NULL," +
                "valor REAL NOT NULL," +
                "detalles TEXT NOT NULL," +
                "cantidad INTEGER," +
                "fecha_registro DATETIME" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_VENTAS);
        onCreate(sqLiteDatabase);
    }



//    public long insertarGasto(String producto, double total, String detalles, int cantidad) {
//        long id = 0;
//        SQLiteDatabase db = null;
//        try {
//            db = getWritableDatabase();
//            ContentValues values = new ContentValues();
//            values.put("producto", producto);
//            values.put("valor", total);
//            values.put("detalles", detalles);
//            values.put("cantidad", cantidad);
//            values.put("fecha_registro", obtenerFechaActual());
//            id = db.insert(TABLE_VENTAS, null, values);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        } finally {
//            if (db != null && db.isOpen()) {
//                db.close();
//            }
//        }
//        return id;
//    }

    private String obtenerFechaActual() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public List<Items> getResultsByDay(String date) {
        return getResults("SELECT * FROM " + TABLE_VENTAS + " WHERE date(fecha_registro) = ?", new String[]{date});
    }

    public List<Items> getResultsByWeek(String date) {
        return getResults("SELECT * FROM " + TABLE_VENTAS + " WHERE strftime('%W', fecha_registro) = strftime('%W', ?)", new String[]{date});
    }

    public List<Items> getResultsByMonth(String date) {
        return getResults("SELECT * FROM " + TABLE_VENTAS + " WHERE strftime('%m', fecha_registro) = strftime('%m', ?)", new String[]{date});
    }

    public List<Items> getResultsByYear(String date) {
        return getResults("SELECT * FROM " + TABLE_VENTAS + " WHERE strftime('%Y', fecha_registro) = strftime('%Y', ?)", new String[]{date});
    }

    private List<Items> getResults(String query, String[] selectionArgs) {
        List<Items> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, selectionArgs);
            if (cursor.moveToFirst()) {
                do {
                    Items result = new Items();
                    result.setProducto(cursor.getString(cursor.getColumnIndex("producto")));
                    result.setValor(cursor.getDouble(cursor.getColumnIndex("valor")));
                    result.setDetalles(cursor.getString(cursor.getColumnIndex("detalles")));
                    result.setCantidad(cursor.getInt(cursor.getColumnIndex("cantidad")));
                    result.setFechaRegistro(cursor.getString(cursor.getColumnIndex("fecha_registro")));
                    results.add(result);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return results;
    }

    public List<Items> getItemsByDates(String startDate, String endDate) {
        List<Items> filteredItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT * FROM " + TABLE_VENTAS +
                    " WHERE date(fecha_registro) BETWEEN date(?) AND date(?)";
            cursor = db.rawQuery(query, new String[]{startDate, endDate});
            if (cursor.moveToFirst()) {
                do {
                    Items result = new Items();
                    result.setProducto(cursor.getString(cursor.getColumnIndex("producto")));
                    result.setValor(cursor.getDouble(cursor.getColumnIndex("valor")));
                    result.setDetalles(cursor.getString(cursor.getColumnIndex("detalles")));
                    result.setCantidad(cursor.getInt(cursor.getColumnIndex("cantidad")));
                    result.setFechaRegistro(cursor.getString(cursor.getColumnIndex("fecha_registro")));
                    filteredItems.add(result);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return filteredItems;
    }

    public List<Items> getAllItems() {
        List<Items> itemsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_VENTAS, null);
            if (cursor.moveToFirst()) {
                do {
                    Items item = new Items();
                    item.setProducto(cursor.getString(cursor.getColumnIndex("producto")));
                    item.setValor(cursor.getDouble(cursor.getColumnIndex("valor")));
                    item.setDetalles(cursor.getString(cursor.getColumnIndex("detalles")));
                    item.setCantidad(cursor.getInt(cursor.getColumnIndex("cantidad")));
                    item.setFechaRegistro(cursor.getString(cursor.getColumnIndex("fecha_registro")));
                    itemsList.add(item);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return itemsList;
    }
}