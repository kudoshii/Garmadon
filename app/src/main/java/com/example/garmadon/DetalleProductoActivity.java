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

        Toolbar toolbar = findViewById(R.id.toolbar_detalle);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle del Producto");
        }

        productosRef = FirebaseDatabase.getInstance().getReference("productos");
        usuariosRef = FirebaseDatabase.getInstance().getReference("users");

        inicializarVistas();

        Intent intent = getIntent();
        String productoId = intent.getStringExtra(EXTRA_PRODUCTO_ID);
        if (productoId != null) {
            Log.d(TAG, "Cargando detalles para ID: " + productoId);
            cargarDatosProducto(productoId);
        } else {
            Toast.makeText(this, "Error: No se encontró el ID del producto.",
                    Toast.LENGTH_LONG).show();
            finish();
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

    private void cargarDatosProducto(String productoId) {
        productosRef.child(productoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        Producto producto = snapshot.getValue(Producto.class);
                        if (producto != null) {
                            producto.setId(snapshot.getKey());
                            productoActual = producto;
                            mostrarDetallesProducto(producto);

                            String vendedorId = producto.getVendedorId();
                            if (vendedorId != null) {
                                cargarDatosVendedor(vendedorId);
                            } else {
                                Log.e(TAG, "Producto sin VendedorId");
                                mostrarDetallesVendedor("Vendedor Desconocido", "N/A", null);
                            }
                        } else {
                            mostrarDetallesProductoIndividual(snapshot);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al mapear Producto: " + e.getMessage(), e);
                        mostrarDetallesProductoIndividual(snapshot);
                    }
                } else {
                    Toast.makeText(DetalleProductoActivity.this,
                            "Producto no encontrado", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al leer producto: " + error.getMessage());
                Toast.makeText(DetalleProductoActivity.this,
                        "Error de Firebase: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarDetallesProductoIndividual(@NonNull DataSnapshot snapshot) {
        String nombre = snapshot.child("nombre").getValue(String.class);
        Double precioDouble = snapshot.child("precio").getValue(Double.class);
        String descripcion = snapshot.child("descripcion").getValue(String.class);
        String marca = snapshot.child("marca").getValue(String.class);
        String condicion = snapshot.child("condicion").getValue(String.class);
        String categoria = snapshot.child("categoria").getValue(String.class);
        String direccion = snapshot.child("direccion").getValue(String.class);
        String vendedorId = snapshot.child("vendedorId").getValue(String.class);

        tvNombreDetalle.setText(nombre != null ? nombre : "N/A");
        tvDescripcionDetalle.setText(descripcion != null ? descripcion : "Sin descripción");

        if (precioDouble != null) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
            tvPrecioDetalle.setText(currencyFormat.format(precioDouble));
        } else {
            tvPrecioDetalle.setText("Precio: N/A");
        }

        tvMarcaDetalle.setText("Marca: " + (marca != null ? marca : "No especificada"));
        tvCondicionDetalle.setText("Condición: " + (condicion != null ? condicion : "N/A"));
        tvCategoriaDetalle.setText("Categoría: " + (categoria != null ? categoria : "N/A"));
        tvDireccionDetalle.setText("Retiro en: " + (direccion != null ? direccion : "No provista"));

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
        }
    }

    private void cargarDatosVendedor(String vendedorId) {
        usuariosRef.child(vendedorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nombre = snapshot.child("nombre").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String codigo = snapshot.child("codigoTelefono").getValue(String.class);
                String numero = String.valueOf(snapshot.child("telefono").getValue());

                if (nombre != null || email != null) {
                    String telefonoCompleto = combinarTelefono(codigo, numero);
                    telefonoVendedor = telefonoCompleto;
                    mostrarDetallesVendedor(nombre, email, telefonoCompleto);
                } else {
                    Log.w(TAG, "Usuario no encontrado: " + vendedorId);
                    mostrarDetallesVendedor("Usuario No Encontrado", "No disponible", null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar vendedor: " + error.getMessage());
                mostrarDetallesVendedor("Error de Carga", "No disponible", null);
            }
        });
    }

    private void mostrarDetallesProducto(Producto producto) {
        if (producto.getImageUrls() != null && !producto.getImageUrls().isEmpty()) {
            configurarCarousel(producto.getImageUrls());
        }

        tvNombreDetalle.setText(producto.getNombre());
        tvDescripcionDetalle.setText(producto.getDescripcion());

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
        tvPrecioDetalle.setText(currencyFormat.format(producto.getPrecio()));

        tvMarcaDetalle.setText("Marca: " + (producto.getMarca() != null ?
                producto.getMarca() : "No especificada"));
        tvCondicionDetalle.setText("Condición: " + (producto.getCondicion() != null ?
                producto.getCondicion() : "N/A"));
        tvCategoriaDetalle.setText("Categoría: " + (producto.getCategoria() != null ?
                producto.getCategoria() : "N/A"));
        tvDireccionDetalle.setText("Retiro en: " + (producto.getDireccion() != null ?
                producto.getDireccion() : "No provista"));
    }

    private void configurarCarousel(List<String> imageUrls) {
        ImageCarouselAdapter carouselAdapter = new ImageCarouselAdapter(this, imageUrls);
        viewPagerImages.setAdapter(carouselAdapter);
        dotsIndicator.attachTo(viewPagerImages);
    }

    private void mostrarDetallesVendedor(String nombre, String email, String telefono) {
        String displayNombre = (nombre != null && !nombre.isEmpty()) ? nombre : "Usuario Desconocido";
        tvVendedorNombre.setText("Nombre: " + displayNombre);

        String displayEmail = (email != null && !email.isEmpty()) ? email : "No proporcionado";
        tvVendedorEmail.setText("Correo: " + displayEmail);

        String displayTelefono = (telefono != null && !telefono.isEmpty()) ? telefono : "No disponible";
        tvVendedorTelefono.setText("Teléfono: " + displayTelefono);

        boolean telefonoValido = telefono != null && !telefono.isEmpty() &&
                telefono.matches("^\\+?[0-9\\s()-]*$");
        fabContactar.setEnabled(telefonoValido);

        // Configurar botón de llamar
        fabContactar.setOnClickListener(v -> realizarLlamada());

        // Configurar botón de chat
        btnChat.setOnClickListener(v -> abrirChat());
    }

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

    private void abrirChat() {
        if (productoActual == null) {
            Toast.makeText(this, "Error: Datos del producto no disponibles",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String vendedorNombre = tvVendedorNombre.getText().toString().replace("Nombre: ", "");

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("vendedor_id", productoActual.getVendedorId());
        intent.putExtra("vendedor_nombre", vendedorNombre);
        intent.putExtra("producto_id", productoActual.getId());
        intent.putExtra("producto_nombre", productoActual.getNombre());
        startActivity(intent);
    }

    private String combinarTelefono(String codigo, String numero) {
        if (codigo != null && !codigo.isEmpty() && numero != null && !numero.isEmpty()) {
            String cleanedCodigo = codigo.replaceAll("[^0-9]", "");
            String cleanedNumero = numero.replaceAll("[^0-9]", "");
            return "+" + cleanedCodigo + cleanedNumero;
        }
        return null;
    }
}