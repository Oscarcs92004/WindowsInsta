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
 * Interacciones (enunciado 4.8).
 *
 * Muestra las publicaciones de OTROS usuarios donde me mencionan con
 * &#64;mi_username, sin repetir ninguna. Se ven con el mismo aspecto que el
 * feed (una TarjetaPublicacion por publicación). El resultado se arma en una
 * ListaEnlazada propia.
 */
public class PanelInteracciones extends JPanel {

    private static final int ANCHO_FEED = 380;

    private final InstaServicio insta;
    private final UsuarioServicio usuarios;
    private final Usuario usuarioActual;
    private final PanelInsta panelInsta;

    private final PanelFeed lista = new PanelFeed();

    public PanelInteracciones(InstaServicio insta, UsuarioServicio usuarios,
                              Usuario usuarioActual, PanelInsta panelInsta) {
        this.insta = insta;
        this.usuarios = usuarios;
        this.usuarioActual = usuarioActual;
        this.panelInsta = panelInsta;

        setBackground(EstiloInsta.FONDO);
        setLayout(new BorderLayout());

        JLabel titulo = new JLabel("Publicaciones que te mencionan");
        titulo.setFont(EstiloInsta.FUERTE);
        titulo.setOpaque(true);
        titulo.setBackground(EstiloInsta.BLANCO);
        titulo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloInsta.BORDE),
                EstiloInsta.margen(10, 12, 10, 12)));
        add(titulo, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(lista,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(EstiloInsta.FONDO);
        EstiloInsta.scrollFino(scroll);
        add(scroll, BorderLayout.CENTER);

        recargar();
    }

    public void recargar() {
        lista.removeAll();
        String yo = usuarioActual.getUsername();

        ListaEnlazada<Publicacion> encontradas = new ListaEnlazada<>();
        for (String autor : insta.todosLosUsuarios()) {
            if (autor.equalsIgnoreCase(yo) || !insta.estaVisible(autor)) {
                continue;                       // no cuentan mis propias menciones
            }
            for (Publicacion p : insta.publicacionesDe(autor)) {
                if (TextoInsta.menciona(p.getTexto(), yo) && !encontradas.contiene(p)) {
                    encontradas.agregarFinal(p);
                }
            }
        }

        List<Publicacion> ordenadas = new ArrayList<>(encontradas.comoLista());
        ordenadas.sort(Comparator.comparing(Publicacion::getFecha).reversed());

        if (ordenadas.isEmpty()) {
            JLabel vacio = new JLabel("Nadie te ha mencionado todavía.");
            vacio.setForeground(EstiloInsta.TEXTO_GRIS);
            vacio.setBorder(EstiloInsta.margen(24, 12, 24, 12));
            lista.add(vacio);
        } else {
            for (Publicacion p : ordenadas) {
                lista.add(new TarjetaPublicacion(p, usuarios, ANCHO_FEED,
                        panelInsta::verPerfilDe));
            }
        }
        lista.add(Box.createVerticalGlue());
        lista.revalidate();
        lista.repaint();
    }
}
