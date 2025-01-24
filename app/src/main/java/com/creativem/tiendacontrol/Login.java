package com.creativem.tiendacontrol;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.creativem.tiendacontrol.monitor.BaseDatos;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    private static final String PREFS_NAME = "CodePrefs";
    private static final String LOGIN_STATUS = "loginStatus";
    private static final String CODE_KEY = "code"; // Añadimos la clave para el código
    private static final String TAG = "Login";
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        TextView btnGoogleSignIn = findViewById(R.id.tvGoogleSignIn);

        // Configurar Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Obtén este ID de la consola de Firebase
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        btnGoogleSignIn.setOnClickListener(v -> iniciarSesionConGoogle());


    }

    private void iniciarSesionConGoogle() {
        // Si ya hay cuentas de Google guardadas, el usuario puede elegir entre ellas
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // Si ya hay una cuenta firmada, usamos esta cuenta automáticamente
            firebaseAuthConGoogle(account);
        } else {
            // Si no hay cuenta firmada, mostramos la lista de cuentas disponibles
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        }
    }

    // Manejar el resultado del inicio de sesión con Google
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                                .getResult(ApiException.class);
                        if (account != null) {
                            firebaseAuthConGoogle(account);
                        }
                    } catch (ApiException e) {
                        Log.w(TAG, "Google sign in failed", e);
                        Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void firebaseAuthConGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Inicio de sesión exitoso
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "Usuario autenticado: " + user.getDisplayName());
                            verificarPerfilUsuario(user.getUid());
                        }
                    } else {
                        Log.w(TAG, "Error en la autenticación", task.getException());
                        Toast.makeText(this, "No se pudo autenticar con Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verificarPerfilUsuario(String userId) {
        // Referencia a la base de datos de Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Empresas");

        // Verificar si el perfil del usuario existe usando su userId
        databaseReference.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "Perfil encontrado, redirigiendo a Database...");
                    // Al iniciar sesión y si tiene savedCode, va a Inicio, caso contrario a BaseDatos
                    guardarEstadoLoginYRedireccionar(true);
                } else {
                    // El perfil no existe, redirigir a la actividad de creación de perfil
                    Log.d(TAG, "No se encontró perfil, redirigiendo a creación de perfil...");
                    guardarEstadoLoginYRedireccionar(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error al verificar perfil: " + databaseError.getMessage());
                Toast.makeText(Login.this, "Error al verificar perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Esta es la funcion que usas en la Activity Login
    private void guardarEstadoLoginYRedireccionar(boolean perfilExiste){

        sessionManager.setLoggedIn(true);
        Log.d(TAG, "Iniciando sesion - Estado despues de guardar: " + sessionManager.isLoggedIn());


        Intent intent;
        if(perfilExiste){
            intent = new Intent(Login.this, BaseDatos.class);
            // Como inicia sesion por primera vez, no tiene ningun saved code, asi que lo borramos
            sessionManager.clearSavedCode();
        }else{
            intent = new Intent(Login.this, PerfilEmpresa.class);
        }
        startActivity(intent);
        finish();

    }
}