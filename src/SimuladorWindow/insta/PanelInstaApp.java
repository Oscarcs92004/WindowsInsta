package SimuladorWindow.insta;

import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import java.awt.*;

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
        removeAll();
        add(new PanelLoginInsta(insta, this::entrar), "LOGIN");
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

        if (!cuenta.isActiva()) {
            app.irAEditarPerfil();
            JOptionPane.showMessageDialog(this,
                    "Tu cuenta está desactivada: nadie puede ver tu perfil ni tus publicaciones.\n"
                            + "Puedes reactivarla con \"Activar / Desactivar cuenta\".");
        }
    }
}
