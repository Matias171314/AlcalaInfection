/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package user.apocalipsiszombi2025.remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import user.apocalipsiszombi2025.ApocalipsisZombi2025;
/**
 *
 * @author matin
 */
public class ServicioApocalipsisImpl extends UnicastRemoteObject implements ServicioApocalipsis {

    public ServicioApocalipsisImpl() throws RemoteException {
        super();
    }

    @Override
    public int getNumHumanosRefugio() throws RemoteException {
        return ApocalipsisZombi2025.getNumHumanosRefugio();
    }

    @Override
    public int[] getNumHumanosTuneles() throws RemoteException {
        return ApocalipsisZombi2025.getNumHumanosTuneles();
    }

    @Override
    public int[] getNumHumanosZonas() throws RemoteException {
        return ApocalipsisZombi2025.getNumHumanosZonas();
    }

    @Override
    public int[] getNumZombisZonas() throws RemoteException {
        return ApocalipsisZombi2025.getNumZombisZonas();
    }

    @Override
    public List<String> getTopZombis() throws RemoteException {
        return ApocalipsisZombi2025.getTopZombis();
    }

    @Override
    public void pausarSimulacion() throws RemoteException {
        ApocalipsisZombi2025.pausarSimulacion();
    }

    @Override
    public void reanudarSimulacion() throws RemoteException {
        ApocalipsisZombi2025.reanudarSimulacion();
    }
}