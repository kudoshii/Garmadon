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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta);

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
            Toast.makeText(this, "Mostrar diálogo de confirmación para eliminar anuncios", Toast.LENGTH_SHORT).show();
            // Lógica para mostrar AlertDialog
        });

        btnCerrarSesion.setOnClickListener(v -> {
            Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();
            // Lógica para cerrar sesión (Firebase/SharedPreferences) y navegar a LoginActivity
        });

    }
    private void configurarCabecera() {
        // Listener para el botón de retroceso (flecha <- )
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }
}
