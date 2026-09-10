package SimuladorWindow.insta;

import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

/**
 * Editar perfil (enunciado 4.4, 4.10 y 4.13).
 *
 * Deja cambiar nombre, edad, contraseña y foto de perfil, y activar o
 * desactivar la cuenta. Al guardar, UsuarioServicio actualiza usuarios.sop.
 */
public class PanelEditarPerfil extends JPanel {

    private final UsuarioServicio usuarios;
    private final Usuario usuarioActual;

    private final JLabel avatar = new JLabel("", SwingConstants.CENTER);
    private final JTextField txtNombre = new JTextField(20);
    private final JTextField txtEdad = new JTextField(4);
    private final JPasswordField txtClave = new JPasswordField(20);
    private final JTextField txtFoto = new JTextField(20);
    private final JLabel lblEstado = new JLabel();

    private final PanelFeed form = new PanelFeed();

    public PanelEditarPerfil(InstaServicio insta, UsuarioServicio usuarios,
                             Usuario usuarioActual, PanelInsta panelInsta) {
        this.usuarios = usuarios;
        this.usuarioActual = usuarioActual;

        setBackground(EstiloInsta.BLANCO);
        setLayout(new BorderLayout());

        // Cabecera con la flecha de volver.
        JButton volver = new JButton("‹");
        volver.setFont(EstiloInsta.TITULO);
        volver.setContentAreaFilled(false);
        volver.setBorderPainted(false);
        volver.setFocusPainted(false);
        volver.setForeground(EstiloInsta.TEXTO);
        volver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        volver.addActionListener(e -> panelInsta.volverAlPerfil());
        JLabel titulo = new JLabel("Editar perfil");
        titulo.setFont(EstiloInsta.FUERTE.deriveFont(15f));
        JPanel cab = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        cab.setBackground(EstiloInsta.BLANCO);
        cab.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloInsta.BORDE));
        cab.add(volver);
        cab.add(titulo);
        add(cab, BorderLayout.NORTH);

        // Formulario.
        form.setBackground(EstiloInsta.BLANCO);
        form.setBorder(EstiloInsta.margen(14, 16, 14, 16));

        txtFoto.setEditable(false);
        EstiloInsta.estiloCampo(txtNombre);
        EstiloInsta.estiloCampo(txtEdad);
        EstiloInsta.estiloCampo(txtClave);
        EstiloInsta.estiloCampo(txtFoto);

        avatar.setAlignmentX(CENTER_ALIGNMENT);
        JButton btnFoto = EstiloInsta.enlace("Cambiar foto de perfil");
        btnFoto.setAlignmentX(CENTER_ALIGNMENT);
        btnFoto.addActionListener(e -> elegirFoto());

        form.add(centrado(avatar));
        form.add(Box.createVerticalStrut(4));
        form.add(centrado(btnFoto));
        form.add(Box.createVerticalStrut(14));

        form.add(etiqueta("Nombre completo"));
        form.add(campo(txtNombre));
        form.add(etiqueta("Edad"));
        form.add(campo(txtEdad));
        form.add(etiqueta("Contraseña"));
        form.add(campo(txtClave));
        form.add(etiqueta("Foto de perfil"));
        form.add(campo(txtFoto));
        form.add(Box.createVerticalStrut(14));

        JButton btnGuardar = EstiloInsta.botonPrimario("Guardar cambios");
        btnGuardar.addActionListener(e -> guardar());
        form.add(anchoCompleto(btnGuardar));
        form.add(Box.createVerticalStrut(18));

        form.add(sep());
        form.add(Box.createVerticalStrut(10));
        form.add(etiqueta("Estado de la cuenta"));
        lblEstado.setAlignmentX(LEFT_ALIGNMENT);
        form.add(lblEstado);
        form.add(Box.createVerticalStrut(6));
        JButton btnEstado = EstiloInsta.botonSecundario("Activar / Desactivar cuenta");
        btnEstado.addActionListener(e -> cambiarEstado());
        form.add(anchoCompleto(btnEstado));
        form.add(Box.createVerticalStrut(14));

        JButton btnSalir = EstiloInsta.enlace("Cerrar sesión");
        btnSalir.setForeground(EstiloInsta.ROJO);
        btnSalir.setAlignmentX(LEFT_ALIGNMENT);
        btnSalir.addActionListener(e -> panelInsta.cerrarSesion());
        form.add(btnSalir);
        form.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(form,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(EstiloInsta.BLANCO);
        EstiloInsta.scrollFino(scroll);
        add(scroll, BorderLayout.CENTER);

        recargar();
    }

    public void recargar() {
        txtNombre.setText(usuarioActual.getNombreCompleto());
        txtEdad.setText(String.valueOf(usuarioActual.getEdad()));
        txtClave.setText(usuarioActual.getPassword());
        txtFoto.setText(usuarioActual.getFotoPerfil() == null ? "" : usuarioActual.getFotoPerfil());
        avatar.setIcon(EstiloInsta.avatar(usuarioActual.getFotoPerfil(),
                usuarioActual.getUsername(), 88));
        lblEstado.setText(usuarioActual.isActiva() ? "Activa" : "Desactivada");
        lblEstado.setForeground(usuarioActual.isActiva()
                ? EstiloInsta.TEXTO : EstiloInsta.ROJO);
    }

    private void elegirFoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Imágenes (png, jpg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtFoto.setText(chooser.getSelectedFile().getPath());
            avatar.setIcon(EstiloInsta.avatar(chooser.getSelectedFile().getPath(),
                    usuarioActual.getUsername(), 88));
        }
    }

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre no puede quedar vacío.");
            return;
        }
        int edad;
        try {
            edad = Integer.parseInt(txtEdad.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La edad debe ser un número.");
            return;
        }
        String clave = new String(txtClave.getPassword());
        if (clave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La contraseña no puede quedar vacía.");
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
                    "¿Desactivar tu cuenta? No aparecerás en búsquedas ni en el timeline.",
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

    // -----------------------------------------------------------------

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(EstiloInsta.CHICA);
        l.setForeground(EstiloInsta.TEXTO_GRIS);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(EstiloInsta.margen(6, 0, 3, 0));
        return l;
    }

    private JComponent campo(JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return c;
    }

    private JComponent anchoCompleto(JComponent comp) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JComponent centrado(JComponent comp) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, comp.getPreferredSize().height + 4));
        p.add(comp);
        return p;
    }

    private JComponent sep() {
        JSeparator s = new JSeparator();
        s.setAlignmentX(LEFT_ALIGNMENT);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        return s;
    }
}
