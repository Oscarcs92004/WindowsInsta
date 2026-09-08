package SimuladorWindow.ui;

import SimuladorWindow.excepciones.UsernameDuplicadoException;
import SimuladorWindow.insta.EstiloInsta;
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
        setBackground(EstiloInsta.FONDO);
        setLayout(new GridBagLayout());

        JPanel caja = EstiloInsta.tarjeta();
        caja.setLayout(new GridBagLayout());
        caja.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloInsta.BORDE, 1, true),
                EstiloInsta.margen(28, 40, 24, 40)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 4, 6, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtNombre = new JTextField(18);
        JComboBox<String> cmbGenero = new JComboBox<>(new String[] {"M", "F"});
        JTextField txtUsuario = new JTextField(18);
        JPasswordField txtClave = new JPasswordField(18);
        JTextField txtEdad = new JTextField(4);
        EstiloInsta.estiloCampo(txtNombre);
        EstiloInsta.estiloCampo(txtUsuario);
        EstiloInsta.estiloCampo(txtClave);
        EstiloInsta.estiloCampo(txtEdad);
        JButton btnFoto = EstiloInsta.botonSecundario("Elegir foto");
        JLabel lblFoto = new JLabel("Sin foto");
        JButton btnCrear = EstiloInsta.botonPrimario("Crear cuenta");
        JButton btnVolver = EstiloInsta.enlace("Volver al login");

        int fila = 0;
        JLabel titulo = new JLabel("Crear una cuenta nueva", SwingConstants.CENTER);
        titulo.setFont(EstiloInsta.LOGO.deriveFont(19f));
        titulo.setForeground(EstiloInsta.TEXTO);
        c.gridx = 0; c.gridy = fila; c.gridwidth = 2;
        caja.add(titulo, c);
        c.gridwidth = 1;
        fila++;
        c.gridx = 1; caja.add(txtNombre, c);
        fila++;
        c.gridx = 0; c.gridy = fila; caja.add(new JLabel("Genero:"), c);
        c.gridx = 1; caja.add(cmbGenero, c);
        fila++;
        c.gridx = 0; c.gridy = fila; caja.add(new JLabel("Username:"), c);
        c.gridx = 1; caja.add(txtUsuario, c);
        fila++;
        c.gridx = 0; c.gridy = fila; caja.add(new JLabel("Contrasena:"), c);
        c.gridx = 1; caja.add(txtClave, c);
        fila++;
        c.gridx = 0; c.gridy = fila; caja.add(new JLabel("Edad:"), c);
        c.gridx = 1; caja.add(txtEdad, c);
        fila++;
        c.gridx = 0; c.gridy = fila; caja.add(new JLabel("Foto de perfil:"), c);
        c.gridx = 1; caja.add(btnFoto, c);
        fila++;
        c.gridx = 1; c.gridy = fila; caja.add(lblFoto, c);
        fila++;
        c.gridx = 0; c.gridy = fila; c.gridwidth = 2; c.insets = new Insets(14, 4, 4, 4);
        caja.add(btnCrear, c);
        fila++;
        c.gridy = fila; c.insets = new Insets(2, 4, 0, 4);
        caja.add(btnVolver, c);
        c.gridwidth = 1;

        add(caja, new GridBagConstraints());

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
