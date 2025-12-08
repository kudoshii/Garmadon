package com.example.garmadon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.garmadon.adapter.ChatAdapter;
import com.example.garmadon.model.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListaChatsActivity extends AppCompatActivity {
    private static final String TAG = "ListaChatsActivity";

    private RecyclerView rvChats;
    private TextView tvNoChats;
    private ImageButton btnBack;

    private FirebaseAuth mAuth;
    private DatabaseReference chatsRef;
    private ValueEventListener chatsListener;

    private List<Chat> listaChats;
    private ChatAdapter chatAdapter;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_chats);

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

        inicializarVistas();
        configurarRecyclerView();
        cargarChats();
    }

    private void inicializarVistas() {
        rvChats = findViewById(R.id.rv_chats);
        tvNoChats = findViewById(R.id.tv_no_chats);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    private void configurarRecyclerView() {
        listaChats = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, listaChats, currentUserId);

        rvChats.setLayoutManager(new LinearLayoutManager(this));
        rvChats.setAdapter(chatAdapter);
    }

    private void cargarChats() {
        Log.d(TAG, "Cargando chats para usuario: " + currentUserId);

        chatsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaChats.clear();

                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    try {
                        Chat chat = chatSnapshot.getValue(Chat.class);
                        if (chat != null) {
                            chat.setChatId(chatSnapshot.getKey());

                            // Solo agregar chats donde el usuario actual es participante
                            if (chat.getParticipantes() != null &&
                                    chat.getParticipantes().containsKey(currentUserId)) {
                                listaChats.add(chat);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al mapear chat: " + e.getMessage());
                    }
                }

                // Ordenar por último mensaje (más reciente primero)
                listaChats.sort((c1, c2) ->
                        Long.compare(c2.getUltimoMensajeTimestamp(), c1.getUltimoMensajeTimestamp()));

                if (listaChats.isEmpty()) {
                    mostrarMensajeNoChats(true);
                } else {
                    mostrarMensajeNoChats(false);
                }

                chatAdapter.notifyDataSetChanged();
                Log.d(TAG, "Chats cargados: " + listaChats.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar chats: " + error.getMessage());
                Toast.makeText(ListaChatsActivity.this,
                        "Error al cargar conversaciones", Toast.LENGTH_SHORT).show();
                mostrarMensajeNoChats(true);
            }
        };

        chatsRef.addValueEventListener(chatsListener);
    }

    private void mostrarMensajeNoChats(boolean mostrar) {
        if (mostrar) {
            tvNoChats.setVisibility(View.VISIBLE);
            rvChats.setVisibility(View.GONE);
        } else {
            tvNoChats.setVisibility(View.GONE);
            rvChats.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatsRef != null && chatsListener != null) {
            chatsRef.removeEventListener(chatsListener);
        }
    }
}