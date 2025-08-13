package com.creativem.tiendacontrol.monitor;

public class Perfil {
    private String userId;       // UID del usuario autenticado (clave principal)
    private String email;        // Email del usuario
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
    public Perfil(String userId, String email, String nombrePersona, String nombreEmpresa, String telefono, String direccion, String pais, String ciudad) {
        this.userId = userId;
        this.email = email;
        this.nombrePersona = nombrePersona;
        this.nombreEmpresa = nombreEmpresa;
        this.telefono = telefono;
        this.direccion = direccion;
        this.pais = pais;
        this.ciudad = ciudad;
    }

    // Getters y setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
