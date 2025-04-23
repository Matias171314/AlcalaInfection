/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package user.apocalipsiszombi2025.gui;

/**
 *
 * @author matin
 */
public class UIManager {
    private static VentanaSimulacion ventanaSimulacion;

    public static void setVentanaSimulacion(VentanaSimulacion ventana) {
        ventanaSimulacion = ventana;
    }

    public static VentanaSimulacion getVentanaSimulacion() {
        return ventanaSimulacion;
    }
}

