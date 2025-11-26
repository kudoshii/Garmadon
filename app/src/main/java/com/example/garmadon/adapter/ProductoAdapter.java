package com.example.garmadon.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;

// Importación de Glide para la carga de imágenes
import com.bumptech.glide.Glide;

import com.example.garmadon.DetalleProductoActivity;
import com.example.garmadon.R;
import com.example.garmadon.model.Producto;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Adaptador modificado para soportar el filtrado de productos y carga segura de imágenes
public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private static final String TAG = "ProductoAdapter";

    private final Context context;
    // Lista visible para el RecyclerView (la que se filtra)
    private List<Producto> listaProductos;
    // Copia de la lista original para restaurar el filtro
    private final List<Producto> listaOriginal;

    public ProductoAdapter(Context context, List<Producto> lista) {
        this.context = context;
        // Inicializa ambas listas con la lista pasada
        this.listaProductos = new ArrayList<>(lista);
        this.listaOriginal = new ArrayList<>(lista);
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }


    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);

        // A. Asignar datos del Producto a las Vistas
        holder.tvNombre.setText(producto.getNombre());
        // Formateamos el precio para asegurar un formato de moneda consistente
        holder.tvPrecio.setText(String.format(Locale.getDefault(), "$%.2f", producto.getPrecio()));

        // 1. Asignamos el placeholder por defecto antes de intentar cargar la URL
        holder.ivImagenProducto.setImageResource(R.drawable.agregar_img);

        if (producto.getImageUrls() != null && !producto.getImageUrls().isEmpty()) {
            String imageUrl = producto.getImageUrls().get(0); // Obtener la primera URL

            // 2. Validación CRÍTICA: La URL debe ser una cadena válida (no nula ni vacía)
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.agregar_img) // Placeholder temporal mientras carga
                        .error(R.drawable.agregar_img)
                        .centerCrop()
                        .into(holder.ivImagenProducto);
            }
        }


        // B. Manejo del Clic en el Ítem
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalleProductoActivity.class);
            // Enviamos el ID del producto seleccionado a la pantalla de detalle
            intent.putExtra(DetalleProductoActivity.EXTRA_PRODUCTO_ID, producto.getId());
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    public void actualizarProductos(List<Producto> nuevaLista) {
        // 1. Limpia y reemplaza la lista visible
        this.listaProductos.clear();
        this.listaProductos.addAll(nuevaLista);

        // 2. Limpia y reemplaza la lista original (para que el filtro funcione con la nueva data)
        this.listaOriginal.clear();
        this.listaOriginal.addAll(nuevaLista);

        // 3. Notifica al RecyclerView para que se redibuje
        notifyDataSetChanged();
        Log.d(TAG, "Lista de productos actualizada. Total: " + nuevaLista.size());
    }


    // Filtra los productos según el texto ingresado
    public void filtrar(String texto) {
        String textoBusqueda = texto.toLowerCase(Locale.getDefault()).trim();

        // Crea una nueva lista para los resultados filtrados.
        List<Producto> listaFiltrada = new ArrayList<>();

        if (textoBusqueda.isEmpty()) {
            // Si el texto está vacío, usamos la lista original completa para restaurar.
            listaFiltrada.addAll(listaOriginal);
            Log.d(TAG, "Búsqueda vacía. Mostrando todos los productos: " + listaFiltrada.size());
        } else {
            // Filtramos iterando sobre la lista original (la inmutable).
            for (Producto producto : listaOriginal) {
                if (producto.getNombre().toLowerCase(Locale.getDefault()).contains(textoBusqueda)) {
                    listaFiltrada.add(producto);
                }
            }
            Log.d(TAG, "Productos encontrados para '" + texto + "': " + listaFiltrada.size());
        }

        // Reemplaza la lista actual con la nueva lista filtrada (o completa).
        this.listaProductos = listaFiltrada;

        notifyDataSetChanged();
    }

    // ViewHolder con los IDs
    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        // Agregamos la referencia al ImageView
        ImageView ivImagenProducto;
        TextView tvNombre;
        TextView tvPrecio;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            // CRÍTICO: Debes asegurarte de que este ID exista en tu layout activity_item_producto.xml
            ivImagenProducto = itemView.findViewById(R.id.iv_producto_imagen);
            tvNombre = itemView.findViewById(R.id.tv_producto_titulo);
            tvPrecio = itemView.findViewById(R.id.tv_producto_precio);
        }
    }
}

