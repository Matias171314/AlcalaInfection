/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package user.apocalipsiszombi2025.gui;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import user.apocalipsiszombi2025.util.*;
import java.util.Map;

/**
 *
 * @author matin
 */
public class InterfazManager {
    private final PantallaPrincipal gui;
    private final Map<String, JLabel> entidades = new ConcurrentHashMap<>();
    private final Map<Integer, AtomicInteger> contadoresZonasHumanos = new ConcurrentHashMap<>();
    private final Map<Integer, AtomicInteger> contadoresZonasZombis = new ConcurrentHashMap<>();
    private final JLabel comidaLabel;
    private final Object contadorLock = new Object();

    public InterfazManager(PantallaPrincipal gui) {
        this.gui = gui;
        this.comidaLabel = gui.getComidaLabel();
        
        // Inicializar contadores
        for (int i = 0; i < Config.NUM_ZONAS; i++) {
            contadoresZonasHumanos.put(i, new AtomicInteger(0));
            contadoresZonasZombis.put(i, new AtomicInteger(0));
        }
    }

    // ======== REGISTRO DE ENTIDADES =========
    public synchronized void registrarHumano(String id) {
        JLabel label = crearEtiqueta(id, new Color(173, 216, 230));
        entidades.put(id, label);
        moverHumanoAZonaComun(id);
    }

    public synchronized void registrarZombi(String id, int zona) {
        JLabel label = crearEtiqueta(id, new Color(220, 20, 60));
        entidades.put(id, label);
        moverZombiAZonaExterior(id, zona); // Aquí ya se actualiza el contador
    }

    private JLabel crearEtiqueta(String id, Color color) {
        JLabel label = new JLabel(id);
        label.setForeground(color);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setOpaque(true);
        label.setBackground(new Color(255, 255, 255, 80));
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        return label;
    }

    // ======== MOVIMIENTO DE HUMANOS =========
    public void moverHumanoAZonaComun(String id) {
        moverEntidad(id, "zonaComun");
    }

    public void moverHumanoAZonaComedor(String id) {
        moverEntidad(id, "zonaComedor");
    }

    public void moverHumanoAZonaDescanso(String id) {
        moverEntidad(id, "zonaDescanso");
    }

    public void moverHumanoAFormarGrupo(String id, int zona) {
        moverEntidad(id, "formarGrupo" + zona);
    }

    public void moverHumanoATunel(String id, int tunelID) {
        moverEntidad(id, "tunel" + tunelID);
    }

    public void moverHumanoAZonaExterior(String id, int zona) {
        actualizarContadorHumanos(zona, 1);
        moverEntidad(id, "zonaExteriorHumano" + zona);
    }

    public void moverHumanoASalida(String id, int zona) {
        actualizarContadorHumanos(zona, -1);
        moverEntidad(id, "salida" + zona);
    }

    // ======== MOVIMIENTO DE ZOMBIS =========
    public void moverZombiAZonaExterior(String id, int zona) {
        actualizarContadorZombis(zona, 1);
        moverEntidad(id, "zonaExteriorZombi" + zona);
    }

    public void moverZombiAFueraDeZona(String id, int zona) {
        actualizarContadorZombis(zona, -1);
    }

    // ======== GESTIÓN DE COMIDA =========
    public void actualizarComida(int cantidad) {
        SwingUtilities.invokeLater(() -> comidaLabel.setText("Comida: " + cantidad));
    }

    // ======== MÉTODOS DE APOYO =========
    private void moverEntidad(String id, String nombrePanelDestino) {
        JLabel label = entidades.get(id);
        if (label == null) return;

        JPanel destino = obtenerPanel(nombrePanelDestino);
        if (destino == null) {
            System.err.println("Panel no encontrado: " + nombrePanelDestino);
            return;
        }

        Container parent = label.getParent();
        if (parent != null) {
            parent.remove(label);
        }

        destino.add(label);
        destino.revalidate();
        destino.repaint();

        if (parent != null && parent != destino) {
            parent.revalidate();
            parent.repaint();
        }
    }

    private void actualizarContadorHumanos(int zona, int delta) {
        synchronized (contadorLock) {
            contadoresZonasHumanos.get(zona).addAndGet(delta);
            actualizarContadorZona(zona);
        }
    }

    private void actualizarContadorZombis(int zona, int delta) {
        synchronized (contadorLock) {
            contadoresZonasZombis.get(zona).addAndGet(delta);
            actualizarContadorZona(zona);
        }
    }

    private void actualizarContadorZona(int zona) {
        JPanel panelZona = obtenerPanel("zonaExteriorZombi" + zona);
        if (panelZona != null) {
            int humanos = contadoresZonasHumanos.get(zona).get();
            int zombis = contadoresZonasZombis.get(zona).get();
            panelZona.setToolTipText(String.format("Zona %d - Humanos: %d | Zombis: %d", zona, humanos, zombis));
        }
    }

    private JPanel obtenerPanel(String nombre) {
        try {
            var field = gui.getClass().getDeclaredField(nombre);
            field.setAccessible(true);
            Object obj = field.get(gui);
            if (obj instanceof JPanel) {
                return (JPanel) obj;
            } else {
                System.err.println("Campo no es un JPanel: " + nombre);
                return null;
            }
        } catch (NoSuchFieldException e) {
            System.err.println("Campo no encontrado: " + nombre);
        } catch (Exception e) {
            System.err.println("Error al acceder al panel " + nombre + ": " + e.getMessage());
        }
        return null;
    }

    // ========= ELIMINACIÓN =========
    public void eliminarEntidad(String id) {
        JLabel label = entidades.remove(id);
        if (label != null) {
            Container parent = label.getParent();
            if (parent != null) {
                parent.remove(label);
                parent.revalidate();
                parent.repaint();
            }
        }
    }

    // ========= ESTADÍSTICAS =========
    public void actualizarEstadisticas(int humanosRefugio, int[] humanosTuneles, 
                                       int[] humanosZonas, int[] zombisZonas) {
        SwingUtilities.invokeLater(() -> {
            // Pendiente de implementar en PantallaPrincipal
        });
    }
}