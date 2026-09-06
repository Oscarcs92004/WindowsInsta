package SimuladorWindow;

import java.io.File;

import javax.swing.SwingUtilities;

import SimuladorWindow.servicios.UsuarioServicio;
import SimuladorWindow.ui.Estilo;
import SimuladorWindow.ui.VentanaPrincipal;

public class Main {

    public static void main(String[] args) {
        File carpetaDatos = new File("datos");

        UsuarioServicio servicio = new UsuarioServicio(carpetaDatos);
        servicio.asegurarAdmin();

        SwingUtilities.invokeLater(() -> {
            Estilo.aplicarLookAndFeel();
            VentanaPrincipal ventana = new VentanaPrincipal(servicio);
            ventana.setVisible(true);
        });
    }
}
