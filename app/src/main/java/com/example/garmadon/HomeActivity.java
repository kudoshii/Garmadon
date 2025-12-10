package com.example.garmadon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.SearchView;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.util.Log;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import com.example.garmadon.adapter.ProductoAdapter;
import com.example.garmadon.model.Producto;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private RecyclerView recyclerView;
    private FloatingActionButton fabPublicar;
    private BottomNavigationView bottomNav;
    private Toolbar toolbar;
    private SearchView searchViewProductos;
    private ProductoAdapter productoAdapter;

    private DatabaseReference databaseRef;
    private List<Producto> listaProductosActual;

    // FIX: Guardar referencia del listener para removerlo
    private ValueEventListener productosListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.rv_productos);
        fabPublicar = findViewById(R.id.fab_publicar);
        searchViewProductos = findViewById(R.id.search_view_productos);
        bottomNav = findViewById(R.id.bottom_navigation_view);

        databaseRef = FirebaseDatabase.getInstance().getReference("productos");
        listaProductosActual = new ArrayList<>();

        configurarRecyclerView();
        configurarBuscador();

        fabPublicar.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PublicarActivity.class);
            startActivity(intent);
        });

        MenuItem homeItem = bottomNav.getMenu().findItem(R.id.navigation_home);
        if (homeItem != null) {
            homeItem.setChecked(true);
        }
        bottomNav.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent;

            if (itemId == R.id.navigation_chats) {
                intent = new Intent(HomeActivity.this, ListaChatsActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.navigation_account) {
                intent = new Intent(HomeActivity.this, CuentaActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.navigation_ads) {
                intent = new Intent(HomeActivity.this, AnunciosActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.navigation_home) {
                return true;
            }

            return true;
        });

        cargarProductosDesdeFirebase();
    }

    private void configurarBuscador() {
        if (searchViewProductos != null) {
            searchViewProductos.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (productoAdapter != null) {
                        productoAdapter.filtrar(newText);
                    }
                    return true;
                }
            });
        }
    }

    private void configurarRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productoAdapter = new ProductoAdapter(this, listaProductosActual);
        recyclerView.setAdapter(productoAdapter);
    }

    private void cargarProductosDesdeFirebase() {
        Log.d(TAG, "Iniciando ValueEventListener para Realtime Database...");

        // FIX: Guardar la referencia del listener
        productosListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaProductosActual.clear();

                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    try {
                        Producto producto = productSnapshot.getValue(Producto.class);

                        if (producto != null) {
                            producto.setId(productSnapshot.getKey());
                            listaProductosActual.add(producto);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al mapear producto: " + productSnapshot.getKey(), e);
                    }
                }

                productoAdapter.actualizarProductos(listaProductosActual);

                if (listaProductosActual.isEmpty()) {
                    Log.d(TAG, "No hay productos en la base de datos.");
                } else {
                    Log.d(TAG, "Total de productos: " + listaProductosActual.size());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al leer productos: " + error.getMessage());
                Toast.makeText(HomeActivity.this,
                        "Error al cargar productos: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        databaseRef.addValueEventListener(productosListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            Toast.makeText(HomeActivity.this, "Cerrando Sesión", Toast.LENGTH_SHORT).show();
            irALogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void irALogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // FIX: Remover el listener cuando se destruye la actividad
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseRef != null && productosListener != null) {
            try {
                databaseRef.removeEventListener(productosListener);
                productosListener = null;
                Log.d(TAG, "ValueEventListener removido correctamente");
            } catch (Exception e) {
                Log.e(TAG, "Error al remover listener: " + e.getMessage(), e);
            }
        }
    }
}