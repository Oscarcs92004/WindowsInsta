package SimuladorWindow.insta;

import SimuladorWindow.modelo.Usuario;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Crear una publicacion, como el boton "+" de Instagram (enunciado 4.3 y 4.6).
 *
 * Tres pestañas, igual que en la app:
 *   - Publicacion: una foto + descripcion (max 220), con carpeta personal
 *     opcional, #hashtags y @menciones.
 *   - Reel: un video (.mp4, .mov, .avi) + descripcion (max 220).
 *   - Texto: un insta de solo texto (max 140).
 *
 * Las fotos se copian a /imagenes y los videos a /videos de la cuenta, y todo
 * queda registrado en insta.ins como una Publicacion.
 */
public class PanelCrear extends JPanel {

    private static final int MAX_TEXTO = 140;
    private static final int MAX_DESCRIPCION = 220;

    private final InstaServicio insta;
    private final Usuario usuarioActual;

    // Pestaña "Publicacion".
    private final JTextField txtRutaImg = new JTextField(20);
    private final JTextField txtCarpeta = new JTextField(14);
    private final JTextArea txtDescImg = new JTextArea(4, 20);
    private File imagenElegida;

    // Pestaña "Reel".
    private final JTextField txtRutaVid = new JTextField(20);
    private final JTextArea txtDescVid = new JTextArea(4, 20);
    private File videoElegido;

    // Pestaña "Texto".
    private final JTextArea txtSoloTexto = new JTextArea(4, 20);

    public PanelCrear(InstaServicio insta, Usuario usuarioActual) {
        this.insta = insta;
        this.usuarioActual = usuarioActual;

        setBackground(EstiloInsta.FONDO);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(EstiloInsta.NORMAL);
        tabs.addTab("Publicacion", pestanaImagen());
        tabs.addTab("Reel", pestanaVideo());
        tabs.addTab("Texto", pestanaTexto());
        add(tabs, BorderLayout.CENTER);
    }

    public void recargar() {
        txtRutaImg.setText("");
        txtCarpeta.setText("");
        txtDescImg.setText("");
        imagenElegida = null;
        txtRutaVid.setText("");
        txtDescVid.setText("");
        videoElegido = null;
        txtSoloTexto.setText("");
    }

    // -----------------------------------------------------------------

    private JComponent pestanaImagen() {
        JPanel p = form();
        GridBagConstraints c = filaBase();

        txtRutaImg.setEditable(false);
        EstiloInsta.estiloCampo(txtRutaImg);
        EstiloInsta.estiloCampo(txtCarpeta);
        EstiloInsta.estiloCampo(txtDescImg);
        txtDescImg.setLineWrap(true);
        txtDescImg.setWrapStyleWord(true);

        JButton elegir = EstiloInsta.botonSecundario("Elegir foto");
        elegir.addActionListener(e -> {
            File f = elegirArchivo("Imagenes (png, jpg)", "png", "jpg", "jpeg");
            if (f != null) { imagenElegida = f; txtRutaImg.setText(f.getName()); }
        });

        int fila = 0;
        fila = fila(p, c, fila, "Foto:", txtRutaImg, elegir);
        fila = fila(p, c, fila, "Carpeta personal (opcional):", txtCarpeta, null);
        fila = area(p, c, fila, "Descripcion (max " + MAX_DESCRIPCION + "):", txtDescImg);

        JButton publicar = EstiloInsta.botonPrimario("Compartir");
        publicar.addActionListener(e -> publicarImagen());
        c.gridx = 0; c.gridy = fila; c.gridwidth = 2; p.add(publicar, c); c.gridwidth = 1;
        return p;
    }

    private JComponent pestanaVideo() {
        JPanel p = form();
        GridBagConstraints c = filaBase();

        txtRutaVid.setEditable(false);
        EstiloInsta.estiloCampo(txtRutaVid);
        EstiloInsta.estiloCampo(txtDescVid);
        txtDescVid.setLineWrap(true);
        txtDescVid.setWrapStyleWord(true);

        JButton elegir = EstiloInsta.botonSecundario("Elegir video");
        elegir.addActionListener(e -> {
            File f = elegirArchivo("Videos (mp4, mov, avi, webm)", "mp4", "mov", "avi", "webm", "mkv");
            if (f != null) { videoElegido = f; txtRutaVid.setText(f.getName()); }
        });

        int fila = 0;
        fila = fila(p, c, fila, "Video:", txtRutaVid, elegir);
        fila = area(p, c, fila, "Descripcion (max " + MAX_DESCRIPCION + "):", txtDescVid);

        JButton publicar = EstiloInsta.botonPrimario("Compartir reel");
        publicar.addActionListener(e -> publicarVideo());
        c.gridx = 0; c.gridy = fila; c.gridwidth = 2; p.add(publicar, c); c.gridwidth = 1;
        return p;
    }

