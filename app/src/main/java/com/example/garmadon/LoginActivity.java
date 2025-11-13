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
// SEMANA 8: Importaciones de Firebase Auth
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;


public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvIrRegistro;

    // SEMANA 8: Declaración de Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // SEMANA 8: Inicialización de Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 1. Inicialización de Vistas
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvIrRegistro = findViewById(R.id.tv_link_registro);

        // A. Botón de Login (Ahora con Firebase Auth)
        btnLogin.setOnClickListener(v -> {
            if (validarCampos()) {
                // MODIFICACIÓN SEMANA 8: Llamamos a la función real de inicio de sesión
                iniciarSesionConFirebase();
            }
        });

        // B. Navegación a la pantalla de Registro
        tvIrRegistro.setOnClickListener(v -> irARegistro());
    }

    // SEMANA 8: INICIO DE PERSISTENCIA
    @Override
    public void onStart() {
        super.onStart();

        // Verificar si el usuario ya ha iniciado sesión previamente (sesión persistente).
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Si el usuario ya está logueado, saltamos el login y vamos a la actividad principal.
            //Toast.makeText(this, "Sesión activa, ingresando...", Toast.LENGTH_SHORT).show();
            irAHome();
            // No es necesario llamar a finish() aquí ya que irAHome() lo hace y limpia la pila.
        }
    }
    // SEMANA 8: FIN DE PERSISTENCIA

    private void iniciarSesionConFirebase() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Asumiendo que validarCampos() ya fue llamada y pasó

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Inicio de sesión exitoso
                            Toast.makeText(LoginActivity.this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();
                            irAHome();
                        } else {
                            // Si falla el inicio de sesión, se informa al usuario.
                            //String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido.";
                            Toast.makeText(LoginActivity.this, "Fallo la autenticación: Usuario No Registrado! ", Toast.LENGTH_LONG).show();
                        }
                    }
                });
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
        // Estas flags son cruciales para que el usuario no pueda volver al Login con el botón de retroceso
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

