package SimuladorWindow.insta;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class GaleriaStickers {

    private static final int LADO = 72;
    private static final int ALTO_MAXIMO = 560;

    private GaleriaStickers() {
    }

    public static Sticker elegir(Component padre, InstaServicio insta, String username) {
        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(padre), "Stickers",
                Dialog.ModalityType.APPLICATION_MODAL);
        Sticker[] elegido = new Sticker[1];

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(EstiloInsta.BLANCO);
        contenido.setBorder(EstiloInsta.margen(8, 12, 8, 12));

        Runnable llenar = () -> {
            contenido.removeAll();
            String carpetaGlobal = insta.carpetaStickersGlobales().getPath();
            JPanel porDefecto = grilla();
            JPanel personales = grilla();
            for (Sticker s : insta.stickersDe(username)) {
                JButton boton = botonSticker(s);
                boton.addActionListener(e -> {
                    elegido[0] = s;
                    dialogo.dispose();
                });
                if (s.getRuta().startsWith(carpetaGlobal)) {
                    porDefecto.add(boton);
                } else {
                    personales.add(boton);
                }
            }
            contenido.add(titulo("Por defecto"));
            contenido.add(porDefecto);
            contenido.add(titulo("Mis stickers"));
            if (personales.getComponentCount() == 0) {
                JLabel vacio = new JLabel("Todavía no has importado stickers.");
                vacio.setFont(EstiloInsta.CHICA);
                vacio.setForeground(EstiloInsta.TEXTO_GRIS);
                vacio.setAlignmentX(Component.LEFT_ALIGNMENT);
                vacio.setBorder(EstiloInsta.margen(4, 2, 8, 2));
                contenido.add(vacio);
            } else {
                contenido.add(personales);
            }
            contenido.revalidate();
            contenido.repaint();
        };
        llenar.run();

        JButton importar = EstiloInsta.botonSecundario("Importar sticker...");
        importar.addActionListener(e -> {
            if (importar(dialogo, insta, username)) {
                llenar.run();
                ajustarTamano(dialogo);
            }
        });
        JButton cancelar = EstiloInsta.botonSecundario("Cancelar");
        cancelar.addActionListener(e -> dialogo.dispose());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 8));
        botones.setBackground(EstiloInsta.BLANCO);
        botones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloInsta.BORDE));
        botones.add(importar);
        botones.add(cancelar);

        JScrollPane scroll = new JScrollPane(contenido,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(EstiloInsta.BLANCO);
        EstiloInsta.scrollFino(scroll);

        dialogo.setLayout(new BorderLayout());
        dialogo.add(scroll, BorderLayout.CENTER);
        dialogo.add(botones, BorderLayout.SOUTH);
        ajustarTamano(dialogo);
        dialogo.setLocationRelativeTo(padre);
        dialogo.setVisible(true);
        return elegido[0];
    }

    static ImageIcon icono(String ruta, int lado) {
        if (ruta == null || !new File(ruta).exists()) {
            return null;
        }
        ImageIcon original = new ImageIcon(ruta);
        int ancho = original.getIconWidth();
        int alto = original.getIconHeight();
        if (ancho <= 0 || alto <= 0) {
            return null;
        }
        double factor = Math.min((double) lado / ancho, (double) lado / alto);
        int nuevoAncho = Math.max(1, (int) Math.round(ancho * factor));
        int nuevoAlto = Math.max(1, (int) Math.round(alto * factor));
        return new ImageIcon(original.getImage()
                .getScaledInstance(nuevoAncho, nuevoAlto, Image.SCALE_SMOOTH));
    }

    static boolean importar(Component padre, InstaServicio insta, String username) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Imagenes (png, jpg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(padre) != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        File origen = chooser.getSelectedFile();
        String nombre = origen.getName();
        String minusculas = nombre.toLowerCase();
        if (!minusculas.endsWith(".png") && !minusculas.endsWith(".jpg")
                && !minusculas.endsWith(".jpeg")) {
            JOptionPane.showMessageDialog(padre, "El sticker debe ser .png o .jpg");
            return false;
        }

        File carpeta = insta.carpetaStickersPersonalesDe(username);
        carpeta.mkdirs();
        File destino = new File(carpeta, nombre);
        try {
            Files.copy(origen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(padre, "No se pudo importar: " + ex.getMessage());
            return false;
        }

        for (Sticker s : insta.stickersDe(username)) {
            if (s.getRuta().equals(destino.getPath())) {
                JOptionPane.showMessageDialog(padre, "Sticker actualizado.");
                return true;
            }
        }
        insta.agregarSticker(username,
                new Sticker(nombre.substring(0, nombre.lastIndexOf('.')), destino.getPath()));
        JOptionPane.showMessageDialog(padre, "Sticker importado.");
        return true;
    }

    private static JPanel grilla() {
        JPanel grilla = new JPanel(new GridLayout(0, 4, 6, 6));
        grilla.setOpaque(false);
        grilla.setAlignmentX(Component.LEFT_ALIGNMENT);
        grilla.setBorder(EstiloInsta.margen(2, 0, 10, 0));
        return grilla;
    }

    private static JLabel titulo(String texto) {
        JLabel titulo = new JLabel(texto);
        titulo.setFont(EstiloInsta.FUERTE);
        titulo.setForeground(EstiloInsta.TEXTO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        titulo.setBorder(EstiloInsta.margen(4, 2, 4, 2));
        return titulo;
    }

    private static JButton botonSticker(Sticker s) {
        String nombre = s.getNombre();
        if (nombre.length() > 12) {
            nombre = nombre.substring(0, 11) + "…";
        }
        JButton boton = new JButton(nombre, icono(s.getRuta(), LADO));
        boton.setToolTipText(s.getNombre());
        boton.setVerticalTextPosition(SwingConstants.BOTTOM);
        boton.setHorizontalTextPosition(SwingConstants.CENTER);
        boton.setFont(EstiloInsta.CHICA);
        boton.setForeground(EstiloInsta.TEXTO);
        boton.setContentAreaFilled(false);
        boton.setFocusPainted(false);
        boton.setBorder(EstiloInsta.margen(6, 4, 6, 4));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    private static void ajustarTamano(JDialog dialogo) {
        dialogo.pack();
        if (dialogo.getHeight() > ALTO_MAXIMO) {
            dialogo.setSize(dialogo.getWidth() + 14, ALTO_MAXIMO);
        }
    }
}
