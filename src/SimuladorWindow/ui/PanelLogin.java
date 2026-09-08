package SimuladorWindow.ui;

import SimuladorWindow.excepciones.CuentaDesactivadaException;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.red.Cliente;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import java.awt.*;

/**
 * Pantalla de inicio de sesion (enunciado 4.2a).
 *
 * Pide usuario y contrasena y llama a UsuarioServicio.login(...).
 * - Si el login falla, pregunta si quiere crear una cuenta.
 * - Si la cuenta esta desactivada, muestra el mensaje de la excepcion.
 *
 * Si se marca "Usar servidor", el login se hace por sockets contra el
 * Servidor (Pilar 4). Sin marcar, se hace en local. Por defecto va sin marcar
 * para que la app funcione aunque el servidor no este encendido.
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
        JCheckBox chkServidor = new JCheckBox("Usar servidor (sockets)");

        c.gridx = 0; c.gridy = 0; add(new JLabel("Simulador Windows"), c);
        c.gridx = 0; c.gridy = 1; add(new JLabel("Usuario:"), c);
        c.gridx = 1; add(txtUsuario, c);
        c.gridx = 0; c.gridy = 2; add(new JLabel("Contrasena:"), c);
        c.gridx = 1; add(txtClave, c);
        c.gridx = 1; c.gridy = 3; add(btnEntrar, c);
        c.gridx = 1; c.gridy = 4; add(btnCrear, c);
        c.gridx = 1; c.gridy = 5; add(chkServidor, c);

        btnEntrar.addActionListener(e -> {
            String usuario = txtUsuario.getText().trim();
            String clave = new String(txtClave.getPassword());
            if (chkServidor.isSelected()) {
                entrarPorServidor(ventana, servicio, usuario, clave);
            } else {
                entrarLocal(ventana, servicio, usuario, clave);
            }
        });

        // Pulsar Enter en la caja de contrasena es como pulsar "Entrar".
        txtClave.addActionListener(e -> btnEntrar.doClick());

        btnCrear.addActionListener(e -> ventana.mostrarRegistro());
    }

    /** Login normal, contra el UsuarioServicio local. */
    private void entrarLocal(VentanaPrincipal ventana, UsuarioServicio servicio,
                             String usuario, String clave) {
        try {
            Usuario u = servicio.login(usuario, clave);
            if (u == null) {
                ofrecerRegistro(ventana);
            } else {
                ventana.mostrarEscritorio(u);
            }
        } catch (CuentaDesactivadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    /** Login de punta a punta por el servidor de sockets (Pilar 4). */
    private void entrarPorServidor(VentanaPrincipal ventana, UsuarioServicio servicio,
                                   String usuario, String clave) {
        String respuesta = Cliente.intentar("LOGIN;" + usuario + ";" + clave);

        if (respuesta == null) {
            JOptionPane.showMessageDialog(this,
                    "No hay conexion con el servidor.\n"
                    + "Arranca SimuladorWindow.red.Servidor o desmarca 'Usar servidor'.");
            return;
        }
        if (respuesta.startsWith("OK")) {
            ventana.mostrarEscritorio(servicio.buscar(usuario));
        } else if (respuesta.contains("desactivada")) {
            JOptionPane.showMessageDialog(this, "La cuenta esta desactivada.");
        } else {
            ofrecerRegistro(ventana);
        }
    }

    private void ofrecerRegistro(VentanaPrincipal ventana) {
        int op = JOptionPane.showConfirmDialog(this,
                "Usuario o contrasena incorrectos.\n"
                + "Quieres crear una cuenta nueva?",
                "Error", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            ventana.mostrarRegistro();
        }
    }
}
