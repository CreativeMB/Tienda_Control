package com.example.tiendacontrol.model;

public class Ventas {

    private int id;
    private String producto;
    private String valor;
    private String detalles;
    private String cantidad;


    private String fechaRegistro;

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public String getProducto() {
        return producto;
    }

    public int getId() {
        return id;
    }

    public String getValor() {
        return valor;
    }

    public double getValorAsDouble() {
        try {
            return Double.parseDouble(valor);
        } catch (NumberFormatException e) {
            return 0.0;  // Manejar el caso donde no se puede convertir a double
        }
    }

    public String getDetalles() {
        return detalles;
    }

    public String getCantidad() {
        return cantidad;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public void setDetalles(String detalles) {
        this.detalles = detalles;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

}
