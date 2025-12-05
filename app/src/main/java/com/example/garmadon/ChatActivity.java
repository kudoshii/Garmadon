package com.example.garmadon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.garmadon.adapter.MensajeAdapter;
import com.example.garmadon.model.Mensaje;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private String CURRENT_USER_ID;
    private String vendedorId;
    private String chatId;
    private String vendedorNombre;

    // Firebase
    private DatabaseReference chatRef;

    // UI
    private RecyclerView recyclerView;
    private EditText etMensaje;
    private ImageButton btnEnviar;
    private ImageButton btnAdjuntar;
    private TextView tvNombreContacto;
    private TextView tvEstadoContacto;

    private MensajeAdapter mensajeAdapter;
    private List<Mensaje> listaMensajes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // UID ACTUAL
        CURRENT_USER_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Datos recibidos desde DetalleProductoActivity
        vendedorId = getIntent().getStringExtra("vendedor_id");
        vendedorNombre = getIntent().getStringExtra("vendedor_nombre");

        // Generamos el chatId (ordenado)
        chatId = generarChatId(CURRENT_USER_ID, vendedorId);

        // Firebase Reference
        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);

        inicializarVistas();
        configurarCabecera();
        configurarRecyclerView();
        configurarListeners();

        escucharMensajes(); // Mensajes en tiempo real
        registrarParticipantes();
    }

    private void inicializarVistas() {
        recyclerView = findViewById(R.id.recyclerViewChat);
        etMensaje = findViewById(R.id.editTextMensaje);
        btnEnviar = findViewById(R.id.btnEnviar);
        btnAdjuntar = findViewById(R.id.btnAttachImage);

        tvNombreContacto = findViewById(R.id.tvContactName);
        tvEstadoContacto = findViewById(R.id.tvContactStatus);
    }

    private void configurarCabecera() {
        tvNombreContacto.setText(vendedorNombre != null ? vendedorNombre : "Contacto");
        tvEstadoContacto.setText("conectado");

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void configurarRecyclerView() {
        listaMensajes = new ArrayList<>();
        mensajeAdapter = new MensajeAdapter(this, listaMensajes, CURRENT_USER_ID);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mensajeAdapter);
    }

    private void configurarListeners() {
        btnEnviar.setOnClickListener(v -> enviarMensaje());

        btnAdjuntar.setOnClickListener(v -> {
            Toast.makeText(this, "Función de enviar imágenes pronto disponible.", Toast.LENGTH_SHORT).show();
        });
    }

    private void enviarMensaje() {
        String texto = etMensaje.getText().toString().trim();

        if (texto.isEmpty()) {
            Toast.makeText(this, "Escribe un mensaje", Toast.LENGTH_SHORT).show();
            return;
        }

        String mensajeId = chatRef.child("mensajes").push().getKey();

        Mensaje mensaje = new Mensaje(
                mensajeId,
                CURRENT_USER_ID,
                texto,
                System.currentTimeMillis()
        );

        // Enviar a Firebase
        chatRef.child("mensajes").child(mensajeId).setValue(mensaje)
                .addOnSuccessListener(a -> etMensaje.setText(""))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show()
                );

        // Actualizar último mensaje
        chatRef.child("ultimoMensaje").setValue(texto);
        chatRef.child("timestamp").setValue(System.currentTimeMillis());
    }

    private void escucharMensajes() {
        chatRef.child("mensajes")
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaMensajes.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Mensaje msj = ds.getValue(Mensaje.class);
                            listaMensajes.add(msj);
                        }

                        mensajeAdapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(listaMensajes.size() - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void registrarParticipantes() {
        chatRef.child("participantes").child(CURRENT_USER_ID).setValue(true);
        chatRef.child("participantes").child(vendedorId).setValue(true);
    }

    private String generarChatId(String id1, String id2) {
        // Ordenar alfabéticamente para evitar duplicados
        return (id1.compareTo(id2) < 0) ? id1 + "_" + id2 : id2 + "_" + id1;
    }
}
