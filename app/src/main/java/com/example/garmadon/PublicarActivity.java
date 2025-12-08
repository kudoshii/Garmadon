package com.example.garmadon;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
// Asegúrate de que Constantes y GaleriaImagenAdapter sean importables
import com.example.garmadon.GaleriaImagenAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PublicarActivity extends AppCompatActivity implements
        GaleriaImagenAdapter.OnImageInteractionListener {
    private static final String TAG = "PublicarActivity";
    private static final int MAX_IMAGES = 10;

    // Variables de Firebase
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private DatabaseReference databaseRef;

    // Vistas
    private ImageView ivPublicarImagenPlaceholder;
    private RecyclerView rvGaleriaImagenes;
    private TextInputEditText etNombre;
    private TextInputEditText etPrecio;
    private TextInputEditText etDireccion;
    private TextInputEditText etDescripcion;
    private TextInputEditText etMarca;
    private AutoCompleteTextView etCategoria;
    private AutoCompleteTextView etCondicion;
    private Button btnPublicar;

    // Adaptador y lista para la galería de imágenes
    private GaleriaImagenAdapter galeriaImagenAdapter;
    private List<Uri> listaImagenesUri;

    // ActivityResultLauncher para seleccionar múltiples imágenes (con manejo robusto de permisos)
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                try {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        boolean imagesAdded = false;
                        int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;

                        // Si se seleccionaron múltiples imágenes (ClipData)
                        if (result.getData().getClipData() != null) {
                            int count = result.getData().getClipData().getItemCount();
                            Log.d(TAG, "Resultado OK: ClipData detectado con " + count + " imágenes.");

                            for (int i = 0; i < count; i++) {
                                if (listaImagenesUri.size() < MAX_IMAGES) {
                                    Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                                    try {
                                        getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                                    } catch (SecurityException e) {
                                        Log.w(TAG, "No se pudo obtener permiso persistente para: " + imageUri);
                                    }
                                    listaImagenesUri.add(imageUri);
                                    imagesAdded = true;
                                } else {
                                    Toast.makeText(this, "Máximo " + MAX_IMAGES + " imágenes permitidas.",
                                            Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                        }
                        // Si se seleccionó una sola imagen (Data)
                        else if (result.getData().getData() != null) {
                            Log.d(TAG, "Resultado OK: Data única detectada.");

                            if (listaImagenesUri.size() < MAX_IMAGES) {
                                Uri imageUri = result.getData().getData();
                                try {
                                    getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                                } catch (SecurityException e) {
                                    Log.w(TAG, "No se pudo obtener permiso persistente para: " + imageUri);
                                }
                                listaImagenesUri.add(imageUri);
                                imagesAdded = true;
                            } else {
                                Toast.makeText(this, "Máximo " + MAX_IMAGES + " imágenes permitidas.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        if (imagesAdded) {
                            galeriaImagenAdapter.notifyDataSetChanged();
                            actualizarPlaceholderImagenPrincipal();
                            Log.d(TAG, "Actualización de UI realizada. Total de imágenes: " +
                                    listaImagenesUri.size());
                        }
                    } else if (result.getResultCode() == RESULT_CANCELED) {
                        Toast.makeText(this, "Selección de imagen cancelada.",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error crítico al seleccionar o procesar la imagen: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al cargar la imagen. Por favor, inténtelo de nuevo.",
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publicar);

        // 1. Inicializar Vistas
        ivPublicarImagenPlaceholder = findViewById(R.id.iv_publicar_imagen_placeholder);
        rvGaleriaImagenes = findViewById(R.id.rv_galeria_imagenes);
        etNombre = findViewById(R.id.et_publicar_nombre);
        etPrecio = findViewById(R.id.et_publicar_precio);
        etDireccion = findViewById(R.id.et_publicar_direccion);
        etDescripcion = findViewById(R.id.et_publicar_descripcion);
        etMarca = findViewById(R.id.et_publicar_marca);
        etCategoria = findViewById(R.id.spinner_categoria);
        etCondicion = findViewById(R.id.spinner_condicion);
        btnPublicar = findViewById(R.id.btn_publicar_producto);

        // 2. Inicialización de Firebase
        storageRef = FirebaseStorage.getInstance().getReference().child("productos_imagenes");
        databaseRef = FirebaseDatabase.getInstance().getReference("productos");
        mAuth = FirebaseAuth.getInstance();

        // 3. Configuración del RecyclerView de la galería
        listaImagenesUri = new ArrayList<>();
        galeriaImagenAdapter = new GaleriaImagenAdapter(this, listaImagenesUri, this);
        rvGaleriaImagenes.setLayoutManager(new GridLayoutManager(this, 4));
        rvGaleriaImagenes.setAdapter(galeriaImagenAdapter);

        // 3.5 Configuración de Categoría y Condición
        // Configuración de Categoría: Usa Constantes.categorias
        ArrayAdapter<String> adaptadorCat = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, Constantes.categorias);
        etCategoria.setAdapter(adaptadorCat);
        etCategoria.setOnClickListener(v -> etCategoria.showDropDown());

        // Configuración de Condición: Usa Constantes.condiciones
        ArrayAdapter<String> adaptadorCon = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, Constantes.condiciones);
        etCondicion.setAdapter(adaptadorCon);
        etCondicion.setOnClickListener(v -> etCondicion.showDropDown());

        // 4. Manejar el clic para seleccionar imagen
        ivPublicarImagenPlaceholder.setOnClickListener(v -> abrirGaleria());

        // 5. Manejar el clic del botón Publicar
        btnPublicar.setOnClickListener(v -> {
            if (validarCampos()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    publicarProducto(user.getUid());
                } else {
                    Toast.makeText(this, "Debe iniciar sesión para publicar un producto.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        actualizarPlaceholderImagenPrincipal();
    }

    // --- Métodos de Imagen y UI ---

    @Override
    public void onRemoveImage(int position) {
        if (!listaImagenesUri.isEmpty() && position < listaImagenesUri.size()) {
            Uri uriToRemove = listaImagenesUri.get(position);

            // Intentar liberar el permiso persistente si existe (Manejo robusto)
            try {
                getContentResolver().releasePersistableUriPermission(
                        uriToRemove,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
                Log.d(TAG, "Permiso persistente liberado para: " + uriToRemove);
            } catch (SecurityException e) {
                Log.w(TAG, "No se pudo liberar el permiso (puede que no existiera): " + e.getMessage());
            }

            listaImagenesUri.remove(position);
            galeriaImagenAdapter.notifyItemRemoved(position);
            galeriaImagenAdapter.notifyItemRangeChanged(position, listaImagenesUri.size());
            actualizarPlaceholderImagenPrincipal();
            Toast.makeText(this, "Imagen eliminada.", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirGaleria() {
        if (listaImagenesUri.size() >= MAX_IMAGES) {
            Toast.makeText(this, "Ya tienes el máximo de " + MAX_IMAGES + " imágenes seleccionadas.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        imagePickerLauncher.launch(intent);
    }

    private void actualizarPlaceholderImagenPrincipal() {
        // Asume que R.drawable.agregar_img existe
        ivPublicarImagenPlaceholder.setImageResource(R.drawable.agregar_img);
        ivPublicarImagenPlaceholder.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    @Override
    public void onSelectMainImage(int position) {
        if (!listaImagenesUri.isEmpty() && position < listaImagenesUri.size()) {
            Uri selectedUri = listaImagenesUri.remove(position);
            listaImagenesUri.add(0, selectedUri);
            galeriaImagenAdapter.notifyDataSetChanged();
            actualizarPlaceholderImagenPrincipal();
            Toast.makeText(this, "Imagen establecida como principal.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // --- Validación de Campos ---

    private boolean validarCampos() {
        String nombre = etNombre.getText().toString().trim();
        String precio = etPrecio.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String categoria = etCategoria.getText().toString().trim();
        String condicion = etCondicion.getText().toString().trim();
        String marca = etMarca.getText().toString().trim();

        if (listaImagenesUri.isEmpty()) {
            Toast.makeText(this, "Debes seleccionar al menos una imagen para el producto.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(nombre)) { etNombre.setError("El nombre del producto es obligatorio."); return false; }
        if (TextUtils.isEmpty(marca)) { etMarca.setError("La marca es obligatoria."); return false; }
        if (TextUtils.isEmpty(categoria)) { etCategoria.setError("La categoría es obligatoria."); return false; }
        if (TextUtils.isEmpty(condicion)) { etCondicion.setError("La condición es obligatoria."); return false; }

        // Validar Precio: Debe ser no vacío y coincidir con el formato numérico (entero o decimal con 1-2 cifras, usando . o ,)
        if (TextUtils.isEmpty(precio) || !precio.matches("^[0-9]+([.,][0-9]{1,2})?$")) {
            etPrecio.setError("Ingresa un precio válido (ej: 10000 o 10.50)."); return false;
        }

        if (TextUtils.isEmpty(direccion) || direccion.length() < 5) { etDireccion.setError("La dirección es obligatoria y debe ser detallada."); return false; }
        if (TextUtils.isEmpty(descripcion) || descripcion.length() < 10) { etDescripcion.setError("La descripción es obligatoria y debe tener al menos 10 caracteres."); return false; }

        return true;
    }

    // --- Lógica de Publicación Firebase (Optimizado) ---

    private void publicarProducto(String currentUserId) {
        btnPublicar.setEnabled(false);

        final int totalImagenes = listaImagenesUri.size();
        Toast.makeText(this, "Iniciando publicación. Subiendo " + totalImagenes + " imágenes...",
                Toast.LENGTH_LONG).show();

        final List<String> imageUrls = new ArrayList<>();
        // Contadores atómicos para el manejo asíncrono
        final AtomicInteger imagesProcessed = new AtomicInteger(0);
        final AtomicInteger imagesUploaded = new AtomicInteger(0);
        final AtomicInteger imagesFailed = new AtomicInteger(0);

        Log.d(TAG, "Iniciando subida de imágenes para Vendedor ID: " + currentUserId);

        if (listaImagenesUri.isEmpty()) {
            Toast.makeText(this, "Error: Debes seleccionar al menos una imagen", Toast.LENGTH_SHORT).show();
            btnPublicar.setEnabled(true);
            return;
        }

        for (int i = 0; i < totalImagenes; i++) {
            Uri imageUri = listaImagenesUri.get(i);

            if (imageUri == null) {
                Log.e(TAG, "URI nula en posición " + i);
                if (imagesProcessed.incrementAndGet() == totalImagenes) {
                    finalizarPublicacion(imageUrls, imagesUploaded.get(), imagesFailed.get(), totalImagenes, currentUserId);
                }
                continue;
            }

            final int index = i;
            // Generar nombre de archivo único
            final StorageReference fileRef = storageRef.child(
                    Constantes.obtenerTiempoDis() + "_" + i + "_" + imageUri.getLastPathSegment()
            );

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            synchronized (imageUrls) {
                                imageUrls.add(uri.toString());
                                imagesUploaded.incrementAndGet();
                            }
                            Log.d(TAG, "Imagen " + (index + 1) + " subida exitosamente");

                            if (imagesProcessed.incrementAndGet() == totalImagenes) {
                                finalizarPublicacion(imageUrls, imagesUploaded.get(), imagesFailed.get(), totalImagenes, currentUserId);
                            }

                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Error al obtener URL de descarga para imagen " + index + ": " + e.getMessage());
                            imagesFailed.incrementAndGet();

                            if (imagesProcessed.incrementAndGet() == totalImagenes) {
                                finalizarPublicacion(imageUrls, imagesUploaded.get(), imagesFailed.get(), totalImagenes, currentUserId);
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al subir imagen " + index + ": " + e.getMessage());
                        imagesFailed.incrementAndGet();

                        if (imagesProcessed.incrementAndGet() == totalImagenes) {
                            finalizarPublicacion(imageUrls, imagesUploaded.get(), imagesFailed.get(), totalImagenes, currentUserId);
                        }
                    });
        }
    }

    private void finalizarPublicacion(List<String> imageUrls, int exitosas, int fallidas,
                                      int total, String currentUserId) {
        Log.d(TAG, "Finalización: " + exitosas + " exitosas, " + fallidas + " fallidas de " + total);

        if (imageUrls.isEmpty()) {
            Toast.makeText(this,
                    "Error: No se pudo subir ninguna imagen. Verifique su conexión e intente nuevamente.",
                    Toast.LENGTH_LONG).show();
            btnPublicar.setEnabled(true);
            return;
        }

        if (fallidas > 0) {
            Toast.makeText(this,
                    "Advertencia: " + fallidas + " imagen(es) no se pudieron subir. Continuando con "
                            + exitosas + " imagen(es)...",
                    Toast.LENGTH_LONG).show();
        }

        guardarDatosProducto(imageUrls, currentUserId);
    }

    private void guardarDatosProducto(List<String> imageUrls, String userId) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            Log.e(TAG, "Error: imageUrls está vacía al intentar guardar");
            Toast.makeText(this, "Error: No hay imágenes para publicar", Toast.LENGTH_SHORT).show();
            btnPublicar.setEnabled(true);
            return;
        }

        String nombre = etNombre.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String categoria = etCategoria.getText().toString().trim();
        String condicion = etCondicion.getText().toString().trim();
        String marca = etMarca.getText().toString().trim();

        // Revalidación de campos de texto (aunque validarCampos() ya lo hizo, es una capa extra)
        if (nombre.isEmpty() || precioStr.isEmpty() || direccion.isEmpty() || descripcion.isEmpty() ||
                categoria.isEmpty() || condicion.isEmpty() || marca.isEmpty()) {
            Toast.makeText(this, "Error: Todos los campos son obligatorios y deben estar validados.",
                    Toast.LENGTH_LONG).show();
            btnPublicar.setEnabled(true);
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr.replace(",", "."));
            if (precio <= 0 || precio > 999999999.99) {
                Toast.makeText(this, "Error: Precio fuera de rango válido", Toast.LENGTH_SHORT).show();
                btnPublicar.setEnabled(true);
                return;
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error de formato de precio al parsear: " + precioStr, e);
            Toast.makeText(this, "Error: Formato de precio inválido", Toast.LENGTH_SHORT).show();
            btnPublicar.setEnabled(true);
            return;
        }

        // Sanitización ligera (remover espacios múltiples)
        nombre = nombre.replaceAll("\\s+", " ").trim();
        descripcion = descripcion.replaceAll("\\s+", " ").trim();
        direccion = direccion.replaceAll("\\s+", " ").trim();
        marca = marca.replaceAll("\\s+", " ").trim();

        // Creación del objeto Producto
        Map<String, Object> producto = new HashMap<>();
        producto.put("nombre", nombre);
        producto.put("precio", precio);
        producto.put("direccion", direccion);
        producto.put("descripcion", descripcion);
        producto.put("categoria", categoria);
        producto.put("condicion", condicion);
        producto.put("marca", marca);
        producto.put("imageUrls", imageUrls);
        producto.put("fechaPublicacion", Constantes.obtenerTiempoDis());
        producto.put("vendedorId", userId);
        producto.put("estado", Constantes.anuncio_disponible);

        Log.d(TAG, "Guardando producto con vendedorId: " + userId);

        // Guardar en Realtime Database
        databaseRef.push().setValue(producto)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Producto publicado con éxito en Realtime Database. Vendedor ID: " + userId);
                    Toast.makeText(this, "¡Producto publicado exitosamente!", Toast.LENGTH_LONG).show();

                    // Navegar a Home.class y limpiar la pila
                    Intent intent = new Intent(PublicarActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar datos en Realtime Database: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al guardar datos del producto en la base de datos.", Toast.LENGTH_LONG).show();
                    btnPublicar.setEnabled(true);
                });
    }
}