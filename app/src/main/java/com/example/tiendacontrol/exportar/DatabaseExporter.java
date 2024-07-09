package com.example.tiendacontrol.exportar;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.widget.Toast;

import com.example.tiendacontrol.Bd.BdVentas;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class DatabaseExporter {

    private Context context;

    public DatabaseExporter(Context context) {
        this.context = context;
    }

    public boolean exportDatabase() {
        // Nombre del archivo de la base de datos SQLite
        String currentDBPath = context.getDatabasePath("Ventas.db").getPath();

        // Nombre del archivo de destino para la exportación
        String exportFileName = "Ventas.db";

        // Obtener la ruta de la carpeta de archivos externos de la app
        File exportDir = context.getExternalFilesDir(null);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, exportFileName);
        try {
            BdVentas dbHelper = new BdVentas(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            FileChannel inChannel = null;
            FileChannel outChannel = null;
            try {
                file.createNewFile();
                inChannel = new FileInputStream(currentDBPath).getChannel();
                outChannel = new FileOutputStream(file).getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } finally {
                if (inChannel != null) {
                    inChannel.close();
                }
                if (outChannel != null) {
                    outChannel.close();
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al exportar la base de datos", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}