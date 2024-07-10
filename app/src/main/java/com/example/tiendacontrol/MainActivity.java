package com.example.tiendacontrol;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendacontrol.Bd.BdVentas;

import com.example.tiendacontrol.Bd.GoogleDriveHelper;
import com.example.tiendacontrol.adaptadores.ListaVentasAdapter;
import com.example.tiendacontrol.dialogFragment.GastoDialogFragment;
import com.example.tiendacontrol.entidades.Ventas;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    SearchView txtBuscar;
    RecyclerView listaVentas;
    ArrayList<Ventas> listaArrayVentas;
    ListaVentasAdapter adapter;
    FloatingActionButton fabNuevo;
    FloatingActionButton fabGasto;
    TextView textVenta, textTotal, textGasto;

    Toolbar toolbar;

    private GoogleSignInClient googleSignInClient;
    private GoogleDriveHelper googleDriveHelper;
    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtBuscar = findViewById(R.id.txtBuscar);
        listaVentas = findViewById(R.id.listaVentas);
        fabNuevo = findViewById(R.id.favNuevo);
        fabGasto = findViewById(R.id.favGasto);
        textVenta = findViewById(R.id.textVenta);
        textTotal = findViewById(R.id.textTotal);
        textGasto = findViewById(R.id.textGasto);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tienda Control");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listaVentas.setLayoutManager(new LinearLayoutManager(this));

        BdVentas bdVentas = new BdVentas(MainActivity.this);
        listaArrayVentas = new ArrayList<>(bdVentas.mostrarVentas());
        adapter = new ListaVentasAdapter(bdVentas.mostrarVentas());
        listaVentas.setAdapter(adapter);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInClient.silentSignIn()
                .addOnSuccessListener(this, googleSignInAccount -> {
                    // Crear el GoogleDriveHelper con el cliente de recursos de Drive y el contexto
                    googleDriveHelper = new GoogleDriveHelper(Drive.getDriveResourceClient(getApplicationContext(), googleSignInAccount), MainActivity.this);
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Error al iniciar sesión en Google de forma silenciosa", e);
                    // Manejar el error, por ejemplo, mostrar un mensaje de error al usuario
                });

        fabGasto.setOnClickListener(view -> {
            GastoDialogFragment dialogFragment = new GastoDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "GastoDialogFragment");
        });

        fabNuevo.setOnClickListener(view -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            com.example.tiendacontrol.IngresoDialogFragment ingresoDialogFragment = com.example.tiendacontrol.IngresoDialogFragment.newInstance();
            ingresoDialogFragment.show(fragmentManager, "ingreso_dialog");
        });

        txtBuscar.setOnQueryTextListener(this);

        calcularSumaGanancias();
        calcularSumaTotalVenta();
        calcularSumaTotalGasto();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.exportar_db) {
            exportarBaseDatos();
            return true;
        } else if (id == R.id.nueva_venta) {
            nuevoRegistro();
            return true;
        } else if (id == R.id.nuevo_gasto) {
            nuevoGasto();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void nuevoRegistro() {
        Intent intent = new Intent(this, com.example.tiendacontrol.IngresoDialogFragment.class);
        startActivity(intent);
    }

    private void nuevoGasto() {
        Intent intent = new Intent(this, GastoDialogFragment.class);
        startActivity(intent);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.filtrado(newText);
        return false;
    }

    private void calcularSumaGanancias() {
        double suma = 0.0;
        for (Ventas venta : listaArrayVentas) {
            double valorVenta = venta.getValorAsDouble();
            suma += valorVenta;
        }
        int sumaFormateada = (int) suma;
        String sumaFormateadaStr = "$" + sumaFormateada;
        textTotal.setText(sumaFormateadaStr);
    }

    private void calcularSumaTotalVenta() {
        double suma = 0.0;
        for (Ventas venta : listaArrayVentas) {
            double valorVenta = venta.getValorAsDouble();
            if (valorVenta > 0) {
                suma += valorVenta;
            }
        }
        int sumaFormateada = (int) suma;
        String sumaFormateadaStr = "$" + sumaFormateada;
        textVenta.setText(sumaFormateadaStr);
    }

    private void calcularSumaTotalGasto() {
        double suma = 0.0;
        for (Ventas venta : listaArrayVentas) {
            double valorVenta = venta.getValorAsDouble();
            if (valorVenta < 0) {
                suma += valorVenta;
            }
        }
        int sumaFormateada = (int) suma;
        String sumaFormateadaStr = "$" + sumaFormateada;
        textGasto.setText(sumaFormateadaStr);
    }

    private void exportarBaseDatos() {
        File dbFile = this.getDatabasePath(BdVentas.DATABASE_NAME);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            iniciarSesionGoogle();
        } else {
            subirArchivoAGoogleDrive(dbFile);
        }
    }

    private void iniciarSesionGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                File dbFile = this.getDatabasePath(BdVentas.DATABASE_NAME);
                subirArchivoAGoogleDrive(dbFile);
            } else {
                Toast.makeText(this, "Inicio de sesión en Google fallido", Toast.LENGTH_SHORT).show();
            }
        } catch (ApiException e) {
            Log.e(TAG, "Error al iniciar sesión en Google", e);
        }
    }

    private void subirArchivoAGoogleDrive(File file) {
        googleDriveHelper.subirBaseDeDatos(file);
    }
}