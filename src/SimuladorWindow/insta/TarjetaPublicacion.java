package SimuladorWindow.insta;

import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

/**
 * Una publicacion dibujada con el aspecto de una tarjeta del feed de Instagram:
 *
 *   - cabecera: avatar con anillo, usuario en negrita, "· hace X" y los "..."
 *   - la foto o el reel (o, si es solo texto, una tarjeta de texto centrada)
 *   - fila de acciones: me gusta, comentar, compartir  ...  guardar
 *   - linea de "N Me gusta"
 *   - pie: usuario en negrita + el texto, con #hashtags y @menciones en azul
 *
 * La misma tarjeta la usan el Timeline, Interacciones y Buscar Hashtag, para que
 * las tres pantallas se vean igual (enunciado 4.7, 4.8 y 4.10).
 *
 * El "me gusta" se guarda solo en la tarjeta (no se persiste): es un detalle
 * visual para que el feed no se vea vacio.
 */
public class TarjetaPublicacion extends JPanel {

    private final Publicacion publicacion;
    private final UsuarioServicio usuarios;
    private final int anchoFeed;
    private final Consumer<String> alVerPerfil;   // puede ser null

    private boolean meGusta = false;
    private boolean guardado = false;
    private final int likesBase;

    private JButton botonLike;
    private JButton botonGuardar;
    private JLabel lineaLikes;

