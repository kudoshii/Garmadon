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

public class ListaChatsActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

    // Vistas
    private RecyclerView recyclerView;
    private EditText etMensaje;
    private ImageButton btnEnviarMensaje;
    private ImageButton btnEnviarImagen;
    private TextView tvNombreContacto;
    private TextView tvEstadoContacto;
    private ImageButton btnBack;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference chatsRef;
    private DatabaseReference mensajesRef;
    private DatabaseReference usersRef;
    private ChildEventListener mensajesListener;

    // Datos
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

        // Inicializar Firebase
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

        // Obtener datos del intent
        vendedorId = getIntent().getStringExtra("vendedor_id");
        vendedorNombre = getIntent().getStringExtra("vendedor_nombre");
        productoId = getIntent().getStringExtra("producto_id");
        productoNombre = getIntent().getStringExtra("producto_nombre");

        if (vendedorId == null || productoId == null) {
            Toast.makeText(this, "Error: Datos de chat incompletos", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Si el usuario actual es el vendedor, mostrar error
        if (currentUserId.equals(vendedorId)) {
            Toast.makeText(this, "No puedes chatear con tu propio producto", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        inicializarVistas();
        cargarNombreUsuarioActual();
        configurarRecyclerView();
        configurarListeners();
    }

    private void inicializarVistas() {
        tvNombreContacto = findViewById(R.id.tvContactName);
        tvEstadoContacto = findViewById(R.id.tvContactStatus);
        recyclerView = findViewById(R.id.recyclerViewChat);
        etMensaje = findViewById(R.id.editTextMensaje);
        btnEnviarMensaje = findViewById(R.id.btnEnviar);
        btnEnviarImagen = findViewById(R.id.btnAttachImage);
        btnBack = findViewById(R.id.btnBack);

        // Configurar cabecera
        tvNombreContacto.setText(vendedorNombre != null ? vendedorNombre : "Usuario");
        tvEstadoContacto.setText("Producto: " + (productoNombre != null ? productoNombre : ""));
    }

    private void cargarNombreUsuarioActual() {
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserNombre = snapshot.child("nombre").getValue(String.class);
                if (currentUserNombre == null) {
                    currentUserNombre = "Usuario";
                }
                // Ahora que tenemos el nombre, inicializamos el chat
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
        // Generar ID único para el chat
        chatId = Chat.generarChatId(currentUserId, vendedorId, productoId);
        mensajesRef = chatsRef.child(chatId).child("mensajes");

        // Verificar si el chat existe, si no, crearlo
        chatsRef.child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    crearNuevoChat();
                }
                cargarMensajes();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al verificar chat: " + error.getMessage());
                Toast.makeText(ListaChatsActivity.this, "Error al cargar chat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void crearNuevoChat() {
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
                .addOnFailureListener(e -> Log.e(TAG, "Error al crear chat: " + e.getMessage()));
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
                    listaMensajes.add(mensaje);
                    mensajeAdapter.notifyItemInserted(listaMensajes.size() - 1);
                    recyclerView.scrollToPosition(listaMensajes.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                Mensaje mensaje = snapshot.getValue(Mensaje.class);
                if (mensaje != null) {
                    mensaje.setId(snapshot.getKey());
                    // Buscar y actualizar el mensaje
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
                for (int i = 0; i < listaMensajes.size(); i++) {
                    if (listaMensajes.get(i).getId().equals(mensajeId)) {
                        listaMensajes.remove(i);
                        mensajeAdapter.notifyItemRemoved(i);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar mensajes: " + error.getMessage());
                Toast.makeText(ListaChatsActivity.this, "Error al cargar mensajes", Toast.LENGTH_SHORT).show();
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

        // Crear el mensaje
        String mensajeId = mensajesRef.push().getKey();
        if (mensajeId == null) {
            Toast.makeText(this, "Error al generar ID de mensaje", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();
        Mensaje nuevoMensaje = new Mensaje(mensajeId, currentUserId, texto, timestamp);

        // Guardar el mensaje
        mensajesRef.child(mensajeId).setValue(nuevoMensaje)
                .addOnSuccessListener(aVoid -> {
                    // Actualizar el último mensaje del chat
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("ultimoMensaje", texto);
                    updates.put("ultimoMensajeTimestamp", timestamp);

                    chatsRef.child(chatId).updateChildren(updates);

                    // Limpiar el campo de texto
                    etMensaje.setText("");

                    Log.d(TAG, "Mensaje enviado correctamente");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al enviar mensaje: " + e.getMessage());
                    Toast.makeText(ListaChatsActivity.this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remover el listener para evitar memory leaks
        if (mensajesRef != null && mensajesListener != null) {
            mensajesRef.removeEventListener(mensajesListener);
        }
    }
}