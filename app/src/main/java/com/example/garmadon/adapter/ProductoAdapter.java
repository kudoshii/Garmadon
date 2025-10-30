package com.example.garmadon.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;
import com.example.garmadon.DetalleProductoActivity;
import com.example.garmadon.R;
import com.example.garmadon.model.Producto;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
// SEMANA 5: Adaptador modificado para soportar el filtrado de productos
public class ProductoAdapter extends
        RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {
    private static final String TAG = "ProductoAdapter"; // Etiqueta para el Logcat
    private final Context context;
    // Lista visible para el RecyclerView (la que se filtra)
    private List<Producto> listaProductos;
    // SEMANA 5: Copia de la lista original para restaurar el filtro
    private final List<Producto> listaOriginal;
    public ProductoAdapter(Context context, List<Producto> lista) {
        this.context = context;
        this.listaProductos = new ArrayList<>(lista);
        // SEMANA 5: Inicializa la lista original
        this.listaOriginal = new ArrayList<>(lista);
    }
    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.item_producto, parent,
                false);
        return new ProductoViewHolder(view);
    }
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
    @Override
    public int getItemCount() {
        return listaProductos.size();
    }
    //SEMANA 5: Filtra los productos según el texto ingresado
    public void filtrar(String texto) {
        String textoBusqueda = texto.toLowerCase(Locale.getDefault()).trim();
        // Crea una nueva lista para los resultados filtrados.
        List<Producto> listaFiltrada = new ArrayList<>();
        if (textoBusqueda.isEmpty()) {
            // SEMANA 5: Si el texto está vacío, usamos la lista original completa para restaurar.
            listaFiltrada.addAll(listaOriginal);
            Log.d(TAG, "Búsqueda vacía. Mostrando todos los productos: " + listaFiltrada.size());
        } else {
            // SEMANA 5: Si hay texto, filtramos iterando sobre la lista original (la inmutable).
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
        TextView tvNombre;
        TextView tvPrecio;
        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            // SEMANA 5: IDs para coincidir con activity_item_producto.xml
            tvNombre = itemView.findViewById(R.id.tv_producto_titulo);
            tvPrecio = itemView.findViewById(R.id.tv_producto_precio);
        }
    }
}
