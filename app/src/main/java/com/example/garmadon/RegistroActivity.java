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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;


public class RegistroActivity extends AppCompatActivity {

    private TextInputEditText etNombre;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnRegistro;
    private TextView tvIrLogin;

    // Instancias de Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Inicialización de Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 1. Inicialización de Vistas
        etNombre = findViewById(R.id.et_nombre);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnRegistro = findViewById(R.id.btn_registro);
        tvIrLogin = findViewById(R.id.tv_link_login);

        // A. Botón de Registro: Llama a la función de registro de Firebase
        btnRegistro.setOnClickListener(v -> {
            if (validarCampos()) {
                // Si la validación local es exitosa, se procede con Firebase
                registrarUsuario();
            }
        });

        // B. Navegación a la pantalla de Login
        tvIrLogin.setOnClickListener(v -> irALogin());
    }

    private void registrarUsuario() {
        final String nombre = etNombre.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 1. Crear usuario en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registro de Auth exitoso
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // 2. Guardar datos adicionales en Realtime Database
                            guardarDatosUsuario(user.getUid(), nombre, email);
                        }

                        Toast.makeText(RegistroActivity.this, "¡Registro exitoso! Bienvenido.", Toast.LENGTH_SHORT).show();
                        // Redirigir al Login o a la actividad principal
                        irALogin();
                    } else {
                        // Si falla el registro (ej. email ya en uso, mala conexión, contraseña débil)
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Error de registro desconocido.";
                        Toast.makeText(RegistroActivity.this, "Fallo el registro: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void guardarDatosUsuario(String uid, String nombre, String email) {
        // Objeto Map para guardar los datos
        Map<String, Object> userData = new HashMap<>();
        userData.put("nombre", nombre);
        userData.put("email", email);
        userData.put("fecha_creacion", System.currentTimeMillis()); // Marca de tiempo en milisegundos

        // Guardar en la ruta 'users/UID_DEL_USUARIO'
        mDatabase.child("users").child(uid).setValue(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Los datos del perfil se guardaron correctamente en la base de datos
                        // El Toast de éxito general ya se mostró en registrarUsuario()
                    } else {
                        // Error al guardar los datos del perfil
                        Toast.makeText(RegistroActivity.this, "Advertencia: No se pudieron guardar los datos del perfil.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validarCampos() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean valido = true;

        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("El nombre es obligatorio.");
            valido = false;
        } else {
            etNombre.setError(null);
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingrese un email válido.");
            valido = false;
        } else {
            etEmail.setError(null);
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres.");
            valido = false;
        } else {
            etPassword.setError(null);
        }

        return valido;
    }
    private void irALogin() {
        // Asumiendo que existe una clase Login
        Intent intent = new Intent(RegistroActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
