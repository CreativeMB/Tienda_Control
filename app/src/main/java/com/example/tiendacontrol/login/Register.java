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

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Link interface components with variables
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        editTextPassword = findViewById(R.id.editTextPassword);
        textViewTermsLink = findViewById(R.id.textViewTermsLink);
        checkBoxTerms = findViewById(R.id.checkBoxTerms);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Set up "Login" button listener to redirect the user to the login screen
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
                finish(); // Close this activity so the user cannot navigate back
            }
        });

        // Set up "Register" button listener to register the user
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the user has accepted the terms and conditions
                if (!checkBoxTerms.isChecked()) {
                    Toast.makeText(Register.this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
                    return;
                }
                registerUser(); // Method to register the user
            }
        });

        // Set up terms and conditions link to open the corresponding webpage
        textViewTermsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open an activity or URL to display terms and conditions
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.floristerialoslirios.com"));
                startActivity(intent);
            }
        });

        // Set up button to select profile image
        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
    }

    // Method to register the user in Firebase Authentication and Firestore
    private void registerUser() {
        // Get user-entered data
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Check for empty fields
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check password length
        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User successfully registered in Firebase Authentication
                            Log.d("Firebase Authentication", "Usuario registrado con éxito");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Upload profile image if one was selected
                                if (imageUri != null) {
                                    uploadImage(user, name, email, password);
                                } else {
                                    saveUserData(user, name, email, password, null); // Updated to handle without image
                                }
                            }
                        } else {
                            // If authentication fails, show error message
                            Log.e("Firebase Authentication", "Error al registrar usuario en Firebase Authentication", task.getException());
                            Toast.makeText(Register.this, "Error al registrar usuario en Firebase Authentication", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Method to open file chooser to select an image
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Method to handle the result of selecting an image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            // Display the selected image in an ImageView before uploading
            ImageView imageViewProfile = findViewById(R.id.imageViewProfile);
            imageViewProfile.setImageURI(imageUri);
        }
    }

    // Method to upload the selected image to Firebase Storage and save user data in Firestore
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

    // Method to save user data in Firestore
    private void saveUserData(FirebaseUser user, String name, String email, String password, String imageUrl) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("password", password);
        if (imageUrl != null) {
            userData.put("profileImageUrl", imageUrl); // Add profile image URL if available
        }

        db.collection("usuarios")
                .document(user.getUid())
                .set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // User data successfully saved in Firestore
                        Log.d("Firestore", "Datos del usuario guardados con éxito");
                        Toast.makeText(Register.this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();
                        // Redirect user to the main screen of the application
                        Intent intent = new Intent(Register.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Close the registration activity so the user cannot navigate back
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // If writing to Firestore fails, show error message
                        Log.e("Firestore", "Error al guardar datos del usuario", e);
                        Toast.makeText(Register.this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
