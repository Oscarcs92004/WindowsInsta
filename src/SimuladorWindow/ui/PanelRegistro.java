package SimuladorWindow.ui;

import SimuladorWindow.excepciones.UsernameDuplicadoException;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * Pantalla para crear una cuenta nueva (enunciado 4.2b), con el mismo aspecto
 * de Windows 98 que el login: un cuadro gris con sombra que flota sobre el
 * escritorio, barra de titulo con degradado azul, el dibujo de una ficha de
 * usuario a la izquierda, campos hundidos y botones con relieve.
 */
public class PanelRegistro extends JPanel {

    /** Ruta de la foto de perfil elegida, o null si no eligio ninguna. */
    private String rutaFoto = null;

    public PanelRegistro(VentanaPrincipal ventana, UsuarioServicio servicio) {
        setBackground(Estilo.FONDO_ESCRITORIO);
        setLayout(new GridBagLayout());

        JTextField txtNombre = new JTextField(16);
        JComboBox<String> cmbGenero = new JComboBox<>(new String[] {"M", "F"});
        JTextField txtUsuario = new JTextField(16);
        JPasswordField txtClave = new JPasswordField(16);
        JTextField txtEdad = new JTextField(4);
        Estilo.aplicarCampo(txtNombre);
        Estilo.aplicarCampo(txtUsuario);
        Estilo.aplicarCampo(txtClave);
        Estilo.aplicarCampo(txtEdad);
        cmbGenero.setFont(Estilo.NORMAL);
        cmbGenero.setBackground(Color.WHITE);

        JButton btnFoto = Estilo.boton("Elegir foto...");
        JLabel lblFoto = new JLabel("(sin foto)");
        lblFoto.setFont(Estilo.NORMAL);
        JButton btnCrear = Estilo.boton("Crear");
        JButton btnVolver = Estilo.boton("Volver al login");
        igualarAncho(btnCrear, btnVolver);

        // --- Formulario ------------------------------------------------
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 0, 3, 0);
        c.anchor = GridBagConstraints.EAST;

        int fila = 0;
        agregar(form, c, fila++, "Nombre completo:", txtNombre);
        agregar(form, c, fila++, "Genero:", cmbGenero);
        agregar(form, c, fila++, "Nombre de usuario:", txtUsuario);
        agregar(form, c, fila++, "Contrasena:", txtClave);
        agregar(form, c, fila++, "Edad:", txtEdad);
        agregar(form, c, fila++, "Foto de perfil:", btnFoto);
        c.gridx = 1; c.gridy = fila++; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        c.insets = new Insets(0, 6, 3, 0);
        c.anchor = GridBagConstraints.WEST;
        form.add(lblFoto, c);

        // --- El "dialogo" que flota sobre el escritorio ----------------
        JPanel dialogo = new JPanel(new BorderLayout());
        dialogo.setBackground(Estilo.PANEL);
        dialogo.setBorder(BorderFactory.createCompoundBorder(
                Estilo.bordeSombra(6), Estilo.ventana()));
        dialogo.add(Estilo.barraTitulo("Crear una cuenta nueva",
                Estilo.iconoWindows(16)), BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(16, 0));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(18, 18, 14, 18));

        JPanel oeste = new JPanel(new BorderLayout());
        oeste.setOpaque(false);
        oeste.add(new PanelFicha(), BorderLayout.NORTH);
        cuerpo.add(oeste, BorderLayout.WEST);

        JPanel centro = new JPanel();
        centro.setOpaque(false);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));

        JLabel ayuda = new JLabel("<html>Rellene sus datos para crear una cuenta<br>"
                + "nueva en el sistema.</html>");
        ayuda.setFont(Estilo.NORMAL);
        ayuda.setAlignmentX(LEFT_ALIGNMENT);
        centro.add(ayuda);
        centro.add(Box.createVerticalStrut(12));

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
        botones.add(btnCrear);
        botones.add(btnVolver);
        centro.add(botones);

        cuerpo.add(centro, BorderLayout.CENTER);
        dialogo.add(cuerpo, BorderLayout.CENTER);

        add(dialogo, new GridBagConstraints());

        // --- Acciones --------------------------------------------------
        btnFoto.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Elegir foto de perfil");
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Imagenes (png, jpg)", "png", "jpg", "jpeg"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                rutaFoto = chooser.getSelectedFile().getPath();
                lblFoto.setText(new File(rutaFoto).getName());
            }
        });

        btnCrear.addActionListener(e -> {
            try {
                String nombre = txtNombre.getText().trim();
                char genero = cmbGenero.getSelectedItem().toString().charAt(0);
                String usuario = txtUsuario.getText().trim();
                String clave = new String(txtClave.getPassword());

                if (nombre.isEmpty() || usuario.isEmpty() || clave.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Completa todos los campos.");
                    return;
                }

                int edad = Integer.parseInt(txtEdad.getText().trim());

                Usuario nuevo = new Usuario(nombre, genero, usuario, clave, edad);
                nuevo.setFotoPerfil(rutaFoto);
                servicio.registrar(nuevo);

                JOptionPane.showMessageDialog(this,
                        "Cuenta creada. Ya puedes iniciar sesion.");
                ventana.mostrarLogin();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "La edad debe ser un numero.");
            } catch (UsernameDuplicadoException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        btnVolver.addActionListener(e -> ventana.mostrarLogin());
    }

    // -----------------------------------------------------------------

    private void agregar(JPanel form, GridBagConstraints c, int fila,
                         String etiqueta, JComponent campo) {
        JLabel l = new JLabel(etiqueta);
        l.setFont(Estilo.NORMAL);
        c.gridx = 0; c.gridy = fila;
        c.fill = GridBagConstraints.NONE; c.weightx = 0;
        c.insets = new Insets(3, 0, 3, 0);
        c.anchor = GridBagConstraints.EAST;
        form.add(l, c);

        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        c.insets = new Insets(3, 6, 3, 0);
        c.anchor = GridBagConstraints.WEST;
        form.add(campo, c);
    }

    /** Deja los botones del mismo ancho (el del texto mas largo, minimo 90 px). */
    private void igualarAncho(JButton... botones) {
        int ancho = 90;
        int alto = 0;
        for (JButton b : botones) {
            ancho = Math.max(ancho, b.getPreferredSize().width);
            alto = Math.max(alto, b.getPreferredSize().height);
        }
        Dimension d = new Dimension(ancho, alto);
        for (JButton b : botones) {
            b.setPreferredSize(d);
        }
    }

    /** El dibujo de una ficha de usuario sobre una placa verde. */
    private static class PanelFicha extends JPanel {

        PanelFicha() {
            setPreferredSize(new Dimension(76, 76));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int s = 72;

            // Placa verde con relieve.
            g2.setPaint(new GradientPaint(2, 2, new Color(0, 130, 90),
                    s, s, new Color(0, 78, 54)));
            g2.fillRoundRect(2, 2, s, s, 14, 14);
            g2.setColor(new Color(140, 230, 190));
            g2.drawRoundRect(2, 2, s - 1, s - 1, 14, 14);

            // Silueta de una persona, en blanco.
            g2.setColor(Color.WHITE);
            g2.fillOval(27, 16, 20, 20);                 // cabeza
            g2.fillRoundRect(16, 40, 42, 26, 20, 20);    // hombros
            g2.setColor(new Color(0, 78, 54));
            g2.fillRect(16, 60, 42, 10);                 // recorta la base

            g2.dispose();
        }
    }
}
