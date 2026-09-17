package SimuladorWindow.insta;

import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.red.Cliente;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;


public class PanelCrear extends JPanel {

    private static final int MAX_TEXTO = 140;
    private static final int MAX_DESCRIPCION = 220;

    private final InstaServicio insta;
    private final Usuario usuarioActual;

    private final CardLayout cartas = new CardLayout();
    private final JPanel cuerpo = new JPanel(cartas);

    private final JLabel previewImg = new JLabel("Toca para elegir una foto", SwingConstants.CENTER);
    private final JTextField txtCarpeta = new JTextField(14);
    private final JTextArea txtDescImg = new JTextArea(3, 20);
    private File imagenElegida;

    private final JTextArea txtSoloTexto = new JTextArea(4, 20);

    private final SelectorSticker stickerImagen = new SelectorSticker();
    private final SelectorSticker stickerTexto = new SelectorSticker();

    public PanelCrear(InstaServicio insta, Usuario usuarioActual) {
        this.insta = insta;
        this.usuarioActual = usuarioActual;

        setBackground(EstiloInsta.BLANCO);
        setLayout(new BorderLayout());

        JLabel titulo = new JLabel("Nueva publicación");
        titulo.setFont(EstiloInsta.FUERTE.deriveFont(15f));
        titulo.setBorder(EstiloInsta.margen(10, 12, 10, 12));

        JComponent tabs = EstiloInsta.barraSegmentos(
                new String[] {"Publicación", "Texto"},
                i -> cartas.show(cuerpo, String.valueOf(i)));

        cuerpo.setBackground(EstiloInsta.BLANCO);
        cuerpo.add(pestanaImagen(), "0");
        cuerpo.add(pestanaTexto(), "1");

        JPanel norte = new JPanel(new BorderLayout());
        norte.setBackground(EstiloInsta.BLANCO);
        norte.add(titulo, BorderLayout.NORTH);
        norte.add(tabs, BorderLayout.SOUTH);

        add(norte, BorderLayout.NORTH);
        add(cuerpo, BorderLayout.CENTER);
    }

    public void recargar() {
        imagenElegida = null;
        previewImg.setIcon(null);
        previewImg.setText("Toca para elegir una foto");
        txtCarpeta.setText("");
        txtDescImg.setText("");
        txtSoloTexto.setText("");
        stickerImagen.limpiar();
        stickerTexto.limpiar();
        cartas.show(cuerpo, "0");
    }

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
                    ponerPreview(previewImg, f);
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
        form.add(Box.createVerticalStrut(8));
        form.add(etiqueta("Sticker (opcional)"));
        form.add(stickerImagen);
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
        form.add(Box.createVerticalStrut(8));
        form.add(etiqueta("Sticker (opcional)"));
        form.add(stickerTexto);
        form.add(Box.createVerticalStrut(14));
        form.add(anchoCompleto(publicar));
        return envolver(form);
    }

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
        if (!publicarEnServidor(desc, destino.getPath(), stickerImagen.ruta())) {
            return;
        }
        JOptionPane.showMessageDialog(this, "Publicación compartida.");
        recargar();
    }

    private void publicarTexto() {
        String texto = txtSoloTexto.getText().trim();
        if (texto.isEmpty() && stickerTexto.ruta() == null) {
            JOptionPane.showMessageDialog(this, "Escribe algo o agrega un sticker.");
            return;
        }
        if (texto.length() > MAX_TEXTO) {
            JOptionPane.showMessageDialog(this,
                    "El texto no puede pasar de " + MAX_TEXTO + " caracteres.");
            return;
        }
        if (!publicarEnServidor(texto, null, stickerTexto.ruta())) {
            return;
        }
        JOptionPane.showMessageDialog(this, "Publicado.");
        recargar();
    }

    private boolean publicarEnServidor(String texto, String rutaImagen, String rutaSticker) {
        String respuesta = Cliente.enviar("POST", usuarioActual.getUsername(),
                texto, rutaImagen, rutaSticker);
        if (!Cliente.esOk(respuesta)) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo publicar: " + Cliente.motivo(respuesta));
            return false;
        }
        return true;
    }

    private PanelFeed columnaForm() {
        PanelFeed p = new PanelFeed();
        p.setBackground(EstiloInsta.BLANCO);
        p.setBorder(EstiloInsta.margen(14, 16, 14, 16));
        return p;
    }

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

    private void ponerPreview(JLabel preview, File archivo) {
        ImageIcon ic = new ImageIcon(new ImageIcon(archivo.getPath()).getImage()
                .getScaledInstance(-1, 230, Image.SCALE_SMOOTH));
        preview.setIcon(ic);
        preview.setText("");
    }

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

    private class SelectorSticker extends JPanel {

        private final JLabel vista = new JLabel("Sin sticker");
        private Sticker elegido;

        SelectorSticker() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setOpaque(false);
            setAlignmentX(LEFT_ALIGNMENT);
            vista.setFont(EstiloInsta.CHICA);
            vista.setForeground(EstiloInsta.TEXTO_GRIS);

            JButton elegir = EstiloInsta.botonSecundario("Agregar sticker");
            elegir.addActionListener(e -> {
                Sticker s = GaleriaStickers.elegir(PanelCrear.this, insta,
                        usuarioActual.getUsername());
                if (s != null) {
                    elegido = s;
                    vista.setIcon(GaleriaStickers.icono(s.getRuta(), 44));
                    vista.setText(s.getNombre());
                }
            });
            JButton quitar = EstiloInsta.enlace("Quitar");
            quitar.addActionListener(e -> limpiar());

            add(elegir);
            add(Box.createHorizontalStrut(8));
            add(vista);
            add(Box.createHorizontalStrut(8));
            add(quitar);
            add(Box.createHorizontalGlue());
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        String ruta() {
            return (elegido == null) ? null : elegido.getRuta();
        }

        void limpiar() {
            elegido = null;
            vista.setIcon(null);
            vista.setText("Sin sticker");
        }
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
