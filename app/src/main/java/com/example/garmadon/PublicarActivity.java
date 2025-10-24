package com.example.garmadon;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // Necesario para Log.e()
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;

public class PublicarActivity extends AppCompatActivity {

    // Constante para el log
    private static final String TAG = "PublicarActivity";

    // Vistas
    private ImageView ivImagen;
    private Button btnSeleccionarImagen;
    private TextInputEditText etNombre;
    private TextInputEditText etPrecio;
    // INICIO MODIFICACIÓN: Declaración del nuevo campo de Dirección
    private TextInputEditText etDireccion;
    // FIN MODIFICACIÓN
    private TextInputEditText etDescripcion;
    private Button btnPublicar;

    private Uri imagenUriSeleccionada;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

                // INICIO MODIFICACIÓN SEMANA 4: Manejo de Excepciones (try-catch)
                try {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        // Obtener la URI
                        imagenUriSeleccionada = result.getData().getData();

                        if (imagenUriSeleccionada != null) {
                            // Muestra la imagen en el ImageView
                            ivImagen.setImageURI(imagenUriSeleccionada);
                            Toast.makeText(this, "Imagen seleccionada correctamente.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Caso: data no es nula, pero getData().getData() retorna nulo inesperadamente
                            throw new NullPointerException("La URI de la imagen seleccionada es nula.");
                        }
                    } else if (result.getResultCode() == RESULT_CANCELED) {
                        Toast.makeText(this, "Selección de imagen cancelada.",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    // Bloque CATCH: Maneja cualquier excepción, como permisos o archivos corruptos.
                    Log.e(TAG, "Error crítico al seleccionar o procesar la imagen: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al cargar la imagen. Por favor, inténtelo de nuevo.",
                            Toast.LENGTH_LONG).show();
                    imagenUriSeleccionada = null; // Reinicia la URI para forzar al usuario a reintentar
                }
                // FIN MODIFICACIÓN SEMANA 4: Manejo de Excepciones (try-catch)
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Usamos el layout de publicación
        setContentView(R.layout.activity_publicar);

        // 1. Inicializar Vistas
        // Asegúrate de que activity_publicar.xml contenga estos IDs
        ivImagen = findViewById(R.id.iv_publicar_imagen);
        btnSeleccionarImagen = findViewById(R.id.btn_seleccionar_imagen);
        etNombre = findViewById(R.id.et_publicar_nombre);
        etPrecio = findViewById(R.id.et_publicar_precio);
        // INICIO MODIFICACIÓN: Inicialización del nuevo campo de Dirección
        etDireccion = findViewById(R.id.et_publicar_direccion);
        // FIN MODIFICACIÓN
        etDescripcion = findViewById(R.id.et_publicar_descripcion);
        btnPublicar = findViewById(R.id.btn_publicar_producto);

        // 2. Manejar el clic para seleccionar imagen
        // INICIO MODIFICACIÓN SEMANA 4: try-catch en la llamada de la galería (mínimo, pero útil)
        btnSeleccionarImagen.setOnClickListener(v -> {
            try {
                abrirGaleria();
            } catch (Exception e) {
                Log.e(TAG, "Error al intentar abrir la galería: " + e.getMessage());
                Toast.makeText(this, "No se pudo acceder a la galería.", Toast.LENGTH_SHORT).show();
            }
        });
        // FIN MODIFICACIÓN SEMANA 4

        // 3. Manejar el clic del botón Publicar (Validación)
        btnPublicar.setOnClickListener(v -> {
            if (validarCampos()) {
                simularPublicacion();
            }
        });
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    // SEMANA 3/4: La función de validación ahora incluye el campo de dirección
    private boolean validarCampos() {
        String nombre = etNombre.getText().toString().trim();
        String precio = etPrecio.getText().toString().trim();
        // INICIO MODIFICACIÓN: Obtener Dirección
        String direccion = etDireccion.getText().toString().trim();
        // FIN MODIFICACIÓN
        String descripcion = etDescripcion.getText().toString().trim();

        // 1. Validar Imagen Seleccionada
        if (imagenUriSeleccionada == null) {
            Toast.makeText(this, "Debes seleccionar una imagen para el producto.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // 2. Validar Nombre
        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("El nombre del producto es obligatorio.");
            return false;
        }

        // 3. Validar Precio
        if (TextUtils.isEmpty(precio) || !precio.matches("^[0-9]+([.,][0-9]{1,2})?$")) {
            etPrecio.setError("Ingresa un precio válido (solo números y opcionalmente dos decimales).");
            return false;
        }

        // INICIO MODIFICACIÓN SEMANA 3: Validar Dirección (campo obligatorio)
        if (TextUtils.isEmpty(direccion) || direccion.length() < 5) {
            etDireccion.setError("La dirección es obligatoria y debe ser detallada.");
            return false;
        }
        // FIN MODIFICACIÓN

        // 4. Validar Descripción
        if (TextUtils.isEmpty(descripcion) || descripcion.length() < 10) {
            etDescripcion.setError("La descripción es obligatoria y debe tener al menos 10 caracteres.");
            return false;
        }

        return true;
    }


    private void simularPublicacion() {
        String nombre = etNombre.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim(); // Usar la dirección en la simulación

        // Lógica de publicación real (Firebase) iría aquí, en las Semanas 6 y 7.
        Toast.makeText(this, "Producto '" + nombre + "' listo. Retiro en: " + direccion,
                Toast.LENGTH_LONG).show();

        // Volver a Home
        finish();
    }
}