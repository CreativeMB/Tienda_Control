package com.creativem.tiendacontrol.model;

public class Items {
    private String id;  // Ahora es String
    private String producto;
    private double valor;
    private String detalles;
    private int cantidad;
    private String fechaRegistro;
    private boolean isPredefined;
    private String type;
    private Long timestamp;

    public Items() {
        // Constructor vacío necesario para Firebase
    }

    public Items(String id, String producto, double valor, String detalles, int cantidad, String fechaRegistro, boolean isPredefined, String type, Long timestamp) {
        this.id = id;
        this.producto = producto;
        this.valor = valor;
        this.detalles = detalles;
        this.cantidad = cantidad;
        this.fechaRegistro = fechaRegistro;
        this.isPredefined = isPredefined;
        this.type = type;
        this.timestamp = timestamp;
    }


    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getDetalles() {
        return detalles;
    }

    public void setDetalles(String detalles) {
        this.detalles = detalles;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public double getValorAsDouble() {
        return valor;
    }

    @Override
    public String toString() {
        return producto;
    }
    public boolean isPredefined() {
        return isPredefined;
    }

    public void setPredefined(boolean predefined) {
        isPredefined = predefined;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}