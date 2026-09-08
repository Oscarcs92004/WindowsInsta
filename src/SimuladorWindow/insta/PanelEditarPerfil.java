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

    public PanelEditarPerfil(InstaServicio insta, UsuarioServicio usuarios,
                             Usuario usuarioActual, PanelInsta panelInsta) {
        this.usuarios = usuarios;
        this.usuarioActual = usuarioActual;

        setBackground(EstiloInsta.FONDO);
        setLayout(new GridBagLayout());
        setBorder(EstiloInsta.margen(12, 14, 12, 14));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 0, 3, 0);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;

        txtFoto.setEditable(false);
        EstiloInsta.estiloCampo(txtNombre);
        EstiloInsta.estiloCampo(txtEdad);
        EstiloInsta.estiloCampo(txtClave);
        EstiloInsta.estiloCampo(txtFoto);
        JButton btnFoto = EstiloInsta.botonSecundario("Elegir foto");
        btnFoto.addActionListener(e -> elegirFoto());

        int fila = 0;
        JButton btnVolver = EstiloInsta.enlace("< Volver al perfil");
        btnVolver.addActionListener(e -> panelInsta.volverAlPerfil());
        c.gridy = fila++; add(btnVolver, c);

        JLabel titulo = new JLabel("Editar perfil");
        titulo.setFont(EstiloInsta.TITULO);
        c.gridy = fila++; add(titulo, c);

        fila = etiquetaCampo(c, fila, "Nombre completo:", txtNombre);
        fila = etiquetaCampo(c, fila, "Edad:", txtEdad);
        fila = etiquetaCampo(c, fila, "Contrasena:", txtClave);
        fila = etiquetaCampo(c, fila, "Foto de perfil:", txtFoto);
        c.gridy = fila++; add(btnFoto, c);

        JButton btnGuardar = EstiloInsta.botonPrimario("Guardar cambios");
        btnGuardar.addActionListener(e -> guardar());
        c.gridy = fila++; c.insets = new Insets(12, 0, 6, 0); add(btnGuardar, c);
        c.insets = new Insets(3, 0, 3, 0);

        c.gridy = fila++; add(new JLabel("Estado de la cuenta:"), c);
        c.gridy = fila++; add(lblEstado, c);
        JButton btnEstado = EstiloInsta.botonSecundario("Activar / Desactivar cuenta");
        btnEstado.addActionListener(e -> cambiarEstado());
        c.gridy = fila++; add(btnEstado, c);

        JButton btnSalir = EstiloInsta.enlace("Cerrar sesion");
        btnSalir.setForeground(EstiloInsta.ROJO);
        btnSalir.addActionListener(e -> panelInsta.cerrarSesion());
        c.gridy = fila++; add(btnSalir, c);

        recargar();
    }

    /** Etiqueta en una linea y el campo a lo ancho debajo. */
    private int etiquetaCampo(GridBagConstraints c, int fila, String etiqueta, JComponent campo) {
        JLabel l = new JLabel(etiqueta);
        l.setFont(EstiloInsta.NORMAL);
        c.gridy = fila; add(l, c);
        c.gridy = fila + 1; add(campo, c);
        return fila + 2;
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
