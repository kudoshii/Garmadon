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

    // Bandera para indicar el rol
    private boolean esVendedor;

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

        // VALIDACIÓN DE DATOS DEL INTENT
        if (TextUtils.isEmpty(vendedorId) || TextUtils.isEmpty(productoId)) {
            Toast.makeText(this, "Error: Datos de chat incompletos", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Determinar el rol una sola vez al inicio
        esVendedor = currentUserId.equals(vendedorId);

        inicializarVistas();
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

                String vendedorDelProducto = snapshot.child("vendedorId").getValue(String.class);
                if (vendedorDelProducto == null || !vendedorDelProducto.equals(vendedorId)) {
                    Toast.makeText(ChatActivity.this,
                            "Error: Información del vendedor inconsistente",
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                if (TextUtils.isEmpty(productoNombre)) {
                    productoNombre = snapshot.child("nombre").getValue(String.class);
                    if (productoNombre == null) {
                        productoNombre = "Producto";
                    }
                }

                // Continuar la configuración
                tvEstadoContacto.setText("Producto: " + (productoNombre != null ? productoNombre : "Cargando..."));

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

        btnBack.setOnClickListener(v -> finish()); // Configurar el botón de regreso

        // Lógica de carga del nombre del contacto
        if (esVendedor) {
            // Si soy el vendedor, necesito cargar el nombre del comprador
            cargarNombreComprador();
        } else {
            // Si soy el comprador, uso el nombre del vendedor que ya tengo
            tvNombreContacto.setText(vendedorNombre != null ? vendedorNombre : "Vendedor");
        }

        // El tvEstadoContacto se actualiza en verificarProductoYContinuar
    }

    private void cargarNombreComprador() {
        // Generamos el ID canónico para buscar el chat
        String compradorIdTemp = currentUserId; // Usamos el ID del vendedor temporalmente
        String vendedorIdTemp = vendedorId; // Usamos el ID del comprador temporalmente

        // CRÍTICO: Debemos pasar los IDs en el orden que se requiere para generar el ID canónico.
        // Pero como Chat.generarChatId() maneja el orden, solo pasamos los dos IDs de usuario.
        chatId = Chat.generarChatId(currentUserId, vendedorId, productoId);

        chatsRef.child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Si el vendedor abre el chat, necesitamos el ID del NO-vendedor (el comprador)
                    String posibleCompradorId = snapshot.child("compradorId").getValue(String.class);
                    String compradorNombre = snapshot.child("compradorNombre").getValue(String.class);

                    if (compradorNombre != null) {
                        tvNombreContacto.setText(compradorNombre);
                    } else if (posibleCompradorId != null) {
                        // Si no hay nombre, cargarlo desde users
                        usersRef.child(posibleCompradorId).child("nombre")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snap) {
                                        String nombre = snap.getValue(String.class);
                                        tvNombreContacto.setText(nombre != null ? nombre : "Usuario");
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        tvNombreContacto.setText("Usuario");
                                    }
                                });
                    } else {
                        tvNombreContacto.setText("Usuario");
                    }
                } else {
                    // Si el chat aún no existe (el comprador no ha escrito), mostrar genérico
                    tvNombreContacto.setText("Esperando Comprador");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvNombreContacto.setText("Usuario");
            }
        });
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
        // Generar el ID canónico del chat
        chatId = Chat.generarChatId(currentUserId, vendedorId, productoId);
        mensajesRef = chatsRef.child(chatId).child("mensajes");

        chatsRef.child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // *** LÓGICA DE SEGURIDAD CONTRA CHATS DUPLICADOS ***
                    // Solo el comprador puede crear el chat la primera vez.
                    if (!esVendedor) {
                        crearNuevoChat();
                    } else {
                        Log.d(TAG, "Vendedor accediendo a chat nuevo. Esperando primer mensaje del comprador.");
                        // El vendedor puede ver la pantalla, pero el nodo no se crea hasta el comprador.
                    }
                } else {
                    // El chat existe. Verificar si el usuario es un participante válido
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
        if (TextUtils.isEmpty(currentUserNombre) || TextUtils.isEmpty(vendedorNombre)) {
            Toast.makeText(this, "Error: Datos incompletos para crear el chat",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Al crearse el chat, el currentUserId es el Comprador
        Chat nuevoChat = new Chat(
                productoId,
                productoNombre,
                currentUserId,    // Comprador ID (currentUserId)
                currentUserNombre,// Comprador Nombre (currentUserNombre)
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
                    // No finalizamos si falla la creación, permitimos que intente enviar el mensaje
                    // o que el usuario vuelva a intentar.
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
        btnEnviarMensaje.setOnClickListener(v -> enviarMensaje());
        // ... (resto de listeners)
    }

    private void cargarMensajes() {
        // ... (Tu implementación de cargarMensajes con ChildEventListener es correcta)
        mensajesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Mensaje mensaje = snapshot.getValue(Mensaje.class);
                if (mensaje != null) {
                    mensaje.setId(snapshot.getKey());
                    if (!TextUtils.isEmpty(mensaje.getContenido()) &&
                            !TextUtils.isEmpty(mensaje.getRemitenteId())) {
                        listaMensajes.add(mensaje);
                        mensajeAdapter.notifyItemInserted(listaMensajes.size() - 1);
                        recyclerView.scrollToPosition(listaMensajes.size() - 1);
                    }
                }
            }
            // ... (onChildChanged, onChildRemoved, etc., son correctos)
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) { /* Lógica de eliminación */ }
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar mensajes: " + error.getMessage());
                Toast.makeText(ChatActivity.this, "Error al cargar mensajes", Toast.LENGTH_SHORT).show();
            }
        };

        mensajesRef.addChildEventListener(mensajesListener);
    }

    private void enviarMensaje() {
        String texto = etMensaje.getText().toString().trim();

        if (TextUtils.isEmpty(texto)) {
            Toast.makeText(this, "Escriba un mensaje", Toast.LENGTH_SHORT).show();
            return;
        }

        // ... (El resto de tus validaciones de longitud y sanitización son correctas) ...
        String sanitizedText = texto.replaceAll("\\s+", " ").trim();
        final String mensajeContenido = sanitizedText;

        if (TextUtils.isEmpty(chatId) || mensajesRef == null) {
            Toast.makeText(this, "Error: Chat no inicializado correctamente", Toast.LENGTH_SHORT).show();
            return;
        }

        String mensajeId = mensajesRef.push().getKey();
        if (mensajeId == null) {
            Toast.makeText(this, "Error al generar ID de mensaje", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();
        Mensaje nuevoMensaje = new Mensaje(mensajeId, currentUserId, mensajeContenido, timestamp);

        btnEnviarMensaje.setEnabled(false);

        mensajesRef.child(mensajeId).setValue(nuevoMensaje)
                .addOnSuccessListener(aVoid -> {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("ultimoMensaje", mensajeContenido);
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