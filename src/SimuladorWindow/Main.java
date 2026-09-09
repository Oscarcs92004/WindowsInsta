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

        // Usuarios de Windows 98 (usuarios.sop) y, aparte, los de INSTA+
        // (users.ins): son cuentas independientes.
        UsuarioServicio servicio = new UsuarioServicio(carpetaDatos);
        UsuarioServicio usuariosInsta = new UsuarioServicio(carpetaDatos, "users.ins");
        InstaServicio insta = new InstaServicio(carpetaDatos, usuariosInsta);

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
