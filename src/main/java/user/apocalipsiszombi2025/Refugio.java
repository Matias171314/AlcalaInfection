/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package user.apocalipsiszombi2025;

import user.apocalipsiszombi2025.util.*;
import user.apocalipsiszombi2025.gui.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;
import java.util.concurrent.Semaphore;
/**
 *
 * @author matin
 */
public class Refugio {
    private final AtomicInteger comidaDisponible = new AtomicInteger(Config.COMIDA_INICIAL);
    private final java.util.Set<Humano> humanosEnRefugio = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final Lock lockComida = new ReentrantLock(true);
    private final Condition hayComida = lockComida.newCondition();
    private final Semaphore comedor = new Semaphore(Config.PLAZAS_COMEDOR, true);
    private final InterfazManager interfazManager;

    public Refugio(InterfazManager interfazManager) {
        this.interfazManager = interfazManager;
    }

    // ===== ZONA COMÚN =====
    public void entrarZonaComun(Humano humano) throws InterruptedException {
        registrarEntrada(humano);
        interfazManager.moverHumanoAZonaComun(humano.getID());
        LoggerApocalipsis.registroHumano(humano.getID(), "Entró en Zona Común");
        esperaConPausa(Config.randTiempo(Config.TIEMPO_PREPARACION_MIN, Config.TIEMPO_PREPARACION_MAX));
    }

    // ===== ZONA DE DESCANSO =====
    public void entrarZonaDescanso(Humano humano, int tipo) throws InterruptedException {
        interfazManager.moverHumanoAZonaDescanso(humano.getID());
        long tiempo = (tipo == 0) ? Config.TIEMPO_DESCANSO_NORMAL : Config.TIEMPO_DESCANSO_HERIDO;
        LoggerApocalipsis.registroHumano(humano.getID(), tipo == 0 ? "Descanso normal" : "Descanso herido");
        esperaConPausa(tiempo);
        if (tipo == 1) humano.setHerido(false);
    }

    // ===== COMEDOR =====
    public void entrarZonaComedor(Humano humano) throws InterruptedException {
        comedor.acquire();
        try {
            interfazManager.moverHumanoAZonaComedor(humano.getID());
            lockComida.lock();
            try {
                while (comidaDisponible.get() == 0 && humano.estaVivo()) {
                    hayComida.await();
                }
                if (humano.estaVivo() && comidaDisponible.get() > 0) {
                    comidaDisponible.decrementAndGet();
                    interfazManager.actualizarComida(comidaDisponible.get());
                    LoggerApocalipsis.registroHumano(humano.getID(), "Está comiendo. Queda: " + comidaDisponible.get());

                }
            } finally {
                lockComida.unlock();
            }
            esperaConPausa(Config.randTiempo(Config.TIEMPO_COMER_MIN, Config.TIEMPO_COMER_MAX));
            LoggerApocalipsis.registroHumano(humano.getID(), "Terminó de comer");
        } finally {
            comedor.release();
        }
    }

    // ===== REPOSICIÓN DE COMIDA =====
    public void reponerComida(int cantidad) {
        lockComida.lock();
        try {
            comidaDisponible.addAndGet(cantidad);
            interfazManager.actualizarComida(comidaDisponible.get());
            hayComida.signalAll();
            LoggerApocalipsis.registroComida(comidaDisponible.get());
        } finally {
            lockComida.unlock();
        }
    }

    // ===== MÉTODO AUXILIAR DE PAUSA =====
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

    public void registrarEntrada(Humano humano) { humanosEnRefugio.add(humano); }
    public void salirRefugio(Humano humano) { humanosEnRefugio.remove(humano); }

    // ===== ESTADÍSTICAS =====
    public int getHumanosEnRefugio() {
        return humanosEnRefugio.size();
    }
}