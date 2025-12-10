package com.example.garmadon.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.garmadon.ChatActivity;
import com.example.garmadon.R;
import com.example.garmadon.model.Chat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final Context context;
    private final List<Chat> listaChats;
    private final String currentUserId;

    public ChatAdapter(Context context, List<Chat> listaChats, String currentUserId) {
        this.context = context;
        this.listaChats = listaChats;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = listaChats.get(position);

        // CORRECCIÓN PRINCIPAL: Determinar quién es el "otro usuario" (contacto)
        boolean esVendedor = currentUserId.equals(chat.getVendedorId());

        // Si soy el vendedor, el contacto es el comprador
        // Si soy el comprador, el contacto es el vendedor
        String nombreContacto = esVendedor ? chat.getCompradorNombre() : chat.getVendedorNombre();
        String contactoId = esVendedor ? chat.getCompradorId() : chat.getVendedorId();

        holder.tvNombreContacto.setText(nombreContacto);
        holder.tvProducto.setText(chat.getProductoNombre());
        holder.tvUltimoMensaje.setText(chat.getUltimoMensaje());
        holder.tvHora.setText(formatearHora(chat.getUltimoMensajeTimestamp()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);

            // CRÍTICO: Siempre pasar los datos del VENDEDOR (no del contacto)
            // porque ChatActivity espera vendedor_id y producto_id para generar el chatId
            intent.putExtra("vendedor_id", chat.getVendedorId());
            intent.putExtra("vendedor_nombre", chat.getVendedorNombre());
            intent.putExtra("producto_id", chat.getProductoId());
            intent.putExtra("producto_nombre", chat.getProductoNombre());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaChats.size();
    }

    private String formatearHora(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreContacto;
        TextView tvProducto;
        TextView tvUltimoMensaje;
        TextView tvHora;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreContacto = itemView.findViewById(R.id.tv_nombre_contacto);
            tvProducto = itemView.findViewById(R.id.tv_producto);
            tvUltimoMensaje = itemView.findViewById(R.id.tv_ultimo_mensaje);
            tvHora = itemView.findViewById(R.id.tv_hora);
        }
    }
}