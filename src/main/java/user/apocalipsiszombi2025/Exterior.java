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
    private final List<Zombi> zombis = new CopyOnWriteArrayList<>();
    private final Random rand = new Random();
    private final InterfazManager interfazManager;

    public Exterior(int idZona, InterfazManager interfazManager) {
        this.idZona = idZona;
        this.interfazManager = interfazManager;
    }

    // ===== MÉTODOS PRINCIPALES =====
    public void explorar(Humano humano) throws InterruptedException {
        humanos.add(humano);
        interfazManager.moverHumanoAZonaExterior(humano.getID(), idZona);
        LoggerApocalipsis.registroHumano(humano.getID(), "Entró a Zona " + idZona);

        try {
            esperaConPausa(Config.randTiempo(Config.TIEMPO_EXPLORACION_MIN, Config.TIEMPO_EXPLORACION_MAX));
            if (humano.estaSiendoAtacado()) {
                    // Esperar a que termine el ataque
                while (humano.estaSiendoAtacado() && humano.estaVivo()) {
                    esperaConPausa(500);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (humano.estaVivo() && humanos.contains(humano)) {
                humanos.remove(humano);
                interfazManager.moverHumanoASalida(humano.getID(), idZona);
                LoggerApocalipsis.registroHumano(humano.getID(), "Salió de Zona " + idZona);
            }
        }
    }
    
    // ===== MÉTODOS PARA ZOMBIS =====

    public void atacar(Zombi zombi) throws InterruptedException {
        zombis.add(zombi);
        interfazManager.moverZombiAZonaExterior(zombi.getID(), idZona);
        LoggerApocalipsis.registroZombi(zombi.getID(), "Entró a Zona " + idZona);
        Humano humano = buscarHumanoDisponible();
        if (humano == null) return;

        try {
            humano.setSiendoAtacado(true);
            boolean ataqueExitoso = rand.nextDouble() >= Config.PROBABILIDAD_DEFENSA;

            esperaConPausa(Config.randTiempo(Config.TIEMPO_ATAQUE_MIN, Config.TIEMPO_ATAQUE_MAX));

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
            humano.setSiendoAtacado(false);
            zombis.remove(zombi);
            LoggerApocalipsis.registroZombi(zombi.getID(), "Salió de Zona " + idZona);
        }
        esperaConPausa(Config.randTiempo(Config.TIEMPO_ESPERA_ZOMBI_MIN, Config.TIEMPO_ESPERA_ZOMBI_MAX));
    }

    // ===== MÉTODOS DE APOYO =====
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