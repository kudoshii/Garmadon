package com.example.garmadon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.example.garmadon.model.Producto;
import com.example.garmadon.adapter.ImageCarouselAdapter;
import com.google.android.material.button.MaterialButton;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetalleProductoActivity extends AppCompatActivity {
    public static final String EXTRA_PRODUCTO_ID = "producto_id";
    private static final String TAG = "Detalle_Producto";

    // Vistas del Producto
    private ViewPager2 viewPagerImages;
    private DotsIndicator dotsIndicator;
    private TextView tvNombreDetalle;
    private TextView tvPrecioDetalle;
    private TextView tvDescripcionDetalle;
    private TextView tvMarcaDetalle;
    private TextView tvCategoriaDetalle;
    private TextView tvCondicionDetalle;
    private TextView tvDireccionDetalle;

    // Vistas del Vendedor
    private TextView tvVendedorNombre;
    private TextView tvVendedorEmail;
    private TextView tvVendedorTelefono;
    private MaterialButton fabContactar;
    private MaterialButton btnChat;

    // Firebase
    private DatabaseReference productosRef;
    private DatabaseReference usuariosRef;

    private String telefonoVendedor = null;
    private Producto productoActual = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_producto);

        setupToolbar();

        productosRef = FirebaseDatabase.getInstance().getReference("productos");
        usuariosRef = FirebaseDatabase.getInstance().getReference("users");

        inicializarVistas();

        String productoId = getIntent().getStringExtra(EXTRA_PRODUCTO_ID);
        if (productoId != null) {
            Log.d(TAG, "Cargando detalles para ID: " + productoId);
            cargarDatosProducto(productoId);
        } else {
            Toast.makeText(this, "Error: No se encontró el ID del producto.",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_detalle);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle del Producto");
        }
    }

    private void inicializarVistas() {
        viewPagerImages = findViewById(R.id.viewpager_images);
        dotsIndicator = findViewById(R.id.dots_indicator);
        tvNombreDetalle = findViewById(R.id.tv_detalle_titulo);
        tvPrecioDetalle = findViewById(R.id.tv_detalle_precio);
        tvDescripcionDetalle = findViewById(R.id.tv_detalle_descripcion);
        tvMarcaDetalle = findViewById(R.id.tv_detalle_marca);
        tvCategoriaDetalle = findViewById(R.id.tv_detalle_categoria);
        tvCondicionDetalle = findViewById(R.id.tv_detalle_condicion);
        tvDireccionDetalle = findViewById(R.id.tv_detalle_direccion);
        tvVendedorNombre = findViewById(R.id.tv_detalle_vendedor);
        tvVendedorEmail = findViewById(R.id.tv_vendedor_email);
        tvVendedorTelefono = findViewById(R.id.tv_vendedor_telefono);
        fabContactar = findViewById(R.id.fab_contactar);
        btnChat = findViewById(R.id.btn_chat);

        tvVendedorNombre.setText("Nombre: Cargando detalles...");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Carga los datos del producto y del vendedor desde Firebase.
     * Implementa validación robusta y manejo de errores.
     */
    private void cargarDatosProducto(String productoId) {
        findViewById(R.id.card_info).setVisibility(View.INVISIBLE); // Ocultar UI mientras carga

        productosRef.child(productoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(DetalleProductoActivity.this,
                            "Producto no encontrado o eliminado", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                Producto producto = null;
                try {
                    producto = snapshot.getValue(Producto.class);
                } catch (Exception e) {
                    Log.e(TAG, "Error al mapear Producto con model: " + e.getMessage(), e);
                    // Si falla el mapeo, intentar cargar campo por campo (fallback)
                }

                if (producto == null) {
                    Log.w(TAG, "Mapeo a Producto fallido o nulo. Intentando con datos individuales.");
                    mostrarDetallesProductoFallback(snapshot);
                } else {
                    producto.setId(snapshot.getKey());
                    productoActual = producto;

                    if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
                        Log.e(TAG, "Error: Datos del producto críticos incompletos");
                        Toast.makeText(DetalleProductoActivity.this,
                                "Error: Datos del producto incompletos", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    mostrarDetallesProducto(producto);

                    String vendedorId = producto.getVendedorId();
                    if (vendedorId != null && !vendedorId.trim().isEmpty()) {
                        cargarDatosVendedor(vendedorId);
                    } else {
                        Log.e(TAG, "Producto sin VendedorId válido");
                        mostrarDetallesVendedor("Vendedor Desconocido", "N/A", null);
                        deshabilitarBotonesContacto();
                    }
                }

                // Mostrar la UI
                findViewById(R.id.card_info).setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al leer producto: " + error.getMessage());
                Toast.makeText(DetalleProductoActivity.this,
                        "Error de conexión: " + error.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    /**
     * Muestra detalles del producto usando el objeto Producto (Método robusto)
     */
    private void mostrarDetallesProducto(Producto producto) {
        // --- 1. Carrusel de Imágenes ---
        if (producto.getImageUrls() != null && !producto.getImageUrls().isEmpty()) {
            List<String> imagenesValidas = new ArrayList<>();
            for (String url : producto.getImageUrls()) {
                if (url != null && !url.trim().isEmpty() && url.startsWith("http")) {
                    imagenesValidas.add(url);
                }
            }
            if (!imagenesValidas.isEmpty()) {
                configurarCarousel(imagenesValidas);
            } else {
                Log.w(TAG, "No hay URLs de imágenes válidas");
            }
        }

        // --- 2. Información General ---
        tvNombreDetalle.setText(producto.getNombre() != null ?
                producto.getNombre() : "Sin nombre");

        tvDescripcionDetalle.setText(producto.getDescripcion() != null &&
                !producto.getDescripcion().trim().isEmpty() ?
                producto.getDescripcion() : "Sin descripción disponible");

        // --- 3. Precio ---
        try {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
            double precio = producto.getPrecio();
            tvPrecioDetalle.setText(currencyFormat.format(precio));
        } catch (Exception e) {
            Log.e(TAG, "Error al formatear precio: " + e.getMessage());
            tvPrecioDetalle.setText("Precio: N/A");
        }

        // --- 4. Atributos ---
        tvMarcaDetalle.setText("Marca: " + getSafeString(producto.getMarca(), "No especificada"));
        tvCondicionDetalle.setText("Condición: " + getSafeString(producto.getCondicion(), "N/A"));
        tvCategoriaDetalle.setText("Categoría: " + getSafeString(producto.getCategoria(), "N/A"));
        tvDireccionDetalle.setText("Retiro en: " + getSafeString(producto.getDireccion(), "No provista"));
    }

    /**
     * Muestra detalles del producto leyendo campos individualmente (Método Fallback)
     */
    private void mostrarDetallesProductoFallback(@NonNull DataSnapshot snapshot) {
        String nombre = snapshot.child("nombre").getValue(String.class);
        Double precioDouble = snapshot.child("precio").getValue(Double.class);
        String descripcion = snapshot.child("descripcion").getValue(String.class);
        String marca = snapshot.child("marca").getValue(String.class);
        String condicion = snapshot.child("condicion").getValue(String.class);
        String categoria = snapshot.child("categoria").getValue(String.class);
        String direccion = snapshot.child("direccion").getValue(String.class);
        String vendedorId = snapshot.child("vendedorId").getValue(String.class);

        // Intentar construir un objeto Producto si es posible para tener productoActual
        if (nombre != null && vendedorId != null) {
            productoActual = new Producto();
            productoActual.setId(snapshot.getKey());
            productoActual.setNombre(nombre);
            productoActual.setVendedorId(vendedorId);
            // Otros campos podrían ser nulos, pero los críticos están presentes.
        }

        tvNombreDetalle.setText(getSafeString(nombre, "N/A"));
        tvDescripcionDetalle.setText(getSafeString(descripcion, "Sin descripción"));

        if (precioDouble != null) {
            try {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
                tvPrecioDetalle.setText(currencyFormat.format(precioDouble));
            } catch (Exception e) {
                tvPrecioDetalle.setText("Precio: N/A");
            }
        } else {
            tvPrecioDetalle.setText("Precio: N/A");
        }

        tvMarcaDetalle.setText("Marca: " + getSafeString(marca, "No especificada"));
        tvCondicionDetalle.setText("Condición: " + getSafeString(condicion, "N/A"));
        tvCategoriaDetalle.setText("Categoría: " + getSafeString(categoria, "N/A"));
        tvDireccionDetalle.setText("Retiro en: " + getSafeString(direccion, "No provista"));

        // Cargar imágenes
        List<String> imageUrls = new ArrayList<>();
        DataSnapshot imageUrlsSnapshot = snapshot.child("imageUrls");
        if (imageUrlsSnapshot.exists()) {
            for (DataSnapshot imageSnapshot : imageUrlsSnapshot.getChildren()) {
                String imageUrl = imageSnapshot.getValue(String.class);
                if (imageUrl != null) {
                    imageUrls.add(imageUrl);
                }
            }
        }

        if (!imageUrls.isEmpty()) {
            configurarCarousel(imageUrls);
        }

        if (vendedorId != null) {
            cargarDatosVendedor(vendedorId);
        } else {
            mostrarDetallesVendedor("Vendedor Desconocido", "N/A", null);
            deshabilitarBotonesContacto();
        }
    }

    /**
     * Carga y muestra los datos del vendedor.
     */
    private void cargarDatosVendedor(String vendedorId) {
        usuariosRef.child(vendedorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.w(TAG, "Usuario no encontrado: " + vendedorId);
                    mostrarDetallesVendedor("Usuario No Encontrado", "No disponible", null);
                    deshabilitarBotonesContacto();
                    return;
                }

                String nombre = snapshot.child("nombre").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String codigo = snapshot.child("codigoTelefono").getValue(String.class);

                // Manejar telefono como String o Long/Number
                String numero = null;
                Object telefonoObj = snapshot.child("telefono").getValue();
                if (telefonoObj != null) {
                    numero = String.valueOf(telefonoObj);
                }

                String telefonoCompleto = combinarTelefono(codigo, numero);
                telefonoVendedor = telefonoCompleto;
                mostrarDetallesVendedor(nombre, email, telefonoCompleto);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar vendedor: " + error.getMessage());
                mostrarDetallesVendedor("Error de Carga", "No disponible", null);
                deshabilitarBotonesContacto();
            }
        });
    }

    /**
     * Configura el adaptador y el indicador de puntos para el carrusel de imágenes.
     */
    private void configurarCarousel(List<String> imageUrls) {
        ImageCarouselAdapter carouselAdapter = new ImageCarouselAdapter(this, imageUrls);
        viewPagerImages.setAdapter(carouselAdapter);
        dotsIndicator.attachTo(viewPagerImages);
    }

    /**
     * Muestra la información del vendedor y configura los botones de contacto.
     */
    private void mostrarDetallesVendedor(String nombre, String email, String telefono) {
        String displayNombre = getSafeString(nombre, "Usuario Desconocido");
        tvVendedorNombre.setText("Nombre: " + displayNombre);

        String displayEmail = getSafeString(email, "No proporcionado");
        tvVendedorEmail.setText("Correo: " + displayEmail);

        String displayTelefono = getSafeString(telefono, "No disponible");
        tvVendedorTelefono.setText("Teléfono: " + displayTelefono);

        boolean telefonoValido = telefono != null && !telefono.trim().isEmpty() &&
                telefono.replaceAll("[^0-9+]", "").length() > 6;

        fabContactar.setEnabled(telefonoValido);
        if (telefonoValido) {
            fabContactar.setAlpha(1.0f);
            fabContactar.setOnClickListener(v -> realizarLlamada());
        } else {
            fabContactar.setAlpha(0.5f);
            fabContactar.setOnClickListener(null);
        }

        // El botón de chat se habilita si el productoActual y el vendedor son válidos (revisado en abrirChat)
        btnChat.setEnabled(productoActual != null && productoActual.getVendedorId() != null);
        if (btnChat.isEnabled()) {
            btnChat.setAlpha(1.0f);
            btnChat.setOnClickListener(v -> abrirChat());
        } else {
            btnChat.setAlpha(0.5f);
            btnChat.setOnClickListener(null);
        }
    }

    private void deshabilitarBotonesContacto() {
        if (fabContactar != null) {
            fabContactar.setEnabled(false);
            fabContactar.setAlpha(0.5f);
        }
        if (btnChat != null) {
            btnChat.setEnabled(false);
            btnChat.setAlpha(0.5f);
        }
    }

    /**
     * Inicia una Intent de llamada telefónica.
     */
    private void realizarLlamada() {
        if (telefonoVendedor == null || telefonoVendedor.isEmpty()) {
            Toast.makeText(this, "Número de contacto no disponible",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri telefonoUri = Uri.parse("tel:" + telefonoVendedor);
            Intent intentLlamada = new Intent(Intent.ACTION_DIAL, telefonoUri);
            if (intentLlamada.resolveActivity(getPackageManager()) != null) {
                startActivity(intentLlamada);
            } else {
                Toast.makeText(this, "No se encontró una aplicación para llamar",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al iniciar llamada: " + e.getMessage());
            Toast.makeText(this, "No se pudo iniciar la llamada", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Inicia la actividad de chat con validaciones.
     */
    private void abrirChat() {
        if (productoActual == null) {
            Toast.makeText(this, "Error: Datos del producto no disponibles",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String vendedorId = productoActual.getVendedorId();
        if (vendedorId == null || vendedorId.trim().isEmpty()) {
            Toast.makeText(this, "Error: Información del vendedor no disponible",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String productoId = productoActual.getId();
        if (productoId == null || productoId.trim().isEmpty()) {
            Toast.makeText(this, "Error: ID del producto no válido",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener nombre del vendedor de la vista, usando un fallback
        String vendedorNombre = tvVendedorNombre.getText().toString().replace("Nombre: ", "").trim();
        if (vendedorNombre.isEmpty() || vendedorNombre.equals("Usuario No Encontrado")) {
            vendedorNombre = "Vendedor";
        }

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("vendedor_id", vendedorId);
        intent.putExtra("vendedor_nombre", vendedorNombre);
        intent.putExtra("producto_id", productoId);
        intent.putExtra("producto_nombre", productoActual.getNombre());

        startActivity(intent);
    }

    /**
     * Combina el código de país y el número de teléfono con limpieza y validación básica.
     */
    private String combinarTelefono(String codigo, String numero) {
        if (codigo == null || numero == null) {
            return null;
        }

        String cleanedCodigo = codigo.trim().replaceAll("[^0-9]", "");
        String cleanedNumero = numero.trim().replaceAll("[^0-9]", "");

        if (cleanedCodigo.isEmpty() || cleanedNumero.isEmpty()) {
            return null;
        }

        // Validación de longitud básica para evitar números absurdos
        if (cleanedCodigo.length() > 4 || cleanedNumero.length() < 6 || cleanedNumero.length() > 15) {
            Log.w(TAG, "Formato de teléfono inválido detectado: +" + cleanedCodigo + cleanedNumero);
            return null;
        }

        return "+" + cleanedCodigo + cleanedNumero;
    }

    /**
     * Función utilitaria para devolver un valor seguro.
     */
    private String getSafeString(String value, String defaultValue) {
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }
}