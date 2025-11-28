package com.example.garmadon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.garmadon.adapter.ProductoAdapter;
import com.example.garmadon.model.Producto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class AnunciosActivity extends AppCompatActivity {
    private static final String TAG = "AnunciosActivity";
    // Vistas y Componentes
    private RecyclerView rvMisAnuncios;
    private TextView tvNoAnuncios;
    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference productosRef;
    private List<Producto> listaProductos;
    private ProductoAdapter productoAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncios);
        // 1. Inicializar Vistas
        rvMisAnuncios = findViewById(R.id.rv_mis_productos);
        tvNoAnuncios = findViewById(R.id.tv_no_anuncios);
        // 2. Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        productosRef = FirebaseDatabase.getInstance().getReference("productos");
        // 3. Configurar RecyclerView
        listaProductos = new ArrayList<>();
        productoAdapter = new ProductoAdapter(this, listaProductos);
        // Configuración del LayoutManager para que sepa dibujar los ítems
        rvMisAnuncios.setLayoutManager(new LinearLayoutManager(this));
        rvMisAnuncios.setAdapter(productoAdapter);
        // 4. Cargar los anuncios
        cargarMisAnuncios();
        // Configurar el botón de retroceso si lo tienes en el layout
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
    private void cargarMisAnuncios() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // 1. Verificar autenticación
        if (currentUser == null) {
            Log.w(TAG, "Usuario no autenticado. No se pueden cargar anuncios.");
            mostrarMensajeNoAnuncios(true);
            return;
        }
        final String userId = currentUser.getUid();
        Log.d(TAG, "Iniciando carga de anuncios para el usuario ID: " + userId);
        // 2. CREAR LA CONSULTA FILTRADA: Ordena por vendedorId e iguala al ID del usuario
        Query queryMisAnuncios = productosRef.orderByChild("vendedorId").equalTo(userId);
        queryMisAnuncios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Usamos una lista temporal para recopilar los productos
                List<Producto> productosCargados = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot productoSnapshot : snapshot.getChildren()) {
                        Producto producto = productoSnapshot.getValue(Producto.class);
                        if (producto != null) {
                            // Asigna la clave del producto como su ID
                            producto.setId(productoSnapshot.getKey());
                            productosCargados.add(producto);
                        }
                    }
                }
                // 3. Actualizar UI
                if (productosCargados.isEmpty()) {
                    mostrarMensajeNoAnuncios(true);
                } else {
                    mostrarMensajeNoAnuncios(false);
                }
                productoAdapter.actualizarProductos(productosCargados);
                Log.d(TAG, "Anuncios cargados: " + productosCargados.size());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Fallo al leer anuncios: " + error.getMessage());
                Toast.makeText(AnunciosActivity.this, "Error: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
                mostrarMensajeNoAnuncios(true);
            }
        });
    }
    private void mostrarMensajeNoAnuncios(boolean mostrar) {
        if (mostrar) {
            tvNoAnuncios.setVisibility(View.VISIBLE);
            rvMisAnuncios.setVisibility(View.GONE);
        } else {
            tvNoAnuncios.setVisibility(View.GONE);
            rvMisAnuncios.setVisibility(View.VISIBLE);
        }
    }
}
