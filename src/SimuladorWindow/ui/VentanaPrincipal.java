package SimuladorWindow.ui;

import SimuladorWindow.insta.InstaServicio;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import java.awt.*;

/**
 * La unica ventana del programa. No dibuja nada por si misma: solo cambia
 * el panel que se ve por dentro (login, registro o escritorio).
 *
 * Los paneles llaman a estos metodos para pasar de una pantalla a otra.
 */
public class VentanaPrincipal extends JFrame {

    private final UsuarioServicio servicio;
    private final InstaServicio insta;

    public VentanaPrincipal(UsuarioServicio servicio, InstaServicio insta) {
        this.servicio = servicio;
        this.insta = insta;

        setTitle("Windows 98");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Ocupar toda la pantalla PERO sin taparse con la barra de tareas de
        // Windows, para que se vea nuestra propia barra de tareas de abajo.
        Rectangle libre = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        setBounds(libre);

        Image icono = Iconos.imagen("file.png");
        if (icono != null) {
            setIconImage(icono);
        }

        mostrarLogin();
    }

    public void mostrarLogin() {
        cambiarPanel(new PanelLogin(this, servicio));
    }

    /** Formulario para crear una cuenta. Solo lo abre el administrador desde
     *  el menu Inicio; al terminar se vuelve a su escritorio. */
    public void mostrarRegistro(Usuario administrador) {
        cambiarPanel(new PanelRegistro(this, servicio, administrador));
    }

    public void mostrarEscritorio(Usuario usuarioActual) {
        cambiarPanel(new PanelEscritorio(this, servicio, insta, usuarioActual));
    }

    /** Reemplaza el contenido de la ventana por otro panel. */
    private void cambiarPanel(javax.swing.JComponent panel) {
        setContentPane(panel);
        revalidate();
        repaint();
    }
}
