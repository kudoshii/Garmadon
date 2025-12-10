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

    private RecyclerView rvMisAnuncios;
    private TextView tvNoAnuncios;
    private FirebaseAuth mAuth;
    private DatabaseReference productosRef;
    private List<Producto> listaProductos;
    private ProductoAdapter productoAdapter;
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncios);

        rvMisAnuncios = findViewById(R.id.rv_mis_productos);
        tvNoAnuncios = findViewById(R.id.tv_no_anuncios);

        mAuth = FirebaseAuth.getInstance();
        productosRef = FirebaseDatabase.getInstance().getReference("productos");

        listaProductos = new ArrayList<>();
        productoAdapter = new ProductoAdapter(this, listaProductos, true);

        rvMisAnuncios.setLayoutManager(new LinearLayoutManager(this));
        rvMisAnuncios.setAdapter(productoAdapter);

        cargarMisAnuncios();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void cargarMisAnuncios() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Log.w(TAG, "Usuario no autenticado");
            mostrarMensajeNoAnuncios(true);
            return;
        }

        final String userId = currentUser.getUid();
        Log.d(TAG, "Cargando anuncios para usuario: " + userId);

        Query queryMisAnuncios = productosRef.orderByChild("vendedorId").equalTo(userId);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                List<Producto> productosCargados = new ArrayList<>();

                if (snapshot.exists()) {
                    for (DataSnapshot productoSnapshot : snapshot.getChildren()) {
                        Producto producto = productoSnapshot.getValue(Producto.class);
                        if (producto != null) {
                            producto.setId(productoSnapshot.getKey());
                            productosCargados.add(producto);
                        }
                    }
                }

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
                if (!isFinishing() && !isDestroyed()) {
                    Log.e(TAG, "Error al leer anuncios: " + error.getMessage());
                    Toast.makeText(AnunciosActivity.this,
                            "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    mostrarMensajeNoAnuncios(true);
                }
            }
        };

        queryMisAnuncios.addValueEventListener(valueEventListener);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (productosRef != null && valueEventListener != null) {
            try {
                productosRef.removeEventListener(valueEventListener);
                valueEventListener = null;
                Log.d(TAG, "ValueEventListener de anuncios removido correctamente");
            } catch (Exception e) {
                Log.e(TAG, "Error al remover listener: " + e.getMessage(), e);
            }
        }
    }
}