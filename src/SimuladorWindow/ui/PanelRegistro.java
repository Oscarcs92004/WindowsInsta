package SimuladorWindow.ui;

import SimuladorWindow.excepciones.UsernameDuplicadoException;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * Pantalla para crear una cuenta nueva (enunciado 4.2b), con el mismo aspecto
 * de Windows 98 que el login: dialogo gris con barra de titulo azul, campos
 * hundidos y botones con relieve.
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
        campoHundido(txtNombre);
        campoHundido(txtUsuario);
        campoHundido(txtClave);
        campoHundido(txtEdad);
        cmbGenero.setFont(Estilo.NORMAL);

        JButton btnFoto = boton("Elegir foto...");
        JLabel lblFoto = new JLabel("(sin foto)");
        lblFoto.setFont(Estilo.NORMAL);
        JButton btnCrear = boton("Crear");
        JButton btnVolver = boton("Volver al login");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.anchor = GridBagConstraints.WEST;

        int fila = 0;
        agregar(form, c, fila++, "Nombre completo:", txtNombre);
        agregar(form, c, fila++, "Genero:", cmbGenero);
        agregar(form, c, fila++, "Nombre de usuario:", txtUsuario);
        agregar(form, c, fila++, "Contrasena:", txtClave);
        agregar(form, c, fila++, "Edad:", txtEdad);
        agregar(form, c, fila++, "Foto de perfil:", btnFoto);
        c.gridx = 1; c.gridy = fila++; form.add(lblFoto, c);

        JPanel dialogo = new JPanel(new BorderLayout());
        dialogo.setBackground(Estilo.PANEL);
        dialogo.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        dialogo.add(barraTitulo("Crear una cuenta nueva"), BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout());
        cuerpo.setOpaque(false);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 14));
        cuerpo.add(form, BorderLayout.CENTER);
        dialogo.add(cuerpo, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 8));
        botones.setOpaque(false);
        botones.add(btnCrear);
        botones.add(btnVolver);
        dialogo.add(botones, BorderLayout.SOUTH);

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
        c.gridx = 0; c.gridy = fila; c.fill = GridBagConstraints.NONE;
        form.add(l, c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        form.add(campo, c);
        c.fill = GridBagConstraints.NONE;
    }

    private JComponent barraTitulo(String texto) {
        JLabel titulo = new JLabel(texto);
        titulo.setForeground(Estilo.TEXTO_CLARO);
        titulo.setFont(Estilo.SUBTITULO);
        titulo.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Estilo.ACENTO);
        barra.add(titulo, BorderLayout.WEST);
        return barra;
    }

    private JButton boton(String texto) {
        JButton b = new JButton(texto);
        b.setFont(Estilo.NORMAL);
        b.setBackground(Estilo.PANEL);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createBevelBorder(BevelBorder.RAISED),
                BorderFactory.createEmptyBorder(3, 12, 3, 12)));
        return b;
    }

    private void campoHundido(JTextField campo) {
        campo.setFont(Estilo.NORMAL);
        campo.setBackground(Color.WHITE);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)));
    }
}
