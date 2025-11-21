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

    // Vistas para los datos
    private ImageView imgPerfil;
    private TextView tvValorNombres;
    private TextView tvValorEmail;
    private TextView tvValorMiembro;
    private TextView tvValorTelefono;
    private TextView tvValorEstado;

    // Botones de Opciones
    private MaterialButton btnEditarPerfil;
    private MaterialButton btnCambiarPassword;
    private MaterialButton btnEliminarAnuncios;
    private MaterialButton btnCerrarSesion;

    // Elementos de Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private final int DEFAULT_PROFILE_IMAGE = R.drawable.perfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta);

        // SEMANA 8.1: Inicialización de Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // 1. Configurar la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_cuenta);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 2. Inicializar Vistas de Información
        inicializarVistasInformacion();

        // 3. Inicializar y Configurar Listeners para los Botones
        inicializarBotonesOpciones();
        configurarListenersBotones();
        configurarCabecera();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Cargar los datos y la foto cada vez que la actividad es visible
        if (currentUser != null) {
            cargarDatosPerfil();
        } else {
            // Manejar sesión no iniciada si fuera el caso
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // IMPORTANTE: Detener el listener de Firebase al salir de la Activity
        if (databaseReference != null && valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
            Log.d("CuentaActivity", "ValueEventListener de perfil removido.");
        }
    }

    private void inicializarVistasInformacion() {
        // Enlazar los IDs definidos en activity_cuenta.xml
        imgPerfil = findViewById(R.id.img_perfil); // <-- Inicialización de la ImageView
        tvValorNombres = findViewById(R.id.tv_valor_nombres);
        tvValorEmail = findViewById(R.id.tv_valor_email);
        tvValorMiembro = findViewById(R.id.tv_valor_miembro);
        tvValorTelefono = findViewById(R.id.tv_valor_telefono);
        tvValorEstado = findViewById(R.id.tv_valor_estado);
    }

    private void cargarDatosPerfil() {
        String uid = mAuth.getUid();

        if (uid == null) {
            tvValorEstado.setText("No autenticado");
            return;
        }

        // Referencia a la base de datos para escuchar cambios
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // El listener se adjunta al nodo del usuario
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String nombres = snapshot.child("nombre").getValue(String.class);
                String urlImagenPerfil = snapshot.child("urlImagenPerfil").getValue(String.class);
                String email = currentUser.getEmail();
                String tiempoStr = snapshot.child("tiempo").getValue(String.class);
                String telefono = snapshot.child("telefono").getValue(String.class);
                String codTelefono = snapshot.child("codigoTelefono").getValue(String.class);

                // Manejo de nulos y obtención de estado
                nombres = nombres != null ? nombres : "No disponible";
                email = email != null ? email : "No disponible";
                telefono = telefono != null ? telefono : "No disponible";
                codTelefono = codTelefono != null ? codTelefono : "";
                String estado = "Verificado";

                // Formateo de Teléfono
                String cod_tel_completo = "";
                if (!codTelefono.isEmpty()) {
                    cod_tel_completo += "+" + codTelefono + " ";
                }
                cod_tel_completo += telefono;
                if (cod_tel_completo.trim().isEmpty()) {
                    cod_tel_completo = "No disponible";
                }

                // Establecer los valores en los TextViews
                tvValorNombres.setText(nombres);
                tvValorEmail.setText(email);
                tvValorMiembro.setText(obtenerFecha(tiempoStr));
                tvValorTelefono.setText(cod_tel_completo);
                tvValorEstado.setText(estado);

                // SOLUCIÓN A LA FOTO DE PERFIL: Cargar la imagen usando Glide
                if (urlImagenPerfil != null && !urlImagenPerfil.isEmpty()) {
                    try {
                        // Carga la imagen desde la URL de Firebase Storage
                        Glide.with(CuentaActivity.this)
                                .load(urlImagenPerfil)
                                .placeholder(DEFAULT_PROFILE_IMAGE) // Se muestra mientras carga
                                .error(DEFAULT_PROFILE_IMAGE) // Se muestra si falla la carga
                                .into(imgPerfil);
                        Log.d("CuentaActivity", "Foto de perfil cargada correctamente desde Firebase URL.");

                    } catch (Exception e) {
                        Log.e("GLIDE_ERROR", "Error al cargar la imagen con Glide", e);
                        imgPerfil.setImageResource(DEFAULT_PROFILE_IMAGE);
                    }
                } else {
                    // Si la URL es nula o vacía, usamos la imagen predeterminada
                    imgPerfil.setImageResource(DEFAULT_PROFILE_IMAGE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejo de errores
                tvValorNombres.setText("Error al cargar datos.");
                tvValorEstado.setText("Error");
                Toast.makeText(CuentaActivity.this, "Fallo al leer la BD: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        // Adjuntamos el listener al nodo del usuario para que se actualice en tiempo real
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
            // Lógica para mostrar AlertDialog
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
        // 1. Cierra la sesión de Firebase
        mAuth.signOut();

        // 2. Muestra un mensaje al usuario
        Toast.makeText(this, "Sesión cerrada con éxito.", Toast.LENGTH_SHORT).show();

        // 3. Navega de vuelta a la pantalla de Login y limpia la pila de actividades
        Intent intent = new Intent(CuentaActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private String obtenerFecha(String timestampString) {
        if (timestampString == null || timestampString.isEmpty() || timestampString.equals("null"))
        {
        }
        return "N/A";
    }
    try {
        long timestamp = Long.parseLong(timestampString);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    } catch (NumberFormatException e) {
        Log.e("FECHA_FORMAT", "Error al parsear timestamp: " + timestampString, e);
        return "N/A";
    }
}