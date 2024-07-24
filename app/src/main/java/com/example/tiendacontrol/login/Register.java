package com.example.tiendacontrol.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tiendacontrol.monitor.MainActivity;
import com.example.tiendacontrol.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private TextView textViewTermsLink;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button btnRegister;
    private Button btnLogin;
    private CheckBox checkBoxTerms;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // Inicializar Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Vincular los componentes de la interfaz con las variables
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        editTextPassword = findViewById(R.id.editTextPassword);
        textViewTermsLink = findViewById(R.id.textViewTermsLink);
        checkBoxTerms = findViewById(R.id.checkBoxTerms);

        // Inicializar Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Configurar el botón "Login" para redirigir al usuario a la pantalla de inicio de sesión
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
                finish(); // Cerrar esta actividad para que el usuario no pueda navegar hacia atrás
            }
        });

        // Configurar el botón "Register" para registrar al usuario
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verificar si el usuario ha aceptado los términos y condiciones
                if (!checkBoxTerms.isChecked()) {
                    Toast.makeText(Register.this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
                    return;
                }
                registerUser(); // Método para registrar al usuario
            }
        });

        // Configurar el enlace de términos y condiciones para abrir la página web correspondiente
        textViewTermsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrir una actividad o URL para mostrar los términos y condiciones
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.floristerialoslirios.com"));
                startActivity(intent);
            }
        });

        // Configurar el botón para seleccionar una imagen de perfil
        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser(); // Abrir el selector de archivos
            }
        });
    }

    // Método para registrar al usuario en Firebase Authentication y Firestore
    private void registerUser() {
        // Obtener los datos ingresados por el usuario
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Verificar campos vacíos
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar la longitud de la contraseña
        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear usuario en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Usuario registrado exitosamente en Firebase Authentication
                            Log.d("Firebase Authentication", "Usuario registrado con éxito");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Subir imagen de perfil si se seleccionó una
                                if (imageUri != null) {
                                    uploadImage(user, name, email, password);
                                } else {
                                    saveUserData(user, name, email, password, null); // Actualizado para manejar sin imagen
                                }
                            }
                        } else {
                            // Si falla la autenticación, mostrar mensaje de error
                            Log.e("Firebase Authentication", "Error al registrar usuario en Firebase Authentication", task.getException());
                            Toast.makeText(Register.this, "Error al registrar usuario en Firebase Authentication", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Método para abrir el selector de archivos para seleccionar una imagen
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Método para manejar el resultado de la selección de una imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            // Mostrar la imagen seleccionada en un ImageView antes de subirla
            ImageView imageViewProfile = findViewById(R.id.imageViewProfile);
            imageViewProfile.setImageURI(imageUri);
        }
    }

    // Método para subir la imagen seleccionada a Firebase Storage y guardar los datos del usuario en Firestore
    private void uploadImage(FirebaseUser user, String name, String email, String password) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("tiendacontrol/perfil/" + user.getUid() + ".jpg");
        storageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                saveUserData(user, name, email, password, imageUrl);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Register.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                        Log.e("Firebase Storage", "Error al subir la imagen", e);
                    }
                });
    }

    // Método para guardar los datos del usuario en Firestore
    private void saveUserData(FirebaseUser user, String name, String email, String password, String imageUrl) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("password", password);
        if (imageUrl != null) {
            userData.put("profileImageUrl", imageUrl); // Agregar URL de la imagen de perfil si está disponible
        }

        db.collection("usuarios")
                .document(user.getUid())
                .set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Datos del usuario guardados exitosamente en Firestore
                        Log.d("Firestore", "Datos del usuario guardados con éxito");
                        Toast.makeText(Register.this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();
                        // Redirigir al usuario a la pantalla principal de la aplicación
                        Intent intent = new Intent(Register.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Cerrar la actividad de registro para que el usuario no pueda navegar hacia atrás
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Si falla la escritura en Firestore, mostrar mensaje de error
                        Log.e("Firestore", "Error al guardar datos del usuario", e);
                        Toast.makeText(Register.this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}