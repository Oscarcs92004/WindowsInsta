package SimuladorWindow.insta;

import SimuladorWindow.estructuras.ListaEnlazada;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Feed / Comentarios / Timeline (enunciado 4.7), con el aspecto del feed de
 * Instagram: una columna de publicaciones, cada una con la cabecera
 * (avatar + usuario + hace X), la foto o el video (reel), la fila de acciones
 * (me gusta / comentar) y el pie con el texto (#hashtags y @menciones).
 *
 * El feed son mis publicaciones y las de quienes sigo, de la mas nueva a la
 * mas vieja, armadas en una ListaEnlazada propia.
 */
public class PanelTimeline extends JPanel {

    private static final int ANCHO_FEED = 380;

    private final InstaServicio insta;
    private final UsuarioServicio usuarios;
    private final Usuario usuarioActual;

    private final JPanel feed = new JPanel();

    /** Publicaciones a las que el usuario les dio "me gusta" en esta sesion. */
    private final Set<Publicacion> meGusta = new HashSet<>();

    public PanelTimeline(InstaServicio insta, UsuarioServicio usuarios, Usuario usuarioActual) {
        this.insta = insta;
        this.usuarios = usuarios;
        this.usuarioActual = usuarioActual;

        setBackground(EstiloInsta.FONDO);
        setLayout(new GridBagLayout());

        feed.setLayout(new BoxLayout(feed, BoxLayout.Y_AXIS));
        feed.setBackground(EstiloInsta.FONDO);
        feed.setBorder(EstiloInsta.margen(0, 0, 16, 0));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.HORIZONTAL;   // el feed ocupa todo el ancho
        c.weightx = 1;
        c.weighty = 1;
        add(feed, c);

        recargar();
    }

    public void recargar() {
        feed.removeAll();

        boolean hay = false;
        for (Publicacion p : construirTimeline().comoLista()) {
            feed.add(tarjetaPublicacion(p));
            hay = true;
        }
        if (!hay) {
            JLabel vacio = new JLabel("Todavia no hay publicaciones. Crea una o sigue a alguien.");
            vacio.setForeground(EstiloInsta.TEXTO_GRIS);
            vacio.setAlignmentX(CENTER_ALIGNMENT);
            feed.add(vacio);
        }
        feed.revalidate();
        feed.repaint();
    }

    /** Timeline ya ordenado (mas nuevo primero), en una ListaEnlazada propia. */
    private ListaEnlazada<Publicacion> construirTimeline() {
        List<String> autores = new ArrayList<>();
        autores.add(usuarioActual.getUsername());
        autores.addAll(insta.aQuienesSigue(usuarioActual.getUsername()));

        List<Publicacion> todas = new ArrayList<>();
        for (String autor : autores) {
            if (!insta.estaVisible(autor)) {
                continue;                       // cuenta desactivada: como si no existiera
            }
            todas.addAll(insta.publicacionesDe(autor));
        }
        todas.sort(Comparator.comparing(Publicacion::getFecha).reversed());

        ListaEnlazada<Publicacion> resultado = new ListaEnlazada<>();
        for (Publicacion p : todas) {
            resultado.agregarFinal(p);
        }
        return resultado;
    }

    // -----------------------------------------------------------------

    private JComponent tarjetaPublicacion(Publicacion p) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(EstiloInsta.BLANCO);
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloInsta.BORDE));

        card.add(cabeceraPublicacion(p));

        if (p.esVideo()) {
            card.add(tarjetaVideo(p));
        } else if (p.tieneImagen()) {
            ImageIcon icono = ConfigInsta.escalarParaFeed(new File(p.getRutaImagen()));
            JLabel imagen = new JLabel(icono != null ? icono : new ImageIcon(),
                    SwingConstants.CENTER);
            if (icono == null) {
                imagen.setText("[imagen no disponible]");
            }
            imagen.setAlignmentX(LEFT_ALIGNMENT);
            imagen.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            card.add(imagen);
        }

        card.add(filaAcciones(p));
        card.add(pie(p));
        card.add(Box.createVerticalStrut(6));

        // El feed ocupa todo el ancho; la tarjeta no debe estirarse a lo alto.
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                card.getPreferredSize().height));
        return card;
    }

    private JComponent cabeceraPublicacion(Publicacion p) {
        Usuario autor = usuarios.buscar(p.getAutor());
        String foto = (autor == null) ? null : autor.getFotoPerfil();

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        fila.setOpaque(false);
        fila.setAlignmentX(LEFT_ALIGNMENT);

        fila.add(new JLabel(EstiloInsta.avatar(foto, p.getAutor(), 32)));

        JLabel usuario = new JLabel(p.getAutor());
        usuario.setFont(EstiloInsta.FUERTE);
        usuario.setForeground(EstiloInsta.TEXTO);
        fila.add(usuario);

        JLabel tiempo = new JLabel("· " + TextoInsta.hace(p.getFecha()));
        tiempo.setFont(EstiloInsta.CHICA);
        tiempo.setForeground(EstiloInsta.TEXTO_GRIS);
        fila.add(tiempo);
        return fila;
    }

    /** El "player" del reel: fondo oscuro, un triangulo de play y el nombre. */
    private JComponent tarjetaVideo(Publicacion p) {
        JPanel video = new JPanel(new GridBagLayout());
        video.setBackground(new Color(20, 20, 20));
        video.setAlignmentX(LEFT_ALIGNMENT);
        video.setPreferredSize(new Dimension(ANCHO_FEED, 200));
        video.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel play = new JLabel("▶");     // triangulo "play"
        play.setForeground(Color.WHITE);
        play.setFont(new Font("SansSerif", Font.BOLD, 44));

        JLabel nombre = new JLabel("REEL · " + new File(p.getRutaVideo()).getName());
        nombre.setForeground(new Color(220, 220, 220));
        nombre.setFont(EstiloInsta.CHICA);

        JPanel centro = new JPanel();
        centro.setOpaque(false);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        play.setAlignmentX(CENTER_ALIGNMENT);
        nombre.setAlignmentX(CENTER_ALIGNMENT);
        centro.add(play);
        centro.add(Box.createVerticalStrut(6));
        centro.add(nombre);
        video.add(centro);

        video.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        video.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                reproducirVideo(new File(p.getRutaVideo()));
            }
        });
        return video;
    }

    /** Abre el video con el reproductor del sistema (Java no reproduce video). */
    private void reproducirVideo(File archivo) {
        if (!archivo.exists()) {
            JOptionPane.showMessageDialog(this, "El video ya no esta disponible.");
            return;
        }
        try {
            Desktop.getDesktop().open(archivo);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo abrir el video: " + ex.getMessage());
        }
    }

    /** Fila de acciones bajo la foto: me gusta y comentar (como Instagram). */
    private JComponent filaAcciones(Publicacion p) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        fila.setOpaque(false);
        fila.setAlignmentX(LEFT_ALIGNMENT);

        JButton like = new JButton(IconosInsta.icono(
                IconosInsta.CORAZON, 26, meGusta.contains(p)));
        like.setContentAreaFilled(false);
        like.setBorderPainted(false);
        like.setFocusPainted(false);
        like.setMargin(new Insets(0, 0, 0, 0));
        like.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        like.addActionListener(e -> {
            if (meGusta.contains(p)) {
                meGusta.remove(p);
            } else {
                meGusta.add(p);
            }
            recargar();
        });

        JLabel comentar = new JLabel(IconosInsta.icono(IconosInsta.MENSAJE, 26, false));

        fila.add(like);
        fila.add(comentar);
        return fila;
    }

    private JComponent pie(Publicacion p) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(EstiloInsta.margen(2, 12, 12, 12));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel likes = new JLabel(meGusta.contains(p)
                ? "Te gusta y a alguien mas"
                : "Se el primero en indicar que te gusta");
        likes.setFont(EstiloInsta.FUERTE);
        likes.setForeground(EstiloInsta.TEXTO);
        likes.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(likes);
        panel.add(Box.createVerticalStrut(3));

        // El pie de foto: el usuario en negrita y a continuacion el texto,
        // igual que Instagram. Se usa una etiqueta HTML con ancho fijo para
        // que el texto largo baje de linea en vez de cortarse.
        JLabel texto = new JLabel("<html><div style='width:" + (ANCHO_FEED - 30) + "px'>"
                + "<b>" + escaparHtml(p.getAutor()) + "</b> "
                + resaltar(escaparHtml(p.getTexto())) + "</div></html>");
        texto.setFont(EstiloInsta.NORMAL);
        texto.setForeground(EstiloInsta.TEXTO);
        texto.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(texto);
        return panel;
    }

    /** Escapa los caracteres que romperian el HTML de una etiqueta. */
    private static String escaparHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Pinta de azul los #hashtags y las @menciones, como Instagram. */
    private static String resaltar(String texto) {
        StringBuilder sb = new StringBuilder();
        for (String palabra : texto.split(" ")) {
            if (palabra.startsWith("#") || palabra.startsWith("@")) {
                sb.append("<font color='#0095F6'>").append(palabra).append("</font>");
            } else {
                sb.append(palabra);
            }
            sb.append(' ');
        }
        return sb.toString().trim();
    }
}
