/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package user.apocalipsiszombi2025;

import user.apocalipsiszombi2025.gui.*;
import user.apocalipsiszombi2025.util.*;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
/**
 *
 * @author matin
 */
public class Zombi extends Thread {
    private final String idZombi;
    private final Exterior[] zonas;
    private final AtomicInteger muertes = new AtomicInteger(0);
    private final Random rand = new Random();
    private final InterfazManager interfazManager;
    private volatile boolean activo = true;
    private int zonaActual;

    public Zombi(String idZombi, Exterior[] zonas, InterfazManager interfazManager, int zonaInicial) {
        this.idZombi = idZombi;
        this.zonas = zonas;
        this.interfazManager = interfazManager;
        this.zonaActual = zonaInicial;
        this.setName("Zombi-" + idZombi);
    }

    @Override
    public void run() {
        try {
            interfazManager.registrarZombi(idZombi, zonaActual);
            while (activo && !isInterrupted()) {
                if (!simulacionPausada()) {
                    patrullarZona();
                } else {
                    esperarReanudacion();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            LoggerApocalipsis.registroZombi(idZombi, "Dejó de patrullar");
        }
    }

    private void patrullarZona() throws InterruptedException {
        // El zombi permanece en su zona actual (no cambia como en el código original)
        Exterior zona = zonas[zonaActual];

        // Realizar ataque en la zona actual
        zona.atacar(this);

        // Tiempo de espera después de atacar (cooldown)
        esperaConPausa(3000); // 3 segundos de cooldown
    }

    private void esperaConPausa(long tiempoMs) throws InterruptedException {
        long restante = tiempoMs;
        while (restante > 0 && !Thread.interrupted()) {
            if (ApocalipsisZombi2025.isSimulacionPausada()) {
                synchronized (ApocalipsisZombi2025.class) {
                    while (ApocalipsisZombi2025.isSimulacionPausada()) {
                        ApocalipsisZombi2025.class.wait();
                    }
                }
            }
            long dormir = Math.min(500, restante);
            Thread.sleep(dormir);
            restante -= dormir;
        }
    }

    public void incrementarMuertes() {
            muertes.incrementAndGet();
    }
    
    private boolean simulacionPausada() {
        return ApocalipsisZombi2025.isSimulacionPausada();
    }

    private void esperarReanudacion() throws InterruptedException {
        synchronized (ApocalipsisZombi2025.class) {
            while (simulacionPausada()) {
                ApocalipsisZombi2025.class.wait();
            }
        }
    }

    public void detener() {
        activo = false;
        this.interrupt();
    }

    // ===== GETTERS =====
    public String getID() {
        return idZombi;
    }

    public int getMuertes() {
        return muertes.get();
    }

    public int getZonaActual() {
        return zonaActual;
    }
}