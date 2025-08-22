package com.creativem.tiendacontrol.notificacion;

import java.io.Serializable; // Implementar Serializable para pasar objetos en Intent si fuera necesario

public class RecordatorioModel implements Serializable {
    private int id; // ID único para cada recordatorio
    private String titulo;
    private String hora; // Hora en formato "hh:mm AM/PM"
    private String repeticion; // Ej: "Diario", "Semanal", "Mensual", "Una vez"
    private boolean activo;
    private long initialTriggerMillis; // Almacena el tiempo en milisegundos de la primera vez que se programó (útil para repeticiones)

    // Constructor completo
    public RecordatorioModel(int id, String titulo, String hora, String repeticion, boolean activo, long initialTriggerMillis) {
        this.id = id;
        this.titulo = titulo;
        this.hora = hora;
        this.repeticion = repeticion;
        this.activo = activo;
        this.initialTriggerMillis = initialTriggerMillis;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getHora() {
        return hora;
    }

    public String getRepeticion() {
        return repeticion;
    }

    public boolean isActivo() {
        return activo;
    }

    public long getInitialTriggerMillis() {
        return initialTriggerMillis;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public void setRepeticion(String repeticion) {
        this.repeticion = repeticion;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public void setInitialTriggerMillis(long initialTriggerMillis) {
        this.initialTriggerMillis = initialTriggerMillis;
    }

    // Sobrescribir equals y hashCode para comparar RecordatorioModels por su ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordatorioModel that = (RecordatorioModel) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}