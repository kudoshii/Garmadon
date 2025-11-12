package com.example.garmadon;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Calendar;

public class EditarPerfilActivity extends AppCompatActivity {

    // Vistas del Layout
    private Toolbar toolbar;
    private FloatingActionButton fabCambiarImagen;
    private TextInputEditText etNombres;
    private TextInputEditText etFechaNac;
    private TextInputEditText etTelefono;
    private MaterialButton btnActualizar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        inicializarVistas();
        configurarToolbar();
        configurarListeners();
    }

    private void inicializarVistas() {
        toolbar = findViewById(R.id.toolbar_editar_perfil);
        fabCambiarImagen = findViewById(R.id.fab_cambiar_imagen);
        etNombres = findViewById(R.id.et_nombres);
        etFechaNac = findViewById(R.id.et_fecha_nac);
        etTelefono = findViewById(R.id.et_telefono);
        btnActualizar = findViewById(R.id.btn_actualizar);

        // Opcional: Cargar datos actuales del usuario (simulación)
        etNombres.setText("DiegoDev");
        etFechaNac.setText("13/11/1999");
        etTelefono.setText("982254141");
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Listener para el botón de retroceso de la Toolbar
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void configurarListeners() {
        // 1. Listener para abrir el selector de fecha al tocar el campo de Fecha de Nacimiento
        etFechaNac.setOnClickListener(v -> mostrarDatePickerDialog());

        // 2. Listener para el FloatingActionButton (Cambiar Imagen)
        fabCambiarImagen.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo selector de imágenes...", Toast.LENGTH_SHORT).show();
            // Lógica : Abrir intent para seleccionar imagen de la galería/cámara
        });

        // 3. Listener para el botón de Actualizar
        btnActualizar.setOnClickListener(v -> {
            guardarCambios();
        });
    }


    private void mostrarDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int anio = c.get(Calendar.YEAR);
        int mes = c.get(Calendar.MONTH);
        int dia = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    // Formatea la fecha seleccionada y la establece en el EditText
                    String fechaSeleccionada = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                    etFechaNac.setText(fechaSeleccionada);
                }, anio, mes, dia);
        datePickerDialog.show();
    }

    private void guardarCambios() {
        // Obtener valores
        String nombres = etNombres.getText().toString().trim();
        String fechaNac = etFechaNac.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        //PASO 1: Validación de campos
        if (nombres.isEmpty() || fechaNac.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_LONG).show();
            // Implementar lógica visual para indicar campos vacíos
            return;
        }

        //PASO 2: Procesamiento/Guardado de datos
        // Lógica: Llamada a la base de datos (Firestore/SQL) o API para actualizar el perfil

        Toast.makeText(this, "Perfil de " + nombres + " actualizado exitosamente!", Toast.LENGTH_LONG).show();

        // finish();
    }
}
