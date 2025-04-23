/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package user.apocalipsiszombi2025;

import java.util.Random;
import user.apocalipsiszombi2025.util.Config;

/**
 *
 * @author matin
 */
public class ApocalipsisZombi2025 {
    private static final Random rand = new Random();
    
    public static void iniciarSimulacion() {
        Refugio refugio = new Refugio();
        
        Tunel[] tuneles = new Tunel[Config.NUM_TUNELES];
        for (int i = 0; i < Config.NUM_TUNELES; i++) {
            tuneles[i] = new Tunel(i);
        }
        
        Exterior[] zonas = new Exterior[Config.NUM_ZONAS];
        for (int i = 0; i < Config.NUM_ZONAS; i++) {
            zonas[i] = new Exterior(i);
        }
        
        Zombi pacienteCero = new Zombi("Z0000", zonas);
        pacienteCero.start();
        
        for (int i = 1; i <= Config.NUM_HUMANOS; i++) {
            String id = String.format("H%04d", i);
            Humano humano = new Humano(id, tuneles, refugio, zonas);
            humano.start();
            try {
                Thread.sleep(rand.nextInt(Config.INTERVALO_CREACION_MIN,Config.INTERVALO_CREACION_MAX));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
