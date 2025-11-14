package com.example.garmadon;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

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
        //Se cargan los datos del usuario (Estos valores son estáticos por ahora)
        tvValorNombres.setText("Prueba Prueba Prueba Prueba");
        tvValorEmail.setText("Prueba@gmail.com");
        tvValorMiembro.setText("16/10/2023");
        tvValorTelefono.setText("No disponible");
        tvValorEstado.setText("Verificado");
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