/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package user.apocalipsiszombi2025.util;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import user.apocalipsiszombi2025.gui.UIManager;

/**
 *
 * @author matin
 */
public class LoggerApocalipsis {
    private static final String FILE_NAME = "apocalipsis_log.txt";
    private static final Object lock = new Object();

    public static void registrar(String mensaje) {
        synchronized (lock) {
            String fechaHora = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String logFinal = "[" + fechaHora + "] " + mensaje;

            // Mostrar en consola
            System.out.println(logFinal);

            // Guardar en archivo
            try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME, true))) {
                out.println(logFinal);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Mostrar en la interfaz gráfica si está cargada
            if (UIManager.getVentanaSimulacion() != null) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    var area = UIManager.getVentanaSimulacion().getTextAreaLog();
                    area.append(logFinal + "\n");
                    area.setCaretPosition(area.getDocument().getLength());
                });
            }
        }
    }
}