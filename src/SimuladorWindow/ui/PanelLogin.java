package SimuladorWindow.ui;

import SimuladorWindow.excepciones.CuentaDesactivadaException;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.red.Cliente;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

/**
 * Pantalla de inicio de sesion (enunciado 4.2a), con el aspecto del cuadro de
 * "Iniciar la sesion" de Windows 98: un dialogo gris con barra de titulo azul
 * marino, el dibujo de una llave a la izquierda, los campos hundidos y los
 * botones con relieve.
 *
 * Si se marca "Usar servidor", el login se hace por sockets contra el
 * Servidor (Pilar 4). Sin marcar, se hace en local.
 */
public class PanelLogin extends JPanel {

    public PanelLogin(VentanaPrincipal ventana, UsuarioServicio servicio) {
        setBackground(Estilo.FONDO_ESCRITORIO);   // escritorio teal de Windows 98
        setLayout(new GridBagLayout());

        JTextField txtUsuario = new JTextField(16);
        JPasswordField txtClave = new JPasswordField(16);
        campoHundido(txtUsuario);
        campoHundido(txtClave);

        JButton btnAceptar = boton("Aceptar");
        JButton btnCrear = boton("Crear cuenta");
        JCheckBox chkServidor = new JCheckBox("Usar servidor (sockets)");
        chkServidor.setOpaque(false);
        chkServidor.setFont(Estilo.NORMAL);

        // --- El "dialogo" ------------------------------------------------
        JPanel dialogo = new JPanel(new BorderLayout());
        dialogo.setBackground(Estilo.PANEL);
        dialogo.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        dialogo.add(barraTitulo("Iniciar la sesion en Simulador Windows"), BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(12, 0));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 14));
        cuerpo.add(new PanelLlave(), BorderLayout.WEST);
        cuerpo.add(formulario(txtUsuario, txtClave, chkServidor), BorderLayout.CENTER);
        dialogo.add(cuerpo, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 8));
        botones.setOpaque(false);
        botones.add(btnAceptar);
        botones.add(btnCrear);
        dialogo.add(botones, BorderLayout.SOUTH);

        add(dialogo, new GridBagConstraints());

        // --- Acciones --------------------------------------------------
        btnAceptar.addActionListener(e -> {
            String usuario = txtUsuario.getText().trim();
            String clave = new String(txtClave.getPassword());
            if (chkServidor.isSelected()) {
                entrarPorServidor(ventana, servicio, usuario, clave);
            } else {
                entrarLocal(ventana, servicio, usuario, clave);
            }
        });
        txtUsuario.addActionListener(e -> btnAceptar.doClick());
        txtClave.addActionListener(e -> btnAceptar.doClick());
        btnCrear.addActionListener(e -> ventana.mostrarRegistro());
    }

    // -----------------------------------------------------------------
    //  Piezas visuales estilo Windows 98
    // -----------------------------------------------------------------

    private JComponent barraTitulo(String texto) {
        JLabel titulo = new JLabel(texto);
        titulo.setForeground(Estilo.TEXTO_CLARO);
        titulo.setFont(Estilo.SUBTITULO);
        titulo.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Estilo.ACENTO);      // azul marino
        barra.add(titulo, BorderLayout.WEST);
        return barra;
    }

    private JComponent formulario(JTextField usuario, JPasswordField clave,
                                  JCheckBox servidor) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.anchor = GridBagConstraints.WEST;

        JLabel ayuda = new JLabel("Escriba su nombre de usuario y su contrasena.");
        ayuda.setFont(Estilo.NORMAL);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        form.add(ayuda, c);
        c.gridwidth = 1;

        c.gridx = 0; c.gridy = 1; form.add(etiqueta("Nombre de usuario:"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; form.add(usuario, c);
        c.fill = GridBagConstraints.NONE;

        c.gridx = 0; c.gridy = 2; form.add(etiqueta("Contrasena:"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; form.add(clave, c);
        c.fill = GridBagConstraints.NONE;

        c.gridx = 1; c.gridy = 3; form.add(servidor, c);
        return form;
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Estilo.NORMAL);
        return l;
    }

    private JButton boton(String texto) {
        JButton b = new JButton(texto);
        b.setFont(Estilo.NORMAL);
        b.setBackground(Estilo.PANEL);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createBevelBorder(BevelBorder.RAISED),
                BorderFactory.createEmptyBorder(3, 14, 3, 14)));
        return b;
    }

    private void campoHundido(JTextField campo) {
        campo.setFont(Estilo.NORMAL);
        campo.setBackground(Color.WHITE);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)));
    }

    /** El dibujo de la llave dorada, como el del logon de Windows 98. */
    private static class PanelLlave extends JPanel {
        PanelLlave() {
            setPreferredSize(new Dimension(64, 64));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // Fondo azul con borde, como una "tarjeta" del logon.
            g2.setColor(new Color(0, 0, 128));
            g2.fillRect(2, 2, 58, 58);
            g2.setColor(Color.WHITE);
            g2.drawRect(2, 2, 58, 58);

            // La llave dorada.
            g2.setColor(new Color(255, 204, 0));
            g2.setStroke(new BasicStroke(5f));
            g2.drawOval(12, 16, 18, 18);          // anillo
            g2.drawLine(29, 27, 50, 44);          // cana
            g2.setStroke(new BasicStroke(4f));
            g2.drawLine(46, 40, 52, 34);          // diente 1
            g2.drawLine(40, 34, 46, 28);          // diente 2
        }
    }

    // -----------------------------------------------------------------
    //  Logica de login (igual que antes)
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
