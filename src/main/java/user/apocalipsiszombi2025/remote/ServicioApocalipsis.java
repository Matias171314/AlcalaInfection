/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package user.apocalipsiszombi2025.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
/**
 *
 * @author matin
 */
public interface ServicioApocalipsis extends Remote {
    // Número de humanos dentro del refugio
    int getNumHumanosRefugio() throws RemoteException;

    // Número de humanos en cada uno de los túneles
    int[] getNumHumanosTuneles() throws RemoteException;

    // Número de humanos en cada área insegura
    int[] getNumHumanosZonas() throws RemoteException;

    // Número de zombis en cada área insegura
    int[] getNumZombisZonas() throws RemoteException;

    // Ranking con los 3 zombis más letales y su número de muertes
    List<String> getTopZombis() throws RemoteException;

    // Botón para detener la ejecución del programa principal
    void pausarSimulacion() throws RemoteException;

    // Botón para reanudar la ejecución del programa principal
    void reanudarSimulacion() throws RemoteException;
}