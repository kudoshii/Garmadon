package com.example.garmadon;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class RegistroActivity extends AppCompatActivity {

    private TextInputEditText etNombre;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnRegistro;
    private TextView tvIrLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // 1. Inicialización de Vistas
        etNombre = findViewById(R.id.et_nombre);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnRegistro = findViewById(R.id.btn_registro);
        tvIrLogin = findViewById(R.id.tv_link_login);

        // A. Botón de Registro (Simulación para probar Intent)
        btnRegistro.setOnClickListener(v -> {
            //MODIFICACION3: Se llama a la función de validación antes de continuar
            if (validarCampos()) {
                // Simulación de registro exitoso (ahora que sabemos que los datos son válidos)
                Toast.makeText(RegistroActivity.this, "Validación OK. Registro simulado.",
                        Toast.LENGTH_SHORT).show();
                irALogin();
            }
        });
        //MODIFICACION3: Se eliminA la lógica de validación simple del listener

        // B. Navegación a la pantalla de Login (Intent directo)
        tvIrLogin.setOnClickListener(v -> irALogin());
    }

    //MODIFICACION3: Se añade la función de validación
    private boolean validarCampos() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 1. Validación de Nombre
        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("El nombre es obligatorio.");
            return false;
        }

        // 2. Validación de Email (No vacío y formato válido)
        // Usamos Patterns.EMAIL_ADDRESS para asegurar que el formato sea correcto.
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingrese un email válido.");
            return false;
        }

        // 3. Validación de Contraseña (Mínimo 6 caracteres, requisito de Firebase y buena práctica)
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres.");
            return false;
        }

        return true;
    }
    //MODIFICACION3: Usamos LoginActivity
    private void irALogin() {
        // Renombrado a LoginActivity por convención
        Intent intent = new Intent(RegistroActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}