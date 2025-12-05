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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.example.garmadon.model.Producto;
import com.example.garmadon.adapter.ImageCarouselAdapter;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.Locale;

import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

public class DetalleProductoActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCTO_ID = "producto_id";

    // Vistas del Producto
    private ImageView ivImagenDetalle;
    private TextView tvNombreDetalle, tvPrecioDetalle, tvDescripcionDetalle;
    private TextView tvMarcaDetalle, tvCategoriaDetalle, tvCondicionDetalle, tvDireccionDetalle;

    // Vistas del vendedor
    private TextView tvVendedorNombre, tvVendedorEmail, tvVendedorTelefono;
    private MaterialButton fabContactar;

    // 🔵 Nuevas vistas para el carrusel
    private ViewPager2 viewPagerImages;
    private DotsIndicator dotsIndicator;

    // Botón de chat
    private MaterialButton btnChat;

    // Firebase
    private DatabaseReference productosRef;
    private DatabaseReference usuariosRef;

    // Variables útiles
    private String telefonoVendedor = null;
    private static final String TAG = "Detalle_Producto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_producto);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_detalle);
        if (toolbar != null) setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle del Producto");
        }

        // Firebase init
        productosRef = FirebaseDatabase.getInstance().getReference("productos");
        usuariosRef = FirebaseDatabase.getInstance().getReference("users");

        // Vistas del producto
        ivImagenDetalle = findViewById(R.id.iv_detalle_imagen);
        tvNombreDetalle = findViewById(R.id.tv_detalle_titulo);
        tvPrecioDetalle = findViewById(R.id.tv_detalle_precio);
        tvDescripcionDetalle = findViewById(R.id.tv_detalle_descripcion);
        tvMarcaDetalle = findViewById(R.id.tv_detalle_marca);
        tvCategoriaDetalle = findViewById(R.id.tv_detalle_categoria);
        tvCondicionDetalle = findViewById(R.id.tv_detalle_condicion);
        tvDireccionDetalle = findViewById(R.id.tv_detalle_direccion);

        // Vistas del vendedor
        tvVendedorNombre = findViewById(R.id.tv_detalle_vendedor);
        tvVendedorEmail = findViewById(R.id.tv_vendedor_email);
        tvVendedorTelefono = findViewById(R.id.tv_vendedor_telefono);
        fabContactar = findViewById(R.id.fab_contactar);

        // Carrusel
        viewPagerImages = findViewById(R.id.viewpager_images);
        dotsIndicator = findViewById(R.id.dots_indicator);

        // Botón chat
        btnChat = findViewById(R.id.btn_chat);

        tvVendedorNombre.setText("Nombre: Cargando detalles...");

        // Obtener ID producto
        String productoId = getIntent().getStringExtra(EXTRA_PRODUCTO_ID);

        if (productoId != null) {
            cargarDatosProducto(productoId);
        } else {
            Toast.makeText(this, "Error: No se encontró el ID del producto.", Toast.LENGTH_LONG).show();
            finish();
        }

        // Botón llamar
        fabContactar.setOnClickListener(v -> realizarLlamada());
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
                if (!snapshot.exists()) {
                    Toast.makeText(DetalleProductoActivity.this, "Producto no encontrado.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                Producto producto = snapshot.getValue(Producto.class);

                if (producto != null) {
                    mostrarDetallesProducto(producto);

                    // Datos del vendedor
                    if (producto.getVendedorId() != null) {
                        cargarDatosVendedor(producto.getVendedorId());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error Firebase: " + error.getMessage());
            }
        });
    }

    private void mostrarDetallesProducto(Producto producto) {

        // Imagen principal (fallback)
        if (producto.getImageUrls() != null && !producto.getImageUrls().isEmpty()) {
            Glide.with(this)
                    .load(producto.getImageUrls().get(0))
                    .placeholder(R.drawable.agregar_img)
                    .into(ivImagenDetalle);
        }

        // Título, descripción, precio
        tvNombreDetalle.setText(producto.getNombre());
        tvDescripcionDetalle.setText(producto.getDescripcion());

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
        tvPrecioDetalle.setText(currencyFormat.format(producto.getPrecio()));

        // Especificaciones
        tvMarcaDetalle.setText("Marca: " + producto.getMarca());
        tvCondicionDetalle.setText("Condición: " + producto.getCondicion());
        tvCategoriaDetalle.setText("Categoría: " + producto.getCategoria());
        tvDireccionDetalle.setText("Retiro en: " + producto.getDireccion());

        //   CARRUSEL DE IMÁGENES
        if (producto.getImageUrls() != null && !producto.getImageUrls().isEmpty()) {
            ImageCarouselAdapter carouselAdapter = new ImageCarouselAdapter(this, producto.getImageUrls());
            viewPagerImages.setAdapter(carouselAdapter);
            dotsIndicator.attachTo(viewPagerImages);
        }

        //   BOTÓN CHAT
        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(DetalleProductoActivity.this, ChatActivity.class);
            intent.putExtra("vendedor_id", producto.getVendedorId());
            intent.putExtra("vendedor_nombre", tvVendedorNombre.getText().toString());
            intent.putExtra("producto_id", producto.getId());
            intent.putExtra("producto_nombre", producto.getNombre());
            startActivity(intent);
        });
    }

    private void cargarDatosVendedor(String vendedorId) {
        usuariosRef.child(vendedorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nombre = snapshot.child("nombre").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String codigo = snapshot.child("codigoTelefono").getValue(String.class);
                String numero = String.valueOf(snapshot.child("telefono").getValue());

                telefonoVendedor = combinarTelefono(codigo, numero);

                mostrarDetallesVendedor(nombre, email, telefonoVendedor);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void mostrarDetallesVendedor(String nombre, String email, String telefono) {
        tvVendedorNombre.setText("Nombre: " + nombre);
        tvVendedorEmail.setText("Correo: " + email);
        tvVendedorTelefono.setText("Teléfono: " + telefono);

        fabContactar.setEnabled(telefono != null && !telefono.isEmpty());
    }

    private void realizarLlamada() {
        if (telefonoVendedor == null) {
            Toast.makeText(this, "No hay número disponible.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intentLlamada = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefonoVendedor));
        startActivity(intentLlamada);
    }

    private String combinarTelefono(String codigo, String numero) {
        if (codigo != null && numero != null) {
            return "+" + codigo.replaceAll("[^0-9]", "") + numero.replaceAll("[^0-9]", "");
        }
        return null;
    }
}
