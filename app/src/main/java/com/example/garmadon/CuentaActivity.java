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
        if (currentUser != null && !isLoggingOut) {
            cargarDatosPerfil();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        limpiarListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        limpiarListeners();
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

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isLoggingOut || isFinishing() || isDestroyed()) {
                    return;
                }

                String nombres = snapshot.child("nombre").getValue(String.class);
                String urlImagenPerfil = snapshot.child("urlImagenPerfil").getValue(String.class);
                String email = currentUser != null ? currentUser.getEmail() : null;
                String tiempoStr = snapshot.child("tiempo").getValue(String.class);
                String telefono = snapshot.child("telefono").getValue(String.class);
                String codTelefono = snapshot.child("codigoTelefono").getValue(String.class);

                nombres = nombres != null ? nombres : "No disponible";
                email = email != null ? email : "No disponible";
                telefono = telefono != null ? telefono : "No disponible";
                codTelefono = codTelefono != null ? codTelefono : "";
                String estado = "Verificado";

                String cod_tel_completo = "";
                if (!codTelefono.isEmpty()) {
                    cod_tel_completo += "+" + codTelefono + " ";
                }
                cod_tel_completo += telefono;
                if (cod_tel_completo.trim().isEmpty()) {
                    cod_tel_completo = "No disponible";
                }

                tvValorNombres.setText(nombres);
                tvValorEmail.setText(email);
                tvValorMiembro.setText(obtenerFecha(tiempoStr));
                tvValorTelefono.setText(cod_tel_completo);
                tvValorEstado.setText(estado);

                if (urlImagenPerfil != null && !urlImagenPerfil.isEmpty()) {
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

            if (timestamp < 1000000000000L) {
                timestamp *= 1000;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));

        } catch (NumberFormatException e) {
            Log.e("FECHA_FORMAT", "Error al parsear timestamp: " + timestampString, e);
            return "N/A";
        }
    }
}