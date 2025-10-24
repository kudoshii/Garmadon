package com.example.garmadon.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.garmadon.DetalleProductoActivity;
import com.example.garmadon.R;
import com.example.garmadon.model.Producto;
import java.util.List;


//Adaptador para el RecyclerView de la pantalla Home.
//Se encarga de conectar la lista de objetos Producto con el diseño de la tarjeta item_producto.xml.
public class ProductoAdapter extends
        RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private final Context context;
    private final List<Producto> listaProductos;

    public ProductoAdapter(Context context, List<Producto> listaProductos) {
        this.context = context;
        this.listaProductos = listaProductos;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        // Inflamos el layout de cada item de la lista
        View view = LayoutInflater.from(context).inflate(R.layout.item_producto, parent,
                false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);

        // A. Asignar datos del Producto a las Vistas
        holder.tvNombre.setText(producto.getNombre());
        holder.tvPrecio.setText(producto.getPrecio());

        // B. Manejo del Clic en el Ítem
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalleProductoActivity.class);
            //Enviamos el ID del producto seleccionado a la pantalla de detalle
            intent.putExtra(DetalleProductoActivity.EXTRA_PRODUCTO_ID, producto.getId());
            context.startActivity(intent);
        });
    }

    //3. Devuelve la cantidad total de items en la lista.
    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    //4. Clase interna ViewHolder: Contiene las referencias a las vistas de cada item.
    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImagen;
        TextView tvNombre;
        TextView tvPrecio;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Conexión con los IDs de res/layout/item_producto.xml
            ivImagen = itemView.findViewById(R.id.iv_producto_imagen);
            tvNombre = itemView.findViewById(R.id.tv_producto_titulo);
            tvPrecio = itemView.findViewById(R.id.tv_producto_precio);
        }
    }
}
