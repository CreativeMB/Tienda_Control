package com.creativem.tiendacontrol;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.creativem.tiendacontrol.model.Perfil;
import com.creativem.tiendacontrol.monitor.BaseDatos;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class PerfilEmpresa extends AppCompatActivity {

    private static final String TAG = "PerfilEmpresa";

    private EditText etNombrePersona, etNombreEmpresa, etTelefono, etDireccion, etPais, etCiudad;
    private TextView btnGuardarPerfil;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perfilempresa);

        // Inicializar vistas
        etNombrePersona = findViewById(R.id.etNombrePersona);
        etNombreEmpresa = findViewById(R.id.etNombreEmpresa);
        etTelefono = findViewById(R.id.etTelefono);
        etDireccion = findViewById(R.id.etDireccion);
        etPais = findViewById(R.id.etPais);
        etCiudad = findViewById(R.id.etCiudad);
        btnGuardarPerfil = findViewById(R.id.Guardarperfil);

        // Inicializar Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Empresas");

        // Configurar botón para guardar el perfil
        btnGuardarPerfil.setOnClickListener(v -> guardarPerfil());
    }

    private void guardarPerfil() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid(); // Obtener el UID del usuario autenticado
        String perfilId = databaseReference.push().getKey(); // Generar un ID único para el perfil

        if (perfilId != null) {
            Perfil perfil = new Perfil(
                    perfilId,
                    userId, // UID del usuario
                    etNombrePersona.getText().toString(),
                    etNombreEmpresa.getText().toString(),
                    etTelefono.getText().toString(),
                    etDireccion.getText().toString(),
                    etPais.getText().toString(),
                    etCiudad.getText().toString()
            );

            databaseReference.child(perfilId).setValue(perfil)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Perfil guardado exitosamente", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PerfilEmpresa.this, BaseDatos.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e(TAG, "Error al guardar el perfil", task.getException());
                            Toast.makeText(this, "Error al guardar el perfil", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Error al generar clave para el perfil", Toast.LENGTH_SHORT).show();
        }
    }
}