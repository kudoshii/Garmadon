package com.example.garmadon;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class CambiarPasswordActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputEditText etCurrentPassword;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnActualizarPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_password);

        inicializarVistas();
        configurarToolbar();
        configurarListeners();
    }

    private void inicializarVistas() {
        // Inicializar la Toolbar
        toolbar = findViewById(R.id.toolbar_cambiar_password);

        // Inicializar los campos de texto
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        // Inicializar el botón
        btnActualizarPassword = findViewById(R.id.btn_actualizar_password);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            // Habilita el ícono de navegación (la flecha)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Configura el listener para que la flecha de retroceso cierre la actividad
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void configurarListeners() {
        btnActualizarPassword.setOnClickListener(v -> {
            validarYActualizarContrasena();
        });
    }

    private void validarYActualizarContrasena() {
        String actual = etCurrentPassword.getText().toString();
        String nueva = etNewPassword.getText().toString();
        String confirmacion = etConfirmPassword.getText().toString();

        if (actual.isEmpty() || nueva.isEmpty() || confirmacion.isEmpty()) {
            Toast.makeText(this, "Todos los campos de contraseña son obligatorios.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!nueva.equals(confirmacion)) {
            Toast.makeText(this, "La nueva contraseña y su confirmación no coinciden.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nueva.equals(actual)) {
            Toast.makeText(this, "La nueva contraseña debe ser diferente a la actual.", Toast.LENGTH_LONG).show();
            return;
        }

        // --- Lógica de cambio de contraseña iría aquí (Firebase) ---

        Toast.makeText(this, "Contraseña actualizada exitosamente.", Toast.LENGTH_SHORT).show();
        finish(); // Vuelve a la actividad anterior (CuentaActivity)
    }
}
