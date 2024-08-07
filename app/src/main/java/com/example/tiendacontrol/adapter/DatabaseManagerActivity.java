package com.example.tiendacontrol.adapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendacontrol.R;
import com.example.tiendacontrol.adapter.DatabaseAdapter;
import com.example.tiendacontrol.helper.BdHelper;
import com.example.tiendacontrol.monitor.MainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManagerActivity extends AppCompatActivity implements DatabaseAdapter.OnDatabaseClickListener {
    private EditText editTextDatabaseName;
    private Button buttonCreateDatabase;
    private RecyclerView recyclerViewDatabases;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_CURRENT_DATABASE = "currentDatabase";
    private List<String> databaseList;
    private DatabaseAdapter adapter;
    private BdHelper bdHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.database_manager_activity);

        editTextDatabaseName = findViewById(R.id.editTextDatabaseName);
        buttonCreateDatabase = findViewById(R.id.buttonCreateDatabase);
        recyclerViewDatabases = findViewById(R.id.recyclerViewDatabases);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        recyclerViewDatabases.setLayoutManager(new LinearLayoutManager(this));
        databaseList = new ArrayList<>();
        adapter = new DatabaseAdapter(this, databaseList, this);
        recyclerViewDatabases.setAdapter(adapter);

        // Cierra la base de datos actual al iniciar la actividad
        closeCurrentDatabase();

        buttonCreateDatabase.setOnClickListener(v -> {
            String databaseName = editTextDatabaseName.getText().toString().trim();
            if (!databaseName.isEmpty()) {
                createDatabase(databaseName);
                loadDatabases(); // Llama a loadDatabases() después de crear la base de datos
            } else {
                showToast("Ingrese un nombre de base de datos");
            }
        });

        loadDatabases();
    }

    private void createDatabase(String databaseName) {
        BdHelper dbHelper = new BdHelper(this, databaseName + ".db");
        dbHelper.getWritableDatabase().close();

        // Verificar que la base de datos se haya creado
        File dbFile = getDatabasePath(databaseName + ".db");
        if (dbFile.exists()) {
            showToast("Base de datos creada: " + dbFile.getAbsolutePath());
        } else {
            showToast("Error al crear la base de datos");
        }
    }

    private void loadDatabases() {
        databaseList.clear();
        File dbFolder = getApplicationContext().getDatabasePath("default.db").getParentFile();
        if (dbFolder != null && dbFolder.isDirectory()) {
            File[] dbFiles = dbFolder.listFiles();
            if (dbFiles != null) {
                for (File file : dbFiles) {
                    if (file.isFile() && file.getName().endsWith(".db") && !file.getName().equals("google_app_measurement_local.db")) {
                        // Eliminar la extensión .db del nombre del archivo
                        String fileNameWithoutExtension = file.getName().replace(".db", "");
                        databaseList.add(fileNameWithoutExtension);
                    }
                }
            } else {
                showToast("No se encontraron bases de datos");
            }
        } else {
            showToast("No se encontró el directorio de bases de datos");
        }
        adapter.notifyDataSetChanged();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDatabaseClick(String databaseName) {
        if (databaseName != null && !databaseName.isEmpty()) {
            showDatabaseOptionsDialog(databaseName);
        } else {
            showToast("Nombre de base de datos inválido");
        }
    }

    private void showDatabaseOptionsDialog(String databaseName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar opción")
                .setIcon(R.drawable.baseline_dataset_linked_24) // Añade un icono, opcional

                // Estiliza los botones del diálogo
                .setPositiveButton("Editar", (dialog, which) -> {
                    editDatabase(databaseName);
                })
                .setNegativeButton("Eliminar", (dialog, which) -> {
                    confirmDeleteDatabase(databaseName);
                });

        AlertDialog dialog = builder.create();
        dialog.show();

        // Personaliza los botones si es necesario
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if (positiveButton != null) {
            positiveButton.setTextColor(getResources().getColor(R.color.colorPositivo)); // Cambia el color del texto
        }

        if (negativeButton != null) {
            negativeButton.setTextColor(getResources().getColor(R.color.colorNegativo)); // Cambia el color del texto
        }
    }
    private void editDatabase(String databaseName) {
        // Cierra la base de datos actual si está abierta
        closeCurrentDatabase();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CURRENT_DATABASE, databaseName); // Guarda el nombre de la base de datos
        editor.putBoolean("KEY_DATABASE_SELECTED", true);
        editor.apply();
        showToast("Base de datos actual: " + databaseName);

        Log.d("DatabaseManagerActivity", "Base de datos seleccionada: " + databaseName);

        // Redirigir a MainActivity, pasando el nombre de la base de datos como extra
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("databaseName", databaseName);
        startActivity(intent);
        finish();
    }

    private void confirmDeleteDatabase(String databaseName) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar base de datos")
                .setMessage("¿Estás seguro de que deseas eliminar la base de datos \"" + databaseName + "\"?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Llama al método para eliminar la base de datos
                    boolean resultado = deleteDatabase(databaseName); // Asegúrate de tener este método disponible
                    if (resultado) {
                        Toast.makeText(this, "Base de datos eliminada", Toast.LENGTH_SHORT).show();

                        // Actualiza la lista de bases de datos
                        loadDatabases(); // Recarga la lista de bases de datos después de eliminar

                    } else {
                        Toast.makeText(this, "Error al eliminar la base de datos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .setIcon(R.drawable.baseline_delete_forever_24)
                .show();
    }

    private void deleteDatabaseFile(String databaseName) {
        File dbFile = getDatabasePath(databaseName);
        if (dbFile.exists() && dbFile.delete()) {
            showToast("Base de datos eliminada: " + databaseName);
            loadDatabases(); // Recargar la lista de bases de datos
        } else {
            showToast("Error al eliminar la base de datos");
        }
    }

    private void closeCurrentDatabase() {
        if (bdHelper != null) {
            bdHelper.close(); // Cierra la base de datos
            bdHelper = null; // Establece bdHelper a null
            // Agrega un mensaje de log para confirmar el cierre de la base de datos
            Log.d("DatabaseManagerActivity", "Base de datos cerrada correctamente.");
        } else {
            // Agrega un mensaje de log si bdHelper es null
            Log.d("DatabaseManagerActivity", "No hay ninguna base de datos abierta para cerrar.");
        }
    }
    public boolean deleteDatabase(String databaseName) {
        return getApplicationContext().deleteDatabase(databaseName);
    }
}


    //    private EditText editTextDatabaseName;
//    private Button buttonCreateDatabase;
//    private RecyclerView recyclerViewDatabases;
//    private SharedPreferences sharedPreferences;
//    private static final String PREFS_NAME = "MyPrefs";
//    private static final String KEY_CURRENT_DATABASE = "currentDatabase";
//    private List<String> databaseList;
//    private DatabaseAdapter adapter;
//    private BdHelper bdHelper;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.database_manager_activity);
//
//        editTextDatabaseName = findViewById(R.id.editTextDatabaseName);
//        buttonCreateDatabase = findViewById(R.id.buttonCreateDatabase);
//        recyclerViewDatabases = findViewById(R.id.recyclerViewDatabases);
//        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//
//        recyclerViewDatabases.setLayoutManager(new LinearLayoutManager(this));
//        databaseList = new ArrayList<>();
//        adapter = new DatabaseAdapter(this, databaseList, this);
//        recyclerViewDatabases.setAdapter(adapter);
//
//        // Cierra la base de datos actual al iniciar la actividad
//        closeCurrentDatabase();
//
//        buttonCreateDatabase.setOnClickListener(v -> {
//            String databaseName = editTextDatabaseName.getText().toString().trim();
//            if (!databaseName.isEmpty()) {
//                createDatabase(databaseName);
//                loadDatabases(); // Llama a loadDatabases() después de crear la base de datos
//            } else {
//                showToast("Ingrese un nombre de base de datos");
//            }
//        });
//
//        loadDatabases();
//    }
//
//    private void createDatabase(String databaseName) {
//        BdHelper dbHelper = new BdHelper(this, databaseName + ".db");
//        dbHelper.getWritableDatabase().close();
//
//        // Verificar que la base de datos se haya creado
//        File dbFile = getDatabasePath(databaseName + ".db");
//        if (dbFile.exists()) {
//            showToast("Base de datos creada: " + dbFile.getAbsolutePath());
//        } else {
//            showToast("Error al crear la base de datos");
//        }
//    }
//
//    private void loadDatabases() {
//        databaseList.clear();
//        File dbFolder = getApplicationContext().getDatabasePath("default.db").getParentFile();
//        if (dbFolder != null && dbFolder.isDirectory()) {
//            File[] dbFiles = dbFolder.listFiles();
//            if (dbFiles != null) {
//                for (File file : dbFiles) {
//                    if (file.isFile() && file.getName().endsWith(".db")) {
//                        databaseList.add(file.getName());
//                    }
//                }
//            } else {
//                showToast("No se encontraron bases de datos");
//            }
//        } else {
//            showToast("No se encontró el directorio de bases de datos");
//        }
//        adapter.notifyDataSetChanged();
//    }
//
//    private void showToast(String message) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onDatabaseClick(String databaseName) {
//        if (databaseName != null && !databaseName.isEmpty()) {
//            // Cierra la base de datos actual si está abierta
//            closeCurrentDatabase();
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putString(KEY_CURRENT_DATABASE, databaseName); // Guarda el nombre de la base de datos
//            editor.putBoolean("KEY_DATABASE_SELECTED", true);
//            editor.apply();
//            showToast("Base de datos actual: " + databaseName);
//
//            Log.d("DatabaseManagerActivity", "Base de datos seleccionada: " + databaseName);
//
//            // Registra la acción de guardar la base de datos en SharedPreferences
//            Log.d("DatabaseManagerActivity", "Base de datos guardada en SharedPreferences: " + databaseName);
//
//            // Redirigir a MainActivity, pasando el nombre de la base de datos como extra
//            Intent intent = new Intent(this, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            intent.putExtra("databaseName", databaseName);
//            startActivity(intent);
//            finish();
//        } else {
//            showToast("Nombre de base de datos inválido");
//        }
//    }
//
//    private void closeCurrentDatabase() {
//        if (bdHelper != null) {
//            bdHelper.close(); // Cierra la base de datos
//            bdHelper = null; // Establece bdHelper a null
//            // Agrega un mensaje de log para confirmar el cierre de la base de datos
//            Log.d("DatabaseManagerActivity", "Base de datos cerrada correctamente.");
//        } else {
//            // Agrega un mensaje de log si bdHelper es null
//            Log.d("DatabaseManagerActivity", "No hay ninguna base de datos abierta para cerrar.");
//        }
//    }
//}