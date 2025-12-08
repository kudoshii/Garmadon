package com.example.garmadon;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CambiarPasswordActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputEditText etCurrentPassword;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnActualizarPassword;

    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_password);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Inicializar ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Por favor espere");
        progressDialog.setCanceledOnTouchOutside(false);

        inicializarVistas();
        configurarToolbar();
        configurarListeners();
    }

    private void inicializarVistas() {
        toolbar = findViewById(R.id.toolbar_cambiar_password);
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnActualizarPassword = findViewById(R.id.btn_actualizar_password);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void configurarListeners() {
        btnActualizarPassword.setOnClickListener(v -> {
            if (validarCampos()) {
                cambiarPasswordConReautenticacion();
            }
        });
    }

    private boolean validarCampos() {
        String actual = etCurrentPassword.getText().toString().trim();
        String nueva = etNewPassword.getText().toString().trim();
        String confirmacion = etConfirmPassword.getText().toString().trim();

        // Validar campos vacíos
        if (actual.isEmpty()) {
            etCurrentPassword.setError("Campo obligatorio");
            etCurrentPassword.requestFocus();
            return false;
        }

        if (nueva.isEmpty()) {
            etNewPassword.setError("Campo obligatorio");
            etNewPassword.requestFocus();
            return false;
        }

        if (confirmacion.isEmpty()) {
            etConfirmPassword.setError("Campo obligatorio");
            etConfirmPassword.requestFocus();
            return false;
        }

        // Validar que la nueva contraseña no sea igual a la actual
        if (nueva.equals(actual)) {
            etNewPassword.setError("La nueva contraseña debe ser diferente a la actual");
            etNewPassword.requestFocus();
            Toast.makeText(this, "La nueva contraseña debe ser diferente a la actual.",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        // Validar que las contraseñas coincidan
        if (!nueva.equals(confirmacion)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            etConfirmPassword.requestFocus();
            Toast.makeText(this, "La nueva contraseña y su confirmación no coinciden.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar longitud mínima
        if (nueva.length() < 6) {
            etNewPassword.setError("Mínimo 6 caracteres");
            etNewPassword.requestFocus();
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void cambiarPasswordConReautenticacion() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String email = user.getEmail();
        String passwordActual = etCurrentPassword.getText().toString().trim();
        String passwordNueva = etNewPassword.getText().toString().trim();

        progressDialog.setMessage("Verificando contraseña actual...");
        progressDialog.show();

        // Paso 1: Reautenticar al usuario con su contraseña actual
        AuthCredential credential = EmailAuthProvider.getCredential(email, passwordActual);

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Reautenticación exitosa, ahora cambiar la contraseña
                            actualizarPassword(user, passwordNueva);
                        } else {
                            // Fallo en la reautenticación (contraseña actual incorrecta)
                            progressDialog.dismiss();
                            etCurrentPassword.setError("Contraseña incorrecta");
                            etCurrentPassword.requestFocus();
                            Toast.makeText(CambiarPasswordActivity.this,
                                    "La contraseña actual es incorrecta.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void actualizarPassword(FirebaseUser user, String nuevaPassword) {
        progressDialog.setMessage("Actualizando contraseña...");

        user.updatePassword(nuevaPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            Toast.makeText(CambiarPasswordActivity.this,
                                    "Contraseña actualizada exitosamente.",
                                    Toast.LENGTH_SHORT).show();

                            // Limpiar los campos
                            etCurrentPassword.setText("");
                            etNewPassword.setText("");
                            etConfirmPassword.setText("");

                            // Cerrar la actividad
                            finish();
                        } else {
                            String errorMsg = task.getException() != null ?
                                    task.getException().getMessage() : "Error desconocido";

                            Toast.makeText(CambiarPasswordActivity.this,
                                    "Error al actualizar la contraseña: " + errorMsg,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}