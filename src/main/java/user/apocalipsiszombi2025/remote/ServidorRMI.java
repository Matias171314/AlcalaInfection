/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package user.apocalipsiszombi2025.remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import user.apocalipsiszombi2025.util.Config;
import user.apocalipsiszombi2025.util.LoggerApocalipsis;
/**
 *
 * @author matin
 */
public class ServidorRMI {
    private static Registry registry;
    private static ServicioApocalipsisImpl servicio;
    public static void detenerServicio() {
        try { if (servicio != null) java.rmi.server.UnicastRemoteObject.unexportObject(servicio, true); }
        catch (java.rmi.NoSuchObjectException e) { LoggerApocalipsis.registrarError("Cerrar servicio RMI", e); }
        try { if (registry != null) java.rmi.server.UnicastRemoteObject.unexportObject(registry, true); }
        catch (java.rmi.NoSuchObjectException e) { LoggerApocalipsis.registrarError("Cerrar registro RMI", e); }
    }

    public static void iniciarServicio() {
        try {
            servicio = new ServicioApocalipsisImpl();
            registry = LocateRegistry.createRegistry(Config.PUERTO_RMI);
            registry.rebind(Config.NOMBRE_SERVICIO, servicio);
            LoggerApocalipsis.registroSistema("Servicio RMI iniciado en puerto " + Config.PUERTO_RMI);
        } catch (Exception e) {
            LoggerApocalipsis.registroSistema("Error iniciando el servicio RMI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}