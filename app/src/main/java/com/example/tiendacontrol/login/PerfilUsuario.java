package com.example.tiendacontrol.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.example.tiendacontrol.MainActivity;
import com.example.tiendacontrol.R;
import com.example.tiendacontrol.dialogFragment.GastoDialogFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class PerfilUsuario extends AppCompatActivity {
    private TextView textViewName;
    private EditText editTextName;
    private TextView textViewEmail;
    private ImageView imageViewProfile;
    private Button btnSave;
    private EditText editTextSlogan;
    private EditText editTextDescription;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private String userId;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_usuario);

        // Inicializar componentes de la interfaz de usuario
        textViewName = findViewById(R.id.textViewName);
        editTextName = findViewById(R.id.editTextName);
        textViewEmail = findViewById(R.id.textViewEmail);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        btnSave = findViewById(R.id.btnSave);
        editTextSlogan = findViewById(R.id.editTextSlogan);
        editTextDescription = findViewById(R.id.editTextDescription);
        toolbar = findViewById(R.id.toolbar);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Obtener el usuario actual de Firebase Authentication
        FirebaseUser user = mAuth.getCurrentUser();
        userId = user.getUid();

        // Obtener la Toolbar y configurarla
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tienda Control");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilitar el botón de retroceso

        if (user != null) {
            // Mostrar los datos del usuario
            textViewEmail.setText(user.getEmail());

            // Cargar datos del usuario desde Firestore
            loadUserData(userId);
        }

        // Configurar el click del ImageView para seleccionar una imagen
        imageViewProfile.setOnClickListener(v -> openFileChooser());

        // Configurar el botón "Guardar"
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });
    }

    // Método para abrir el selector de archivos para elegir una imagen
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Método para manejar el resultado de seleccionar una imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageViewProfile.setImageURI(imageUri);
            // Sube la imagen seleccionada a Firebase Storage
            uploadImage();
        }
    }

    // Método para subir la imagen seleccionada a Firebase Storage
    private void uploadImage() {
        if (imageUri != null) {
            StorageReference fileRef = storageRef.child("/tiendacontrolperfil" + userId + ".jpg");

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Obteniendo la URL de descarga de la imagen
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                            Toast.makeText(PerfilUsuario.this, "Imagen subida correctamente", Toast.LENGTH_SHORT).show();
                            // Guardar la URL de la imagen en la base de datos del usuario
                            updateProfileImageUrl(uri.toString());
                            // Actualiza la ImageView con la nueva imagen
                            Picasso.get().load(uri).into(imageViewProfile);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PerfilUsuario.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Método para cargar los datos del usuario desde Firestore
    private void loadUserData(String userId) {
        DocumentReference userRef = db.collection("usuarios").document(userId);

        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    String slogan = documentSnapshot.getString("slogan");
                    String description = documentSnapshot.getString("description");
                    String imageUrl = documentSnapshot.getString("profileImageUrl");

                    editTextName.setText(name);
                    textViewName.setText(name);
                    editTextSlogan.setText(slogan);
                    editTextDescription.setText(description);

                    // Cargar la imagen de perfil si existe
                    if (imageUrl != null) {
                        Picasso.get().load(imageUrl).into(imageViewProfile);
                    }
                } else {
                    Toast.makeText(PerfilUsuario.this, "No se encontró el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PerfilUsuario.this, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para guardar los datos del usuario en Firestore
    private void saveUserData() {
        String name = editTextName.getText().toString().trim();
        String slogan = editTextSlogan.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        // Validar que el campo de nombre no esté vacío
        if (name.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa un nombre", Toast.LENGTH_SHORT).show();
            return;
        }

        // Actualizar los datos del usuario en Firestore
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("slogan", slogan);
        userData.put("description", description);

        db.collection("usuarios").document(userId)
                .update(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PerfilUsuario.this, "Datos del usuario guardados correctamente", Toast.LENGTH_SHORT).show();
                    // Actualizar la vista con los datos actualizados
                    textViewName.setText(name);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PerfilUsuario.this, "Error al guardar datos del usuario", Toast.LENGTH_SHORT).show();
                });
    }

    // Método para actualizar la URL de la imagen de perfil en Firestore
    private void updateProfileImageUrl(String imageUrl) {
        db.collection("usuarios").document(userId)
                .update("profileImageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PerfilUsuario.this, "Imagen de perfil actualizada correctamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PerfilUsuario.this, "Error al actualizar la imagen de perfil", Toast.LENGTH_SHORT).show();
                });
    }

    // Metodo para crear el menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    // Metodo para manejar los clicks en el menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.exportar_db) {
                       return true;
        } else if (id == R.id.nueva_venta) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            com.example.tiendacontrol.IngresoDialogFragment ingresoDialogFragment = com.example.tiendacontrol.IngresoDialogFragment.newInstance();
            ingresoDialogFragment.show(fragmentManager, "ingreso_dialog");
            return true;
        } else if (id == R.id.nuevo_gasto) {
            GastoDialogFragment dialogFragment = new GastoDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "GastoDialogFragment");
            return true;
        } else if (id == R.id.perfil_usuario) {
            // Ir a la pantalla de perfil de usuario
            Intent intent = new Intent(this, PerfilUsuario.class);
            finish();
            return true;

        } else if (id == R.id.BaseDatos) {
            Intent intent = new Intent(this, MainActivity.class);
            finish();
            return true;
        } else if (id == R.id.salir) {
            dirigirAInicioSesion();
            return true;

        }


        return super.onOptionsItemSelected(item);
    }

    // Método para redirigir al usuario a la pantalla de inicio de sesión
    private void  dirigirAInicioSesion() {
        mAuth.signOut();
        startActivity(new Intent(PerfilUsuario.this, Login.class));
        finish(); // Cierra la actividad actual
    }
}