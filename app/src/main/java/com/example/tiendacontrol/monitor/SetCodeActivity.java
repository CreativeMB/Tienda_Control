package com.example.tiendacontrol.monitor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.tiendacontrol.R;
import com.example.tiendacontrol.dialogFragment.MenuDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SetCodeActivity extends AppCompatActivity {
    private FloatingActionButton fabMenu;
    private EditText editTextCode;
    private Button buttonSaveCode;
    private static final String PREFS_NAME = "CodePrefs";
    private static final String CODE_KEY = "access_code";
    private static final String TAG = "SetCodeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_code);

        editTextCode = findViewById(R.id.editTextCode);
        buttonSaveCode = findViewById(R.id.buttonSaveCode);
        fabMenu = findViewById(R.id.fabMenu);

        fabMenu.setOnClickListener(view -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            MenuDialogFragment menuDialogFragment = MenuDialogFragment.newInstance();
            menuDialogFragment.show(fragmentManager, "servicios_dialog");
        });

        buttonSaveCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editTextCode.getText().toString().trim();
                if (!code.isEmpty()) {
                    saveCode(code);
                    Toast.makeText(SetCodeActivity.this, "Código guardado correctamente", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Código guardado correctamente: " + code);
                    finish(); // Cierra la actividad después de guardar el código
                } else {
                    Toast.makeText(SetCodeActivity.this, "El campo de código está vacío", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "El campo de código está vacío");
                }
            }
        });
    }

    private void saveCode(String code) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CODE_KEY, code);
        editor.apply();
    }
}