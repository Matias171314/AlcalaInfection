/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package user.apocalipsiszombi2025;

import user.apocalipsiszombi2025.util.*;
import user.apocalipsiszombi2025.gui.*;
import user.apocalipsiszombi2025.remote.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.*;
import java.util.stream.Collectors;

/**
 *
 * @author matin
 */
public class ApocalipsisZombi2025 {
    // === Recursos globales ===
    private static final List<Humano> humanos = new CopyOnWriteArrayList<>();
    private static final List<Zombi> zombis = new CopyOnWriteArrayList<>();
    private static InterfazManager interfazManager;
    private static Refugio refugio;
    private static Tunel[] tuneles;
    private static Exterior[] zonas;
    private static final Random rand = new Random();

    // === Control de pausa ===
    private static final AtomicBoolean simulacionPausada = new AtomicBoolean(false);
    private static final Lock pausaLock = new ReentrantLock(true);
    private static final Condition pausaCondition = pausaLock.newCondition();

    public static void main(String[] args) {
        try {
            inicializarSimulacion();
            ServidorRMI.iniciarServicio(); // Arranca el servicio remoto
            lanzarEntidades();
            // Si quieres que el cliente remoto se lance automáticamente en la misma máquina:
            ClienteRMI cliente = new ClienteRMI("localhost");
            cliente.setVisible(true);  
        } catch (Exception e) {
            LoggerApocalipsis.registroSistema("Error en la simulación: " + e.getMessage());
            apagarSimulacion();
        }
    }

    private static void inicializarSimulacion() {
        LoggerApocalipsis.registroSistema("==== INICIO SIMULACIÓN ====");
        Runtime.getRuntime().addShutdownHook(new Thread(ApocalipsisZombi2025::apagarSimulacion));

        // Inicializar GUI
        PantallaPrincipal gui = new PantallaPrincipal();
        gui.setVisible(true);
        interfazManager = new InterfazManager(gui);

        // Inicializar mundo
        refugio = new Refugio(interfazManager);
        tuneles = new Tunel[Config.NUM_TUNELES];
        for (int i = 0; i < Config.NUM_TUNELES; i++) {
            tuneles[i] = new Tunel(i, interfazManager);
        }
        zonas = new Exterior[Config.NUM_ZONAS];
        for (int i = 0; i < Config.NUM_ZONAS; i++) {
            zonas[i] = new Exterior(i, interfazManager);
        }
    }

    private static void lanzarEntidades() {
        // Lanzar paciente cero
        Zombi pacienteCero = new Zombi(Config.ID_PACIENTE_CERO, zonas, interfazManager, rand.nextInt(Config.NUM_ZONAS));
        zombis.add(pacienteCero);
        pacienteCero.start();
        LoggerApocalipsis.registroZombi(Config.ID_PACIENTE_CERO, "Liberado como paciente cero");

        // Lanzar humanos escalonadamente
        new Thread(() -> {
            for (int i = 1; i <= Config.NUM_HUMANOS; i++) {
                while (isSimulacionPausada()) esperarReanudacion();
                String id = String.format(Config.FORMATO_ID_HUMANO, i);
                Humano humano = new Humano(id, tuneles, refugio, zonas, interfazManager);
                humanos.add(humano);
                humano.start();
                try {
                    Thread.sleep(Config.randTiempo(Config.INTERVALO_CREACION_MIN, Config.INTERVALO_CREACION_MAX));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    // ==== Métodos de control de simulación ====
    public static void pausarSimulacion() {
        pausaLock.lock();
        try {
            simulacionPausada.set(true);
            LoggerApocalipsis.registroSistema("Simulación pausada");
        } finally {
            pausaLock.unlock();
        }
    }

    public static void reanudarSimulacion() {
        pausaLock.lock();
        try {
            simulacionPausada.set(false);
            pausaCondition.signalAll();
            LoggerApocalipsis.registroSistema("Simulación reanudada");
        } finally {
            pausaLock.unlock();
        }
    }

    public static boolean isSimulacionPausada() {
        return simulacionPausada.get();
    }

    private static void esperarReanudacion() {
        pausaLock.lock();
        try {
            while (simulacionPausada.get()) {
                pausaCondition.await();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            pausaLock.unlock();
        }
    }

    public static synchronized void agregarZombi(Zombi zombi) {
        zombis.add(zombi);

        // Asegurarse que el contador existe antes de registrar
        if (interfazManager != null) {
            interfazManager.registrarZombi(zombi.getID(), zombi.getZonaActual());
        }
    }

    public static synchronized void apagarSimulacion() {
        LoggerApocalipsis.registroSistema("==== APAGANDO SIMULACIÓN ====");
        zombis.forEach(z -> {
            z.detener();
            interfazManager.eliminarEntidad(z.getID());
        });
        humanos.forEach(h -> {
            h.setVivo(false);
            interfazManager.eliminarEntidad(h.getID());
        });
        LoggerApocalipsis.apagar();
    }

    // ==== Métodos para RMI y estadísticas ====
    public static synchronized int getNumHumanosRefugio() {
        return refugio.getHumanosEnRefugio();
    }

    public static synchronized int[] getNumHumanosTuneles() {
        int[] counts = new int[tuneles.length];
        for (int i = 0; i < tuneles.length; i++) {
            counts[i] = tuneles[i].getHumanosEsperandoEntrada() + tuneles[i].getHumanosFormandoGrupo();
        }
        return counts;
    }

    public static synchronized int[] getNumHumanosZonas() {
        return Arrays.stream(zonas).mapToInt(Exterior::getNumHumanos).toArray();
    }

    public static synchronized int[] getNumZombisZonas() {
        return Arrays.stream(zonas).mapToInt(Exterior::getNumZombis).toArray();
    }

    public static synchronized List<String> getTopZombis() {
        return zombis.stream()
                .sorted((z1, z2) -> Integer.compare(z2.getMuertes(), z1.getMuertes()))
                .limit(3)
                .map(z -> z.getID() + " - " + z.getMuertes() + " muertes")
                .collect(Collectors.toList());
    }
}