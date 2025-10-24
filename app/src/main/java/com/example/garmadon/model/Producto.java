package com.example.garmadon.model;

public class Producto {

    private String id;
    private String nombre;
    private String descripcion;
    private String precio;
    private String imagenUrl;

    public Producto() {
    }

    // Constructor con todos los campos
    public Producto(String id, String nombre, String descripcion, String precio, String imagenUrl) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.imagenUrl = imagenUrl;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getPrecio() {
        return precio;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public void setPrecio(String precio) {
        this.precio = precio;
    }
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
}
