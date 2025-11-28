package com.example.garmadon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.example.garmadon.model.Producto;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import java.text.NumberFormat;
import java.util.Locale;

public class DetalleProductoActivity extends AppCompatActivity {
    public static final String EXTRA_PRODUCTO_ID = "producto_id";
    // Vistas del Producto
    private ImageView ivImagenDetalle;
    private TextView tvNombreDetalle;
    private TextView tvPrecioDetalle;
    private TextView tvDescripcionDetalle;
    private TextView tvMarcaDetalle;
    private TextView tvCategoriaDetalle;
    private TextView tvCondicionDetalle;
    private TextView tvDireccionDetalle;
    // VISTAS DEL VENDEDOR
    private TextView tvVendedorNombre;
    private TextView tvVendedorEmail;
    private TextView tvVendedorTelefono;
    private MaterialButton fabContactar;
    // Variables de Firebase
    private DatabaseReference productosRef;
    // Referencia de la base de datos para la colección de usuarios
    private DatabaseReference usuariosRef;
    // Variable de clase para guardar el teléfono y usarlo en la función de llamada
    private String telefonoVendedor = null;
    private static final String TAG = "Detalle_Producto";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_producto);
        // Configuración del Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_detalle);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle del Producto");
        }
        // Inicialización de Firebase
        productosRef = FirebaseDatabase.getInstance().getReference("productos");
        usuariosRef = FirebaseDatabase.getInstance().getReference("users");
        // 1. Inicializar Vistas del Producto
        ivImagenDetalle = findViewById(R.id.iv_detalle_imagen);
        tvNombreDetalle = findViewById(R.id.tv_detalle_titulo);
        tvPrecioDetalle = findViewById(R.id.tv_detalle_precio);
        tvDescripcionDetalle = findViewById(R.id.tv_detalle_descripcion);
        tvMarcaDetalle = findViewById(R.id.tv_detalle_marca);
        tvCategoriaDetalle = findViewById(R.id.tv_detalle_categoria);
        tvCondicionDetalle = findViewById(R.id.tv_detalle_condicion);
        tvDireccionDetalle = findViewById(R.id.tv_detalle_direccion);
        // 2. Inicializar Vistas del Vendedor
        tvVendedorNombre = findViewById(R.id.tv_detalle_vendedor);
        tvVendedorEmail = findViewById(R.id.tv_vendedor_email);
        tvVendedorTelefono = findViewById(R.id.tv_vendedor_telefono);
        // Inicialización del botón
        fabContactar = findViewById(R.id.fab_contactar);
        // Placeholder mientras carga
        tvVendedorNombre.setText("Nombre: Cargando detalles...");
        // 3. Obtener datos del Intent
        Intent intent = getIntent();
        String productoId = intent.getStringExtra(EXTRA_PRODUCTO_ID);
        if (productoId != null) {
            Log.d(TAG, "Cargando detalles para el ID: " + productoId);
            cargarDatosProducto(productoId);
        } else {
            Toast.makeText(this, "Error: No se encontró el ID del producto.",
                    Toast.LENGTH_LONG).show();
            finish();
        }
        // 4. Botón Contactar (Llamar)
        // Llama a la función realizarLlamada, que usa la variable de clase telefonoVendedor
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
        productosRef.child(productoId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        // Intenta mapear a la clase Producto
                        Producto producto = snapshot.getValue(Producto.class);
                        if (producto != null) {
                            // Mostrar todos los detalles del producto
                            mostrarDetallesProducto(producto);
                            // LECTURA 2: Buscar datos del vendedor usando el VendedorId
                            String vendedorId = producto.getVendedorId();
                            if (vendedorId != null) {
                                // Se valida que el vendedorId exista antes de buscar el usuario
                                cargarDatosVendedor(vendedorId);
                            } else {
                                Log.e(TAG, "Producto sin VendedorId. No se pueden cargar los datos del usuario.");
                                        mostrarDetallesVendedor("Vendedor Desconocido", "N/A", null);
                            }
                        } else {
                            // Si el mapeo falla completamente, intenta la lectura individual robusta.
                            mostrarDetallesProductoIndividual(snapshot);
                        }
                    } catch (Exception e) {
                        // Si falla el mapeo, intenta la lectura individual para no fallar
                        Log.e(TAG, "Error al mapear el objeto Producto. Intentando lectura individual: " +
                                e.getMessage(), e);
                        mostrarDetallesProductoIndividual(snapshot);
                    }
                } else {
                    Toast.makeText(DetalleProductoActivity.this, "Producto no encontrado en la base de datos.", Toast.LENGTH_LONG).show();
                            finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Fallo al leer el producto de DB: " + error.getMessage(),
                        error.toException());
                Toast.makeText(DetalleProductoActivity.this, "Error de Firebase: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    private void mostrarDetallesProductoIndividual(@NonNull DataSnapshot snapshot) {
        // Lectura directa de campos
        String nombre = snapshot.child("nombre").getValue(String.class);
        Double precioDouble = snapshot.child("precio").getValue(Double.class);
        String descripcion = snapshot.child("descripcion").getValue(String.class);
        // Campos opcionales
        String marca = snapshot.child("marca").getValue(String.class);
        String condicion = snapshot.child("condicion").getValue(String.class);
        String categoria = snapshot.child("categoria").getValue(String.class);
        String direccion = snapshot.child("direccion").getValue(String.class);
        String vendedorId = snapshot.child("vendedorId").getValue(String.class);
        // Asignar texto
        tvNombreDetalle.setText(nombre != null ? nombre : "N/A");
        tvDescripcionDetalle.setText(descripcion != null ? descripcion : "Sin descripción");
        // Formatear precio
        if (precioDouble != null) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es",
                    "CL"));
            String precioFormateado = currencyFormat.format(precioDouble);
            tvPrecioDetalle.setText(precioFormateado);
        } else {
            tvPrecioDetalle.setText("Precio: N/A");
        }
        // Asignar Texto a las Vistas de ESPECIFICACIONES
        tvMarcaDetalle.setText("Marca: " + (marca != null ? marca : "No especificada"));
        tvCondicionDetalle.setText("Condición: " + (condicion != null ? condicion : "N/A"));
        tvCategoriaDetalle.setText("Categoría: " + (categoria != null ? categoria : "N/A"));
        tvDireccionDetalle.setText("Retiro en: " + (direccion != null ? direccion : "No provista"));
        // Cargar Imagen
        DataSnapshot imageUrlsSnapshot = snapshot.child("imageUrls");
        if (imageUrlsSnapshot.exists() && imageUrlsSnapshot.getChildrenCount() > 0) {
            String firstImageUrl =
                    imageUrlsSnapshot.getChildren().iterator().next().getValue(String.class);
            if (firstImageUrl != null) {
                Glide.with(DetalleProductoActivity.this)
                        .load(firstImageUrl)
                        .placeholder(R.drawable.agregar_img)
                        .error(R.drawable.error_image)
                        .centerCrop()
                        .into(ivImagenDetalle);
            }
        } else {
            ivImagenDetalle.setImageResource(R.drawable.agregar_img);
        }
        // LECTURA 2: Buscar datos del vendedor
        if (vendedorId != null) {
            cargarDatosVendedor(vendedorId);
        } else {
            Log.e(TAG, "Producto sin VendedorId. No se pueden cargar los datos del usuario.");
            mostrarDetallesVendedor("Vendedor Desconocido", "N/A", null);
        }
    }
    private void cargarDatosVendedor(String vendedorId) {
        usuariosRef.child(vendedorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Leer los valores directamente
                String nombre = snapshot.child("nombre").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String codigo = snapshot.child("codigoTelefono").getValue(String.class);
                //String numero = snapshot.child("telefono").getValue(String.class);
                String numero = String.valueOf(snapshot.child("telefono").getValue());
                // Si se encontró AL MENOS un dato, asumimos que el perfil existe.
                if (nombre != null || email != null || codigo != null || numero != null) {
                    // COMBINAMOS Y FORMATEAMOS EL TELÉFONO
                    String telefonoCompleto = combinarTelefono(codigo, numero);
                    // PASO CLAVE: Guardar teléfono completo en la variable de clase para la función de Contactar (Llamar)
                    telefonoVendedor = telefonoCompleto;
                    // Mostrar los detalles
                    mostrarDetallesVendedor(nombre, email, telefonoCompleto);
                } else {
                    // Si todos los valores son nulos, el nodo del usuario no existe
                    Log.w(TAG, "Usuario no encontrado o datos nulos para ID: " + vendedorId);
                    mostrarDetallesVendedor("Usuario No Encontrado", "No disponible", null);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Fallo al cargar datos del vendedor: " + error.getMessage());
                mostrarDetallesVendedor("Error de Carga", "No disponible", null);
            }
        });
    }
    /**
     * Muestra todos los detalles del producto en las vistas.
     */
    private void mostrarDetallesProducto(Producto producto) {
        // 1. Cargar la imagen principal
        if (producto.getImageUrls() != null && !producto.getImageUrls().isEmpty()) {
            String imageUrl = producto.getImageUrls().get(0);
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.agregar_img)
                    .error(R.drawable.error_image)
                    .centerCrop()
                    .into(ivImagenDetalle);
        } else {
            ivImagenDetalle.setImageResource(R.drawable.agregar_img);
        }
        // 2. Asignar Texto a las Vistas Principales (Título, Precio, Descripción)
        tvNombreDetalle.setText(producto.getNombre());
        tvDescripcionDetalle.setText(producto.getDescripcion());
        // Formatear precio
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es",
                "CL"));
        String precioFormateado = currencyFormat.format(producto.getPrecio());
        tvPrecioDetalle.setText(precioFormateado);
        // 3. Asignar Texto a las Vistas de ESPECIFICACIONES (Marca, Condición, Categoría,Dirección)
        tvMarcaDetalle.setText("Marca: " + (producto.getMarca() != null ? producto.getMarca() :
                "No especificada"));
        tvCondicionDetalle.setText("Condición: " + (producto.getCondicion() != null ?
                producto.getCondicion() : "N/A"));
        tvCategoriaDetalle.setText("Categoría: " + (producto.getCategoria() != null ?
                producto.getCategoria() : "N/A"));
        tvDireccionDetalle.setText("Retiro en: " + (producto.getDireccion() != null ?
                producto.getDireccion() : "No provista"));
    }
    /**
     * Muestra los detalles de contacto del vendedor y controla la disponibilidad del botón de
     contacto.
     */
    private void mostrarDetallesVendedor(String nombre, String email, String telefono) {
        // Nombre
        String displayNombre = (nombre != null && !nombre.isEmpty()) ? nombre : "Usuario Desconocido";
        tvVendedorNombre.setText("Nombre: " + displayNombre);
        // Email
        String displayEmail = (email != null && !email.isEmpty()) ? email : "No proporcionado";
        tvVendedorEmail.setText("Correo: " + displayEmail);
        // Teléfono
        String displayTelefono = (telefono != null && !telefono.isEmpty()) ? telefono : "No disponible";
        tvVendedorTelefono.setText("Teléfono: " + displayTelefono);
        // Habilitar/Deshabilitar el botón de llamar
        boolean telefonoValido = telefono != null && !telefono.isEmpty() &&
                telefono.matches("^\\+?[0-9\\s()-]*$");
        fabContactar.setEnabled(telefonoValido);
    }
    // --- Funciones de Contacto ---
    private void realizarLlamada() {
        // Se asegura de que la variable de clase tenga un número cargado
        if (telefonoVendedor == null || telefonoVendedor.isEmpty()) {
            Toast.makeText(this, "El vendedor no ha proporcionado un número de contacto para llamar.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // Utilizamos ACTION_DIAL para abrir el marcador con el número precargado
            // El formato 'tel:' es clave para que Android sepa que es una llamada
            Uri telefonoUri = Uri.parse("tel:" + telefonoVendedor);
            Intent intentLlamada = new Intent(Intent.ACTION_DIAL, telefonoUri);
            if (intentLlamada.resolveActivity(getPackageManager()) != null) {
                startActivity(intentLlamada);
            } else {
                Toast.makeText(this, "No se encontró una aplicación para realizar llamadas.",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al intentar iniciar el Intent de llamada: " + e.getMessage());
            Toast.makeText(this, "No se pudo iniciar la función de llamada.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    private String combinarTelefono(String codigo, String numero) {
        // Revisa que ambos parámetros no sean nulos O vacíos.
        if (codigo != null && !codigo.isEmpty() && numero != null && !numero.isEmpty()) {
            // Eliminamos cualquier carácter que no sea dígito
            String cleanedCodigo = codigo.replaceAll("[^0-9]", "");
            String cleanedNumero = numero.replaceAll("[^0-9]", "");
            // Devuelve el número en formato internacional limpio
            return "+" + cleanedCodigo + cleanedNumero;
        }
        return null;
    }
}
