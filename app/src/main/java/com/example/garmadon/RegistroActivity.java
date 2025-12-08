package com.example.garmadon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog; // Importación necesaria para ProgressDialog
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // Importación necesaria para Log.e
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
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

    private static final String TAG = "RegistroActivity"; // Para logging

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

    // Reemplazo del método registrarUsuario con mejor manejo de errores
    private void registrarUsuario() {
        final String nombre = etNombre.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        final String telefono = etTelefono.getText().toString().trim();
        final String codigoTelefono = ccpCodigoTelefono.getSelectedCountryCode();

        // FIX 1: Deshabilitar botón durante el registro
        btnRegistro.setEnabled(false);

        // FIX 2: Mostrar ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creando cuenta...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registro exitoso
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Llamar al método mejorado para guardar datos
                            guardarDatosUsuario(user.getUid(), nombre, email, telefono,
                                    codigoTelefono, progressDialog);
                        } else {
                            progressDialog.dismiss();
                            btnRegistro.setEnabled(true);
                            Toast.makeText(RegistroActivity.this,
                                    "Error inesperado al obtener usuario",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // FIX 3: Manejo de errores específicos
                        progressDialog.dismiss();
                        btnRegistro.setEnabled(true);

                        String errorMessage = "Error de registro desconocido";

                        try {
                            throw task.getException();
                        } catch (FirebaseAuthWeakPasswordException e) {
                            errorMessage = "Contraseña débil: " + e.getReason();
                            etPassword.setError(errorMessage);
                            etPassword.requestFocus();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            errorMessage = "Email inválido o mal formateado.";
                            etEmail.setError(errorMessage);
                            etEmail.requestFocus();
                        } catch (FirebaseAuthUserCollisionException e) {
                            errorMessage = "Este email ya está registrado. Intenta iniciar sesión.";
                            etEmail.setError(errorMessage);
                            etEmail.requestFocus();
                        } catch (Exception e) {
                            errorMessage = task.getException() != null ?
                                    task.getException().getMessage() : errorMessage;
                            Log.e(TAG, "Error en Firebase Auth: " + errorMessage, e);
                        }

                        Toast.makeText(RegistroActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // FIX 4: Mejorar guardarDatosUsuario con validaciones y manejo de ProgressDialog
    private void guardarDatosUsuario(String uid, String nombre, String email,
                                     String telefono, String codigoTelefono,
                                     ProgressDialog progressDialog) {
        // Validar datos antes de guardar
        if (uid == null || uid.trim().isEmpty()) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error: UID inválido", Toast.LENGTH_SHORT).show();
            btnRegistro.setEnabled(true);
            return;
        }

        // Sanitizar inputs
        nombre = nombre.replaceAll("\\s+", " ").trim();
        telefono = telefono.replaceAll("[^0-9]", "");

        Map<String, Object> userData = new HashMap<>();
        userData.put("nombre", nombre);
        userData.put("email", email);
        userData.put("telefono", telefono);
        userData.put("codigoTelefono", codigoTelefono);
        userData.put("tiempo", System.currentTimeMillis());
        userData.put("urlImagenPerfil", ""); // Inicializar vacío

        mDatabase.child("users").child(uid).setValue(userData)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    btnRegistro.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(RegistroActivity.this,
                                "¡Registro exitoso! Bienvenido.",
                                Toast.LENGTH_SHORT).show();
                        irALogin();
                    } else {
                        // FIX 5: Si falla guardar datos, reportar y llevar al login
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Error al guardar datos";

                        Log.e(TAG, "Error al guardar datos: " + errorMsg);

                        Toast.makeText(RegistroActivity.this,
                                "Advertencia: Cuenta creada pero hubo un problema al guardar tus datos. " +
                                        "Por favor, contacta a soporte.",
                                Toast.LENGTH_LONG).show();

                        // Aún así, llevar al usuario al login
                        irALogin();
                    }
                });
    }

    // FIX 6: Agregar validación de longitud y regex más estricta en validarCampos
    private boolean validarCampos() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        boolean valido = true;

        // Validar nombre
        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("El nombre es obligatorio.");
            valido = false;
        } else if (nombre.length() < 3) {
            etNombre.setError("El nombre debe tener al menos 3 caracteres.");
            valido = false;
        } else if (nombre.length() > 50) {
            etNombre.setError("El nombre es demasiado largo (máximo 50 caracteres).");
            valido = false;
        } else if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            etNombre.setError("El nombre solo puede contener letras y espacios.");
            valido = false;
        } else {
            etNombre.setError(null);
        }

        // Validar email
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingrese un email válido.");
            valido = false;
        } else if (email.length() > 100) {
            etEmail.setError("Email demasiado largo.");
            valido = false;
        } else {
            etEmail.setError(null);
        }

        // Validar password
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("La contraseña es obligatoria.");
            valido = false;
        } else if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres.");
            valido = false;
        } else if (password.length() > 50) {
            etPassword.setError("La contraseña es demasiado larga.");
            valido = false;
        } else {
            etPassword.setError(null);
        }

        // Validar teléfono
        if (TextUtils.isEmpty(telefono)) {
            etTelefono.setError("Ingrese un número de teléfono.");
            valido = false;
        } else if (telefono.length() < 8) {
            etTelefono.setError("Ingrese un número de teléfono válido (mínimo 8 dígitos).");
            valido = false;
        } else if (telefono.length() > 15) {
            etTelefono.setError("Número de teléfono demasiado largo.");
            valido = false;
        } else if (!telefono.matches("^[0-9]+$")) {
            etTelefono.setError("El teléfono solo debe contener números.");
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