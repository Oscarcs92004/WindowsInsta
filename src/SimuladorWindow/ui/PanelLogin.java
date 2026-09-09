package SimuladorWindow.ui;

import SimuladorWindow.excepciones.CuentaDesactivadaException;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.red.Cliente;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import java.awt.*;

/**
 * Pantalla de inicio de sesion (enunciado 4.2a), con el aspecto del cuadro de
 * "Iniciar la sesion" de Windows 98: un dialogo gris que flota sobre el
 * escritorio teal, con barra de titulo de degradado azul, el dibujo de una
 * llave dorada a la izquierda, los campos hundidos y los botones con relieve.
 *
 * Aqui solo se inicia sesion: las cuentas las crea el administrador desde el
 * menu Inicio, no hay boton de "crear cuenta". El login siempre intenta primero
 * por sockets contra el Servidor (Pilar 4); si el servidor no esta encendido,
 * se hace en local automaticamente.
 */
public class PanelLogin extends JPanel {

    public PanelLogin(VentanaPrincipal ventana, UsuarioServicio servicio) {
        setBackground(Estilo.FONDO_ESCRITORIO);
        setLayout(new GridBagLayout());

        JTextField txtUsuario = new JTextField(16);
        JPasswordField txtClave = new JPasswordField(16);
        Estilo.aplicarCampo(txtUsuario);
        Estilo.aplicarCampo(txtClave);

        JButton btnAceptar = Estilo.boton("Aceptar");
        btnAceptar.setPreferredSize(new Dimension(
                Math.max(90, btnAceptar.getPreferredSize().width),
                btnAceptar.getPreferredSize().height));

        // --- El "dialogo" que flota sobre el escritorio --------------------
        JPanel dialogo = new JPanel(new BorderLayout());
        dialogo.setBackground(Estilo.PANEL);
        dialogo.setBorder(BorderFactory.createCompoundBorder(
                Estilo.bordeSombra(6), Estilo.ventana()));

        dialogo.add(Estilo.barraTitulo("Iniciar la sesion en Windows 98",
                Estilo.iconoWindows(16)), BorderLayout.NORTH);
        dialogo.add(cuerpo(txtUsuario, txtClave, btnAceptar), BorderLayout.CENTER);

        add(dialogo, new GridBagConstraints());

        // --- Acciones -----------------------------------------------------
        btnAceptar.addActionListener(e -> {
            String usuario = txtUsuario.getText().trim();
            String clave = new String(txtClave.getPassword());
            entrar(ventana, servicio, usuario, clave);
        });
        txtUsuario.addActionListener(e -> btnAceptar.doClick());
        txtClave.addActionListener(e -> btnAceptar.doClick());

        // El cursor arranca en el campo de usuario.
        SwingUtilities.invokeLater(txtUsuario::requestFocusInWindow);
    }

    // -----------------------------------------------------------------
    //  Piezas visuales estilo Windows 98
    // -----------------------------------------------------------------

    private JComponent cuerpo(JTextField usuario, JPasswordField clave,
                              JButton aceptar) {
        JPanel cuerpo = new JPanel(new BorderLayout(16, 0));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(18, 18, 14, 18));

        // La llave dorada, alineada arriba.
        JPanel oeste = new JPanel(new BorderLayout());
        oeste.setOpaque(false);
        oeste.add(new PanelLlave(), BorderLayout.NORTH);
        cuerpo.add(oeste, BorderLayout.WEST);

