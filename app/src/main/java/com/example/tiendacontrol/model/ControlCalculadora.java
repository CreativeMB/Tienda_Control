package com.example.tiendacontrol.model;
public class ControlCalculadora {

    private static ControlCalculadora instance;
    private boolean isCalculadoraDialogVisible = false;

    private ControlCalculadora() {
        // Constructor privado para evitar instanciación
    }

    // Obtener la instancia de ControlCalculadora
    public static synchronized ControlCalculadora getInstance() {
        if (instance == null) {
            instance = new ControlCalculadora();
        }
        return instance;
    }

    // Obtener el estado de visibilidad del diálogo
    public boolean isCalculadoraDialogVisible() {
        return isCalculadoraDialogVisible;
    }

    // Establecer el estado de visibilidad del diálogo
    public void setCalculadoraDialogVisible(boolean visible) {
        isCalculadoraDialogVisible = visible;
    }
}