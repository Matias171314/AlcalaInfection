/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package user.apocalipsiszombi2025.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author matin
 */
public final class Config {
    // === PARÁMETROS GENERALES ===
    public static final int NUM_HUMANOS = 100;
    public static final int NUM_TUNELES = 4;
    public static final int NUM_ZONAS = 4;
    public static final int PLAZAS_COMEDOR = 5;
    public static final int COMIDA_INICIAL = 50;
    public static final int COMIDA_RECOLECTADA = 2;
    public static final int TAMANO_GRUPO = 3;

    // === FORMATO DE IDS ===
    public static final String ID_PACIENTE_CERO = "Z0000";
    public static final String FORMATO_ID_HUMANO = "H%04d";
    public static final String FORMATO_ID_ZOMBI = "Z%04d";

    // === TIEMPOS (milisegundos) ===
    public static final int INTERVALO_CREACION_MIN = 500;
    public static final int INTERVALO_CREACION_MAX = 2000;
    public static final int TIEMPO_PREPARACION_MIN = 1000;
    public static final int TIEMPO_PREPARACION_MAX = 2000;
    public static final int TIEMPO_DESCANSO_NORMAL = 2000;
    public static final int TIEMPO_DESCANSO_HERIDO = 4000;
    public static final int TIEMPO_COMER_MIN = 3000;
    public static final int TIEMPO_COMER_MAX = 5000;
    public static final int TIEMPO_CRUCE_TUNEL = 1000;
    public static final int TIEMPO_EXPLORACION_MIN = 3000;
    public static final int TIEMPO_EXPLORACION_MAX = 5000;
    public static final int TIEMPO_ATAQUE_MIN = 500;
    public static final int TIEMPO_ATAQUE_MAX = 1500;
    public static final int TIEMPO_ESPERA_ZOMBI_MIN = 2000;
    public static final int TIEMPO_ESPERA_ZOMBI_MAX = 3000;
    public static final int TIEMPO_CAMBIO_ZONA_MIN = 5000;
    public static final int TIEMPO_CAMBIO_ZONA_MAX = 15000;

    // === PROBABILIDADES ===
    public static final double PROBABILIDAD_DEFENSA = 2.0 / 3.0; // 66.6%

    // === RMI ===
    public static final int PUERTO_RMI = 1099;
    public static final String NOMBRE_SERVICIO = "ServicioApocalipsis";

    // === MÉTODOS UTILITARIOS ===

    /** Devuelve un tiempo aleatorio entre min y max (ambos incluidos) */
    public static int randTiempo(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /** Devuelve el id de zombi correspondiente a un id de humano */
    public static String humanoAIdZombi(String idHumano) {
        return idHumano.replace('H', 'Z');
    }

    private Config() {} // Evitar instanciación
}