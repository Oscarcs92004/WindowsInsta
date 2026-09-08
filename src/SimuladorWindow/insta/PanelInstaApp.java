package SimuladorWindow.insta;

import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import java.awt.*;

/**
 * La ventana de INSTA+ dentro de Mini-Windows.
 *
 * Primero muestra el login de INSTA+ (cuentas en users.ins, aparte de las de
 * Windows). Al entrar, muestra la app (PanelInsta) con esa cuenta. Al cerrar
 * sesion vuelve al login, sin tocar la sesion de Windows, asi que se puede
 * entrar con otra cuenta de INSTA+ distinta.
 */
public class PanelInstaApp extends JPanel {

    private final InstaServicio insta;
    private final UsuarioServicio usuariosInsta;
    private final CardLayout cartas = new CardLayout();

    public PanelInstaApp(InstaServicio insta) {
        this.insta = insta;
        this.usuariosInsta = insta.getUsuarioServicio();

        setLayout(cartas);
        mostrarLogin();
    }

    private void mostrarLogin() {
        // Quitamos la app anterior (si la habia) y ponemos el login.
        removeAll();
        add(new PanelLoginInsta(usuariosInsta, this::entrar), "LOGIN");
        cartas.show(this, "LOGIN");
        revalidate();
        repaint();
    }

    private void entrar(Usuario cuenta) {
        PanelInsta app = new PanelInsta(insta, usuariosInsta, cuenta, this::mostrarLogin);
        add(app, "APP");
        cartas.show(this, "APP");
        revalidate();
        repaint();
    }
}
