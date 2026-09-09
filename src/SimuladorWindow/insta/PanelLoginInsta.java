package SimuladorWindow.insta;

import SimuladorWindow.excepciones.CuentaDesactivadaException;
import SimuladorWindow.excepciones.UsernameDuplicadoException;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

/**
 * Login y registro de INSTA+ (enunciado 4.2), con el aspecto del login de
 * Instagram real. Las cuentas viven en users.ins, aparte de las de Windows 98,
 * asi que en una misma sesion de Windows se pueden usar dos cuentas de
 * INSTA+ distintas: solo hay que cerrar sesion y entrar con la otra.
 */
public class PanelLoginInsta extends JPanel {

    private final UsuarioServicio usuarios;
    private final Consumer<Usuario> alEntrar;

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);

    private String rutaFoto;

    public PanelLoginInsta(UsuarioServicio usuarios, Consumer<Usuario> alEntrar) {
        this.usuarios = usuarios;
        this.alEntrar = alEntrar;

        setBackground(EstiloInsta.BLANCO);
        setLayout(new GridBagLayout());

        contenido.setOpaque(false);
        contenido.add(formularioLogin(), "IN");
        contenido.add(formularioRegistro(), "UP");
        add(contenido, new GridBagConstraints());
    }

    // -----------------------------------------------------------------

    private JComponent formularioLogin() {
        JTextField usuario = new JTextField(16);
        JPasswordField clave = new JPasswordField(16);
        EstiloInsta.estiloCampo(usuario);
        EstiloInsta.estiloCampo(clave);

        JButton entrar = EstiloInsta.botonPrimario("Entrar");
        entrar.addActionListener(e -> intentarLogin(
                usuario.getText().trim(), new String(clave.getPassword())));
        clave.addActionListener(e -> entrar.doClick());

        JButton irRegistro = EstiloInsta.enlace("Crear cuenta nueva");
        irRegistro.addActionListener(e -> cartas.show(contenido, "UP"));

        JPanel col = columna();
        col.add(logo());
        col.add(Box.createVerticalStrut(24));
        col.add(campo(usuario, "Usuario"));
        col.add(Box.createVerticalStrut(8));
        col.add(campo(clave, "Contrasena"));
        col.add(Box.createVerticalStrut(14));
        col.add(anchoCompleto(entrar));
        col.add(Box.createVerticalStrut(18));
        col.add(separador());
        col.add(Box.createVerticalStrut(10));
        col.add(irRegistro);
        return col;
    }

    private JComponent formularioRegistro() {
        JTextField nombre = new JTextField(16);
        JComboBox<String> genero = new JComboBox<>(new String[] {"M", "F"});
        JTextField usuario = new JTextField(16);
        JPasswordField clave = new JPasswordField(16);
        JTextField edad = new JTextField(4);
        EstiloInsta.estiloCampo(nombre);
        EstiloInsta.estiloCampo(usuario);
        EstiloInsta.estiloCampo(clave);
        EstiloInsta.estiloCampo(edad);

        JButton foto = EstiloInsta.botonSecundario("Elegir foto");
        JLabel fotoNom = new JLabel("(sin foto)");
        fotoNom.setFont(EstiloInsta.CHICA);
        foto.addActionListener(e -> {
            JFileChooser ch = new JFileChooser();
            ch.setFileFilter(new FileNameExtensionFilter("Imagenes (png, jpg)", "png", "jpg", "jpeg"));
            if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                rutaFoto = ch.getSelectedFile().getPath();
                fotoNom.setText(new File(rutaFoto).getName());
            }
        });

        JButton crear = EstiloInsta.botonPrimario("Registrarme");
        crear.addActionListener(e -> intentarRegistro(
                nombre.getText().trim(),
                genero.getSelectedItem().toString().charAt(0),
                usuario.getText().trim(),
                new String(clave.getPassword()),
                edad.getText().trim()));

        JButton volver = EstiloInsta.enlace("Ya tengo cuenta");
        volver.addActionListener(e -> cartas.show(contenido, "IN"));

        JPanel col = columna();
        col.add(logo());
        col.add(Box.createVerticalStrut(6));
        JLabel sub = new JLabel("Registrate para ver fotos y videos de tus amigos.");
        sub.setFont(EstiloInsta.CHICA);
        sub.setForeground(EstiloInsta.TEXTO_GRIS);
        sub.setAlignmentX(CENTER_ALIGNMENT);
        col.add(sub);
        col.add(Box.createVerticalStrut(16));
        col.add(campo(nombre, "Nombre completo"));
        col.add(Box.createVerticalStrut(6));
        col.add(fila("Genero:", genero));
        col.add(Box.createVerticalStrut(6));
        col.add(campo(usuario, "Nombre de usuario"));
        col.add(Box.createVerticalStrut(6));
        col.add(campo(clave, "Contrasena"));
        col.add(Box.createVerticalStrut(6));
        col.add(fila("Edad:", edad));
        col.add(Box.createVerticalStrut(6));
        JPanel filaFoto = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        filaFoto.setOpaque(false);
        filaFoto.add(foto);
        filaFoto.add(fotoNom);
        col.add(filaFoto);
        col.add(Box.createVerticalStrut(12));
        col.add(anchoCompleto(crear));
        col.add(Box.createVerticalStrut(6));
        col.add(volver);
        return col;
    }

    // -----------------------------------------------------------------

    private void intentarLogin(String usuario, String clave) {
        try {
            Usuario u = usuarios.login(usuario, clave);
            if (u == null) {
                JOptionPane.showMessageDialog(this, "Usuario o contrasena incorrectos.");
                return;
            }
            alEntrar.accept(u);
        } catch (CuentaDesactivadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void intentarRegistro(String nombre, char genero, String usuario,
                                  String clave, String edadTxt) {
        if (nombre.isEmpty() || usuario.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa todos los campos.");
            return;
        }
        int edad;
        try {
            edad = Integer.parseInt(edadTxt);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La edad debe ser un numero.");
            return;
        }
        try {
            Usuario nuevo = new Usuario(nombre, genero, usuario, clave, edad);
            nuevo.setFotoPerfil(rutaFoto);
            usuarios.registrar(nuevo);
            JOptionPane.showMessageDialog(this, "Cuenta creada. Ya puedes entrar.");
            cartas.show(contenido, "IN");
        } catch (UsernameDuplicadoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    // -----------------------------------------------------------------
    //  Piezas visuales
    // -----------------------------------------------------------------

    private JPanel columna() {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBorder(EstiloInsta.margen(20, 30, 20, 30));
        return col;
    }

    private JComponent logo() {
        JLabel l = new JLabel("Instagram", SwingConstants.CENTER);
        l.setFont(EstiloInsta.LOGO.deriveFont(36f));
        l.setForeground(EstiloInsta.TEXTO);
        l.setAlignmentX(CENTER_ALIGNMENT);
        return l;
    }

    private JComponent campo(JTextField campo, String ayuda) {
        campo.setToolTipText(ayuda);
        JPanel p = anchoCompleto(campo);
        return p;
    }

    private JComponent fila(String etiqueta, JComponent comp) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        p.setAlignmentX(CENTER_ALIGNMENT);
        p.setMaximumSize(new Dimension(240, 34));
        JLabel l = new JLabel(etiqueta);
        l.setFont(EstiloInsta.NORMAL);
        p.add(l);
        p.add(comp);
        return p;
    }

    private JPanel anchoCompleto(JComponent comp) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(CENTER_ALIGNMENT);
        p.setMaximumSize(new Dimension(240, 40));
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JComponent separador() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setAlignmentX(CENTER_ALIGNMENT);
        p.setMaximumSize(new Dimension(240, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        p.add(new JSeparator(), g);
        JLabel o = new JLabel("  O  ");
        o.setFont(EstiloInsta.CHICA);
        o.setForeground(EstiloInsta.TEXTO_GRIS);
        g.weightx = 0;
        p.add(o, g);
        g.weightx = 1;
        p.add(new JSeparator(), g);
        return p;
    }
}
