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
    private volatile int zonaActual;
    private long tiempoSinVictimaRestante = nuevoPlazoCambio();

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
            zonas[zonaActual].registrarZombi(this);
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
            zonas[zonaActual].retirarZombi(this);
            interfazManager.eliminarEntidad(idZombi);
            LoggerApocalipsis.registroZombi(idZombi, "Dejó de patrullar");
        }
    }

    private void patrullarZona() throws InterruptedException {
        ApocalipsisZombi2025.esperarReanudacion();
        boolean encontroVictima = zonas[zonaActual].atacar(this);
        long espera = Config.randTiempo(Config.TIEMPO_ESPERA_ZOMBI_MIN, Config.TIEMPO_ESPERA_ZOMBI_MAX);
        if (encontroVictima) {
            // Encontrar una víctima reinicia el plazo, aunque consiga defenderse.
            tiempoSinVictimaRestante = nuevoPlazoCambio();
            esperaConPausa(espera);
            return;
        }
        espera = Math.min(espera, tiempoSinVictimaRestante);
        esperaConPausa(espera);
        tiempoSinVictimaRestante -= espera;
        if (tiempoSinVictimaRestante <= 0) {
            cambiarZona();
            tiempoSinVictimaRestante = nuevoPlazoCambio();
        }
    }

    private long nuevoPlazoCambio() {
        return Config.randTiempo(Config.TIEMPO_CAMBIO_ZONA_MIN, Config.TIEMPO_CAMBIO_ZONA_MAX);
    }

    private void cambiarZona() throws InterruptedException {
        ApocalipsisZombi2025.esperarReanudacion();
        if (zonas.length < 2) return;
        int anterior = zonaActual;
        int siguiente = rand.nextInt(zonas.length - 1);
        if (siguiente >= anterior) siguiente++;
        zonas[anterior].retirarZombi(this);
        zonaActual = siguiente;
        zonas[siguiente].registrarZombi(this);
        interfazManager.moverZombiAZonaExterior(idZombi, siguiente);
        LoggerApocalipsis.registroZombi(idZombi, "Cambió de Zona " + anterior + " a " + siguiente + " por falta de víctimas");
    }

    private void esperaConPausa(long tiempoMs) throws InterruptedException {
        long restante = tiempoMs;
        while (restante > 0) {
            if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
            ApocalipsisZombi2025.esperarReanudacion();
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
        ApocalipsisZombi2025.esperarReanudacion();
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