package SimuladorWindow.ui;

import SimuladorWindow.insta.InstaServicio;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import java.awt.*;


public class VentanaPrincipal extends JFrame {

    private final UsuarioServicio servicio;
    private final InstaServicio insta;

    public VentanaPrincipal(UsuarioServicio servicio, InstaServicio insta) {
        this.servicio = servicio;
        this.insta = insta;

        setTitle("Windows 98");
        setDefaultCloseOperation(EXIT_ON_CLOSE);


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


    public void mostrarRegistro(Usuario administrador) {
        cambiarPanel(new PanelRegistro(this, servicio, administrador));
    }

    public void mostrarEscritorio(Usuario usuarioActual) {
        cambiarPanel(new PanelEscritorio(this, servicio, insta, usuarioActual));
    }


    private void cambiarPanel(javax.swing.JComponent panel) {
        setContentPane(panel);
        revalidate();
        repaint();
    }
}
