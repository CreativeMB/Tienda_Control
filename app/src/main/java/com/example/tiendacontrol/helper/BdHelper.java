//package com.example.tiendacontrol.helper;
//
//import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.os.Environment;
//import android.util.Log;
//
//import androidx.annotation.Nullable;
//
//import com.example.tiendacontrol.model.Items;
//
//import java.io.File;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//public class BdHelper extends SQLiteOpenHelper {
//    private static final int DATABASE_VERSION = 2;
//    public static final String TABLE_VENTAS = "Mi_Contabilidad";
//    private static BdHelper instance;
//    private SQLiteDatabase database;
//
//    public BdHelper(@Nullable Context context, String databaseName) {
//        super(context, getDatabasePath(context, databaseName), null, DATABASE_VERSION);
//        Log.d("BdHelper", "Ruta de la base de datos: " + getDatabasePath(context, databaseName));
//    }
//
//    // Método para obtener la ruta completa de la base de datos
//    private static String getDatabasePath(Context context, String databaseName) {
//        File documentsFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "TiendaControl");
//        if (!documentsFolder.exists()) {
//            documentsFolder.mkdirs(); // Crear el directorio si no existe
//        }
//        return new File(documentsFolder, databaseName + ".db").getAbsolutePath();
//    }
//
//    @Override
//    public synchronized SQLiteDatabase getWritableDatabase() {
//        if (database == null || !database.isOpen()) {
//            database = super.getWritableDatabase();
//        }
//        return database;
//    }
//
//    @Override
//    public void close() {
//        super.close();
//        if (database != null && database.isOpen()) {
//            database.close();
//        }
//    }
//
//    public static synchronized BdHelper getInstance(Context context, String databaseName) {
//        if (instance == null) {
//            instance = new BdHelper(context.getApplicationContext(), databaseName);
//        }
//        return instance;
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase sqLiteDatabase) {
//        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_VENTAS + " (" +
//                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
//                "producto TEXT NOT NULL," +
//                "valor REAL NOT NULL," +
//                "detalles TEXT NOT NULL," +
//                "cantidad INTEGER," +
//                "fecha_registro DATETIME" +
//                ");");
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_VENTAS);
//        onCreate(sqLiteDatabase);
//    }
//    @Override
//    public void onConfigure(SQLiteDatabase db) {
//        super.onConfigure(db);
//        db.setForeignKeyConstraintsEnabled(true);
//    }
//
//    public List<Items> getItemsByDates(String startDate, String endDate) {
//        List<Items> filteredItems = new ArrayList<>();
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        // Consulta SQL para obtener elementos dentro del rango de fechas
//        String query = "SELECT * FROM " + TABLE_VENTAS +
//                " WHERE date(fecha_registro) BETWEEN date(?) AND date(?)"; // Usa date() para comparar fechas
//
//        // Ejecutar la consulta con las fechas de inicio y fin como parámetros
//        Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});
//
//        if (cursor.moveToFirst()) {
//            do {
//                Items result = new Items();
//                result.setProducto(cursor.getString(cursor.getColumnIndex("producto")));
//                result.setValor(cursor.getDouble(cursor.getColumnIndex("valor")));
//                result.setDetalles(cursor.getString(cursor.getColumnIndex("detalles")));
//                result.setCantidad(cursor.getInt(cursor.getColumnIndex("cantidad")));
//                result.setFechaRegistro(cursor.getString(cursor.getColumnIndex("fecha_registro")));
//                filteredItems.add(result);
//            } while (cursor.moveToNext());
//        }
//
//        cursor.close();
//        db.close();
//
//        return filteredItems;
//    }
//}
