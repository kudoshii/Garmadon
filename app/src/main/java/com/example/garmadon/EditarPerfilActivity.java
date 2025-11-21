package com.example.garmadon;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditarPerfilActivity extends AppCompatActivity {

    // Vistas del Layout
    private Toolbar toolbar;
    private FloatingActionButton fabCambiarImagen;
    private TextInputEditText etNombres;
    private TextInputEditText etFechaNac;
    private TextInputEditText etTelefono;
    private CountryCodePicker ccpSelectorCod;
    private MaterialButton btnActualizar;
    private ImageView imgPerfil;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private StorageReference storageRef; // Se inicializa en onCreate()

    // ActivityResultLaunchers
    private ActivityResultLauncher<String[]> concederPermisoCamara;
    private ActivityResultLauncher<String> concederPermisoAlmacenamiento;
    private ActivityResultLauncher<Intent> resultadoCamara_ARL;
    private ActivityResultLauncher<String> lanzarGaleria;

    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        // Inicialización de Firebase y ProgressDialog
        firebaseAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference(); // Inicialización de Storage
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Por favor espere");
        progressDialog.setCanceledOnTouchOutside(false);

        // 1. Inicializar TODOS los lanzadores
        inicializarLanzadores();

        // 2. Inicializar Vistas y Listeners
        inicializarVistas();
        configurarToolbar();
        configurarListeners();

        // 3. Cargar datos al iniciar la actividad
        cargarInfo();
    }

    private void inicializarLanzadores() {

        // --- 1. LANZADOR DE PERMISOS DE CÁMARA (y almacenamiento para APIs antiguas) ---
        concederPermisoCamara = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                resultado -> {
                    boolean concedidoTodos = true;
                    // Verifica si se concedió alguno de los permisos
                    for (Boolean seConcede : resultado.values()) {
                        if (!seConcede) {
                            concedidoTodos = false;
                            break;
                        }
                    }
                    if (concedidoTodos) {
                        imageCamara(); // Llama a la función para tomar la foto
                    } else {
                        Toast.makeText(this,
                                "El permiso de la cámara ha sido denegado",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // --- 2. LANZADOR DE RESULTADO DE CÁMARA (Para procesar la imagen tomada) ---
        resultadoCamara_ARL = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                resultado -> {
                    if (resultado.getResultCode() == Activity.RESULT_OK) {
                        // Si la foto se tomó correctamente, el URI ya está en 'imageUri'
                        if (imageUri != null) {
                            subirImagenStorage(imageUri);
                        } else {
                            Toast.makeText(this, "Error: URI de imagen nula después de la cámara.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Toma de foto cancelada", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // --- 3. LANZADOR DE PERMISOS DE ALMACENAMIENTO (solo para APIs antiguas) ---
        concederPermisoAlmacenamiento = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                esConcedido -> {
                    if (esConcedido) {
                        imagenGaleria(); // Llama a la función para abrir la galería
                    } else {
                        Toast.makeText(this,
                                "El permiso de almacenamiento ha sido denegado",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // --- 4. LANZADOR DE GALERÍA (Contrato: GetContent) ---
        lanzarGaleria = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri; // Asignamos la URI de la galería a la variable de clase
                        subirImagenStorage(imageUri);
                    } else {
                        Toast.makeText(this, "Selección de galería cancelada",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }


    //Función para tomar una foto con la cámara
    private void imageCamara() {
        // 1. Crear URI temporal para que la cámara guarde la foto
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Imagen_Perfil_" +
                System.currentTimeMillis());
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Imagen de perfil del usuario");

                imageUri =
                        getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                contentValues);

        if (imageUri == null) {
            Toast.makeText(this, "Error al crear URI para la imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Crear y configurar el Intent de la cámara
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        // 3. Lanzar la actividad de la cámara usando el ARL de resultado
        resultadoCamara_ARL.launch(intent);
    }

    // Función para seleccionar una imagen de la galería
    private void imagenGaleria() {
        // Lanzar la galería usando el ARL de GetContent
        lanzarGaleria.launch("image/*");
    }

    private void inicializarVistas() {
        toolbar = findViewById(R.id.toolbar_editar_perfil);
        fabCambiarImagen = findViewById(R.id.fab_cambiar_imagen);
        etNombres = findViewById(R.id.et_nombres);
        etFechaNac = findViewById(R.id.et_fecha_nac);
        etTelefono = findViewById(R.id.et_telefono);
        btnActualizar = findViewById(R.id.btn_actualizar);
        imgPerfil = findViewById(R.id.img_perfil_editar);
        ccpSelectorCod = findViewById(R.id.ccp_selector_cod);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void configurarListeners() {
        etFechaNac.setOnClickListener(v -> mostrarDatePickerDialog());

        fabCambiarImagen.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, fabCambiarImagen);
            popupMenu.getMenu().add(0, 1, 1, "Cámara");
            popupMenu.getMenu().add(0, 2, 2, "Galería");
            popupMenu.show();

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == 1) { // Cámara
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // API 33+ (solo se necesita el permiso de cámara)
                        concederPermisoCamara.launch(new String[]{
                                Manifest.permission.CAMERA
                        });
                    } else {
                        // API < 33 (se necesita cámara y almacenamiento)
                        concederPermisoCamara.launch(new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        });
                    }
                } else if (itemId == 2) { // Galeria
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // API 33+ (no se necesita permiso para GetContent)
                        imagenGaleria();
                    } else {
                        // API < 33 (se necesita permiso de almacenamiento para la galería)

                        concederPermisoAlmacenamiento.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }
                return true;
            });
        });

        btnActualizar.setOnClickListener(v -> {
            guardarCambios();
        });
    }

    private void mostrarDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int anio = c.get(Calendar.YEAR);
        int mes = c.get(Calendar.MONTH);
        int dia = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    String fechaSeleccionada = String.format("%02d/%02d/%d", dayOfMonth,
                            (monthOfYear + 1), year);
                    etFechaNac.setText(fechaSeleccionada);
                }, anio, mes, dia);
        datePickerDialog.show();
    }

    private void guardarCambios() {
        // Lógica de validación y actualización en Firebase (se mantiene)
        String nombres = etNombres.getText().toString().trim();
        String fechaNac = etFechaNac.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String codigoTelefono = ccpSelectorCod.getSelectedCountryCode();

        if (nombres.isEmpty() || fechaNac.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Actualizando perfil...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("nombre", "" + nombres);
        hashMap.put("fecha_nac", "" + fechaNac);
        hashMap.put("telefono", "" + telefono);
        hashMap.put("codigoTelefono", "" + codigoTelefono);

        String uid = firebaseAuth.getUid();
        if (uid != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users"); //Colección consistente: users
            ref.child(uid)
                    .updateChildren(hashMap)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditarPerfilActivity.this, "Perfil actualizado exitosamente!",
                                Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditarPerfilActivity.this, "Error al actualizar: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        Log.e("FIREBASE_UPDATE", "Error al actualizar perfil", e);
                    });
        } else {
            progressDialog.dismiss();
            Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarInfo(){
        // Lógica de carga de datos desde Firebase (se mantiene)
        progressDialog.setMessage("Cargando datos...");
        progressDialog.show();
        String uid = firebaseAuth.getUid();

        if (uid == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users"); //Colección consistente: users
        ref.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                String nombre = snapshot.child("nombre").getValue(String.class);
                String imagenUrl = snapshot.child("urlImagenPerfil").getValue(String.class);
                String fechaNac = snapshot.child("fecha_nac").getValue(String.class);
                String telefono = snapshot.child("telefono").getValue(String.class);
                String codTelefono = snapshot.child("codigoTelefono").getValue(String.class);

                if (nombre != null) { etNombres.setText(nombre); }
                if (fechaNac != null) { etFechaNac.setText(fechaNac); }
                if (telefono != null) { etTelefono.setText(telefono); }

                // CCP
                if (codTelefono != null && !codTelefono.isEmpty()) {
                    try {
                        int code = Integer.parseInt(codTelefono);
                        ccpSelectorCod.setCountryForPhoneCode(code);
                    } catch (Exception e) {
                        ccpSelectorCod.setCountryForNameCode("CL");
                        Log.e("CCP_ERROR", "Error al parsear código de teléfono", e);
                    }
                } else {
                    ccpSelectorCod.setCountryForNameCode("CL");
                }

                // Cargar imagen de perfil con Glide
                if (imagenUrl != null && !imagenUrl.isEmpty()) {
                    try {
                        Glide.with(EditarPerfilActivity.this)
                                .load(imagenUrl)
                                .error(R.drawable.perfil)
                                .into(imgPerfil);
                    } catch (Exception e) {
                        Log.e("GLIDE_ERROR", "Error al intentar cargar la imagen", e);
                    }
                } else {
                    Glide.with(EditarPerfilActivity.this)
                            .load(R.drawable.perfil)
                            .into(imgPerfil);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(EditarPerfilActivity.this, "Fallo al cargar los datos: " +
                        error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("FIREBASE_READ", "Fallo al cargar datos de perfil", error.toException());
            }
        });
    }
    public void subirImagenStorage(Uri imageUri) {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        // 1. Validar que la URI de la imagen y el usuario existan
        if (imageUri == null || user == null) {
            Toast.makeText(EditarPerfilActivity.this, "Error: URI de imagen o usuario no válidos.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Subiendo imagen a Storage"); // Subiendo imagen a Storage
        progressDialog.setCancelable(false);
        progressDialog.show();

        // 2. Definir la ruta en Storage: "imagenesPerfil/{uid_del_usuario}"
        String rutaImagen = "imagenesPerfil/" + user.getUid();
        final StorageReference ref = storageRef.child(rutaImagen);

        // 3. Iniciar la tarea de subida
        ref.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Tarea de subida exitosa. Ahora obtener la URL de descarga.
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Aquí NO cerramos el dialog, lo cerramos en actualizarImagenBD()
                                String urlImagenCargada = uri.toString();
                                Log.d("Storage", "URL de descarga obtenida: " + urlImagenCargada);

                                // 4. Llamar a la función para actualizar la BD
                                actualizarImagenBD(urlImagenCargada);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                progressDialog.dismiss();
                                String errorMessage = "Error al obtener la URL de descarga: " +
                                        exception.getMessage();
                                Log.e("Storage", errorMessage);
                                Toast.makeText(EditarPerfilActivity.this, errorMessage,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 5. Manejar el fallo de la subida
                        progressDialog.dismiss();
                        String errorMessage = "Fallo en la subida: " + e.getMessage();
                        Log.e("Storage", errorMessage);
                        Toast.makeText(EditarPerfilActivity.this, errorMessage,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizarImagenBD(String urlImagenCargada) {
        // Aseguramos que haya un usuario autenticado
        if (firebaseAuth.getCurrentUser() == null) {
            progressDialog.dismiss();
            Toast.makeText(EditarPerfilActivity.this, "Error: No hay usuario autenticado.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Crear el mapa (HashMap) con el dato a actualizar
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("urlImagenPerfil", urlImagenCargada);

        // 2. Obtener la referencia a la ubicación del usuario en la base de datos
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference userRef = ref.child(firebaseAuth.getCurrentUser().getUid());

        // 3. Realizar la actualización
        userRef.updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Éxito: Se actualizó la BD. Actualizar la vista con Glide
                        progressDialog.dismiss();
                        try {
                            // Cargar la nueva imagen localmente para dar feedback inmediato al usuario
                            // Usamos el 'imageUri' que se capturó o seleccionó para la carga inmediata
                            Glide.with(EditarPerfilActivity.this)
                                    .load(imageUri)
                                    .error(R.drawable.perfil)
                                    .into(imgPerfil);
                        } catch (Exception e) {
                            Log.e("GLIDE_UPDATE", "Error al cargar la imagen después de la BD", e);
                        }
                        Toast.makeText(EditarPerfilActivity.this, "Su imagen de perfil se ha actualizado",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Fallo: Ocurrió un error
                        progressDialog.dismiss();
                        String errorMessage = "Error al actualizar la BD: " + e.getMessage();
                        Toast.makeText(EditarPerfilActivity.this, errorMessage,
                                Toast.LENGTH_LONG).show();
                        Log.e("DB_UPDATE_FAIL", "Error al actualizar Realtime DB", e);
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
