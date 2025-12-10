package com.example.garmadon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CuentaActivity extends AppCompatActivity {

    private ImageView imgPerfil;
    private TextView tvValorNombres;
    private TextView tvValorEmail;
    private TextView tvValorMiembro;
    private TextView tvValorTelefono;
    private TextView tvValorEstado;

    private MaterialButton btnEditarPerfil;
    private MaterialButton btnCambiarPassword;
    private MaterialButton btnEliminarAnuncios;
    private MaterialButton btnCerrarSesion;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private final int DEFAULT_PROFILE_IMAGE = R.drawable.perfil;

    private boolean isLoggingOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        Toolbar toolbar = findViewById(R.id.toolbar_cuenta);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        inicializarVistasInformacion();
        inicializarBotonesOpciones();
        configurarListenersBotones();
        configurarCabecera();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Solo cargar datos si no estamos cerrando sesión Y el usuario existe
        if (currentUser != null && !isLoggingOut) {
            cargarDatosPerfil();
        } else if (currentUser == null) {
            // Si no hay usuario, ir directamente al login
            irALogin();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Siempre limpiar listeners al salir de la actividad
        limpiarListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        limpiarListeners();
    }

    private void irALogin() {
        Intent intent = new Intent(CuentaActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void inicializarVistasInformacion() {
        imgPerfil = findViewById(R.id.img_perfil);
        tvValorNombres = findViewById(R.id.tv_valor_nombres);
        tvValorEmail = findViewById(R.id.tv_valor_email);
        tvValorMiembro = findViewById(R.id.tv_valor_miembro);
        tvValorTelefono = findViewById(R.id.tv_valor_telefono);
        tvValorEstado = findViewById(R.id.tv_valor_estado);
    }

    private void cargarDatosPerfil() {
        String uid = mAuth.getUid();

        if (uid == null || isLoggingOut) {
            tvValorEstado.setText("No autenticado");
            return;
        }

        // Limpiar listener anterior si existe
        limpiarListeners();

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Verificar PRIMERO que la actividad sigue válida
                if (isLoggingOut || isFinishing() || isDestroyed()) {
                    return;
                }

                // Verificar que aún tenemos un usuario autenticado
                if (mAuth.getCurrentUser() == null) {
                    return;
                }

                // NUEVO: Verificar que el snapshot existe
                if (!snapshot.exists()) {
                    Log.w("CuentaActivity", "No existe información del usuario en Firebase");
                    tvValorNombres.setText("Usuario sin datos");
                    tvValorEstado.setText("Incompleto");
                    return;
                }

                // Obtener datos con manejo robusto de diferentes estructuras
                String nombres = obtenerValorSeguro(snapshot, "nombre");
                String urlImagenPerfil = obtenerValorSeguro(snapshot, "urlImagenPerfil");
                String email = currentUser != null ? currentUser.getEmail() : "No disponible";

                // Manejar diferentes formatos de timestamp
                String tiempoStr = obtenerTimestamp(snapshot);

                // Manejar teléfono como String o Number
                String telefono = obtenerTelefonoSeguro(snapshot, "telefono");
                String codTelefono = obtenerValorSeguro(snapshot, "codigoTelefono");

                // Manejar fecha de nacimiento (puede ser fecha_nac o fechaNacimiento)
                String fechaNac = obtenerValorSeguro(snapshot, "fecha_nac");
                if (fechaNac.equals("No disponible")) {
                    fechaNac = obtenerValorSeguro(snapshot, "fechaNacimiento");
                }

                // Construir teléfono completo
                String cod_tel_completo = "";
                if (!codTelefono.isEmpty() && !codTelefono.equals("No disponible")) {
                    cod_tel_completo += "+" + codTelefono + " ";
                }
                if (!telefono.equals("No disponible")) {
                    cod_tel_completo += telefono;
                }
                if (cod_tel_completo.trim().isEmpty()) {
                    cod_tel_completo = "No disponible";
                }

                // Actualizar UI
                tvValorNombres.setText(nombres);
                tvValorEmail.setText(email);
                tvValorMiembro.setText(obtenerFecha(tiempoStr));
                tvValorTelefono.setText(cod_tel_completo);
                tvValorEstado.setText("Verificado");

                // Cargar imagen de perfil
                if (urlImagenPerfil != null && !urlImagenPerfil.isEmpty()
                        && !urlImagenPerfil.equals("No disponible")) {
                    try {
                        if (!isFinishing() && !isDestroyed()) {
                            Glide.with(CuentaActivity.this)
                                    .load(urlImagenPerfil)
                                    .placeholder(DEFAULT_PROFILE_IMAGE)
                                    .error(DEFAULT_PROFILE_IMAGE)
                                    .into(imgPerfil);
                        }
                    } catch (Exception e) {
                        Log.e("GLIDE_ERROR", "Error al cargar la imagen con Glide", e);
                        imgPerfil.setImageResource(DEFAULT_PROFILE_IMAGE);
                    }
                } else {
                    imgPerfil.setImageResource(DEFAULT_PROFILE_IMAGE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isLoggingOut && !isFinishing() && !isDestroyed()) {
                    tvValorNombres.setText("Error al cargar datos.");
                    tvValorEstado.setText("Error");
                    Toast.makeText(CuentaActivity.this,
                            "Fallo al leer la BD: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        databaseReference.child(uid).addValueEventListener(valueEventListener);
    }

    private void inicializarBotonesOpciones() {
        btnEditarPerfil = findViewById(R.id.btn_editar_perfil);
        btnCambiarPassword = findViewById(R.id.btn_cambiar_password);
        btnEliminarAnuncios = findViewById(R.id.btn_eliminar_anuncios);
        btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion);
    }

    private void configurarListenersBotones() {

        btnEditarPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(CuentaActivity.this, EditarPerfilActivity.class);
            startActivity(intent);
        });

        btnCambiarPassword.setOnClickListener(v -> {
            Intent intent = new Intent(CuentaActivity.this, CambiarPasswordActivity.class);
            startActivity(intent);
        });

        btnEliminarAnuncios.setOnClickListener(v -> {
            Toast.makeText(this, "Mostrar diálogo de confirmación para eliminar anuncios",
                    Toast.LENGTH_SHORT).show();
        });

        btnCerrarSesion.setOnClickListener(v -> {
            cerrarSesion();
        });

    }

    private void configurarCabecera() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void cerrarSesion() {
        try {
            isLoggingOut = true;
            limpiarListeners();
            mAuth.signOut();

            Toast.makeText(this, "Sesión cerrada con éxito.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(CuentaActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Log.e("CuentaActivity", "Error al cerrar sesión: " + e.getMessage(), e);
            Toast.makeText(this, "Error al cerrar sesión. Intente nuevamente.",
                    Toast.LENGTH_SHORT).show();
            isLoggingOut = false;
        }
    }

    private void limpiarListeners() {
        try {
            if (databaseReference != null && valueEventListener != null) {
                databaseReference.removeEventListener(valueEventListener);
                valueEventListener = null;
                Log.d("CuentaActivity", "ValueEventListener de perfil removido.");
            }
        } catch (Exception e) {
            Log.e("CuentaActivity", "Error al limpiar listeners: " + e.getMessage(), e);
        }
    }

    private String obtenerFecha(String timestampString) {
        if (timestampString == null || timestampString.isEmpty() || timestampString.equals("null")) {
            return "N/A";
        }

        try {
            long timestamp = Long.parseLong(timestampString);

            // Si el timestamp es muy pequeño, asumimos que está en segundos
            if (timestamp < 10000000000L) {
                timestamp *= 1000;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));

        } catch (NumberFormatException e) {
            Log.e("FECHA_FORMAT", "Error al parsear timestamp: " + timestampString, e);

            // Intentar parsear como fecha directa (por si viene en formato "19/11/2025")
            if (timestampString.contains("/")) {
                return timestampString;
            }

            return "N/A";
        } catch (Exception e) {
            Log.e("FECHA_FORMAT", "Error inesperado: " + e.getMessage(), e);
            return "N/A";
        }
    }

    /**
     * Obtiene un valor String de Firebase de forma segura
     */
    private String obtenerValorSeguro(DataSnapshot snapshot, String campo) {
        try {
            Object valor = snapshot.child(campo).getValue();
            if (valor == null) {
                return "No disponible";
            }
            return String.valueOf(valor).trim();
        } catch (Exception e) {
            Log.w("CuentaActivity", "Error al obtener campo " + campo + ": " + e.getMessage());
            return "No disponible";
        }
    }

    /**
     * Obtiene el teléfono manejando tanto String como Number
     */
    private String obtenerTelefonoSeguro(DataSnapshot snapshot, String campo) {
        try {
            Object valor = snapshot.child(campo).getValue();
            if (valor == null) {
                return "No disponible";
            }

            // Si es un número, convertir a String sin notación científica
            if (valor instanceof Long || valor instanceof Integer) {
                return String.valueOf(valor);
            }

            String telefonoStr = String.valueOf(valor).trim();

            // Limpiar cualquier carácter no numérico excepto '+'
            telefonoStr = telefonoStr.replaceAll("[^0-9+]", "");

            if (telefonoStr.isEmpty()) {
                return "No disponible";
            }

            return telefonoStr;
        } catch (Exception e) {
            Log.w("CuentaActivity", "Error al obtener teléfono: " + e.getMessage());
            return "No disponible";
        }
    }

    /**
     * Obtiene el timestamp manejando diferentes formatos
     */
    private String obtenerTimestamp(DataSnapshot snapshot) {
        try {
            // Intentar con 'tiempo' primero
            Object tiempo = snapshot.child("tiempo").getValue();
            if (tiempo != null) {
                return String.valueOf(tiempo);
            }

            // Intentar con 'fecha_creacion'
            Object fechaCreacion = snapshot.child("fecha_creacion").getValue();
            if (fechaCreacion != null) {
                return String.valueOf(fechaCreacion);
            }

            // Intentar con otros nombres posibles
            Object timestamp = snapshot.child("timestamp").getValue();
            if (timestamp != null) {
                return String.valueOf(timestamp);
            }

            return null;
        } catch (Exception e) {
            Log.w("CuentaActivity", "Error al obtener timestamp: " + e.getMessage());
            return null;
        }
    }
}