    private JComponent pestanaTexto() {
        JPanel p = form();
        GridBagConstraints c = filaBase();

        EstiloInsta.estiloCampo(txtSoloTexto);
        txtSoloTexto.setLineWrap(true);
        txtSoloTexto.setWrapStyleWord(true);

        int fila = 0;
        fila = area(p, c, fila, "Texto (max " + MAX_TEXTO + "):", txtSoloTexto);

        JButton publicar = EstiloInsta.botonPrimario("Publicar");
        publicar.addActionListener(e -> publicarTexto());
        c.gridx = 0; c.gridy = fila; c.gridwidth = 2; p.add(publicar, c); c.gridwidth = 1;
        return p;
    }

    // -----------------------------------------------------------------

    private void publicarImagen() {
        if (imagenElegida == null) {
            JOptionPane.showMessageDialog(this, "Primero elige una foto.");
            return;
        }
        String desc = txtDescImg.getText().trim();
        if (desc.length() > MAX_DESCRIPCION) {
            JOptionPane.showMessageDialog(this,
                    "La descripcion no puede pasar de " + MAX_DESCRIPCION + " caracteres.");
            return;
        }
        String usuario = usuarioActual.getUsername();
        File destino = new File(insta.carpetaImagenesDe(usuario), imagenElegida.getName());
        try {
            insta.carpetaImagenesDe(usuario).mkdirs();
            Files.copy(imagenElegida.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);

            String carpetaPersonal = txtCarpeta.getText().trim();
            if (!carpetaPersonal.isEmpty()) {
                File carpeta = new File(insta.carpetaFoldersPersonalesDe(usuario), carpetaPersonal);
                carpeta.mkdirs();
                Files.copy(imagenElegida.toPath(),
                        new File(carpeta, imagenElegida.getName()).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo copiar la foto: " + ex.getMessage());
            return;
        }
        insta.publicar(usuario, new Publicacion(usuario, desc, destino.getPath()));
        JOptionPane.showMessageDialog(this, "Publicacion compartida.");
        recargar();
    }

    private void publicarVideo() {
        if (videoElegido == null) {
            JOptionPane.showMessageDialog(this, "Primero elige un video.");
            return;
        }
        String desc = txtDescVid.getText().trim();
        if (desc.length() > MAX_DESCRIPCION) {
            JOptionPane.showMessageDialog(this,
                    "La descripcion no puede pasar de " + MAX_DESCRIPCION + " caracteres.");
            return;
        }
        String usuario = usuarioActual.getUsername();
        File destino = new File(insta.carpetaVideosDe(usuario), videoElegido.getName());
        try {
            insta.carpetaVideosDe(usuario).mkdirs();
            Files.copy(videoElegido.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo copiar el video: " + ex.getMessage());
            return;
        }
        insta.publicar(usuario, new Publicacion(usuario, desc, null, destino.getPath()));
        JOptionPane.showMessageDialog(this, "Reel compartido.");
        recargar();
    }

    private void publicarTexto() {
        String texto = txtSoloTexto.getText().trim();
        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribe algo.");
            return;
        }
        if (texto.length() > MAX_TEXTO) {
            JOptionPane.showMessageDialog(this,
                    "El texto no puede pasar de " + MAX_TEXTO + " caracteres.");
            return;
        }
        insta.publicar(usuarioActual.getUsername(),
                new Publicacion(usuarioActual.getUsername(), texto, null));
        JOptionPane.showMessageDialog(this, "Publicado.");
        recargar();
    }

    // -----------------------------------------------------------------

    private JPanel form() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(EstiloInsta.FONDO);
        p.setBorder(EstiloInsta.margen(12, 12, 12, 12));
        return p;
    }

    private GridBagConstraints filaBase() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 0, 4, 0);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridwidth = 2;
        return c;
    }

    /** Etiqueta en una linea y, debajo, el campo a lo ancho (y un boton si hay). */
    private int fila(JPanel p, GridBagConstraints c, int fila,
                     String etiqueta, JComponent campo, JComponent boton) {
        JLabel l = new JLabel(etiqueta);
        l.setFont(EstiloInsta.NORMAL);
        c.gridx = 0; c.gridy = fila; p.add(l, c);
        c.gridy = fila + 1; p.add(campo, c);
        if (boton != null) {
            c.gridy = fila + 2; p.add(boton, c);
            return fila + 3;
        }
        return fila + 2;
    }

    /** Etiqueta en una linea y, debajo, un area de texto ancha. */
    private int area(JPanel p, GridBagConstraints c, int fila, String etiqueta, JTextArea ta) {
        JLabel l = new JLabel(etiqueta);
        l.setFont(EstiloInsta.NORMAL);
        c.gridx = 0; c.gridy = fila; p.add(l, c);
        c.gridy = fila + 1; p.add(new JScrollPane(ta), c);
        return fila + 2;
    }

    private File elegirArchivo(String desc, String... exts) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(desc, exts));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }
}
