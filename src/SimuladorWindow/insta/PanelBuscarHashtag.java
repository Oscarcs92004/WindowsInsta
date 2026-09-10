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
 * Buscar Hashtag (enunciado 4.10).
 *
 * Se escribe una palabra y se muestran todas las publicaciones que llevan ese
 * hashtag, con el mismo aspecto que el feed (una TarjetaPublicacion por
 * publicación). El orden no importa, pero no puede haber publicaciones
 * repetidas: se controla con una ListaEnlazada.
 */
public class PanelBuscarHashtag extends JPanel {

    private static final int ANCHO_FEED = 380;

    private final InstaServicio insta;
    private final UsuarioServicio usuarios;
    private final PanelInsta panelInsta;

    private final JTextField txtHashtag = new JTextField(20);
    private final PanelFeed resultados = new PanelFeed();

    public PanelBuscarHashtag(InstaServicio insta, UsuarioServicio usuarios, PanelInsta panelInsta) {
        this.insta = insta;
        this.usuarios = usuarios;
        this.panelInsta = panelInsta;

        setBackground(EstiloInsta.FONDO);
        setLayout(new BorderLayout());

        EstiloInsta.estiloCampo(txtHashtag);
        txtHashtag.setToolTipText("Escribe el hashtag sin el #");
        JButton btnBuscar = EstiloInsta.botonPrimario("Buscar");
        btnBuscar.addActionListener(e -> buscar());
        txtHashtag.addActionListener(e -> buscar());
        JPanel arriba = new JPanel(new BorderLayout(6, 0));
        arriba.setOpaque(true);
        arriba.setBackground(EstiloInsta.BLANCO);
        arriba.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloInsta.BORDE),
                EstiloInsta.margen(10, 12, 10, 12)));
        arriba.add(txtHashtag, BorderLayout.CENTER);
        arriba.add(btnBuscar, BorderLayout.EAST);
        add(arriba, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(resultados,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(EstiloInsta.FONDO);
        EstiloInsta.scrollFino(scroll);
        add(scroll, BorderLayout.CENTER);

        mensaje("Escribe un hashtag para ver las publicaciones que lo usan.");
    }

    private void buscar() {
        String palabra = txtHashtag.getText().trim();
        if (palabra.startsWith("#")) {
            palabra = palabra.substring(1);
        }
        if (palabra.isEmpty()) {
            mensaje("Escribe una palabra.");
            return;
        }

        ListaEnlazada<Publicacion> yaPuestas = new ListaEnlazada<>();
        for (String autor : insta.todosLosUsuarios()) {
            if (!insta.estaVisible(autor)) {
                continue;
            }
            for (Publicacion p : insta.publicacionesDe(autor)) {
                if (TextoInsta.tieneHashtag(p.getTexto(), palabra) && !yaPuestas.contiene(p)) {
                    yaPuestas.agregarFinal(p);
                }
            }
        }

        List<Publicacion> lista = new ArrayList<>(yaPuestas.comoLista());
        lista.sort(Comparator.comparing(Publicacion::getFecha).reversed());

        resultados.removeAll();
        if (lista.isEmpty()) {
            mensaje("No hay publicaciones con #" + palabra);
        } else {
            for (Publicacion p : lista) {
                resultados.add(new TarjetaPublicacion(p, usuarios, ANCHO_FEED,
                        panelInsta::verPerfilDe));
            }
        }
        resultados.revalidate();
        resultados.repaint();
    }

    private void mensaje(String texto) {
        resultados.removeAll();
        JLabel l = new JLabel(texto);
        l.setForeground(EstiloInsta.TEXTO_GRIS);
        l.setBorder(EstiloInsta.margen(24, 12, 24, 12));
        resultados.add(l);
        resultados.revalidate();
        resultados.repaint();
    }
}
