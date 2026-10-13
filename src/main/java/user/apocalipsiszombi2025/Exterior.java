/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package user.apocalipsiszombi2025;

import user.apocalipsiszombi2025.util.*;
import user.apocalipsiszombi2025.gui.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 *
 * @author matin
 */
public class Exterior {
    private final int idZona;
    private final List<Humano> humanos = new CopyOnWriteArrayList<>();
    private final java.util.Set<Zombi> zombis = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final Object lockHumanos = new Object();
    private final Random rand = new Random();
    private final InterfazManager interfazManager;

    public Exterior(int idZona, InterfazManager interfazManager) {
        this.idZona = idZona;
        this.interfazManager = interfazManager;
    }

    // ===== MÉTODOS PRINCIPALES =====
    public void explorar(Humano humano) throws InterruptedException {
        synchronized (lockHumanos) { humanos.add(humano); }
        interfazManager.moverHumanoAZonaExterior(humano.getID(), idZona);
        LoggerApocalipsis.registroHumano(humano.getID(), "Entró a Zona " + idZona);

        try {
            esperaConPausa(Config.randTiempo(Config.TIEMPO_EXPLORACION_MIN, Config.TIEMPO_EXPLORACION_MAX));
            synchronized (lockHumanos) {
                while (humano.estaSiendoAtacado() && humano.estaVivo()) lockHumanos.wait();
                humanos.remove(humano);
            }
        } finally {
            synchronized (lockHumanos) { humanos.remove(humano); }
            if (humano.estaVivo()) {
                interfazManager.moverHumanoASalida(humano.getID(), idZona);
                LoggerApocalipsis.registroHumano(humano.getID(), "Salió de Zona " + idZona);
            }
        }
    }

    // ===== MÉTODOS PARA ZOMBIS =====

    public boolean atacar(Zombi zombi) throws InterruptedException {
        registrarZombi(zombi);
        Humano humano;
        synchronized (lockHumanos) {
            humano = buscarHumanoDisponible();
            if (humano != null) humano.setSiendoAtacado(true);
        }
        if (humano == null) return false;

        try {
            boolean ataqueExitoso = rand.nextDouble() >= Config.PROBABILIDAD_DEFENSA;

            esperaConPausa(Config.randTiempo(Config.TIEMPO_ATAQUE_MIN, Config.TIEMPO_ATAQUE_MAX));

            if (ApocalipsisZombi2025.isApagada() || !humano.estaVivo()) return true;
            if (ataqueExitoso) {
                zombi.incrementarMuertes();
                humano.setVivo(false);
                humanos.remove(humano);
                LoggerApocalipsis.registrarAtaqueZombi(zombi.getID(), humano.getID(), true);
                humano.interrupt();
            } else {
                humano.setHerido(true);
                humano.setMarcado(true);
                LoggerApocalipsis.registrarAtaqueZombi(zombi.getID(), humano.getID(), false);
            }
        } finally {
            synchronized (lockHumanos) {
                humano.setSiendoAtacado(false);
                lockHumanos.notifyAll();
            }
        }
        return true;
    }

    // ===== MÉTODOS DE APOYO =====
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

    public void registrarZombi(Zombi zombi) { zombis.add(zombi); }
    public void retirarZombi(Zombi zombi) { zombis.remove(zombi); }

    // ===== GETTERS Y CONSULTAS =====

    public Humano buscarHumanoDisponible() {
        List<Humano> disponibles = humanos.stream()
            .filter(h -> h.estaVivo() && !h.estaSiendoAtacado())
            .collect(Collectors.toList());
        
        return disponibles.isEmpty() ? null : disponibles.get(rand.nextInt(disponibles.size()));
    }

    public int getID() {
        return idZona;
    }

    public int getNumHumanos() {
        return (int) humanos.stream().filter(Humano::estaVivo).count();
    }

    public int getNumZombis() {
        return zombis.size();
    }
}