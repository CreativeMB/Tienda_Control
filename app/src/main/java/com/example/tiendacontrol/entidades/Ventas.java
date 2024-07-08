package com.example.tiendacontrol.entidades;

public class Ventas {

    private int id;
    private String producto;
    private String valor;
    private String detalles;
    private String cantidad;

    public String getProducto() {
        return producto;
    }

    public int getId() {
        return id;
    }

    public String getValor() {
        return valor;
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



//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public String getNombre() {
//        return nombre;
//    }
//
//    public void setNombre(String nombre) {
//        this.nombre = nombre;
//    }
//
//    public String getTelefono() {
//        return telefono;
//    }
//
//    public void setTelefono(String telefono) {
//        this.telefono = telefono;
//    }
//
//    public String getCorreo_electornico() {
//        return correo_electornico;
//    }
//
//    public void setCorreo_electornico(String correo_electornico) {
//        this.correo_electornico = correo_electornico;
//    }
}
