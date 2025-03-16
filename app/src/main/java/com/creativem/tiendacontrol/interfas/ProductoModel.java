package com.creativem.tiendacontrol.interfas;

public class ProductoModel {
    private String id;
    private String nombre;
    private double valor; // Cambio de "precio" a "valor"
    private String nota;
    private String fechaHora; // Fecha y hora en un solo campo

    public ProductoModel() {
        // Constructor vacío necesario para Firebase
    }

    public ProductoModel(String id, String nombre, double valor, String nota, String fechaHora) {
        this.id = id;
        this.nombre = nombre;
        this.valor = valor;
        this.nota = nota;
        this.fechaHora = fechaHora;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getValor() { return valor; } // Cambio de "getPrecio" a "getValor"
    public void setValor(double valor) { this.valor = valor; } // Cambio de "setPrecio" a "setValor"

    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }
}
