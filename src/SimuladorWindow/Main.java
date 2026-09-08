package SimuladorWindow;

import java.io.File;

import javax.swing.SwingUtilities;

import javax.swing.JOptionPane;

import SimuladorWindow.excepciones.ArchivoCorruptoException;
import SimuladorWindow.insta.InstaServicio;
import SimuladorWindow.servicios.UsuarioServicio;
import SimuladorWindow.ui.Estilo;
import SimuladorWindow.ui.VentanaPrincipal;

public class Main {

    public static void main(String[] args) {
        File carpetaDatos = new File("datos");

        UsuarioServicio servicio = new UsuarioServicio(carpetaDatos);
        InstaServicio insta = new InstaServicio(carpetaDatos, servicio);

        try {
            servicio.asegurarAdmin();
            insta.asegurarCuentasEjemplo();
        } catch (ArchivoCorruptoException e) {
            JOptionPane.showMessageDialog(null,
                    e.getMessage() + "\nBorra la carpeta 'datos' y vuelve a arrancar.",
                    "Error al leer los datos", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            Estilo.aplicarLookAndFeel();
            VentanaPrincipal ventana = new VentanaPrincipal(servicio, insta);
            ventana.setVisible(true);
        });
    }
}
