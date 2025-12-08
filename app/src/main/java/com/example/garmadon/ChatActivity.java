package com.example.garmadon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.example.garmadon.adapter.MensajeAdapter;
import com.example.garmadon.model.Chat;
import com.example.garmadon.model.Mensaje;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

    private RecyclerView recyclerView;
    private EditText etMensaje;
    private ImageButton btnEnviarMensaje;
    private ImageButton btnEnviarImagen;
    private TextView tvNombreContacto;
    private TextView tvEstadoContacto;
    private ImageButton btnBack;

    private FirebaseAuth mAuth;
    private DatabaseReference chatsRef;
    private DatabaseReference mensajesRef;
    private DatabaseReference usersRef;
    private DatabaseReference productosRef;
    private ChildEventListener mensajesListener;

    private String chatId;
    private String vendedorId;
    private String vendedorNombre;
    private String productoId;
    private String productoNombre;
    private String currentUserId;
    private String currentUserNombre;

    private MensajeAdapter mensajeAdapter;
    private List<Mensaje> listaMensajes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Debe iniciar sesión", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        currentUserId = currentUser.getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        productosRef = FirebaseDatabase.getInstance().getReference("productos");

        vendedorId = getIntent().getStringExtra("vendedor_id");
        vendedorNombre = getIntent().getStringExtra("vendedor_nombre");
        productoId = getIntent().getStringExtra("producto_id");
        productoNombre = getIntent().getStringExtra("producto_nombre");

        // FIX 1: Validación de datos del intent
        if (TextUtils.isEmpty(vendedorId) || TextUtils.isEmpty(productoId)) {
            Toast.makeText(this, "Error: Datos de chat incompletos", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // FIX 2: Validar que el vendedor no sea el usuario actual
        if (currentUserId.equals(vendedorId)) {
            Toast.makeText(this, "No puedes chatear con tu propio producto", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        inicializarVistas();

        // FIX 3: Verificar que el producto existe antes de continuar
        verificarProductoYContinuar();
    }

    private void verificarProductoYContinuar() {
        productosRef.child(productoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(ChatActivity.this,
                            "Error: El producto no existe o fue eliminado",
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                // Verificar que el vendedorId del producto coincide
                String vendedorDelProducto = snapshot.child("vendedorId").getValue(String.class);
                if (vendedorDelProducto == null || !vendedorDelProducto.equals(vendedorId)) {
                    Toast.makeText(ChatActivity.this,
                            "Error: Información del vendedor inconsistente",
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                // Si el nombre del producto no se pasó, obtenerlo
                if (TextUtils.isEmpty(productoNombre)) {
                    productoNombre = snapshot.child("nombre").getValue(String.class);
                    if (productoNombre == null) {
                        productoNombre = "Producto";
                    }
                }

                // Todo validado, continuar
                cargarNombreUsuarioActual();
                configurarRecyclerView();
                configurarListeners();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al verificar producto: " + error.getMessage());
                Toast.makeText(ChatActivity.this,
                        "Error al cargar información del producto",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void inicializarVistas() {
        tvNombreContacto = findViewById(R.id.tvContactName);
        tvEstadoContacto = findViewById(R.id.tvContactStatus);
        recyclerView = findViewById(R.id.recyclerViewChat);
        etMensaje = findViewById(R.id.editTextMensaje);
        btnEnviarMensaje = findViewById(R.id.btnEnviar);
        btnEnviarImagen = findViewById(R.id.btnAttachImage);
        btnBack = findViewById(R.id.btnBack);

        // Configurar cabecera con valores seguros
        tvNombreContacto.setText(vendedorNombre != null ? vendedorNombre : "Usuario");
        tvEstadoContacto.setText("Producto: " + (productoNombre != null ? productoNombre : "Cargando..."));
    }

    private void cargarNombreUsuarioActual() {
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserNombre = snapshot.child("nombre").getValue(String.class);
                if (currentUserNombre == null || currentUserNombre.trim().isEmpty()) {
                    currentUserNombre = "Usuario";
                }
                inicializarChat();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar nombre de usuario: " + error.getMessage());
                currentUserNombre = "Usuario";
                inicializarChat();
            }
        });
    }

    private void inicializarChat() {
        chatId = Chat.generarChatId(currentUserId, vendedorId, productoId);
        mensajesRef = chatsRef.child(chatId).child("mensajes");

        chatsRef.child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    crearNuevoChat();
                } else {
                    // FIX 4: Validar que el usuario es participante del chat existente
                    DataSnapshot participantes = snapshot.child("participantes");
                    if (participantes.exists() && !participantes.hasChild(currentUserId)) {
                        Toast.makeText(ChatActivity.this,
                                "Error: No tienes permiso para acceder a este chat",
                                Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                cargarMensajes();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al verificar chat: " + error.getMessage());
                Toast.makeText(ChatActivity.this, "Error al cargar chat", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void crearNuevoChat() {
        // FIX 5: Validar que tenemos todos los datos necesarios
        if (TextUtils.isEmpty(currentUserNombre) || TextUtils.isEmpty(vendedorNombre)) {
            Toast.makeText(this, "Error: Datos incompletos para crear el chat",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Chat nuevoChat = new Chat(
                productoId,
                productoNombre,
                currentUserId,
                currentUserNombre,
                vendedorId,
                vendedorNombre
        );

        chatsRef.child(chatId).setValue(nuevoChat)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Chat creado exitosamente"))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al crear chat: " + e.getMessage());
                    Toast.makeText(ChatActivity.this,
                            "Error al crear el chat. Intente nuevamente.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void configurarRecyclerView() {
        listaMensajes = new ArrayList<>();
        mensajeAdapter = new MensajeAdapter(this, listaMensajes, currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mensajeAdapter);
    }

    private void configurarListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEnviarMensaje.setOnClickListener(v -> enviarMensaje());

        btnEnviarImagen.setOnClickListener(v -> {
            Toast.makeText(this, "Función de imágenes próximamente", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarMensajes() {
        mensajesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Mensaje mensaje = snapshot.getValue(Mensaje.class);
                if (mensaje != null) {
                    mensaje.setId(snapshot.getKey());

                    // FIX 6: Validar que el mensaje tiene datos válidos
                    if (!TextUtils.isEmpty(mensaje.getContenido()) &&
                            !TextUtils.isEmpty(mensaje.getRemitenteId())) {
                        listaMensajes.add(mensaje);
                        mensajeAdapter.notifyItemInserted(listaMensajes.size() - 1);
                        recyclerView.scrollToPosition(listaMensajes.size() - 1);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                Mensaje mensaje = snapshot.getValue(Mensaje.class);
                if (mensaje != null && !TextUtils.isEmpty(snapshot.getKey())) {
                    mensaje.setId(snapshot.getKey());
                    for (int i = 0; i < listaMensajes.size(); i++) {
                        if (listaMensajes.get(i).getId().equals(mensaje.getId())) {
                            listaMensajes.set(i, mensaje);
                            mensajeAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String mensajeId = snapshot.getKey();
                if (mensajeId != null) {
                    for (int i = 0; i < listaMensajes.size(); i++) {
                        if (listaMensajes.get(i).getId().equals(mensajeId)) {
                            listaMensajes.remove(i);
                            mensajeAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar mensajes: " + error.getMessage());
                Toast.makeText(ChatActivity.this, "Error al cargar mensajes", Toast.LENGTH_SHORT).show();
            }
        };

        mensajesRef.addChildEventListener(mensajesListener);
    }

    private void enviarMensaje() {
        String texto = etMensaje.getText().toString().trim();

        // FIX 7: Validaciones más robustas
        if (TextUtils.isEmpty(texto)) {
            Toast.makeText(this, "Escriba un mensaje", Toast.LENGTH_SHORT).show();
            return;
        }

        if (texto.length() > 1000) {
            Toast.makeText(this, "El mensaje es demasiado largo (máximo 1000 caracteres)",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // FIX 8: Sanitizar el input para evitar problemas
        String sanitizedText = texto.replaceAll("\\s+", " ").trim();

        if (sanitizedText.isEmpty()) {
            Toast.makeText(this, "El mensaje no debe contener solo espacios", Toast.LENGTH_SHORT).show();
            return;
        }

        final String mensajeContenido = sanitizedText;


        String mensajeId = mensajesRef.push().getKey();
        if (mensajeId == null) {
            Toast.makeText(this, "Error al generar ID de mensaje", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();
        // Usamos mensajeContenido en el constructor
        Mensaje nuevoMensaje = new Mensaje(mensajeId, currentUserId, mensajeContenido, timestamp);

        // Deshabilitar botón de enviar mientras se envía
        btnEnviarMensaje.setEnabled(false);

        mensajesRef.child(mensajeId).setValue(nuevoMensaje)
                .addOnSuccessListener(aVoid -> {
                    // Aquí, mensajeContenido es accesible sin error
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("ultimoMensaje", mensajeContenido); // Usar la variable final
                    updates.put("ultimoMensajeTimestamp", timestamp);

                    chatsRef.child(chatId).updateChildren(updates)
                            .addOnCompleteListener(task -> {
                                btnEnviarMensaje.setEnabled(true);
                                if (task.isSuccessful()) {
                                    etMensaje.setText("");
                                    Log.d(TAG, "Mensaje enviado correctamente");
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    btnEnviarMensaje.setEnabled(true);
                    Log.e(TAG, "Error al enviar mensaje: " + e.getMessage());
                    Toast.makeText(ChatActivity.this,
                            "Error al enviar mensaje. Verifique su conexión.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mensajesRef != null && mensajesListener != null) {
            mensajesRef.removeEventListener(mensajesListener);
        }
    }
}