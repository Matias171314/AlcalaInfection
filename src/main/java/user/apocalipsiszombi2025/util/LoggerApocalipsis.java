/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package user.apocalipsiszombi2025.util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
/**
 *
 * @author matin
 */
public class LoggerApocalipsis {
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "apocalipsis.txt";
    private static final String ARCHIVO_ACTUAL = LOG_DIR + File.separator + LOG_FILE;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final int MAX_ARCHIVOS = 5;
    private static final long MAX_TAMANIO = 10 * 1024 * 1024; // 10MB
    private static final Lock LOCK = new ReentrantLock(true);
    private static final BlockingQueue<String> COLA_LOGS = new LinkedBlockingQueue<>();
    private static volatile boolean ACTIVO = true;
    private static Thread hiloEscritor;

    static {
        inicializarSistemaLogs();
        iniciarEscritor();
    }

    private static void inicializarSistemaLogs() {
        try {
            Files.createDirectories(Paths.get(LOG_DIR));
            if (!Files.exists(Paths.get(ARCHIVO_ACTUAL))) {
                Files.createFile(Paths.get(ARCHIVO_ACTUAL));
            }
        } catch (IOException e) {
            System.err.println("Error inicializando logs: " + e.getMessage());
        }
    }

    private static void iniciarEscritor() {
        hiloEscritor = new Thread(() -> {
            try (PrintWriter escritor = new PrintWriter(new FileWriter(ARCHIVO_ACTUAL, true))) {
                while (ACTIVO || !COLA_LOGS.isEmpty()) {
                    String mensaje = COLA_LOGS.poll(100, TimeUnit.MILLISECONDS);
                    if (mensaje != null) {
                        escritor.println(mensaje);
                        escritor.flush();
                        rotarArchivosSiEsNecesario();
                    }
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Error en escritor de logs: " + e.getMessage());
            }
        }, "Logger-Escritor");
        hiloEscritor.setDaemon(true);
        hiloEscritor.start();
    }

    private static void rotarArchivosSiEsNecesario() throws IOException {
        LOCK.lock();
        try {
            Path archivo = Paths.get(ARCHIVO_ACTUAL);
            if (Files.exists(archivo) && Files.size(archivo) > MAX_TAMANIO) {
                // Eliminar archivo más antiguo
                Path ultimoArchivo = Paths.get(LOG_DIR + File.separator + LOG_FILE + "." + MAX_ARCHIVOS);
                if (Files.exists(ultimoArchivo)) {
                    Files.delete(ultimoArchivo);
                }
                // Rotar archivos existentes
                for (int i = MAX_ARCHIVOS - 1; i >= 1; i--) {
                    Path origen = Paths.get(LOG_DIR + File.separator + LOG_FILE + "." + i);
                    if (Files.exists(origen)) {
                        Files.move(origen, Paths.get(LOG_DIR + File.separator + LOG_FILE + "." + (i + 1)));
                    }
                }
                // Rotar archivo actual
                Files.move(archivo, Paths.get(LOG_DIR + File.separator + LOG_FILE + ".1"));
                Files.createFile(archivo);
            }
        } finally {
            LOCK.unlock();
        }
    }

    public static void registrar(String mensaje) {
        String entrada = "[" + LocalDateTime.now().format(DATE_FORMAT) + "] " + mensaje;
        System.out.println(entrada); // También por consola
        if (ACTIVO) {
            COLA_LOGS.offer(entrada);
        }
    }

    public static void registroHumano(String id, String accion) {
        registrar("[HUMANO]" + id + " | " + accion);
    }

    public static void registroZombi(String id, String accion) {
        registrar("[ZOMBI] " + id + " | " + accion);
    }

    public static void registrarAtaqueZombi(String idZombi, String idHumano, boolean exito) {
        String mensaje = exito ? "Mató a " + idHumano : "Falló ataque a " + idHumano;
        registroZombi(idZombi, mensaje);
    }

    public static void registroComida(int cantidad) {
        registrar("[COMIDA] Stock actual: " + cantidad);
    }

    public static void registroSistema(String mensaje) {
        registrar("[SISTEMA] " + mensaje);
    }
    
    public static void registroTunel(String mensaje) {
        registrar("[TUNEL] " + mensaje);
    }

    public static void registrarError(String contexto, Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        registrar("[ERROR] En " + contexto + ": " + ex.getMessage() + "\n" + sw);
    }

    public static void apagar() {
        ACTIVO = false;
        try {
            if (hiloEscritor != null) {
                hiloEscritor.join(2000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}