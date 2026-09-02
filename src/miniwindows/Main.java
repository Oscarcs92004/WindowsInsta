package miniwindows;

import java.io.File;

import javax.swing.SwingUtilities;

import miniwindows.servicios.UsuarioServicio;
import miniwindows.ui.Estilo;
import miniwindows.ui.VentanaPrincipal;

/**
 * Punto de arranque de Mini-Windows.
 *
 * 1. Prepara el servicio de usuarios y crea el administrador si es la?
 *    primera vez que se abre el programa.
 * 2. Muestra la ventana principal dentro del hilo de Swing (EDT).
 */
public class Main {

    public static void main(String[] args) {
        File carpetaDatos = new File("datos");

        UsuarioServicio servicio = new UsuarioServicio(carpetaDatos);
        servicio.asegurarAdmin();

        // Toda la interfaz de Swing se crea y se toca desde el EDT.
        SwingUtilities.invokeLater(() -> {
            Estilo.aplicarLookAndFeel();
            VentanaPrincipal ventana = new VentanaPrincipal(servicio);
            ventana.setVisible(true);
        });
    }
}
