package com.example.garmadon;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.garmadon.R;
import com.bumptech.glide.Glide; // Importación de Glide

import java.util.List;

public class GaleriaImagenAdapter extends RecyclerView.Adapter<GaleriaImagenAdapter.ImageViewHolder> {

    private final Context context;
    private final List<Uri> listaImagenesUri;
    private final OnImageInteractionListener listener;

    public interface OnImageInteractionListener {
        void onRemoveImage(int position);
        void onSelectMainImage(int position);
    }

    public GaleriaImagenAdapter(Context context, List<Uri> listaImagenesUri, OnImageInteractionListener listener) {
        this.context = context;
        this.listaImagenesUri = listaImagenesUri;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el layout item_galeria_imagen.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_galeria_imagen, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = listaImagenesUri.get(position);

        // Cargar imagen con Glide
        Glide.with(context)
                .load(imageUri) // Carga la URI de la imagen
                .centerCrop()   // Ajusta y recorta para llenar el ImageView
                .placeholder(R.drawable.item_imagen) // Placeholder mientras carga
                .into(holder.ivThumbnail); // El ImageView de la miniatura


        // 1. Listener para el botón de ELIMINAR (cerrar_item)
        holder.ivRemoveImage.setOnClickListener(v -> {
            if (listener != null) {
                // Se usa getAdapterPosition() para obtener la posición actual segura
                listener.onRemoveImage(holder.getAdapterPosition());
            }
        });

        // 2. Listener para SELECCIONAR IMAGEN PRINCIPAL (al tocar la miniatura item_imagen)
        holder.ivThumbnail.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSelectMainImage(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaImagenesUri.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        // Vista para mostrar la miniatura (item_imagen)
        ImageView ivThumbnail;

        // Vista para el botón de eliminar (cerrar_item)
        ImageView ivRemoveImage;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            ivThumbnail = itemView.findViewById(R.id.item_imagen);
            ivRemoveImage = itemView.findViewById(R.id.cerrar_item);

        }
    }
}
