package com.example.tiendacontrol.helper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import androidx.annotation.Nullable;
import com.example.tiendacontrol.model.Items;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BdVentas extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    public static final String TABLE_VENTAS = "Mi_Contabilidad";
    private static BdVentas instance;
    private SQLiteDatabase database;
    private String currentDatabase;

    public BdVentas(@Nullable Context context, @Nullable String databaseName) {
        super(context, getDatabasePath(context, databaseName), null, DATABASE_VERSION);
        if (databaseName == null || databaseName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la base de datos no puede ser null o vacío.");
        }
        this.currentDatabase = databaseName;
        Log.d("BdVentas", "Ruta de la base de datos: " + getDatabasePath(context, databaseName));
    }
    private static String getDatabasePath(Context context, @Nullable String databaseName) {
        if (databaseName == null || databaseName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la base de datos no puede ser null o vacío.");
        }

        File documentsFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "TiendaControl");
        if (!documentsFolder.exists()) {
            boolean result = documentsFolder.mkdirs(); // Crear el directorio si no existe
            Log.d("BdVentas", "Creando directorio: " + result);
        }
        return new File(documentsFolder, databaseName + ".db").getAbsolutePath();
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        if (database == null || !database.isOpen()) {
            database = super.getWritableDatabase();
            Log.d("BdVentas", "Base de datos abierta para escritura");
        }
        return database;
    }


    public static synchronized BdVentas getInstance(Context context, String databaseName) {
        if (instance == null) {
            instance = new BdVentas(context.getApplicationContext(), databaseName);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("BdVentas", "Creando tabla: " + TABLE_VENTAS);
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_VENTAS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "producto TEXT NOT NULL," +
                "valor REAL NOT NULL," +
                "detalles TEXT NOT NULL," +
                "cantidad INTEGER," +
                "fecha_registro DATETIME" +
                ");");
        Log.d("BdVentas", "Tabla creada: " + TABLE_VENTAS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.d("BdVentas", "Actualizando tabla de versión " + oldVersion + " a " + newVersion);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_VENTAS);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
        Log.d("BdVentas", "Configuración de la base de datos: Habilitando claves foráneas");
    }

    // Métodos de BdVentas

    public long insertarVenta(String producto, double total, String detalles, int cantidad) {
        long id = 0;
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("producto", producto);
            values.put("valor", total);
            values.put("detalles", detalles);
            values.put("cantidad", cantidad);
            values.put("fecha_registro", obtenerFechaActual());
            id = db.insert(TABLE_VENTAS, null, values);
            Log.d("BdVentas", "Insertado nuevo item con ID: " + id);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("BdVentas", "Error al insertar item: " + ex.getMessage());
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
                Log.d("BdVentas", "Base de datos cerrada después de insertar");
            }
        }
        return id;
    }

    public ArrayList<Items> mostrarVentas() {
        ArrayList<Items> listaVentas = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursorVentas = null;

        try {
            db = getReadableDatabase();
            cursorVentas = db.rawQuery("SELECT * FROM " + TABLE_VENTAS + " ORDER BY producto ASC", null);

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
                    Log.d("BdVentas", "Item recuperado: " + venta.getProducto());
                } while (cursorVentas.moveToNext());
            } else {
                Log.d("BdVentas", "La base de datos está vacía");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("BdVentas", "Error al mostrar ventas: " + ex.getMessage());
        } finally {
            if (cursorVentas != null) {
                cursorVentas.close();
                Log.d("BdVentas", "Cursor cerrado después de mostrar ventas");
            }
            if (db != null && db.isOpen()) {
                db.close();
                Log.d("BdVentas", "Base de datos cerrada después de mostrar ventas");
            }
        }

        return listaVentas;
    }

    public boolean eliminarItem(Items item) {
        SQLiteDatabase db = null;
        boolean eliminado = false;
        try {
            db = getWritableDatabase();
            String whereClause = "id = ?";
            String[] whereArgs = new String[]{String.valueOf(item.getId())};
            int rowsDeleted = db.delete(TABLE_VENTAS, whereClause, whereArgs);
            eliminado = rowsDeleted > 0;
            Log.d("BdVentas", "Item eliminado, ID: " + item.getId());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("BdVentas", "Error al eliminar item: " + e.getMessage());
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
                Log.d("BdVentas", "Base de datos cerrada después de eliminar");
            }
        }
        return eliminado;
    }

    public boolean actualizarItem(Items item) {
        boolean correcto = false;
        SQLiteDatabase db = null;

        try {
            db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("producto", item.getProducto());
            values.put("valor", item.getValor());
            values.put("detalles", item.getDetalles());
            values.put("cantidad", item.getCantidad());

            int rowsAffected = db.update(TABLE_VENTAS, values, "id = ?", new String[]{String.valueOf(item.getId())});
            correcto = rowsAffected > 0;
            Log.d("BdVentas", "Item actualizado, ID: " + item.getId());
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("BdVentas", "Error SQL al actualizar item: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("BdVentas", "Error al actualizar item: " + e.getMessage());
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
                Log.d("BdVentas", "Base de datos cerrada después de actualizar");
            }
        }

        return correcto;
    }

    private String obtenerFechaActual() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        String fechaActual = dateFormat.format(date);
        Log.d("BdVentas", "Fecha actual obtenida: " + fechaActual);
        return fechaActual;
    }

    public boolean eliminarTodo() {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.execSQL("DELETE FROM " + TABLE_VENTAS);
            Log.d("BdVentas", "Todos los items eliminados");
            return true;
        } catch (Exception e) {
            Log.e("BdVentas", "Error al eliminar las filas: " + e.getMessage());
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
                Log.d("BdVentas", "Base de datos cerrada después de eliminar todos los items");
            }
        }
    }

    public List<Items> getItemsByDates(String startDate, String endDate) {
        List<Items> filteredItems = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_VENTAS +
                    " WHERE fecha_registro BETWEEN ? AND ?" +
                    " ORDER BY fecha_registro ASC";
            cursor = db.rawQuery(query, new String[]{startDate, endDate});

            if (cursor.moveToFirst()) {
                do {
                    Items item = new Items();
                    item.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    item.setProducto(cursor.getString(cursor.getColumnIndexOrThrow("producto")));
                    item.setValor(cursor.getDouble(cursor.getColumnIndexOrThrow("valor")));
                    item.setDetalles(cursor.getString(cursor.getColumnIndexOrThrow("detalles")));
                    item.setCantidad(cursor.getInt(cursor.getColumnIndexOrThrow("cantidad")));
                    item.setFechaRegistro(cursor.getString(cursor.getColumnIndexOrThrow("fecha_registro")));
                    filteredItems.add(item);
                } while (cursor.moveToNext());
            } else {
                Log.d("BdVentas", "No se encontraron items para las fechas proporcionadas.");
            }
        } catch (Exception e) {
            Log.e("BdVentas", "Error al obtener items por fechas: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
                Log.d("BdVentas", "Cursor cerrado después de obtener items por fechas");
            }
            if (db != null && db.isOpen()) {
                db.close();
                Log.d("BdVentas", "Base de datos cerrada después de obtener items por fechas");
            }
        }
        return filteredItems;
    }
}