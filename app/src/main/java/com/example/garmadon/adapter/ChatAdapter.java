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

        // Determinar si el usuario actual es el comprador o vendedor
        boolean esComprador = currentUserId.equals(chat.getCompradorId());
        String nombreContacto = esComprador ? chat.getVendedorNombre() : chat.getCompradorNombre();
        String contactoId = esComprador ? chat.getVendedorId() : chat.getCompradorId();

        holder.tvNombreContacto.setText(nombreContacto);
        holder.tvProducto.setText(chat.getProductoNombre());
        holder.tvUltimoMensaje.setText(chat.getUltimoMensaje());
        holder.tvHora.setText(formatearHora(chat.getUltimoMensajeTimestamp()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
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