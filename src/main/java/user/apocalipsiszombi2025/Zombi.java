/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package user.apocalipsiszombi2025;

import java.util.Random;
import user.apocalipsiszombi2025.util.Config;
import user.apocalipsiszombi2025.util.LoggerApocalipsis;
import user.apocalipsiszombi2025.gui.VentanaSimulacion;

/**
 *
 * @author matin
 */
public class Zombi extends Thread {
    private final String zombiID;
    private final Exterior[] zonasInseguras;
    private int muertes = 0;
    private Random rand = new Random();
    private static VentanaSimulacion gui;
    
    public Zombi (String zombiID, Exterior[] zonasInseguras) {
        this.zombiID = zombiID;
        this.zonasInseguras = zonasInseguras;
        if (gui != null) {
            gui.crearLabelZombi(zombiID);
        }
    }
    
    public static void setGUI(VentanaSimulacion guiRef) {
        gui = guiRef;
    }
    
    @Override
    public void run() {
        try {
            while(true) {
                patrullar();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (gui != null) {
                gui.eliminarLabelZombi(zombiID);
            }
        }
    }
    
    private void patrullar () throws InterruptedException {
        int zonaID = rand.nextInt(zonasInseguras.length);
        
        for (int i = 0; i < zonasInseguras.length; i++) {
            int index = (zonaID + i) % zonasInseguras.length; // recorrido circular
            Exterior zona = zonasInseguras[index];

            LoggerApocalipsis.registrar("El zombi " + zombiID + " patrulla la zona " + zona.getID());

            Humano objetivo = zona.elegirHumano();
            if (objetivo != null && objetivo.estaVivo()) {
                atacar(objetivo, zona);
                return;
        }
    }

    LoggerApocalipsis.registrar("El zombi " + zombiID + " no encontró humanos en ninguna zona. Espera...");
    Thread.sleep(rand.nextInt(Config.TIEMPO_ESPERA_ZOMBI_MIN, Config.TIEMPO_ESPERA_ZOMBI_MAX));
        
    }
    
    private void atacar(Humano objetivo, Exterior zona) throws InterruptedException {
        LoggerApocalipsis.registrar("El zombi " + zombiID + " ataca al humano " + objetivo.getID());

        Thread.sleep(rand.nextInt(Config.TIEMPO_ATAQUE_MIN, Config.TIEMPO_ATAQUE_MAX));

        boolean defensaExitosa = rand.nextDouble() < (2.0 / 3);

        if (defensaExitosa) {
            objetivo.serHerido();
            LoggerApocalipsis.registrar("El humano " + objetivo.getID() + " se defendió del zombi " + zombiID);
        } else {
            objetivo.morir();
            muertes++;
            LoggerApocalipsis.registrar("El zombi " + zombiID + " mató al humano " + objetivo.getID()
                    + " (muertes: " + muertes + ")");
            zona.notificarMuerte(objetivo);
        }

        Thread.sleep(rand.nextInt(500, Config.TIEMPO_POST_ATAQUE)); // pausa tras el ataque
    }

    public String getID() {
        return zombiID;
    }

    public int getMuertes() {
        return muertes;
    } 
}