package SimuladorWindow.ui;

import SimuladorWindow.excepciones.UsernameDuplicadoException;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * Pantalla para crear una cuenta nueva (enunciado 4.2b).
 *
 * Pide los datos del Usuario y llama a UsuarioServicio.registrar(...).
 * Si el username ya existe, captura UsernameDuplicadoException y avisa.
 */
public class PanelRegistro extends JPanel {

    /** Ruta de la foto de perfil elegida, o null si no eligio ninguna. */
    private String rutaFoto = null;

    public PanelRegistro(VentanaPrincipal ventana, UsuarioServicio servicio) {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        JTextField txtNombre = new JTextField(18);
        JComboBox<String> cmbGenero = new JComboBox<>(new String[] {"M", "F"});
        JTextField txtUsuario = new JTextField(18);
        JPasswordField txtClave = new JPasswordField(18);
        JTextField txtEdad = new JTextField(4);
        JButton btnFoto = new JButton("Elegir foto");
        JLabel lblFoto = new JLabel("Sin foto");
        JButton btnCrear = new JButton("Crear");
        JButton btnVolver = new JButton("Volver al login");

        int fila = 0;
        c.gridx = 0; c.gridy = fila; add(new JLabel("Nombre completo:"), c);
        c.gridx = 1; add(txtNombre, c);
        fila++;
        c.gridx = 0; c.gridy = fila; add(new JLabel("Genero:"), c);
        c.gridx = 1; add(cmbGenero, c);
        fila++;
        c.gridx = 0; c.gridy = fila; add(new JLabel("Username:"), c);
        c.gridx = 1; add(txtUsuario, c);
        fila++;
        c.gridx = 0; c.gridy = fila; add(new JLabel("Contrasena:"), c);
        c.gridx = 1; add(txtClave, c);
        fila++;
        c.gridx = 0; c.gridy = fila; add(new JLabel("Edad:"), c);
        c.gridx = 1; add(txtEdad, c);
        fila++;
        c.gridx = 0; c.gridy = fila; add(new JLabel("Foto de perfil:"), c);
        c.gridx = 1; add(btnFoto, c);
        fila++;
        c.gridx = 1; c.gridy = fila; add(lblFoto, c);
        fila++;
        c.gridx = 1; c.gridy = fila; add(btnCrear, c);
        fila++;
        c.gridx = 1; c.gridy = fila; add(btnVolver, c);

        btnFoto.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Elegir foto de perfil");
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Imagenes (png, jpg)", "png", "jpg", "jpeg"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File elegido = chooser.getSelectedFile();
                rutaFoto = elegido.getPath();
                ImageIcon icono = new ImageIcon(new ImageIcon(rutaFoto)
                        .getImage().getScaledInstance(96, 96, Image.SCALE_SMOOTH));
                lblFoto.setIcon(icono);
                lblFoto.setText("");
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
}
