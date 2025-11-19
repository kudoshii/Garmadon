package com.example.garmadon;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CuentaActivity extends AppCompatActivity {
    // Vistas para los datos
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
    // Elementos de Navegación Inferior
    private BottomNavigationView bottomNav;
    private FloatingActionButton fabPublicar;
    // SEMANA 8: Declaración de Firebase Auth
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta);
        // SEMANA 8: Inicialización de Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // 1. Configurar la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_cuenta);
        setSupportActionBar(toolbar);
        // 2. Inicializar Vistas de Información
        inicializarVistasInformacion();
        // 3. Cargar Datos Estáticos
        cargarDatosPerfil();
        // 4. Inicializar y Configurar Listeners para los Botones
        inicializarBotonesOpciones();
        configurarListenersBotones();
        //Configuración de la Cabecera
        configurarCabecera();
    }
    private void inicializarVistasInformacion() {
        // Enlazar los IDs definidos en activity_cuenta.xml
        tvValorNombres = findViewById(R.id.tv_valor_nombres);
        tvValorEmail = findViewById(R.id.tv_valor_email);
        tvValorMiembro = findViewById(R.id.tv_valor_miembro);
        tvValorTelefono = findViewById(R.id.tv_valor_telefono);
        tvValorEstado = findViewById(R.id.tv_valor_estado);
    }
    private void cargarDatosPerfil() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            tvValorEstado.setText("No autenticado");
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nombres = snapshot.child("nombre").getValue(String.class);
                // Los demás campos coinciden: 'email', 'tiempo', 'telefono', 'codigoTelefono'
                String email = snapshot.child("email").getValue(String.class);
                String tiempoStr = snapshot.child("tiempo").getValue(String.class);
                String telefono = snapshot.child("telefono").getValue(String.class);
                String codTelefono = snapshot.child("codigoTelefono").getValue(String.class);
                // Manejo de nulos (se mantiene)
                nombres = nombres != null ? nombres : "No disponible";
                email = email != null ? email : "No disponible";
                telefono = telefono != null ? telefono : "No disponible";
                codTelefono = codTelefono != null ? codTelefono : "";
                String estado = "Verificado";
                // Manejo de la variable 'tiempo'
                if (tiempoStr == null || tiempoStr.equals("null") || tiempoStr.isEmpty()) {
                    tiempoStr = "0";
                }
                long tiempoLong;
                try {
                    tiempoLong = Long.parseLong(tiempoStr);
                } catch (NumberFormatException e) {
                    tiempoLong = 0L;
                }
                //String fechaMiembro = Constantes.obtenerFecha(tiempoLong);
                String cod_tel_completo = codTelefono + telefono;
                if (cod_tel_completo.isEmpty()) {
                    cod_tel_completo = "No disponible";
                }
                // Establecer los valores en los TextViews
                tvValorNombres.setText(nombres);
                tvValorEmail.setText(email);
                //tvValorMiembro.setText(fechaMiembro);
                tvValorTelefono.setText(cod_tel_completo);
                tvValorEstado.setText(estado);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejo de errores
                tvValorNombres.setText("Error al cargar datos.");
                tvValorEstado.setText("Error");
            }
        });
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
            // Lógica para mostrar AlertDialog
        });
        btnCerrarSesion.setOnClickListener(v -> {
            cerrarSesion();
        });
    }
    private void configurarCabecera() {
        // Listener para el botón de retroceso (flecha <- )
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }
    // SEMANA 8: INICIO DE LÓGICA DE CIERRE DE SESIÓN
    private void cerrarSesion() {
        // 1. Cierra la sesión de Firebase
        mAuth.signOut();
        // 2. Muestra un mensaje al usuario
        Toast.makeText(this, "Sesión cerrada con éxito.", Toast.LENGTH_SHORT).show();
        // 3. Navega de vuelta a la pantalla de Login y limpia la pila de actividades
        Intent intent = new Intent(CuentaActivity.this, LoginActivity.class);
        // Estas flags aseguran que el usuario no pueda usar el botón de retroceso para volver
        // a la actividad anterior (Home o Cuenta) después de cerrar la sesión.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}