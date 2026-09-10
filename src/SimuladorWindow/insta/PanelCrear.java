package SimuladorWindow.insta;

import SimuladorWindow.modelo.Usuario;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Crear una publicación, como el botón "+" de Instagram (enunciado 4.3 y 4.6).
 *
 * Tres pestañas, igual que en la app:
 *   - Publicación: una foto + descripción (máx 220), con carpeta personal
 *     opcional, #hashtags y @menciones.
 *   - Reel: un video (.mp4, .mov, .avi) + descripción (máx 220).
 *   - Texto: un insta de solo texto (máx 140).
 *
 * Las fotos se copian a /imagenes y los videos a /videos de la cuenta, y todo
 * queda registrado en insta.ins como una Publicacion.
 */
public class PanelCrear extends JPanel {

    private static final int MAX_TEXTO = 140;
    private static final int MAX_DESCRIPCION = 220;

    private final InstaServicio insta;
    private final Usuario usuarioActual;

    private final CardLayout cartas = new CardLayout();
    private final JPanel cuerpo = new JPanel(cartas);

    // Pestaña "Publicación".
    private final JLabel previewImg = new JLabel("Toca para elegir una foto", SwingConstants.CENTER);
    private final JTextField txtCarpeta = new JTextField(14);
    private final JTextArea txtDescImg = new JTextArea(3, 20);
    private File imagenElegida;

    // Pestaña "Reel".
    private final JLabel previewVid = new JLabel("Toca para elegir un video", SwingConstants.CENTER);
    private final JTextArea txtDescVid = new JTextArea(3, 20);
    private File videoElegido;

    // Pestaña "Texto".
    private final JTextArea txtSoloTexto = new JTextArea(4, 20);

    public PanelCrear(InstaServicio insta, Usuario usuarioActual) {
        this.insta = insta;
        this.usuarioActual = usuarioActual;

        setBackground(EstiloInsta.BLANCO);
        setLayout(new BorderLayout());

        JLabel titulo = new JLabel("Nueva publicación");
        titulo.setFont(EstiloInsta.FUERTE.deriveFont(15f));
        titulo.setBorder(EstiloInsta.margen(10, 12, 10, 12));

        JComponent tabs = EstiloInsta.barraSegmentos(
                new String[] {"Publicación", "Reel", "Texto"},
                i -> cartas.show(cuerpo, String.valueOf(i)));

        cuerpo.setBackground(EstiloInsta.BLANCO);
        cuerpo.add(pestanaImagen(), "0");
        cuerpo.add(pestanaVideo(), "1");
        cuerpo.add(pestanaTexto(), "2");

        JPanel norte = new JPanel(new BorderLayout());
        norte.setBackground(EstiloInsta.BLANCO);
        norte.add(titulo, BorderLayout.NORTH);
        norte.add(tabs, BorderLayout.SOUTH);

        add(norte, BorderLayout.NORTH);
        add(cuerpo, BorderLayout.CENTER);
    }

    public void recargar() {
        imagenElegida = null;
        videoElegido = null;
        previewImg.setIcon(null);
        previewImg.setText("Toca para elegir una foto");
        previewVid.setIcon(null);
        previewVid.setText("Toca para elegir un video");
        txtCarpeta.setText("");
        txtDescImg.setText("");
        txtDescVid.setText("");
        txtSoloTexto.setText("");
        cartas.show(cuerpo, "0");
    }

    // -----------------------------------------------------------------

