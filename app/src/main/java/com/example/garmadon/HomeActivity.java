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
import com.example.garmadon.R;



import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
// FIN MODIFICACIÓN SEMANA 9

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    // Vistas principales
    private RecyclerView recyclerView;
    private FloatingActionButton fabPublicar;
    private BottomNavigationView bottomNav;
    private Toolbar toolbar;

    // SEMANA 5: Referencia al Buscador y al Adaptador
    private SearchView searchViewProductos;
    private ProductoAdapter productoAdapter;

    // INICIO MODIFICACIÓN SEMANA 9: Variables de Firebase y datos
    private DatabaseReference databaseRef; // Referencia a la base de datos
    private List<Producto> listaProductosActual; // Lista que mantendrá los datos en memoria
    // FIN MODIFICACIÓN SEMANA 9

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Inicializar Vistas y Toolbar
        toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.rv_productos);
        fabPublicar = findViewById(R.id.fab_publicar);
        searchViewProductos = findViewById(R.id.search_view_productos);
        bottomNav = findViewById(R.id.bottom_navigation_view);

        // 2. INICIO MODIFICACIÓN SEMANA 9: Inicializar Firebase y la lista de datos
        databaseRef = FirebaseDatabase.getInstance().getReference("productos");
        listaProductosActual = new ArrayList<>();
        // FIN MODIFICACIÓN SEMANA 9

        // 3. Configuración CLAVE del RecyclerView
        configurarRecyclerView();

        // 4. Configuración del Listener del Buscador para escuchar los cambios de texto
        configurarBuscador();

        // 5. Botón FAB para publicar producto
        fabPublicar.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PublicarActivity.class);
            startActivity(intent);
        });

        // 6. Configuración del Bottom Nav (tu código existente)
        MenuItem homeItem = bottomNav.getMenu().findItem(R.id.navigation_home);
        if (homeItem != null) {
            homeItem.setChecked(true);
        }
        bottomNav.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent;

            if (itemId == R.id.navigation_chats) {
                Toast.makeText(HomeActivity.this, "Abriendo Chats (Semana 6.1)", Toast.LENGTH_SHORT).show();
                intent = new Intent(HomeActivity.this, ChatActivity.class);
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

            Toast.makeText(HomeActivity.this, "Navegando a: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            return true;
        });

        // 7. INICIO MODIFICACIÓN SEMANA 9: Iniciar la carga de datos de Firebase
        cargarProductosDesdeFirebase();
        // FIN MODIFICACIÓN SEMANA 9
    }

    /**
     * SEMANA 5: Método para configurar el buscador.
     * Implementa OnQueryTextListener para detectar el texto ingresado.
     */
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


    /**
     * 2. Configura el RecyclerView.
     * MODIFICACIÓN SEMANA 9: Usa una lista inicialmente vacía.
     */
    private void configurarRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // MODIFICACIÓN SEMANA 9: En lugar de cargar datos de prueba, usamos la lista vacía/actual
        productoAdapter = new ProductoAdapter(this, listaProductosActual);
        recyclerView.setAdapter(productoAdapter);
    }

    /**
     * SEMANA 9: Método para cargar los productos de Realtime Database.
     * Utiliza un ValueEventListener para obtener los datos y escuchar futuros cambios.
     */
    private void cargarProductosDesdeFirebase() {
        Log.d(TAG, "Iniciando ValueEventListener para Realtime Database...");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaProductosActual.clear(); // Limpiamos la lista

                // Iterar sobre los productos publicados
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    try {
                        // Mapeamos los datos del nodo al objeto Producto
                        Producto producto = productSnapshot.getValue(Producto.class);

                        if (producto != null) {
                            // Asignamos la clave de Firebase (el ID único) al objeto Producto
                            producto.setId(productSnapshot.getKey());
                            listaProductosActual.add(producto);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al mapear un producto de DB: " + productSnapshot.getKey(), e);
                    }
                }

                // Actualizar el adaptador con los nuevos datos leídos
                productoAdapter.actualizarProductos(listaProductosActual);

                if (listaProductosActual.isEmpty()) {
                    Log.d(TAG, "Carga exitosa. No hay productos en la base de datos.");
                } else {
                    Log.d(TAG, "Carga exitosa. Total de productos: " + listaProductosActual.size());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Fallo al leer los productos de Realtime Database: " + error.getMessage(), error.toException());
                Toast.makeText(HomeActivity.this, "Error al cargar productos: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            Toast.makeText(HomeActivity.this, "Cerrando Sesión (Simulación)", Toast.LENGTH_SHORT).show();
            irALogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Navega de vuelta a LoginActivity
    public void irALogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
