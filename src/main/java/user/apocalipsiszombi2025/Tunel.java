/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package user.apocalipsiszombi2025;

import user.apocalipsiszombi2025.util.*;
import user.apocalipsiszombi2025.gui.*;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.stream.Collectors;
/**
 *
 * @author matin
 */
public class Tunel {
    private final int idTunel;
    private final Lock lockTunel = new ReentrantLock(true);
    private final Condition puedePasar = lockTunel.newCondition();
    private final CyclicBarrier barreraGrupo = new CyclicBarrier(Config.TAMANO_GRUPO, this::notificarGrupoFormado);
    private final Set<Humano> grupoActual = ConcurrentHashMap.newKeySet();
    private final InterfazManager interfazManager;
    private final java.util.concurrent.atomic.AtomicInteger esperandoEntrada = new java.util.concurrent.atomic.AtomicInteger();
    private volatile boolean tunelOcupado = false;

    public Tunel(int idTunel, InterfazManager interfazManager) {
        this.idTunel = idTunel;
        this.interfazManager = interfazManager;
    }

    // ===== MÉTODO PARA SALIR EN GRUPO =====
    public void salirEnGrupo(Humano h) throws InterruptedException {
        LoggerApocalipsis.registroHumano(h.getID(), "Intenta acceder por Túnel " + idTunel + " para salir");
        interfazManager.moverHumanoAFormarGrupo(h.getID(), idTunel);
        grupoActual.add(h);

        try {
            barreraGrupo.await();
            lockTunel.lockInterruptibly();
            try {
                // Espera si hay alguien con prioridad o el túnel está ocupado
                while (tunelOcupado || esperandoEntrada.get() > 0) {
                    puedePasar.await();
                }
                tunelOcupado = true;
                LoggerApocalipsis.registroHumano(h.getID(), "Cruzando por Túnel " + idTunel);

                interfazManager.moverHumanoATunel(h.getID(), idTunel);
                esperaConPausa(Config.TIEMPO_CRUCE_TUNEL);
            } finally {
                tunelOcupado = false;
                puedePasar.signalAll();
                lockTunel.unlock();
            }
        } catch (BrokenBarrierException e) {
            throw new InterruptedException("Salida en grupo interrumpida");
        } finally {
            grupoActual.remove(h);
        }
    }

    // ===== MÉTODO PARA ENTRAR AL REFUGIO (CON PRIORIDAD) =====
    public void entrarRefugio(Humano h) throws InterruptedException {
        LoggerApocalipsis.registroHumano(h.getID(),"Intenta regresar al refugio por Túnel " + idTunel);
        interfazManager.moverHumanoASalida(h.getID(), idTunel);
        
        esperandoEntrada.incrementAndGet();
        try {
            lockTunel.lockInterruptibly();
            try {
                while (tunelOcupado) {
                    puedePasar.await();
                }
                tunelOcupado = true;
                LoggerApocalipsis.registroHumano(h.getID(), "Regresando al refugio por Túnel " + idTunel);
                
                // Animación de cruce
                interfazManager.moverHumanoATunel(h.getID(), idTunel);
                esperaConPausa(Config.TIEMPO_CRUCE_TUNEL);
                
                interfazManager.moverHumanoAZonaDescanso(h.getID());
            } finally {
                tunelOcupado = false;
                puedePasar.signalAll();
                lockTunel.unlock();
            }
        } finally {
            esperandoEntrada.decrementAndGet();
            lockTunel.lock();
            try { puedePasar.signalAll(); } finally { lockTunel.unlock(); }
        }
    }

    // ===== NOTIFICACIÓN DE GRUPO FORMADO =====
    private void notificarGrupoFormado() {
        String miembros = grupoActual.stream()
            .map(Humano::getID)
            .collect(Collectors.joining(", "));
        
        LoggerApocalipsis.registroTunel("Grupo de Túnel " + idTunel + " completado: " + miembros);
    }

    // ===== MÉTODO DE ESPERA CON PAUSA =====
    private void esperaConPausa(long ms) throws InterruptedException {
        long restante = ms;
        while (restante > 0) {
            if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
            ApocalipsisZombi2025.esperarReanudacion();
            Thread.sleep(Math.min(500, restante));
            restante -= 500;
        }
    }

    // ===== MÉTODOS AUXILIARES =====
    public int getID() { return idTunel; }
    public int getHumanosEsperandoEntrada() { return esperandoEntrada.get(); }
    public int getHumanosFormandoGrupo() { return barreraGrupo.getNumberWaiting(); }
}