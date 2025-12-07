package com.example.garmadon.model;

import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

public class Chat {
    private String chatId;
    private String productoId;
    private String productoNombre;
    private String compradorId;
    private String compradorNombre;
    private String vendedorId;
    private String vendedorNombre;
    private String ultimoMensaje;
    private long ultimoMensajeTimestamp;
    private Map<String, Boolean> participantes;

    public Chat() {
        this.participantes = new HashMap<>();
    }

    public Chat(String productoId, String productoNombre, String compradorId,
                String compradorNombre, String vendedorId, String vendedorNombre) {
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.compradorId = compradorId;
        this.compradorNombre = compradorNombre;
        this.vendedorId = vendedorId;
        this.vendedorNombre = vendedorNombre;
        this.participantes = new HashMap<>();
        this.participantes.put(compradorId, true);
        this.participantes.put(vendedorId, true);
        this.ultimoMensaje = "";
        this.ultimoMensajeTimestamp = System.currentTimeMillis();
    }

    @Exclude
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public String getCompradorId() {
        return compradorId;
    }

    public void setCompradorId(String compradorId) {
        this.compradorId = compradorId;
    }

    public String getCompradorNombre() {
        return compradorNombre;
    }

    public void setCompradorNombre(String compradorNombre) {
        this.compradorNombre = compradorNombre;
    }

    public String getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(String vendedorId) {
        this.vendedorId = vendedorId;
    }

    public String getVendedorNombre() {
        return vendedorNombre;
    }

    public void setVendedorNombre(String vendedorNombre) {
        this.vendedorNombre = vendedorNombre;
    }

    public String getUltimoMensaje() {
        return ultimoMensaje;
    }

    public void setUltimoMensaje(String ultimoMensaje) {
        this.ultimoMensaje = ultimoMensaje;
    }

    public long getUltimoMensajeTimestamp() {
        return ultimoMensajeTimestamp;
    }

    public void setUltimoMensajeTimestamp(long ultimoMensajeTimestamp) {
        this.ultimoMensajeTimestamp = ultimoMensajeTimestamp;
    }

    public Map<String, Boolean> getParticipantes() {
        return participantes;
    }

    public void setParticipantes(Map<String, Boolean> participantes) {
        this.participantes = participantes;
    }

    // Método auxiliar para generar un ID de chat único
    public static String generarChatId(String userId1, String userId2, String productoId) {
        // Ordenamos los IDs para que siempre generen el mismo chatId
        String[] ids = {userId1, userId2};
        java.util.Arrays.sort(ids);
        return ids[0] + "_" + ids[1] + "_" + productoId;
    }
}