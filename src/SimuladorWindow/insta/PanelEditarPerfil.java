package SimuladorWindow.insta;

import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * Editar perfil (enunciado 4.4, 4.10 y 4.13).
 *
 * Deja cambiar nombre, edad, contrasena y foto de perfil, y activar o
 * desactivar la cuenta. Al guardar, UsuarioServicio actualiza usuarios.sop.
 */
public class PanelEditarPerfil extends JPanel {

    private final UsuarioServicio usuarios;
    private final Usuario usuarioActual;

    private final JTextField txtNombre = new JTextField(20);
    private final JTextField txtEdad = new JTextField(4);
    private final JPasswordField txtClave = new JPasswordField(20);
    private final JTextField txtFoto = new JTextField(20);
    private final JLabel lblEstado = new JLabel();

    public PanelEditarPerfil(InstaServicio insta, UsuarioServicio usuarios, Usuario usuarioActual) {
        this.usuarios = usuarios;
        this.usuarioActual = usuarioActual;

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        txtFoto.setEditable(false);
        JButton btnFoto = new JButton("Elegir foto");
        btnFoto.addActionListener(e -> elegirFoto());

        int fila = 0;
        c.gridx = 0; c.gridy = fila; add(new JLabel("Nombre completo:"), c);
        c.gridx = 1; add(txtNombre, c);
        fila++;
        c.gridx = 0; c.gridy = fila; add(new JLabel("Edad:"), c);
        c.gridx = 1; add(txtEdad, c);
        fila++;
        c.gridx = 0; c.gridy = fila; add(new JLabel("Contrasena:"), c);
        c.gridx = 1; add(txtClave, c);
        fila++;
        c.gridx = 0; c.gridy = fila; add(new JLabel("Foto de perfil:"), c);
        c.gridx = 1; add(txtFoto, c);
        c.gridx = 2; add(btnFoto, c);
        fila++;
        JButton btnGuardar = new JButton("Guardar cambios");
        btnGuardar.addActionListener(e -> guardar());
        c.gridx = 1; c.gridy = fila; add(btnGuardar, c);
        fila++;
        c.gridx = 0; c.gridy = fila; add(new JLabel("Estado de la cuenta:"), c);
        c.gridx = 1; add(lblEstado, c);
        fila++;
        JButton btnEstado = new JButton("Activar / Desactivar cuenta");
        btnEstado.addActionListener(e -> cambiarEstado());
        c.gridx = 1; c.gridy = fila; add(btnEstado, c);

        recargar();
    }

    public void recargar() {
        txtNombre.setText(usuarioActual.getNombreCompleto());
        txtEdad.setText(String.valueOf(usuarioActual.getEdad()));
        txtClave.setText(usuarioActual.getPassword());
        txtFoto.setText(usuarioActual.getFotoPerfil() == null ? "" : usuarioActual.getFotoPerfil());
        lblEstado.setText(usuarioActual.isActiva() ? "activa" : "inactiva");
    }

    private void elegirFoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Imagenes (png, jpg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtFoto.setText(chooser.getSelectedFile().getPath());
        }
    }

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre no puede quedar vacio.");
            return;
        }
        int edad;
        try {
            edad = Integer.parseInt(txtEdad.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La edad debe ser un numero.");
            return;
        }
        String clave = new String(txtClave.getPassword());
        if (clave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La contrasena no puede quedar vacia.");
            return;
        }

        usuarioActual.setNombreCompleto(nombre);
        usuarioActual.setEdad(edad);
        usuarioActual.setPassword(clave);
        String foto = txtFoto.getText().trim();
        usuarioActual.setFotoPerfil(foto.isEmpty() ? null : foto);

        usuarios.actualizar(usuarioActual);
        JOptionPane.showMessageDialog(this, "Cambios guardados.");
    }

    private void cambiarEstado() {
        if (usuarioActual.isActiva()) {
            int op = JOptionPane.showConfirmDialog(this,
                    "Desactivar tu cuenta? No apareceras en busquedas ni en el timeline.",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (op != JOptionPane.YES_OPTION) {
                return;
            }
            usuarioActual.setActiva(false);
            usuarios.actualizar(usuarioActual);
            JOptionPane.showMessageDialog(this, "Cuenta desactivada.");
        } else {
            usuarioActual.setActiva(true);
            usuarios.actualizar(usuarioActual);
            JOptionPane.showMessageDialog(this, "Cuenta reactivada.");
        }
        recargar();
    }
}
