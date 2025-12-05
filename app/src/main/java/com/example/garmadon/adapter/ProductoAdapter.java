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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private static final String TAG = "ProductoAdapter";
    private final Context context;
    private List<Producto> listaProductos;
    private final List<Producto> listaOriginal;
    private boolean mostrarOpcionesEdicion = false; // Para AnunciosActivity

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

        holder.tvNombre.setText(producto.getNombre());
        holder.tvPrecio.setText(String.format(Locale.getDefault(), "$%.2f", producto.getPrecio()));

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

        // Mostrar u ocultar botones de edición
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

        // Click en el item para ver detalles
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalleProductoActivity.class);
            intent.putExtra(DetalleProductoActivity.EXTRA_PRODUCTO_ID, producto.getId());
            context.startActivity(intent);
        });
    }

    private void mostrarDialogoEliminar(Producto producto, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar Anuncio")
                .setMessage("¿Está seguro de que desea eliminar este producto?\n\n" + producto.getNombre())
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarProducto(producto, position))
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void eliminarProducto(Producto producto, int position) {
        DatabaseReference productosRef = FirebaseDatabase.getInstance().getReference("productos");
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Eliminar imágenes de Storage
        if (producto.getImageUrls() != null && !producto.getImageUrls().isEmpty()) {
            for (String imageUrl : producto.getImageUrls()) {
                try {
                    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                    imageRef.delete()
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Imagen eliminada de Storage"))
                            .addOnFailureListener(e -> Log.e(TAG, "Error al eliminar imagen: " + e.getMessage()));
                } catch (Exception e) {
                    Log.e(TAG, "Error al obtener referencia de imagen: " + e.getMessage());
                }
            }
        }

        // Eliminar de Realtime Database
        productosRef.child(producto.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    listaProductos.remove(position);
                    listaOriginal.remove(producto);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, listaProductos.size());
                    Toast.makeText(context, "Producto eliminado exitosamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error al eliminar producto: " + e.getMessage());
                });
    }

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