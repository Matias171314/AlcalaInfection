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
    public static void iniciarServicio() {
        try {
            ServicioApocalipsisImpl servicio = new ServicioApocalipsisImpl();
            Registry registry = LocateRegistry.createRegistry(Config.PUERTO_RMI);
            registry.rebind(Config.NOMBRE_SERVICIO, servicio);
            LoggerApocalipsis.registroSistema("Servicio RMI iniciado en puerto " + Config.PUERTO_RMI);
        } catch (Exception e) {
            LoggerApocalipsis.registroSistema("Error iniciando el servicio RMI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}