    private JComponent pestanaImagen() {
        prepararArea(txtDescImg);
        EstiloInsta.estiloCampo(txtCarpeta);

        previewImg.setPreferredSize(new Dimension(360, 240));
        marcoPreview(previewImg);
        previewImg.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                File f = elegirArchivo("Imágenes (png, jpg)", "png", "jpg", "jpeg");
                if (f != null) {
                    imagenElegida = f;
                    ponerPreview(previewImg, f, false);
                }
            }
        });

        JLabel contador = new JLabel();
        contarCaracteres(txtDescImg, contador, MAX_DESCRIPCION);

        JButton compartir = EstiloInsta.botonPrimario("Compartir");
        compartir.addActionListener(e -> publicarImagen());

        PanelFeed form = columnaForm();
        form.add(previewImg);
        form.add(Box.createVerticalStrut(12));
        form.add(etiqueta("Descripción"));
        form.add(scroll(txtDescImg));
        form.add(contador);
        form.add(Box.createVerticalStrut(8));
        form.add(etiqueta("Carpeta personal (opcional)"));
        form.add(txtCarpeta);
        form.add(Box.createVerticalStrut(14));
        form.add(anchoCompleto(compartir));
        return envolver(form);
    }

    private JComponent pestanaVideo() {
        prepararArea(txtDescVid);

        previewVid.setPreferredSize(new Dimension(360, 200));
        marcoPreview(previewVid);
        previewVid.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                File f = elegirArchivo("Videos (mp4, mov, avi, webm, mkv)",
                        "mp4", "mov", "avi", "webm", "mkv");
                if (f != null) {
                    videoElegido = f;
                    ponerPreview(previewVid, f, true);
                }
            }
        });

        JLabel contador = new JLabel();
        contarCaracteres(txtDescVid, contador, MAX_DESCRIPCION);

        JButton compartir = EstiloInsta.botonPrimario("Compartir reel");
        compartir.addActionListener(e -> publicarVideo());

        PanelFeed form = columnaForm();
        form.add(previewVid);
        form.add(Box.createVerticalStrut(12));
        form.add(etiqueta("Descripción"));
        form.add(scroll(txtDescVid));
        form.add(contador);
        form.add(Box.createVerticalStrut(14));
        form.add(anchoCompleto(compartir));
        return envolver(form);
    }

    private JComponent pestanaTexto() {
        prepararArea(txtSoloTexto);
        txtSoloTexto.setFont(EstiloInsta.NORMAL.deriveFont(16f));

        JLabel contador = new JLabel();
        contarCaracteres(txtSoloTexto, contador, MAX_TEXTO);

        JButton publicar = EstiloInsta.botonPrimario("Publicar");
        publicar.addActionListener(e -> publicarTexto());

        PanelFeed form = columnaForm();
        form.add(etiqueta("¿Qué quieres contar?"));
        form.add(scroll(txtSoloTexto));
        form.add(contador);
        form.add(Box.createVerticalStrut(14));
        form.add(anchoCompleto(publicar));
        return envolver(form);
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
                    "La descripción no puede pasar de " + MAX_DESCRIPCION + " caracteres.");
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
        JOptionPane.showMessageDialog(this, "Publicación compartida.");
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
                    "La descripción no puede pasar de " + MAX_DESCRIPCION + " caracteres.");
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
    //  Piezas visuales
    // -----------------------------------------------------------------

    private PanelFeed columnaForm() {
        PanelFeed p = new PanelFeed();
        p.setBackground(EstiloInsta.BLANCO);
        p.setBorder(EstiloInsta.margen(14, 16, 14, 16));
        return p;
    }

    /** Coloca el formulario arriba del todo y deja el resto en blanco. */
    private JComponent envolver(PanelFeed form) {
        form.add(Box.createVerticalGlue());
        JScrollPane scroll = new JScrollPane(form,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(EstiloInsta.BLANCO);
        EstiloInsta.scrollFino(scroll);
        return scroll;
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(EstiloInsta.CHICA);
        l.setForeground(EstiloInsta.TEXTO_GRIS);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(EstiloInsta.margen(0, 0, 3, 0));
        return l;
    }

    private void prepararArea(JTextArea ta) {
        EstiloInsta.estiloCampo(ta);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
    }

    private JComponent scroll(JTextArea ta) {
        JScrollPane s = new JScrollPane(ta,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        s.setBorder(BorderFactory.createLineBorder(EstiloInsta.BORDE));
        s.setAlignmentX(LEFT_ALIGNMENT);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, ta.getPreferredSize().height + 12));
        return s;
    }

    private JComponent anchoCompleto(JComponent comp) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void marcoPreview(JLabel preview) {
        preview.setAlignmentX(LEFT_ALIGNMENT);
        preview.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        preview.setForeground(EstiloInsta.TEXTO_GRIS);
        preview.setFont(EstiloInsta.NORMAL);
        preview.setBackground(new Color(250, 250, 250));
        preview.setOpaque(true);
        preview.setBorder(BorderFactory.createDashedBorder(EstiloInsta.BORDE, 6, 3));
        preview.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void ponerPreview(JLabel preview, File archivo, boolean esVideo) {
        if (esVideo) {
            preview.setIcon(IconosInsta.icono("play", 40, true));
            preview.setText("  " + archivo.getName());
            preview.setHorizontalTextPosition(SwingConstants.RIGHT);
        } else {
            ImageIcon ic = new ImageIcon(new ImageIcon(archivo.getPath()).getImage()
                    .getScaledInstance(-1, 230, Image.SCALE_SMOOTH));
            preview.setIcon(ic);
            preview.setText("");
        }
    }

    /** Muestra "N/máx" bajo un área de texto y lo actualiza al escribir. */
    private void contarCaracteres(JTextArea ta, JLabel etiqueta, int max) {
        etiqueta.setFont(EstiloInsta.CHICA);
        etiqueta.setForeground(EstiloInsta.TEXTO_GRIS);
        etiqueta.setAlignmentX(LEFT_ALIGNMENT);
        etiqueta.setHorizontalAlignment(SwingConstants.RIGHT);
        etiqueta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
        Runnable actualizar = () -> {
            int n = ta.getText().length();
            etiqueta.setText(n + "/" + max);
            etiqueta.setForeground(n > max ? EstiloInsta.ROJO : EstiloInsta.TEXTO_GRIS);
        };
        actualizar.run();
        ta.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { actualizar.run(); }
            public void removeUpdate(DocumentEvent e) { actualizar.run(); }
            public void changedUpdate(DocumentEvent e) { actualizar.run(); }
        });
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
