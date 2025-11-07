package com.example.garmadon;

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
import java.util.ArrayList;
import java.util.List;
public class ChatActivity extends AppCompatActivity {
    // Identificador del usuario actual (simulado)
    private static final String CURRENT_USER_ID = "usuario_propio";
    // Vistas de la interfaz
    private RecyclerView recyclerView;
    private EditText etMensaje;
    private ImageButton btnEnviarMensaje;
    private ImageButton btnEnviarImagen;
    private TextView tvNombreContacto;
    private TextView tvEstadoContacto;
    // Adaptador y lista de mensajes
    private MensajeAdapter mensajeAdapter;
    private List<Mensaje> listaMensajes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //layout chat
        setContentView(R.layout.activity_chat);
        // 1. Inicialización de Vistas
        inicializarVistas();
        // 2. Configuración de la Cabecera de Contacto
        configurarCabecera();
        // 3. Configuración del RecyclerView y carga de datos simulados
        configurarRecyclerView();
        // 4. Configuración del Listener del botón de enviar
        configurarListeners();
    }
    private void inicializarVistas() {
        // Cabecera
        tvNombreContacto = findViewById(R.id.tvContactName);
        tvEstadoContacto = findViewById(R.id.tvContactStatus);
        // Lista de mensajes
        recyclerView = findViewById(R.id.recyclerViewChat);
        // Área de entrada
        etMensaje = findViewById(R.id.editTextMensaje);
        btnEnviarMensaje = findViewById(R.id.btnEnviar);
        btnEnviarImagen = findViewById(R.id.btnAttachImage);
    }
    private void configurarCabecera() {
        // Datos de contacto simulados
        tvNombreContacto.setText("Garmadon_id");
        tvEstadoContacto.setText("online");
        // Listener para el botón de retroceso (flecha <- )
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }
    private void configurarRecyclerView() {
        // Carga datos estáticos
        listaMensajes = cargarMensajesDePrueba();
        // Inicializamos el adaptador con los mensajes y el ID de usuario propio
        mensajeAdapter = new MensajeAdapter(this, listaMensajes, CURRENT_USER_ID);
        // Configuramos el RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // Hacemos que la lista inicie desde abajo
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mensajeAdapter);
    }
    private void configurarListeners() {
        // Listener para el botón de Enviar Mensaje
        btnEnviarMensaje.setOnClickListener(v -> enviarMensajeSimulado());
        // Listener para el botón de Enviar Imagen (simulación)
        btnEnviarImagen.setOnClickListener(v -> {
            Toast.makeText(this, "Simulando envío de imagen...", Toast.LENGTH_SHORT).show();
        });
    }
    private void enviarMensajeSimulado() {
        String texto = etMensaje.getText().toString().trim();
        if (!texto.isEmpty()) {
            // 1. Crea el nuevo objeto Mensaje (como si fuera el usuario propio)
            Mensaje nuevoMensaje = new Mensaje(
                    "ID_SIMULADO_" + (listaMensajes.size() + 1), // ID único simulado
                    CURRENT_USER_ID, // ID del remitente (el usuario propio)
                    texto,
                    System.currentTimeMillis() // Hora actual
            );
            // 2. Agrega el mensaje a la lista
            listaMensajes.add(nuevoMensaje);
            // 3. Notifica al adaptador del cambio (para que se muestre en la lista)
            mensajeAdapter.notifyItemInserted(listaMensajes.size() - 1);
            // 4. Desplaza la lista al último mensaje
            recyclerView.scrollToPosition(listaMensajes.size() - 1);
            // 5. Limpia la caja de texto
            etMensaje.setText("");
            simularRespuesta();
        } else {
            Toast.makeText(this, "Escriba un mensaje.", Toast.LENGTH_SHORT).show();
        }
    }
    // Simula una respuesta automática del contacto después de un pequeño retraso
    private void simularRespuesta() {
        // Simula la respuesta de Antonio Vera
        String respuestaTexto = "¡Hola! Gracias por tu mensaje. El producto sigue disponible.";
        Mensaje respuesta = new Mensaje(
                "ID_RESPUESTA_" + (listaMensajes.size() + 1),
                "Garmadon_id", // ID del remitente (el otro usuario)
                respuestaTexto,
                System.currentTimeMillis() + 1000 // Hora ligeramente posterior
        );
        // Agregamos la respuesta con un pequeño retraso visual
        recyclerView.postDelayed(() -> {
            listaMensajes.add(respuesta);
            mensajeAdapter.notifyItemInserted(listaMensajes.size() - 1);
            recyclerView.scrollToPosition(listaMensajes.size() - 1);
        }, 1000); // Retraso de 1 segundo
    }
    //Carga mensajes estáticos para ver cómo se ve la interfaz al iniciar.
    private List<Mensaje> cargarMensajesDePrueba() {
        List<Mensaje> mensajes = new ArrayList<>();
        // Mensaje del OTRO usuario (izquierda)
        mensajes.add(new Mensaje("m1", "Garmadon_id", "¡Hola! ¿Aún tienes la bicicleta a la venta?", System.currentTimeMillis() - 600000));
                // Mensaje del USUARIO PROPIO (derecha)
                mensajes.add(new Mensaje("m2", CURRENT_USER_ID, "Sí, claro. Está en excelente estado.", System.currentTimeMillis() - 300000));
                        // Mensaje del OTRO usuario (izquierda)
                        mensajes.add(new Mensaje("m3", "Garmadon_id", "¿Podrías enviarme más fotos de los detalles? ¿Y dónde podría recogerla?", System.currentTimeMillis() - 120000));
        return mensajes;
    }
}