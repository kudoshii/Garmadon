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
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {

    private TextInputEditText etNombre;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etTelefono;
    private CountryCodePicker ccpCodigoTelefono;
    private Button btnRegistro;
    private TextView tvIrLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        inicializarVistas();
        configurarListeners();
    }

    private void inicializarVistas() {
        etNombre = findViewById(R.id.et_nombre);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etTelefono = findViewById(R.id.et_telefono);
        ccpCodigoTelefono = findViewById(R.id.ccp_codigo_telefono);
        btnRegistro = findViewById(R.id.btn_registro);
        tvIrLogin = findViewById(R.id.tv_link_login);

        // Configurar CCP por defecto en Chile
        ccpCodigoTelefono.setCountryForNameCode("CL");
    }

    private void configurarListeners() {
        btnRegistro.setOnClickListener(v -> {
            if (validarCampos()) {
                registrarUsuario();
            }
        });

        tvIrLogin.setOnClickListener(v -> irALogin());
    }

    private void registrarUsuario() {
        final String nombre = etNombre.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        final String telefono = etTelefono.getText().toString().trim();
        final String codigoTelefono = ccpCodigoTelefono.getSelectedCountryCode();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            guardarDatosUsuario(user.getUid(), nombre, email, telefono, codigoTelefono);
                        }

                        Toast.makeText(RegistroActivity.this,
                                "¡Registro exitoso! Bienvenido.", Toast.LENGTH_SHORT).show();
                        irALogin();
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Error de registro desconocido.";
                        Toast.makeText(RegistroActivity.this,
                                "Fallo el registro: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void guardarDatosUsuario(String uid, String nombre, String email,
                                     String telefono, String codigoTelefono) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("nombre", nombre);
        userData.put("email", email);
        userData.put("telefono", telefono);
        userData.put("codigoTelefono", codigoTelefono);
        userData.put("tiempo", System.currentTimeMillis());

        mDatabase.child("users").child(uid).setValue(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Datos guardados correctamente
                    } else {
                        Toast.makeText(RegistroActivity.this,
                                "Advertencia: No se pudieron guardar los datos del perfil.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validarCampos() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

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

        if (TextUtils.isEmpty(telefono) || telefono.length() < 8) {
            etTelefono.setError("Ingrese un número de teléfono válido.");
            valido = false;
        } else {
            etTelefono.setError(null);
        }

        return valido;
    }

    private void irALogin() {
        Intent intent = new Intent(RegistroActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}