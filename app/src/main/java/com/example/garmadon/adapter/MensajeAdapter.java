package com.example.garmadon.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.example.garmadon.R;
import com.example.garmadon.model.Mensaje;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
public class MensajeAdapter extends
        RecyclerView.Adapter<MensajeAdapter.MensajeViewHolder> {
    private final Context context;
    private final List<Mensaje> listaMensajes;
    private final String currentUserId;
    // Definición de las constantes de vista
    private static final int VIEW_TYPE_EMISOR = 1; // Mensaje enviado por el usuario actual (derecha)
    private static final int VIEW_TYPE_RECEPTOR = 2; // Mensaje recibido por el usuario actual (izquierda)
    public MensajeAdapter(Context context, List<Mensaje> listaMensajes, String currentUserId) {
        this.context = context;
        this.listaMensajes = listaMensajes;
        this.currentUserId = currentUserId;
    }
    //Determina que tipo de vista usar para una posicion dada: Emisor (propio) o Receptor (otro).
    @Override
    public int getItemViewType(int position) {
        Mensaje mensaje = listaMensajes.get(position);
        // Si el remitenteId del mensaje coincide con el ID del usuario actual, es un EMISOR.
        if (mensaje.getRemitenteId().equals(currentUserId)) {
            return VIEW_TYPE_EMISOR;
        } else {
            // De lo contrario, es un RECEPTOR.
            return VIEW_TYPE_RECEPTOR;
        }
    }
    @NonNull
    @Override
    public MensajeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        // Usa el mismo layout (item_mensaje.xml) para ambos tipos de vista.
        View view = LayoutInflater.from(context).inflate(R.layout.item_mensaje, parent, false);
        return new MensajeViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MensajeViewHolder holder, int position) {
        Mensaje mensaje = listaMensajes.get(position);
        // 1. Mostrar el contenido del mensaje
        holder.tvContenido.setText(mensaje.getContenido());
        // 2. Mostrar la hora del mensaje (formato legible)
        String horaFormateada = formatTimestamp(mensaje.getTimestamp());
        holder.tvHora.setText(horaFormateada);
        // 3. Aplicar estilos y alineación basados en el tipo de vista
        if (getItemViewType(position) == VIEW_TYPE_EMISOR) {
            // Alinea la tarjeta de mensaje a la derecha
            ConstraintLayout.LayoutParams layoutParams =
                    (ConstraintLayout.LayoutParams) holder.cardMensaje.getLayoutParams();
            layoutParams.horizontalBias = 1.0f;
            holder.cardMensaje.setLayoutParams(layoutParams);
            // Usa el drawable de mensaje enviado (fondo azul)
            holder.llFondo.setBackgroundResource(R.drawable.bubble_sent_bg);
            // Establece el color del texto a blanco para contraste con el fondo azul.
            holder.tvContenido.setTextColor(Color.WHITE);
            holder.tvHora.setTextColor(Color.parseColor("#CCCCCC"));
        } else {
            // Alinea la tarjeta de mensaje a la izquierda
            ConstraintLayout.LayoutParams layoutParams =
                    (ConstraintLayout.LayoutParams) holder.cardMensaje.getLayoutParams();
            layoutParams.horizontalBias = 0.0f;
            holder.cardMensaje.setLayoutParams(layoutParams);
            // Usa el drawable de mensaje recibido
            holder.llFondo.setBackgroundResource(R.drawable.bubble_received_bg);
            // Restablece el color del texto a negro, ya que el fondo es claro.
            holder.tvContenido.setTextColor(Color.BLACK);
            holder.tvHora.setTextColor(Color.parseColor("#888888"));
        }
    }
    //Convierte el timestamp (long) en una cadena de hora legible
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    @Override
    public int getItemCount() {
        return listaMensajes.size();
    }
    //ViewHolder: Contiene las referencias a las vistas del item_mensaje.xml
    public static class MensajeViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvContenido;
        public final TextView tvHora;
        public final View cardMensaje;
        public final LinearLayout llFondo;
        public MensajeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContenido = itemView.findViewById(R.id.tv_mensaje_contenido);
            tvHora = itemView.findViewById(R.id.tv_mensaje_hora);
            cardMensaje = itemView.findViewById(R.id.card_mensaje);
            llFondo = itemView.findViewById(R.id.ll_bubble_background);
        }
    }
}
