package com.example.garmadon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.garmadon.adapter.ProductoAdapter;
import com.example.garmadon.model.Producto;
import java.util.ArrayList;
import java.util.List;
public class AnunciosActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncios);
        // 1. Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_anuncios);
        setSupportActionBar(toolbar);
        // 2. Inicializar Vistas
        recyclerView = findViewById(R.id.rv_mis_productos);
        bottomNav = findViewById(R.id.bottom_navigation_view);
        // 3. Configuración del RecyclerView y carga de productos de prueba
        configurarRecyclerView();
        // 4. Configuración del Listener de la barra de navegación (CLAVE)
        configurarNavegacionInferior();
    }
    private void configurarRecyclerView() {
        // Usa los mismos productos de prueba que en Home.java
        List<Producto> productosDePrueba = cargarProductosDePrueba();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ProductoAdapter productoAdapter = new ProductoAdapter(this, productosDePrueba);
        recyclerView.setAdapter(productoAdapter);
    }
    /**
     * Define la lógica de navegación para la barra inferior en esta Activity.
     */
    private void configurarNavegacionInferior() {
        // Marcar el ítem 'Anuncios' como seleccionado al cargar la Activity
        MenuItem anunciosItem = bottomNav.getMenu().findItem(R.id.navigation_ads);
        if (anunciosItem != null) {
            anunciosItem.setChecked(true);
        }
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent;
            if (itemId == R.id.navigation_home) { // Ir a Home
                intent = new Intent(AnunciosActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.navigation_account) { // Ir a Cuenta
                intent = new Intent(AnunciosActivity.this, CuentaActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.navigation_ads) {
                return true;
            }
            // Para otros ítems no implementados (Chats, Vender)
            Toast.makeText(AnunciosActivity.this, "Navegando a: " + item.getTitle(),
                    Toast.LENGTH_SHORT).show();
            return true;
        });
    }
    /**
     * Carga los datos de prueba (igual que en Home.java).
     */
    private List<Producto> cargarProductosDePrueba() {
        List<Producto> lista = new ArrayList<>();
        // ID, Nombre, Descripción, Precio, ImagenUrl (ficticia)
        lista.add(new Producto("ID001", "Bicicleta Vintage", "Clásica bicicleta de ruta.", "150.000",
                "url_ficticia_1"));
        lista.add(new Producto("ID002", "Auriculares Inalámbricos", "Cancelación de ruido activa.",
                "75.990", "url_ficticia_2"));
        lista.add(new Producto("ID003", "Libro: El Señor de los Anillos", "Edición de lujo tapa dura.", "29.990", "url_ficticia_3"));
        lista.add(new Producto("ID004", "Teclado Mecánico RGB", "Switches marrones, 60%.",
                "55.000", "url_ficticia_4"));
        lista.add(new Producto("ID005", "Silla de Oficina Ergonómica", "Soporte lumbar ajustable.",
                "99.990", "url_ficticia_5"));
        lista.add(new Producto("ID006", "Cámara Instantánea", "Incluye 10 películas.", "45.000",
                "url_ficticia_6"));
        lista.add(new Producto("ID007", "Laptop Gaming", "Potente para juegos y trabajo.",
                "750.000", "url_ficticia_7"));
        lista.add(new Producto("ID008", "Mesa de Centro", "Diseño moderno de madera.",
                "80.000", "url_ficticia_8"));
        lista.add(new Producto("ID009", "Zapatillas Deportivas", "Para correr, talla 42.", "35.000",
                "url_ficticia_9"));
        return lista;
    }
}
