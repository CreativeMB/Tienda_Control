package com.example.tiendacontrol.Bd;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class GoogleDriveHelper {

    private static final String TAG = "GoogleDriveHelper";

    private final DriveResourceClient mDriveResourceClient;
    private final Context mContext;

    public GoogleDriveHelper(DriveResourceClient driveResourceClient, Context context) {
        this.mDriveResourceClient = driveResourceClient;
        this.mContext = context;
    }

    // Método para subir la base de datos SQLite a Google Drive
    public void subirBaseDeDatos(File databaseFile) {
        // Nombre del archivo en Drive y MIME type
        String dbFileName = databaseFile.getName();
        String mimeType = "application/x-sqlite3"; // MIME type para SQLite

        mDriveResourceClient.createContents()
                .continueWithTask(task -> {
                    DriveContents driveContents = task.getResult();
                    try (OutputStream outputStream = driveContents.getOutputStream()) {
                        // Copiar el contenido del archivo SQLite a Drive
                        FileInputStream fileInputStream = new FileInputStream(databaseFile);
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        // Metadata para el archivo
                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                .setTitle(dbFileName)
                                .setMimeType(mimeType)
                                .build();

                        // Crear archivo en Drive
                        return mDriveResourceClient.createFile(getDriveFolderTask().getResult(), metadataChangeSet, driveContents);
                    } catch (IOException e) {
                        Log.e(TAG, "Error al subir archivo a Drive", e);
                    }
                    return null;
                })
                .addOnSuccessListener(driveFile -> {
                    Log.d(TAG, "Archivo subido correctamente a Drive: " + driveFile.getDriveId());
                    // Aquí puedes manejar el éxito, por ejemplo, mostrar un mensaje al usuario
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al subir archivo a Drive", e);
                    // Manejar el error, por ejemplo, mostrar un mensaje de error al usuario
                });
    }

    // Método para obtener la carpeta raíz de Drive
    private Task<DriveFolder> getDriveFolderTask() {
        return mDriveResourceClient.getRootFolder();
    }
}
