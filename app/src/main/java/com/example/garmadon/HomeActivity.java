package com.example.garmadon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
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

public class HomeActivity extends AppCompatActivity {

    // Vistas principales
    private RecyclerView recyclerView;
    private FloatingActionButton fabPublicar;
    // SEMANA 4: Declaración del componente de navegación inferior
    private BottomNavigationView bottomNav;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Inicializar Vistas
        toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.rv_productos);
        fabPublicar = findViewById(R.id.fab_publicar);

        // --- SEMANA 4: Inicialización
        bottomNav = findViewById(R.id.bottom_navigation_view);

        bottomNav.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);

        // Configuración mínima del listener
        bottomNav.setOnItemSelectedListener(item -> {

            Toast.makeText(HomeActivity.this, "Navegando a: " + item.getTitle(),
                    Toast.LENGTH_SHORT).show();
            return true;
        });
        // --- FIN SEMANA 4 ---


        // 2. Configuración CLAVE del RecyclerView
        configurarRecyclerView();

        // 3. Botón FAB para publicar producto
        fabPublicar.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PublicarActivity.class);
            startActivity(intent);
        });
    }


    //2. Configura el RecyclerView con el LayoutManager y el ProductoAdapter
    private void configurarRecyclerView() {
        // 2.1. Define cómo se organizan los ítems (en este caso, en una lista vertical)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 2.2. Obtiene datos de prueba
        List<Producto> productosDePrueba = cargarProductosDePrueba();

        // 2.3. Crea el adaptador y lo conecta al RecyclerView
        ProductoAdapter adapter = new ProductoAdapter(this, productosDePrueba);
        recyclerView.setAdapter(adapter);
    }

    //3.Crea una lista de productos ficticios para probar el RecyclerView (Simulación de DB).
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

        return lista;
    }


    // --- Menú Superior para Logout  ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
    //Navega de vuelta a LoginActivity
    public void irALogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
