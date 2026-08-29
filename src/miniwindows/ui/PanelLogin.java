package miniwindows.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import miniwindows.excepciones.CuentaDesactivadaException;
import miniwindows.modelo.Usuario;
import miniwindows.servicios.UsuarioServicio;

/**
 * Pantalla de inicio de sesion (enunciado 4.2a).
 *
 * Pide usuario y contrasena y llama a UsuarioServicio.login(...).
 * - Si el login falla, pregunta si quiere crear una cuenta.
 * - Si la cuenta esta desactivada, muestra el mensaje de la excepcion.
 */
public class PanelLogin extends JPanel {

    public PanelLogin(VentanaPrincipal ventana, UsuarioServicio servicio) {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);

        JTextField txtUsuario = new JTextField(15);
        JPasswordField txtClave = new JPasswordField(15);
        JButton btnEntrar = new JButton("Entrar");
        JButton btnCrear = new JButton("Crear cuenta");

        c.gridx = 0; c.gridy = 0; add(new JLabel("Mini-Windows"), c);
        c.gridx = 0; c.gridy = 1; add(new JLabel("Usuario:"), c);
        c.gridx = 1; add(txtUsuario, c);
        c.gridx = 0; c.gridy = 2; add(new JLabel("Contrasena:"), c);
        c.gridx = 1; add(txtClave, c);
        c.gridx = 1; c.gridy = 3; add(btnEntrar, c);
        c.gridx = 1; c.gridy = 4; add(btnCrear, c);

        btnEntrar.addActionListener(e -> {
            String usuario = txtUsuario.getText().trim();
            String clave = new String(txtClave.getPassword());

            try {
                Usuario u = servicio.login(usuario, clave);
                if (u == null) {
                    int op = JOptionPane.showConfirmDialog(this,
                            "Usuario o contrasena incorrectos.\n"
                            + "Quieres crear una cuenta nueva?",
                            "Error", JOptionPane.YES_NO_OPTION);
                    if (op == JOptionPane.YES_OPTION) {
                        ventana.mostrarRegistro();
                    }
                } else {
                    ventana.mostrarEscritorio(u);
                }
            } catch (CuentaDesactivadaException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        // Pulsar Enter en la caja de contrasena es como pulsar "Entrar".
        txtClave.addActionListener(e -> btnEntrar.doClick());

        btnCrear.addActionListener(e -> ventana.mostrarRegistro());
    }
}
