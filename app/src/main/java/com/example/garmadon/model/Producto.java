package com.example.garmadon.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;
import java.util.ArrayList;

@IgnoreExtraProperties
public class Producto {

    private String id;
    private String nombre;
    private String marca;
    private String categoria;
    private String condicion;
    private String ubicacion;
    private String descripcion;
    private String direccion;
    private double precio;
    private String vendedorId;
    private long fechaPublicacion;
    private List<String> imageUrls;

    // Constructor vacío requerido para Firebase
    public Producto() {
        this.imageUrls = new ArrayList<>();
    }

    public Producto(String nombre, String marca, String categoria, String condicion, String ubicacion, String descripcion, String direccion, double precio, String vendedorId, long fechaPublicacion, List<String> imageUrls) {
        this.nombre = nombre;
        this.marca = marca;
        this.categoria = categoria;
        this.condicion = condicion;
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
        this.direccion = direccion;
        this.precio = precio;
        this.vendedorId = vendedorId;
        this.fechaPublicacion = fechaPublicacion;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    // Getters y Setters

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(String vendedorId) {
        this.vendedorId = vendedorId;
    }

    public long getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(long fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}

