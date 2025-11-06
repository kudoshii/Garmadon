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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.example.garmadon.adapter.ProductoAdapter;
import com.example.garmadon.model.Producto;
import java.util.ArrayList;
import java.util.List;
import com.example.garmadon.R;

// SE ELIMINA 'implements SearchView.OnQueryTextListener'
// La implementación se hace de forma anónima en el método configurarBuscador() (SEMANA5)

public class HomeActivity extends AppCompatActivity {
    // Vistas principales
    private RecyclerView recyclerView;
    private FloatingActionButton fabPublicar;
    private BottomNavigationView bottomNav;
    private Toolbar toolbar;
    // SEMANA 5: Referencia al Buscador y al Adaptador
    private SearchView searchViewProductos;
    private ProductoAdapter productoAdapter; // Necesario para llamar al método filtrar()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // 1. Inicializar Vistas
        toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.rv_productos);
        fabPublicar = findViewById(R.id.fab_publicar);
        // SEMANA 5: Inicializar el SearchView directamente desde el layout
        searchViewProductos = findViewById(R.id.search_view_productos);
        // --- SEMANA 4: Inicialización del Bottom Nav (con Toast)
        bottomNav = findViewById(R.id.bottom_navigation_view);
        // SEMANA 6: Aseguramos que el ítem 'Inicio' esté seleccionado por defecto
        MenuItem homeItem = bottomNav.getMenu().findItem(R.id.navigation_home);
        if (homeItem != null) {
            homeItem.setChecked(true);
        }
        bottomNav.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent;
            // SEMANA 6.1: Manejar la navegación a la pantalla de Chats
            if (itemId == R.id.navigation_chats) {
                // Al presionar el botón de Chat, navegamos a ChatActivity
                Toast.makeText(HomeActivity.this, "Abriendo Chats (Semana 6.1)",
                        Toast.LENGTH_SHORT).show();
                intent = new Intent(HomeActivity.this, ChatActivity.class);
                startActivity(intent);
                return true;
            }
            // SEMANA 5.1: Manejar la navegación a la pantalla de Cuenta
            else if (itemId == R.id.navigation_account) {
                intent = new Intent(HomeActivity.this, CuentaActivity.class);
                startActivity(intent);
                return true;
            }
            // SEMANA 6: Manejar la navegación a la pantalla de Anuncios
            else if (itemId == R.id.navigation_ads) {
                intent = new Intent(HomeActivity.this, AnunciosActivity.class);
                startActivity(intent);
                return true;
            }
            // SEMANA 6: Si presionamos Home, no hacemos nada (ya estamos aquí)
            else if (itemId == R.id.navigation_home) {
                return true;
            }
            // Revertido a la lógica de Toast temporal (para otros ítems)
            Toast.makeText(HomeActivity.this, "Navegando a: " + item.getTitle(),
                    Toast.LENGTH_SHORT).show();
            return true;
        });
        // --- FIN SEMANA 6.1 (SE INCLUYÓ EN EL LISTENER EXISTENTE) ---
        // 2. Configuración CLAVE del RecyclerView
        configurarRecyclerView();
        // SEMANA 5: Configuración del Listener del Buscador para escuchar los cambios de texto
        configurarBuscador();
        // 3. Botón FAB para publicar producto
        fabPublicar.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PublicarActivity.class);
            startActivity(intent);
        });
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
                    // No hacemos nada al enviar la búsqueda (ej: presionar Enter)
                    return false;
                }
                @Override
                public boolean onQueryTextChange(String newText) {
                    // SEMANA 5: Lógica del filtro - llama al método filtrar del ProductoAdapter
                    if (productoAdapter != null) {
                        productoAdapter.filtrar(newText);
                    }
                    return true;
                }
            });
        }
    }
    // 2. Configura el RecyclerView con el LayoutManager y el ProductoAdapter
    private void configurarRecyclerView() {
        // 2.1. Define cómo se organizan los ítems (en este caso, en una lista vertical)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 2.2. Obtiene datos de prueba
        List<Producto> productosDePrueba = cargarProductosDePrueba();
        // 2.3. Crea el adaptador y lo conecta al RecyclerView
        // SEMANA 5: Almacenamos la referencia en la variable de clase (productoAdapter) para usarla en el filtro
        productoAdapter = new ProductoAdapter(this, productosDePrueba);
        recyclerView.setAdapter(productoAdapter);
    }
    // 3. Crea una lista de productos ficticios para probar el RecyclerView (Simulación de DB).
    private List<Producto> cargarProductosDePrueba() {
        List<Producto> lista = new ArrayList<>();
        // ID, Nombre, Descripción (se omite aquí), Precio, ImagenUrl (ficticia)
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
    // --- Menú Superior (Toolbar) ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflamos el menú que contiene solo el ítem de logout
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            Toast.makeText(HomeActivity.this, "Cerrando Sesión (Simulación)",
                    Toast.LENGTH_SHORT).show();
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
