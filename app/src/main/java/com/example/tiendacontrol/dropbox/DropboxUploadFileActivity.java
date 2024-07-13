//package com.example.tiendacontrol.dropbox;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Environment;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.dropbox.core.DbxRequestConfig;
//import com.dropbox.core.v2.DbxClientV2;
//import com.dropbox.core.v2.files.FileMetadata;
//import com.dropbox.core.v2.files.WriteMode;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.InputStream;
//
//public class DropboxUploadFileActivity extends AppCompatActivity {
//
//    private static final String ACCESS_TOKEN_KEY = "dropbox_access_token";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // Cargar el token de acceso desde las preferencias compartidas
//        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
//        String accessToken = prefs.getString(ACCESS_TOKEN_KEY, null);
//
//        if (accessToken != null) {
//            // Subir el archivo a Dropbox
//            new UploadFileTask(this, accessToken).execute();
//        } else {
//            Toast.makeText(this, "No hay token de acceso. Debes autenticarte primero.", Toast.LENGTH_SHORT).show();
//            finish();
//        }
//    }
//
//    private static class UploadFileTask extends AsyncTask<Void, Void, FileMetadata> {
//        private Context context;
//        private String accessToken;
//
//        public UploadFileTask(Context context, String accessToken) {
//            this.context = context;
//            this.accessToken = accessToken;
//        }
//
//        @Override
//        protected FileMetadata doInBackground(Void... voids) {
//            try {
//                // Configurar el cliente de Dropbox
//                DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/TiendaControl").build();
//                DbxClientV2 client = new DbxClientV2(config, accessToken);
//
//                // Especificar la ruta del archivo que deseas subir
//                File file = new File(Environment.getExternalStorageDirectory() + "/Download/archivo_a_subir.txt");
//
//                if (file.exists()) {
//                    try (InputStream in = new FileInputStream(file)) {
//                        return client.files().uploadBuilder("/" + file.getName())
//                                .withMode(WriteMode.ADD)
//                                .uploadAndFinish(in);
//                    }
//                } else {
//                    Toast.makeText(context, "El archivo no existe", Toast.LENGTH_SHORT).show();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(FileMetadata result) {
//            if (result != null) {
//                // El archivo se ha subido correctamente
//                Toast.makeText(context, "Archivo subido: " + result.getName(), Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(context, "Error al subir el archivo a Dropbox", Toast.LENGTH_SHORT).show();
//            }
//            ((DropboxUploadFileActivity) context).finish();
//        }
//    }
//}