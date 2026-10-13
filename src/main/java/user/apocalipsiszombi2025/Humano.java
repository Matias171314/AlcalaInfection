/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package user.apocalipsiszombi2025;

import user.apocalipsiszombi2025.util.*;
import user.apocalipsiszombi2025.gui.*;

import java.util.Random;
/**
 *
 * @author matin
 */
public class Humano extends Thread {
    private final String idHumano;
    private final Object lock = new Object();
    private volatile boolean vivo = true;
    private volatile boolean herido = false;
    private volatile boolean marcadoPorZombi = false;
    private volatile boolean siendoAtacado = false;

    private int zonaActualID = -1;
    private final Tunel[] tuneles;
    private final Refugio refugio;
    private final Exterior[] zonas;
    private final Random random = new Random();
    private final InterfazManager interfazManager;

    public Humano(String idHumano, Tunel[] tuneles, Refugio refugio, Exterior[] zonas, InterfazManager interfazManager) {
        this.idHumano = idHumano;
        this.tuneles = tuneles;
        this.refugio = refugio;
        this.zonas = zonas;
        this.interfazManager = interfazManager;
        interfazManager.registrarHumano(idHumano);
        LoggerApocalipsis.registroHumano(idHumano, "Ha llegado al refugio");
    }

    @Override
    public void run() {
        try {
            while (estaVivo()) {
                if (!simulacionPausada()) {
                    cicloExploracion();
                    if (estaVivo()) cicloRefugio();
                } else {
                    esperarReanudacion();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            refugio.salirRefugio(this);
            if (!estaVivo() && !ApocalipsisZombi2025.isApagada()) {
                convertirEnZombi(zonaActualID);
            }
        }
    }

    // ===== CICLO DE EXPLORACIÓN =====
    private void cicloExploracion() throws InterruptedException {
        refugio.entrarZonaComun(this);

        Tunel tunel = seleccionarTunel();
        Exterior zona = zonas[tunel.getID()];
        refugio.salirRefugio(this);
        tunel.salirEnGrupo(this);

        zonaActualID = zona.getID();
        zona.explorar(this);
        if (!estaVivo()) return;
        tunel.entrarRefugio(this);
        refugio.registrarEntrada(this);
        if (!estaHerido()) refugio.reponerComida(Config.COMIDA_RECOLECTADA);
    }

    // ===== CICLO EN EL REFUGIO =====
    private void cicloRefugio() throws InterruptedException {
        refugio.entrarZonaDescanso(this, 0);
        refugio.entrarZonaComedor(this);
        if (estaMarcado()) {
            refugio.entrarZonaDescanso(this, 1);
            setMarcado(false);
            setHerido(false);
        }
    }

    private void convertirEnZombi(int idZona) {
        try {
            String idZombi = Config.humanoAIdZombi(idHumano);
            Zombi nuevoZombi = new Zombi(idZombi, zonas, interfazManager, idZona);

            // Primero registrar el zombi antes de eliminar al humano
            ApocalipsisZombi2025.agregarZombi(nuevoZombi);

            // Luego eliminar al humano de la interfaz
            interfazManager.eliminarEntidad(idHumano);

            // agregarZombi inicia el hilo bajo el control de apagado.
            LoggerApocalipsis.registroZombi(idZombi, "Creado desde " + idHumano);
        } catch (Exception e) { LoggerApocalipsis.registrarError("Conversión de " + idHumano, e); }
    }

    // ===== MÉTODOS AUXILIARES =====
    private Tunel seleccionarTunel() {
        return tuneles[random.nextInt(tuneles.length)];
    }

    private boolean simulacionPausada() {
        return ApocalipsisZombi2025.isSimulacionPausada();
    }

    private void esperarReanudacion() throws InterruptedException {
        ApocalipsisZombi2025.esperarReanudacion();
    }

    // ===== GETTERS & SETTERS SINCRONIZADOS =====
    public String getID() {
        return idHumano;
    }
    
    public Exterior[] getZonas() {
        return zonas;
    }

    public boolean estaHerido() {
        return herido;
    }

    public boolean estaMarcado() {
        return marcadoPorZombi;
    }

    public boolean estaVivo() {
        return vivo;
    }
    
    public boolean estaSiendoAtacado() {
        synchronized (lock) {
            return siendoAtacado;
        }
    }

    public void setSiendoAtacado(boolean siendoAtacado) {
        synchronized (lock) {
            this.siendoAtacado = siendoAtacado;
        }
    }
    
    public void setMarcado(boolean marcado) {
        this.marcadoPorZombi = marcado;
    }

    public void setHerido(boolean herido) {
        this.herido = herido;
    }

    public void setVivo(boolean vivo) {
        this.vivo = vivo;
    }
}