        JPanel centro = new JPanel();
        centro.setOpaque(false);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));

        JLabel ayuda = new JLabel("<html>Escriba su nombre de usuario y su contrasena<br>"
                + "para iniciar la sesion en el sistema.</html>");
        ayuda.setFont(Estilo.NORMAL);
        ayuda.setAlignmentX(LEFT_ALIGNMENT);
        centro.add(ayuda);
        centro.add(Box.createVerticalStrut(12));

        JComponent form = formulario(usuario, clave);
        form.setAlignmentX(LEFT_ALIGNMENT);
        centro.add(form);
        centro.add(Box.createVerticalStrut(12));

        JComponent sep = Estilo.separador();
        sep.setAlignmentX(LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        centro.add(sep);
        centro.add(Box.createVerticalStrut(8));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botones.setOpaque(false);
        botones.setAlignmentX(LEFT_ALIGNMENT);
        botones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        botones.add(aceptar);
        centro.add(botones);

        cuerpo.add(centro, BorderLayout.CENTER);
        return cuerpo;
    }

    private JComponent formulario(JTextField usuario, JPasswordField clave) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(3, 0, 3, 0);

        c.gridx = 0; c.gridy = 0;
        form.add(etiqueta("Nombre de usuario:"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        c.insets = new Insets(3, 6, 3, 0);
        form.add(usuario, c);

        c.gridx = 0; c.gridy = 1; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        c.insets = new Insets(3, 0, 3, 0);
        form.add(etiqueta("Contrasena:"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        c.insets = new Insets(3, 6, 3, 0);
        form.add(Estilo.campoClaveConToggle(clave), c);

        return form;
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Estilo.NORMAL);
        return l;
    }

    /** El dibujo de la llave dorada sobre una placa azul, como el logon de Windows 98. */
    private static class PanelLlave extends JPanel {

        PanelLlave() {
            setPreferredSize(new Dimension(76, 76));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);

            int s = 72;

            // Placa azul con relieve, como el fondo del icono del logon.
            g2.setPaint(new GradientPaint(2, 2, new Color(0, 10, 170),
                    s, s, new Color(0, 0, 96)));
            g2.fillRoundRect(2, 2, s, s, 14, 14);
            g2.setColor(new Color(130, 150, 245));
            g2.drawRoundRect(2, 2, s - 1, s - 1, 14, 14);

            // Sombra de la llave, un poco desplazada.
            g2.translate(3, 4);
            pintarLlave(g2, new Color(0, 0, 0, 85));
            g2.translate(-3, -4);

            // La llave dorada.
            pintarLlave(g2, null);

            g2.dispose();
        }

        /** Dibuja la llave. Si {@code plano} es null usa el degradado dorado. */
        private void pintarLlave(Graphics2D g2, Color plano) {
            if (plano != null) {
                g2.setColor(plano);
            } else {
                g2.setPaint(new GradientPaint(14, 14, new Color(255, 234, 150),
                        60, 62, new Color(196, 134, 20)));
            }

            // Anillo (la parte que se agarra).
            g2.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(13, 13, 24, 24);

            // Cana.
            g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(33, 33, 57, 57);

            // Dientes.
            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(52, 52, 59, 45);
            g2.drawLine(46, 46, 52, 40);
        }
    }

    // -----------------------------------------------------------------
    //  Logica de login
    // -----------------------------------------------------------------

    /**
     * Intenta entrar. Primero prueba por el servidor de sockets; si no
     * responde (no esta encendido), cae en el login local.
     */
    private void entrar(VentanaPrincipal ventana, UsuarioServicio servicio,
                        String usuario, String clave) {
        String respuesta = Cliente.intentar("LOGIN;" + usuario + ";" + clave);

        if (respuesta == null) {
            entrarLocal(ventana, servicio, usuario, clave);   // servidor apagado
            return;
        }
        if (respuesta.startsWith("OK")) {
            ventana.mostrarEscritorio(servicio.buscar(usuario));
        } else if (respuesta.contains("desactivada")) {
            JOptionPane.showMessageDialog(this, "La cuenta esta desactivada.");
        } else {
            error();
        }
    }

    private void entrarLocal(VentanaPrincipal ventana, UsuarioServicio servicio,
                             String usuario, String clave) {
        try {
            Usuario u = servicio.login(usuario, clave);
            if (u == null) {
                error();
            } else {
                ventana.mostrarEscritorio(u);
            }
        } catch (CuentaDesactivadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void error() {
        JOptionPane.showMessageDialog(this,
                "Usuario o contrasena incorrectos.",
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