    public TarjetaPublicacion(Publicacion p, UsuarioServicio usuarios,
                              int anchoFeed, Consumer<String> alVerPerfil) {
        this.publicacion = p;
        this.usuarios = usuarios;
        this.anchoFeed = anchoFeed;
        this.alVerPerfil = alVerPerfil;
        this.likesBase = likesDecorativos(p);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(EstiloInsta.BLANCO);
        setAlignmentX(LEFT_ALIGNMENT);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloInsta.BORDE));

        add(cabecera());
        add(medio());
        add(filaAcciones());
        add(lineaLikes());
        add(pie());
        add(Box.createVerticalStrut(8));
    }

    /** Ocupa todo el ancho disponible, pero no se estira a lo alto. */
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    // -----------------------------------------------------------------

    private JComponent cabecera() {
        Usuario autor = usuarios.buscar(publicacion.getAutor());
        String foto = (autor == null) ? null : autor.getFotoPerfil();

        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.setAlignmentX(LEFT_ALIGNMENT);
        fila.setBorder(EstiloInsta.margen(7, 10, 7, 8));

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        izq.setOpaque(false);
        izq.add(new JLabel(EstiloInsta.avatar(foto, publicacion.getAutor(), 32)));

        JLabel usuario = new JLabel(publicacion.getAutor());
        usuario.setFont(EstiloInsta.FUERTE);
        usuario.setForeground(EstiloInsta.TEXTO);
        izq.add(usuario);

        JLabel tiempo = new JLabel("· " + TextoInsta.hace(publicacion.getFecha()));
        tiempo.setFont(EstiloInsta.CHICA);
        tiempo.setForeground(EstiloInsta.TEXTO_GRIS);
        izq.add(tiempo);
        fila.add(izq, BorderLayout.WEST);

        JButton opciones = iconoBoton(IconosInsta.OPCIONES, 22, false);
        opciones.addActionListener(e -> menuOpciones(opciones));
        fila.add(opciones, BorderLayout.EAST);

        // Tocar el avatar o el usuario abre su perfil (como en Instagram).
        if (alVerPerfil != null) {
            manoYClic(izq, () -> alVerPerfil.accept(publicacion.getAutor()));
        }
        return fila;
    }

    private JComponent medio() {
        if (publicacion.esVideo()) {
            return tarjetaVideo();
        }
        if (publicacion.tieneImagen()) {
            ImageIcon icono = ConfigInsta.escalarParaFeed(new File(publicacion.getRutaImagen()));
            JLabel imagen = new JLabel(icono != null ? icono : new ImageIcon(),
                    SwingConstants.CENTER);
            if (icono == null) {
                imagen.setText("[imagen no disponible]");
                imagen.setForeground(EstiloInsta.TEXTO_GRIS);
            }
            imagen.setAlignmentX(LEFT_ALIGNMENT);
            imagen.setBackground(EstiloInsta.BLANCO);
            imagen.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            // Doble clic en la foto = me gusta, como en Instagram.
            imagen.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2 && !meGusta) {
                        alternarLike();
                    }
                }
            });
            return imagen;
        }
        return tarjetaTexto();
    }

    /**
     * Publicación de solo texto: una tarjeta con un degradado de color y el
     * texto en blanco centrado, como el "modo texto" de las publicaciones de
     * Instagram. El color sale del texto, así que siempre es el mismo.
     */
    private JComponent tarjetaTexto() {
        final Color[] par = degradadoTexto(publicacion.getTexto());
        JPanel caja = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, par[0], getWidth(), getHeight(), par[1]));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        caja.setAlignmentX(LEFT_ALIGNMENT);
        int alto = Math.min(300, Math.max(180, 150 + publicacion.getTexto().length()));
        caja.setPreferredSize(new Dimension(anchoFeed, alto));
        caja.setMaximumSize(new Dimension(Integer.MAX_VALUE, alto));

        String html = "<div style='text-align:center'><font color='#FFFFFF'>"
                + EstiloInsta.escaparHtml(publicacion.getTexto()) + "</font></div>";
        JLabel texto = EstiloInsta.textoHtml(html, anchoFeed - 80);
        texto.setHorizontalAlignment(SwingConstants.CENTER);
        texto.setFont(EstiloInsta.FUERTE.deriveFont(19f));
        caja.add(texto);
        return caja;
    }

    /** Un par de colores estable para el fondo de una publicación de solo texto. */
    private static Color[] degradadoTexto(String texto) {
        Color[][] paletas = {
                {new Color(0x8A, 0x3A, 0xB9), new Color(0xE9, 0x5A, 0x5A)},
                {new Color(0x2B, 0x86, 0xC5), new Color(0x4A, 0xC2, 0x9A)},
                {new Color(0xF8, 0x8A, 0x24), new Color(0xE0, 0x2A, 0x6B)},
                {new Color(0x30, 0x3A, 0x8C), new Color(0x6A, 0x3F, 0xB5)},
                {new Color(0x14, 0x8F, 0x77), new Color(0x1E, 0x5A, 0x8A)},
        };
        return paletas[Math.abs(texto.hashCode()) % paletas.length];
    }

    /** El "player" del reel: fondo oscuro, un triangulo de play y el nombre. */
    private JComponent tarjetaVideo() {
        JPanel video = new JPanel(new GridBagLayout());
        video.setBackground(new Color(20, 20, 20));
        video.setAlignmentX(LEFT_ALIGNMENT);
        video.setPreferredSize(new Dimension(anchoFeed, 260));
        video.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JLabel play = new JLabel("▶");
        play.setForeground(Color.WHITE);
        play.setFont(new Font("SansSerif", Font.BOLD, 46));

        JLabel nombre = new JLabel("REEL · " + new File(publicacion.getRutaVideo()).getName());
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
                reproducirVideo(new File(publicacion.getRutaVideo()));
            }
        });
        return video;
    }

    private JComponent filaAcciones() {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.setAlignmentX(LEFT_ALIGNMENT);
        fila.setBorder(EstiloInsta.margen(4, 6, 2, 6));

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        izq.setOpaque(false);
        botonLike = iconoBoton(IconosInsta.CORAZON, 26, meGusta);
        botonLike.addActionListener(e -> alternarLike());
        JButton comentar = iconoBoton(IconosInsta.MENSAJE, 26, false);
        JButton compartir = iconoBoton(IconosInsta.AVION, 26, false);
        izq.add(botonLike);
        izq.add(comentar);
        izq.add(compartir);
        fila.add(izq, BorderLayout.WEST);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        der.setOpaque(false);
        botonGuardar = iconoBoton(IconosInsta.GUARDAR, 26, guardado);
        botonGuardar.addActionListener(e -> {
            guardado = !guardado;
            botonGuardar.setIcon(IconosInsta.icono(IconosInsta.GUARDAR, 26, guardado));
        });
        der.add(botonGuardar);
        fila.add(der, BorderLayout.EAST);
        return fila;
    }

    private JComponent lineaLikes() {
        lineaLikes = new JLabel();
        lineaLikes.setAlignmentX(LEFT_ALIGNMENT);
        lineaLikes.setBorder(EstiloInsta.margen(0, 12, 0, 12));
        actualizarLikes();
        return lineaLikes;
    }

    private JComponent pie() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setBorder(EstiloInsta.margen(3, 12, 10, 12));

        String html = "<b>" + EstiloInsta.escaparHtml(publicacion.getAutor()) + "</b> "
                + EstiloInsta.resaltarTags(EstiloInsta.escaparHtml(publicacion.getTexto()));
        JLabel texto = EstiloInsta.textoHtml(html, anchoFeed - 34);
        texto.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(texto);
        return panel;
    }

    // -----------------------------------------------------------------

    private void alternarLike() {
        meGusta = !meGusta;
        botonLike.setIcon(IconosInsta.icono(IconosInsta.CORAZON, 26, meGusta));
        actualizarLikes();
    }

    private void actualizarLikes() {
        int total = likesBase + (meGusta ? 1 : 0);
        if (total <= 0) {
            lineaLikes.setText("Sé el primero en indicar que te gusta");
            lineaLikes.setFont(EstiloInsta.CHICA);
            lineaLikes.setForeground(EstiloInsta.TEXTO_GRIS);
        } else {
            lineaLikes.setText(total + " Me gusta");
            lineaLikes.setFont(EstiloInsta.FUERTE);
            lineaLikes.setForeground(EstiloInsta.TEXTO);
        }
    }

    private void menuOpciones(JComponent ancla) {
        JPopupMenu menu = new JPopupMenu();
        if (alVerPerfil != null) {
            JMenuItem verPerfil = new JMenuItem("Ver perfil de " + publicacion.getAutor());
            verPerfil.addActionListener(e -> alVerPerfil.accept(publicacion.getAutor()));
            menu.add(verPerfil);
        }
        JMenuItem copiar = new JMenuItem("Copiar publicación");
        copiar.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(
                        TextoInsta.formato(publicacion)), null));
        menu.add(copiar);
        menu.show(ancla, 0, ancla.getHeight());
    }

    private void reproducirVideo(File archivo) {
        if (!archivo.exists()) {
            JOptionPane.showMessageDialog(this, "El video ya no está disponible.");
            return;
        }
        try {
            Desktop.getDesktop().open(archivo);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo abrir el video: " + ex.getMessage());
        }
    }

    // -----------------------------------------------------------------
    //  Ayudantes visuales
    // -----------------------------------------------------------------

    private static JButton iconoBoton(String icono, int tam, boolean activo) {
        JButton b = new JButton(IconosInsta.icono(icono, tam, activo));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static void manoYClic(JComponent c, Runnable accion) {
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        c.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                accion.run();
            }
        });
    }

    /** Número decorativo y estable: solo para que el feed no se vea vacío. */
    private static int likesDecorativos(Publicacion p) {
        int h = (p.getAutor() + "|" + p.getFecha()).hashCode();
        return Math.abs(h % 180) + 4;
    }
}
