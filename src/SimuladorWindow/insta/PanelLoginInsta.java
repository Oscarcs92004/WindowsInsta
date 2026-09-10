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
 * así que en una misma sesión de Windows se pueden usar dos cuentas de
 * INSTA+ distintas: solo hay que cerrar sesión y entrar con la otra.
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
        EstiloInsta.placeholder(usuario, "Usuario");
        EstiloInsta.placeholder(clave, "Contraseña");

        JButton entrar = EstiloInsta.botonPrimario("Entrar");
        entrar.addActionListener(e -> intentarLogin(
                EstiloInsta.valorReal(usuario).trim(),
                EstiloInsta.valorReal(clave)));
        usuario.addActionListener(e -> entrar.doClick());
        clave.addActionListener(e -> entrar.doClick());

        JButton olvido = EstiloInsta.enlace("¿Olvidaste tu contraseña?");
        olvido.setFont(EstiloInsta.CHICA);
        olvido.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Pide al administrador del sistema que te ayude a recuperarla."));

        JButton irRegistro = EstiloInsta.enlace("Crear cuenta nueva");
        irRegistro.addActionListener(e -> cartas.show(contenido, "UP"));

        JPanel col = columna();
        col.add(EstiloInsta.wordmark(42f));
        col.add(Box.createVerticalStrut(28));
        col.add(anchoCompleto(usuario));
        col.add(Box.createVerticalStrut(8));
        col.add(anchoCompleto(clave));
        col.add(Box.createVerticalStrut(14));
        col.add(anchoCompleto(entrar));
        col.add(Box.createVerticalStrut(14));
        col.add(centrado(olvido));
        col.add(Box.createVerticalStrut(20));
        col.add(separador());
        col.add(Box.createVerticalStrut(14));
        col.add(centrado(irRegistro));
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
        EstiloInsta.placeholder(nombre, "Nombre completo");
        EstiloInsta.placeholder(usuario, "Nombre de usuario");
        EstiloInsta.placeholder(clave, "Contraseña");
        EstiloInsta.placeholder(edad, "Edad");

        JButton foto = EstiloInsta.botonSecundario("Elegir foto");
        JLabel fotoNom = new JLabel("(sin foto)");
        fotoNom.setFont(EstiloInsta.CHICA);
        fotoNom.setForeground(EstiloInsta.TEXTO_GRIS);
        foto.addActionListener(e -> {
            JFileChooser ch = new JFileChooser();
            ch.setFileFilter(new FileNameExtensionFilter("Imágenes (png, jpg)", "png", "jpg", "jpeg"));
            if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                rutaFoto = ch.getSelectedFile().getPath();
                fotoNom.setText(new File(rutaFoto).getName());
            }
        });

        JButton crear = EstiloInsta.botonPrimario("Registrarme");
        crear.addActionListener(e -> intentarRegistro(
                EstiloInsta.valorReal(nombre).trim(),
                genero.getSelectedItem().toString().charAt(0),
                EstiloInsta.valorReal(usuario).trim(),
                EstiloInsta.valorReal(clave),
                EstiloInsta.valorReal(edad).trim()));

        JButton volver = EstiloInsta.enlace("Ya tengo cuenta");
        volver.addActionListener(e -> cartas.show(contenido, "IN"));

        JPanel col = columna();
        col.add(EstiloInsta.wordmark(38f));
        col.add(Box.createVerticalStrut(6));
        JLabel sub = new JLabel("Regístrate para ver fotos y vídeos de tus amigos.");
        sub.setFont(EstiloInsta.CHICA);
        sub.setForeground(EstiloInsta.TEXTO_GRIS);
        sub.setAlignmentX(CENTER_ALIGNMENT);
        col.add(sub);
        col.add(Box.createVerticalStrut(16));
        col.add(anchoCompleto(nombre));
        col.add(Box.createVerticalStrut(6));
        col.add(fila("Género:", genero));
        col.add(Box.createVerticalStrut(6));
        col.add(anchoCompleto(usuario));
        col.add(Box.createVerticalStrut(6));
        col.add(anchoCompleto(clave));
        col.add(Box.createVerticalStrut(6));
        col.add(fila("Edad:", edad));
        col.add(Box.createVerticalStrut(6));
        JPanel filaFoto = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        filaFoto.setOpaque(false);
        filaFoto.setMaximumSize(new Dimension(260, 34));
        filaFoto.add(foto);
        filaFoto.add(fotoNom);
        col.add(filaFoto);
        col.add(Box.createVerticalStrut(12));
        col.add(anchoCompleto(crear));
        col.add(Box.createVerticalStrut(8));
        col.add(separador());
        col.add(Box.createVerticalStrut(10));
        col.add(centrado(volver));
        return col;
    }

    // -----------------------------------------------------------------

    private void intentarLogin(String usuario, String clave) {
        try {
            Usuario u = usuarios.login(usuario, clave);
            if (u == null) {
                JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.");
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
            JOptionPane.showMessageDialog(this, "La edad debe ser un número.");
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

    private JComponent anchoCompleto(JComponent comp) {
        comp.setAlignmentX(CENTER_ALIGNMENT);
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(CENTER_ALIGNMENT);
        p.setMaximumSize(new Dimension(260, 40));
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JComponent centrado(JComponent comp) {
        comp.setAlignmentX(CENTER_ALIGNMENT);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        p.setAlignmentX(CENTER_ALIGNMENT);
        p.setMaximumSize(new Dimension(260, 30));
        p.add(comp);
        return p;
    }

    private JComponent fila(String etiqueta, JComponent comp) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        p.setAlignmentX(CENTER_ALIGNMENT);
        p.setMaximumSize(new Dimension(260, 34));
        JLabel l = new JLabel(etiqueta);
        l.setFont(EstiloInsta.NORMAL);
        p.add(l);
        p.add(comp);
        return p;
    }

    private JComponent separador() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setAlignmentX(CENTER_ALIGNMENT);
        p.setMaximumSize(new Dimension(260, 20));
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
