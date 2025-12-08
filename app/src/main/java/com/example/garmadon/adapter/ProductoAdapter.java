package com.example.garmadon.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.example.garmadon.DetalleProductoActivity;
import com.example.garmadon.EditarProductoActivity;
import com.example.garmadon.R;
import com.example.garmadon.model.Producto;
// Importaciones de Firebase consolidadas
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
// Importación para el ProgressDialog y AtomicInteger
import android.app.ProgressDialog;
import java.util.concurrent.atomic.AtomicInteger;


public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private static final String TAG = "ProductoAdapter";
    private final Context context;
    private List<Producto> listaProductos;
    private final List<Producto> listaOriginal;
    private boolean mostrarOpcionesEdicion = false;

    public ProductoAdapter(Context context, List<Producto> lista) {
        this(context, lista, false);
    }

    public ProductoAdapter(Context context, List<Producto> lista, boolean mostrarOpciones) {
        this.context = context;
        this.listaProductos = new ArrayList<>(lista);
        this.listaOriginal = new ArrayList<>(lista);
        this.mostrarOpcionesEdicion = mostrarOpciones;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);

        // --- 1. Datos ---
        holder.tvNombre.setText(producto.getNombre());
        // Formatear precio de forma segura (asumiendo que $%.2f es el formato deseado)
        holder.tvPrecio.setText(String.format(Locale.getDefault(), "$%.2f", producto.getPrecio()));

        // --- 2. Imagen (Glide) ---
        holder.ivImagenProducto.setImageResource(R.drawable.agregar_img);

        if (producto.getImageUrls() != null && !producto.getImageUrls().isEmpty()) {
            String imageUrl = producto.getImageUrls().get(0);
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.agregar_img)
                        .error(R.drawable.agregar_img)
                        .centerCrop()
                        .into(holder.ivImagenProducto);
            }
        }

        // --- 3. Opciones de Edición/Eliminación ---
        if (mostrarOpcionesEdicion) {
            holder.btnEditar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setVisibility(View.VISIBLE);

            holder.btnEditar.setOnClickListener(v -> {
                Intent intent = new Intent(context, EditarProductoActivity.class);
                intent.putExtra("producto_id", producto.getId());
                context.startActivity(intent);
            });

            holder.btnEliminar.setOnClickListener(v -> {
                mostrarDialogoEliminar(producto, position);
            });
        } else {
            holder.btnEditar.setVisibility(View.GONE);
            holder.btnEliminar.setVisibility(View.GONE);
        }

        // --- 4. Click en el Item (Detalle) ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalleProductoActivity.class);
            intent.putExtra(DetalleProductoActivity.EXTRA_PRODUCTO_ID, producto.getId());
            context.startActivity(intent);
        });
    }

    // --- Métodos de Eliminación de Producto (FIXES INTEGRADOS) ---

    private void mostrarDialogoEliminar(Producto producto, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar Anuncio")
                .setMessage("¿Está seguro de que desea eliminar este producto?\n\n" +
                        producto.getNombre() +
                        "\n\n Los chats relacionados a este producto también serán eliminados.")
                .setPositiveButton("Eliminar", (dialog, which) ->
                        verificarYEliminarProducto(producto, position))
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // FIX 1: Método para verificar chats antes de eliminar
    private void verificarYEliminarProducto(Producto producto, int position) {
        if (producto.getId() == null || producto.getId().trim().isEmpty()) {
            Toast.makeText(context, "Error: ID de producto inválido",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar un ProgressDialog mientras se verifica
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Eliminando producto...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Buscar chats relacionados con este producto
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("chats");

        chatsRef.orderByChild("productoId").equalTo(producto.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> chatsAEliminar = new ArrayList<>();

                        // Recopilar IDs de chats a eliminar
                        for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                            String chatId = chatSnapshot.getKey();
                            if (chatId != null) {
                                chatsAEliminar.add(chatId);
                            }
                        }

                        // Ahora eliminar producto e imágenes
                        eliminarProductoCompleto(producto, position, chatsAEliminar, progressDialog);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (progressDialog != null) progressDialog.dismiss();
                        Log.e(TAG, "Error al verificar chats: " + error.getMessage());
                        // Continuar con la eliminación aunque falle la verificación de chats
                        eliminarProductoCompleto(producto, position, new ArrayList<>(), null);
                    }
                });
    }

    // FIX 2: Método unificado para eliminar producto completo
    private void eliminarProductoCompleto(Producto producto, int position,
                                          List<String> chatsAEliminar,
                                          ProgressDialog progressDialog) {
        DatabaseReference productosRef = FirebaseDatabase.getInstance().getReference("productos");
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("chats");

        // Contador para tracking de operaciones completadas. Se considera 1 por el producto.
        int totalImages = (producto.getImageUrls() != null ? producto.getImageUrls().size() : 0);
        final int totalOperaciones = 1 + chatsAEliminar.size() + totalImages;
        final AtomicInteger operacionesCompletadas = new AtomicInteger(0);

        // Si el total de operaciones es 1 (solo producto), el contador debería iniciar en 0,
        // y la eliminación del producto será la que active la finalización.
        // Si totalOperaciones es 0 (caso raro), no hacemos nada.
        if (totalOperaciones == 0) return;

        // FIX 3: Eliminar chats relacionados primero
        if (!chatsAEliminar.isEmpty()) {
            Log.d(TAG, "Eliminando " + chatsAEliminar.size() + " chat(s) relacionado(s)");
            for (String chatId : chatsAEliminar) {
                chatsRef.child(chatId).removeValue()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Chat eliminado: " + chatId);
                            } else {
                                Log.w(TAG, "Fallo al eliminar chat: " + chatId);
                            }
                            verificarFinalizacionEliminacion(operacionesCompletadas, totalOperaciones,
                                    producto, position, progressDialog);
                        });
            }
        }

        // FIX 4: Eliminar imágenes de Storage con manejo de errores
        if (producto.getImageUrls() != null && !producto.getImageUrls().isEmpty()) {
            for (String imageUrl : producto.getImageUrls()) {
                if (imageUrl == null || imageUrl.trim().isEmpty()) {
                    verificarFinalizacionEliminacion(operacionesCompletadas, totalOperaciones,
                            producto, position, progressDialog);
                    continue;
                }

                try {
                    StorageReference imageRef = FirebaseStorage.getInstance()
                            .getReferenceFromUrl(imageUrl);
                    imageRef.delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Imagen eliminada de Storage");
                                verificarFinalizacionEliminacion(operacionesCompletadas,
                                        totalOperaciones, producto, position, progressDialog);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al eliminar imagen (continuando): " + e.getMessage());
                                // No es crítico, se cuenta como operación completada
                                verificarFinalizacionEliminacion(operacionesCompletadas,
                                        totalOperaciones, producto, position, progressDialog);
                            });
                } catch (Exception e) {
                    Log.e(TAG, "Error al obtener referencia de imagen: " + e.getMessage());
                    // No es crítico, se cuenta como operación completada
                    verificarFinalizacionEliminacion(operacionesCompletadas, totalOperaciones,
                            producto, position, progressDialog);
                }
            }
        }

        // FIX 5: Eliminar de Realtime Database (la operación principal)
        productosRef.child(producto.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Producto eliminado de la base de datos");
                    verificarFinalizacionEliminacion(operacionesCompletadas, totalOperaciones,
                            producto, position, progressDialog);
                })
                .addOnFailureListener(e -> {
                    if (progressDialog != null) progressDialog.dismiss();
                    Toast.makeText(context, "Error al eliminar producto de la BD: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error crítico al eliminar producto de BD: " + e.getMessage());
                    // Nota: Si esto falla, las otras operaciones podrían seguir ejecutándose, pero
                    // no se intenta finalizar la UI si esta parte crítica falla.
                });
    }

    // FIX 6: Método para verificar cuando todas las operaciones terminaron
    private void verificarFinalizacionEliminacion(
            AtomicInteger completadas,
            int total,
            Producto producto,
            int position,
            ProgressDialog progressDialog) {

        if (completadas.incrementAndGet() >= total) {
            // Todas las operaciones de Firebase (producto, chats, imágenes) completadas

            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            // Actualizar la UI del RecyclerView
            listaProductos.remove(position);
            listaOriginal.remove(producto);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, listaProductos.size());

            Toast.makeText(context, "Producto eliminado exitosamente",
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Eliminación completa finalizada");
        }
    }

    // --- Métodos de RecyclerView y Utilidades ---

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    public void actualizarProductos(List<Producto> nuevaLista) {
        this.listaProductos.clear();
        this.listaProductos.addAll(nuevaLista);
        this.listaOriginal.clear();
        this.listaOriginal.addAll(nuevaLista);
        notifyDataSetChanged();
        Log.d(TAG, "Lista de productos actualizada. Total: " + nuevaLista.size());
    }

    public void filtrar(String texto) {
        String textoBusqueda = texto.toLowerCase(Locale.getDefault()).trim();
        List<Producto> listaFiltrada = new ArrayList<>();

        if (textoBusqueda.isEmpty()) {
            listaFiltrada.addAll(listaOriginal);
        } else {
            for (Producto producto : listaOriginal) {
                if (producto.getNombre().toLowerCase(Locale.getDefault()).contains(textoBusqueda)) {
                    listaFiltrada.add(producto);
                }
            }
        }

        this.listaProductos = listaFiltrada;
        notifyDataSetChanged();
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImagenProducto;
        TextView tvNombre;
        TextView tvPrecio;
        ImageButton btnEditar;
        ImageButton btnEliminar;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImagenProducto = itemView.findViewById(R.id.iv_producto_imagen);
            tvNombre = itemView.findViewById(R.id.tv_producto_titulo);
            tvPrecio = itemView.findViewById(R.id.tv_producto_precio);
            btnEditar = itemView.findViewById(R.id.btn_editar_producto);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar_producto);
        }
    }
}