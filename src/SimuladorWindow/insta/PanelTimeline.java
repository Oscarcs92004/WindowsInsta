package SimuladorWindow.insta;

import SimuladorWindow.estructuras.ListaEnlazada;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Feed / Comentarios / Timeline (enunciado 4.7), con el aspecto del feed de
 * Instagram: arriba la fila de historias y debajo una columna de publicaciones
 * (cada una es una TarjetaPublicacion).
 *
 * El feed son mis publicaciones y las de quienes sigo, de la más nueva a la
 * más vieja, armadas en una ListaEnlazada propia.
 */
public class PanelTimeline extends JPanel {

    private static final int ANCHO_FEED = 380;

    private final InstaServicio insta;
    private final UsuarioServicio usuarios;
    private final Usuario usuarioActual;
    private final PanelInsta panelInsta;

    private final PanelFeed feed = new PanelFeed();

    public PanelTimeline(InstaServicio insta, UsuarioServicio usuarios,
                         Usuario usuarioActual, PanelInsta panelInsta) {
        this.insta = insta;
        this.usuarios = usuarios;
        this.usuarioActual = usuarioActual;
        this.panelInsta = panelInsta;

        setBackground(EstiloInsta.FONDO);
        setLayout(new BorderLayout());

        JScrollPane scroll = new JScrollPane(feed,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(EstiloInsta.FONDO);
        EstiloInsta.scrollFino(scroll);
        add(scroll, BorderLayout.CENTER);

        recargar();
    }

    public void recargar() {
        feed.removeAll();

        feed.add(filaHistorias());

        boolean hay = false;
        for (Publicacion p : construirTimeline().comoLista()) {
            feed.add(new TarjetaPublicacion(p, usuarios, ANCHO_FEED, panelInsta::verPerfilDe));
            hay = true;
        }
        if (!hay) {
            // Ojo: mismo alineado (LEFT) que la fila de historias y que las
            // tarjetas. Si este cartel va CENTER, el BoxLayout mezcla alineados
            // y empuja la fila de historias hacia la derecha, dejándola estrecha.
            JLabel vacio = new JLabel(
                    "Todavía no hay publicaciones. Crea una o sigue a alguien.",
                    SwingConstants.CENTER);
            vacio.setForeground(EstiloInsta.TEXTO_GRIS);
            vacio.setBorder(EstiloInsta.margen(24, 12, 24, 12));
            vacio.setAlignmentX(LEFT_ALIGNMENT);
            vacio.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                    vacio.getPreferredSize().height));
            feed.add(vacio);
        }
        feed.add(Box.createVerticalGlue());
        feed.revalidate();
        feed.repaint();
    }

    /** Timeline ya ordenado (más nuevo primero), en una ListaEnlazada propia. */
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
    //  Fila de historias: una tira horizontal de fotos de perfil arriba.
    //  Todas las celdas miden lo mismo, para que los círculos queden
    //  perfectamente alineados y a la misma distancia.
    // -----------------------------------------------------------------

    private static final int CIRCULO = 56;
    private static final int CELDA_ANCHO = 68;
    private static final int CELDA_ALTO = 82;

    private JComponent filaHistorias() {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 6));
        fila.setBackground(EstiloInsta.BLANCO);

        fila.add(historia(usuarioActual.getUsername(),
                usuarioActual.getFotoPerfil(), "Tu historia", true));

        for (String username : insta.aQuienesSigue(usuarioActual.getUsername())) {
            if (!insta.estaVisible(username)) {
                continue;
            }
            Usuario u = usuarios.buscar(username);
            String foto = (u == null) ? null : u.getFotoPerfil();
            fila.add(historia(username, foto, username, false));
        }

        int alto = CELDA_ALTO + 14;
        JScrollPane scroll = new JScrollPane(fila,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloInsta.BORDE));
        scroll.setAlignmentX(LEFT_ALIGNMENT);
        scroll.getViewport().setBackground(EstiloInsta.BLANCO);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, alto));
        scroll.setPreferredSize(new Dimension(ANCHO_FEED, alto));
        EstiloInsta.scrollFino(scroll);
        return scroll;
    }

    /** Una celda de historia de tamaño fijo: círculo arriba, nombre debajo. */
    private JComponent historia(String username, String foto, String etiqueta, boolean propia) {
        ImageIcon icono = propia
                ? EstiloInsta.avatarMasBadge(foto, username, CIRCULO)
                : EstiloInsta.avatar(foto, username, CIRCULO);
        JButton circulo = new JButton(icono);
        circulo.setContentAreaFilled(false);
        circulo.setBorderPainted(false);
        circulo.setFocusPainted(false);
        circulo.setMargin(new Insets(0, 0, 0, 0));
        // Sin borde: si no, el borde del Look&Feel deja unos pixeles de margen
        // dentro del boton y la foto (56x56) no queda centrada respecto al
        // nombre de abajo. Fijamos el tamano al de la imagen.
        circulo.setBorder(BorderFactory.createEmptyBorder());
        circulo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        circulo.setAlignmentX(CENTER_ALIGNMENT);
        circulo.setPreferredSize(new Dimension(CIRCULO, CIRCULO));
        circulo.setMinimumSize(new Dimension(CIRCULO, CIRCULO));
        circulo.setMaximumSize(new Dimension(CIRCULO, CIRCULO));
        circulo.addActionListener(e -> panelInsta.verPerfilDe(username));

        JLabel nombre = new JLabel(recortar(etiqueta), SwingConstants.CENTER);
        nombre.setFont(EstiloInsta.CHICA);
        nombre.setForeground(propia ? EstiloInsta.TEXTO_GRIS : EstiloInsta.TEXTO);
        nombre.setAlignmentX(CENTER_ALIGNMENT);
        nombre.setMaximumSize(new Dimension(CELDA_ANCHO, 16));

        JPanel celda = new JPanel();
        celda.setOpaque(false);
        celda.setLayout(new BoxLayout(celda, BoxLayout.Y_AXIS));
        celda.setPreferredSize(new Dimension(CELDA_ANCHO, CELDA_ALTO));
        celda.setMinimumSize(new Dimension(CELDA_ANCHO, CELDA_ALTO));
        celda.setMaximumSize(new Dimension(CELDA_ANCHO, CELDA_ALTO));
        celda.add(circulo);
        celda.add(Box.createVerticalStrut(4));
        celda.add(nombre);
        return celda;
    }

    /** Nombres muy largos se acortan con "…" para que no descuadren la fila. */
    private static String recortar(String s) {
        if ("Tu historia".equals(s) || s.length() <= 10) {
            return s;
        }
        return s.substring(0, 9) + "…";
    }
}
