package SimuladorWindow.ui;

import SimuladorWindow.excepciones.CuentaDesactivadaException;
import SimuladorWindow.insta.EstiloInsta;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.red.Cliente;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import java.awt.*;

/**
 * Pantalla de inicio de sesion (enunciado 4.2a), con el aspecto del login de
 * Instagram: un recuadro blanco centrado con el logo, los dos campos y el
 * boton azul, y debajo otro recuadro para crear cuenta.
 *
 * Si se marca "Usar servidor", el login se hace por sockets contra el
 * Servidor (Pilar 4). Sin marcar, se hace en local.
 */
public class PanelLogin extends JPanel {

    public PanelLogin(VentanaPrincipal ventana, UsuarioServicio servicio) {
        setBackground(EstiloInsta.FONDO);
        setLayout(new GridBagLayout());

        JTextField txtUsuario = new JTextField(18);
        JPasswordField txtClave = new JPasswordField(18);
        EstiloInsta.estiloCampo(txtUsuario);
        EstiloInsta.estiloCampo(txtClave);
        EstiloInsta.placeholder(txtUsuario, "Usuario");
        EstiloInsta.placeholder(txtClave, "Contrasena");

        JButton btnEntrar = EstiloInsta.botonPrimario("Entrar");
        JCheckBox chkServidor = new JCheckBox("Usar servidor (sockets)");
        chkServidor.setBackground(EstiloInsta.BLANCO);
        chkServidor.setForeground(EstiloInsta.TEXTO_GRIS);
        chkServidor.setFont(EstiloInsta.CHICA);

        // --- Recuadro principal --------------------------------------
        JPanel caja = EstiloInsta.tarjeta();
        caja.setLayout(new GridBagLayout());
        caja.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloInsta.BORDE, 1, true),
                EstiloInsta.margen(36, 40, 24, 40)));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(5, 0, 5, 0);

        JLabel logo = new JLabel("Simulador Windows", SwingConstants.CENTER);
        logo.setFont(EstiloInsta.LOGO.deriveFont(21f));
        logo.setForeground(EstiloInsta.TEXTO);
        c.gridy = 0; c.insets = new Insets(0, 0, 18, 0); caja.add(logo, c);

        c.insets = new Insets(5, 0, 5, 0);
        c.gridy = 1; caja.add(txtUsuario, c);
        c.gridy = 2; caja.add(txtClave, c);
        c.gridy = 3; c.insets = new Insets(12, 0, 6, 0); caja.add(btnEntrar, c);
        c.gridy = 4; c.insets = new Insets(6, 0, 0, 0); caja.add(separadorO(), c);
        c.gridy = 5; c.insets = new Insets(10, 0, 0, 0); caja.add(chkServidor, c);

        // --- Recuadro "crear cuenta" --------------------------------
        JPanel cajaCrear = EstiloInsta.tarjeta();
        cajaCrear.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 10));
        cajaCrear.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloInsta.BORDE, 1, true),
                EstiloInsta.margen(6, 20, 6, 20)));
        JLabel pregunta = new JLabel("No tienes una cuenta?");
        pregunta.setForeground(EstiloInsta.TEXTO);
        pregunta.setFont(EstiloInsta.NORMAL);
        JButton btnCrear = EstiloInsta.enlace("Crear cuenta nueva");
        cajaCrear.add(pregunta);
        cajaCrear.add(btnCrear);

        // --- Columna central ---------------------------------------
        JPanel columna = new JPanel();
        columna.setOpaque(false);
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        caja.setAlignmentX(CENTER_ALIGNMENT);
        cajaCrear.setAlignmentX(CENTER_ALIGNMENT);
        caja.setMaximumSize(new Dimension(360, 400));
        cajaCrear.setMaximumSize(new Dimension(360, 60));
        columna.add(caja);
        columna.add(Box.createVerticalStrut(12));
        columna.add(cajaCrear);

        add(columna, new GridBagConstraints());

        // --- Acciones ----------------------------------------------
        btnEntrar.addActionListener(e -> {
            String usuario = EstiloInsta.valorReal(txtUsuario).trim();
            String clave = EstiloInsta.valorReal(txtClave);
            if (chkServidor.isSelected()) {
                entrarPorServidor(ventana, servicio, usuario, clave);
            } else {
                entrarLocal(ventana, servicio, usuario, clave);
            }
        });
        txtClave.addActionListener(e -> btnEntrar.doClick());
        btnCrear.addActionListener(e -> ventana.mostrarRegistro());
    }

    private JComponent separadorO() {
        JPanel fila = new JPanel(new GridBagLayout());
        fila.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        fila.add(new JSeparator(), g);
        JLabel o = new JLabel("  o  ");
        o.setForeground(EstiloInsta.TEXTO_GRIS);
        o.setFont(EstiloInsta.CHICA);
        g.weightx = 0;
        fila.add(o, g);
        g.weightx = 1;
        fila.add(new JSeparator(), g);
        return fila;
    }

    // -----------------------------------------------------------------

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
