package miniwindows.ui;

import javax.swing.JFrame;

import miniwindows.modelo.Usuario;
import miniwindows.servicios.UsuarioServicio;

/**
 * La unica ventana del programa. No dibuja nada por si misma: solo cambia
 * el panel que se ve por dentro (login, registro o escritorio).
 *
 * Los paneles llaman a estos metodos para pasar de una pantalla a otra.
 */
public class VentanaPrincipal extends JFrame {

    private final UsuarioServicio servicio;

    public VentanaPrincipal(UsuarioServicio servicio) {
        this.servicio = servicio;

        setTitle("Mini-Windows");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);   // centrada en la pantalla

        mostrarLogin();
    }

    public void mostrarLogin() {
        cambiarPanel(new PanelLogin(this, servicio));
    }

    public void mostrarRegistro() {
        cambiarPanel(new PanelRegistro(this, servicio));
    }

    public void mostrarEscritorio(Usuario usuarioActual) {
        cambiarPanel(new PanelEscritorio(this, servicio, usuarioActual));
    }

    /** Reemplaza el contenido de la ventana por otro panel. */
    private void cambiarPanel(javax.swing.JComponent panel) {
        setContentPane(panel);
        revalidate();
        repaint();
    }
}
