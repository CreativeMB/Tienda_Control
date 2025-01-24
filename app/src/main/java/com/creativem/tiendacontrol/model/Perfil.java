package com.creativem.tiendacontrol.model;
public class Perfil {
    private String id; // Identificador único del perfil
    private String userId; // UID del usuario autenticado
    private String nombrePersona;
    private String nombreEmpresa;
    private String telefono;
    private String direccion;
    private String pais;
    private String ciudad;

    // Constructor vacío requerido por Firebase
    public Perfil() {
    }

    // Constructor con todos los parámetros
    public Perfil(String id, String userId, String nombrePersona, String nombreEmpresa, String telefono, String direccion, String pais, String ciudad) {
        this.id = id;
        this.userId = userId; // UID del usuario autenticado
        this.nombrePersona = nombrePersona;
        this.nombreEmpresa = nombreEmpresa;
        this.telefono = telefono;
        this.direccion = direccion;
        this.pais = pais;
        this.ciudad = ciudad;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNombrePersona() {
        return nombrePersona;
    }

    public void setNombrePersona(String nombrePersona) {
        this.nombrePersona = nombrePersona;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }
}