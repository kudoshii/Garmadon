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

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvIrRegistro;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Asegúrate de que este layout exista en res/layout/
        setContentView(R.layout.activity_login);

        // 1. Inicialización de Vistas
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvIrRegistro = findViewById(R.id.tv_link_registro);

        // A. Botón de Login (Simulación para probar Intent)
        btnLogin.setOnClickListener(v -> {
            //MODIFICACION3 Se llama a la función de validación antes de continuar
            if (validarCampos()) {
                // Simulación de inicio de sesión exitoso y prueba del Intent
                Toast.makeText(LoginActivity.this, "Validación OK. Simulación de Inicio de Sesión.",
                        Toast.LENGTH_SHORT).show();
                irAHome();
            }
        });

        // B. Navegación a la pantalla de Registro
        tvIrRegistro.setOnClickListener(v -> irARegistro()); // <-- MODIFICACIÓN SEMANA 3: Uso de método auxiliar
    }

    //MODIFICACION3: Se añade la función de validación de campos
    private boolean validarCampos() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validación 1: Email (No vacío y formato válido)
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingrese un email válido.");
            return false;
        }

        // Validación 2: Contraseña (Verificamos que no esté vacía)
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("La contraseña es obligatoria.");
            return false;
        }

        return true;
    }

    //MODIFICACION3: Usamos HomeActivity
    public void irAHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //MODIFICACION3: Se añade la función auxiliar para ir a Registro
    private void irARegistro() {
        Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
        startActivity(intent);
    }
}
