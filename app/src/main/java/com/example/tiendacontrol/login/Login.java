package com.example.tiendacontrol.login;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tiendacontrol.adapter.DatabaseManagerActivity;
import com.example.tiendacontrol.monitor.AccessCode;
import com.example.tiendacontrol.monitor.MainActivity;
import com.example.tiendacontrol.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button btnLogin,  btnCode;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // Eliminado EdgeToEdge.enable(this);

        // Inicialización de Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Vinculación de las vistas con los objetos correspondientes
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnCode = findViewById(R.id.btnCodeAcceso);

        // Configuración del botón de registro para abrir la actividad de registro
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
                finish(); // Finaliza esta actividad para que el usuario no pueda volver atrás
            }
        });

        // Configuración del botón de inicio de sesión para llamar al método loginUser()
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Configuración del botón de registro para abrir la actividad de registro
        btnCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, AccessCode.class);
                startActivity(intent);
                finish(); // Finaliza esta actividad para que el usuario no pueda volver atrás
            }
        });
    }

    // Método para iniciar sesión
    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validación de campos de entrada
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Iniciar sesión con Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Inicio de sesión exitoso, mostrar mensaje de éxito y redirigir a la actividad principal
                            Toast.makeText(Login.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, DatabaseManagerActivity.class);
                            startActivity(intent);
                            finish(); // Cierra esta actividad para que el usuario no pueda volver atrás
                        } else {
                            // Si falla el inicio de sesión, mostrar mensaje de error
                            Toast.makeText(Login.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Método llamado cuando se hace clic en "Forgot your password?"
    public void forgotPasswordClicked(View view) {
        EditText editTextEmail = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cambiar Contraseña")
                .setMessage("Ingrese su correo electrónico para recibir el enlace de cambio de contraseña")
                .setView(editTextEmail)
                .setPositiveButton("Enviar", (dialog, which) -> {
                    String email = editTextEmail.getText().toString().trim();

                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(Login.this, "Por favor introduzca su correo electrónico", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(Login.this, "Por favor introduzca un correo electrónico válido", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Enviar correo de restablecimiento de contraseña
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Login.this, "Correo electrónico enviado", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Login.this, "Error al enviar el correo electrónico", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create().show();
    }
}
