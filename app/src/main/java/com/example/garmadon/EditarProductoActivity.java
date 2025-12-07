package com.example.garmadon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditarProductoActivity extends AppCompatActivity {
    private static final String TAG = "EditarProducto";

    private TextInputEditText etNombre, etPrecio, etDireccion, etDescripcion, etMarca;
    private AutoCompleteTextView etCategoria, etCondicion;
    private MaterialButton btnActualizar;
    private ProgressDialog progressDialog;

    private DatabaseReference productosRef;
    private String productoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_producto);

        Toolbar toolbar = findViewById(R.id.toolbar_editar_producto);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Editar Producto");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        inicializarVistas();
        configurarSpinners();

        productosRef = FirebaseDatabase.getInstance().getReference("productos");
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Por favor espere");
        progressDialog.setCanceledOnTouchOutside(false);

        productoId = getIntent().getStringExtra("producto_id");

        if (productoId != null) {
            cargarDatosProducto();
        } else {
            Toast.makeText(this, "Error: ID de producto no válido", Toast.LENGTH_LONG).show();
            finish();
        }

        btnActualizar.setOnClickListener(v -> {
            if (validarCampos()) {
                actualizarProducto();
            }
        });
    }

    private void inicializarVistas() {
        etNombre = findViewById(R.id.et_editar_nombre);
        etPrecio = findViewById(R.id.et_editar_precio);
        etDireccion = findViewById(R.id.et_editar_direccion);
        etDescripcion = findViewById(R.id.et_editar_descripcion);
        etMarca = findViewById(R.id.et_editar_marca);
        etCategoria = findViewById(R.id.spinner_editar_categoria);
        etCondicion = findViewById(R.id.spinner_editar_condicion);
        btnActualizar = findViewById(R.id.btn_actualizar_producto);
    }

    private void configurarSpinners() {
        ArrayAdapter<String> adaptadorCat = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, Constantes.categorias);
        etCategoria.setAdapter(adaptadorCat);
        etCategoria.setOnClickListener(v -> etCategoria.showDropDown());

        ArrayAdapter<String> adaptadorCon = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, Constantes.condiciones);
        etCondicion.setAdapter(adaptadorCon);
        etCondicion.setOnClickListener(v -> etCondicion.showDropDown());
    }

    private void cargarDatosProducto() {
        progressDialog.setMessage("Cargando datos del producto...");
        progressDialog.show();

        productosRef.child(productoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();

                if (snapshot.exists()) {
                    String nombre = snapshot.child("nombre").getValue(String.class);
                    Double precio = snapshot.child("precio").getValue(Double.class);
                    String direccion = snapshot.child("direccion").getValue(String.class);
                    String descripcion = snapshot.child("descripcion").getValue(String.class);
                    String marca = snapshot.child("marca").getValue(String.class);
                    String categoria = snapshot.child("categoria").getValue(String.class);
                    String condicion = snapshot.child("condicion").getValue(String.class);

                    if (nombre != null) etNombre.setText(nombre);
                    if (precio != null) etPrecio.setText(String.valueOf(precio));
                    if (direccion != null) etDireccion.setText(direccion);
                    if (descripcion != null) etDescripcion.setText(descripcion);
                    if (marca != null) etMarca.setText(marca);
                    if (categoria != null) etCategoria.setText(categoria, false);
                    if (condicion != null) etCondicion.setText(condicion, false);
                } else {
                    Toast.makeText(EditarProductoActivity.this,
                            "Producto no encontrado", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(EditarProductoActivity.this,
                        "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error al cargar producto: " + error.getMessage());
            }
        });
    }

    private boolean validarCampos() {
        String nombre = etNombre.getText().toString().trim();
        String precio = etPrecio.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String categoria = etCategoria.getText().toString().trim();
        String condicion = etCondicion.getText().toString().trim();
        String marca = etMarca.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("El nombre es obligatorio");
            return false;
        }
        if (TextUtils.isEmpty(marca)) {
            etMarca.setError("La marca es obligatoria");
            return false;
        }
        if (TextUtils.isEmpty(categoria)) {
            etCategoria.setError("La categoría es obligatoria");
            return false;
        }
        if (TextUtils.isEmpty(condicion)) {
            etCondicion.setError("La condición es obligatoria");
            return false;
        }
        if (TextUtils.isEmpty(precio) || !precio.matches("^[0-9]+([.,][0-9]{1,2})?$")) {
            etPrecio.setError("Ingresa un precio válido");
            return false;
        }
        if (TextUtils.isEmpty(direccion) || direccion.length() < 5) {
            etDireccion.setError("La dirección debe ser detallada");
            return false;
        }
        if (TextUtils.isEmpty(descripcion) || descripcion.length() < 10) {
            etDescripcion.setError("La descripción debe tener al menos 10 caracteres");
            return false;
        }

        return true;
    }

    private void actualizarProducto() {
        progressDialog.setMessage("Actualizando producto...");
        progressDialog.show();

        String nombre = etNombre.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String categoria = etCategoria.getText().toString().trim();
        String condicion = etCondicion.getText().toString().trim();
        String marca = etMarca.getText().toString().trim();

        double precio = Double.parseDouble(precioStr.replace(",", "."));

        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nombre);
        updates.put("precio", precio);
        updates.put("direccion", direccion);
        updates.put("descripcion", descripcion);
        updates.put("categoria", categoria);
        updates.put("condicion", condicion);
        updates.put("marca", marca);

        productosRef.child(productoId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "¡Producto actualizado exitosamente!",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error al actualizar: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error al actualizar producto: " + e.getMessage());
                });
    }
}