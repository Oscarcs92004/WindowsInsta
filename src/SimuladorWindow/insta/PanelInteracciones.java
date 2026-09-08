package SimuladorWindow.insta;

import SimuladorWindow.estructuras.ListaEnlazada;
import SimuladorWindow.modelo.Usuario;

import javax.swing.*;
import java.awt.*;

/**
 * Interacciones (enunciado 4.8).
 *
 * Muestra las publicaciones de OTROS usuarios donde me mencionan con
 * @mi_username, sin repetir ninguna. El resultado se arma en una
 * ListaEnlazada propia.
 */
public class PanelInteracciones extends JPanel {

    private final InstaServicio insta;
    private final Usuario usuarioActual;
    private final JTextArea area = new JTextArea();

    public PanelInteracciones(InstaServicio insta, Usuario usuarioActual) {
        this.insta = insta;
        this.usuarioActual = usuarioActual;

        setBackground(EstiloInsta.FONDO);
        setLayout(new BorderLayout());
        setBorder(EstiloInsta.margen(12, 12, 12, 12));

        JLabel titulo = new JLabel("Publicaciones que te mencionan");
        titulo.setFont(EstiloInsta.FUERTE);
        titulo.setBorder(EstiloInsta.margen(0, 0, 8, 0));
        add(titulo, BorderLayout.NORTH);

        area.setEditable(false);
        area.setFont(EstiloInsta.NORMAL);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(EstiloInsta.BORDE));
        add(scroll, BorderLayout.CENTER);
        recargar();
    }

    public void recargar() {
        String yo = usuarioActual.getUsername();

        ListaEnlazada<String> yaPuestas = new ListaEnlazada<>();
        StringBuilder sb = new StringBuilder();

        for (String autor : insta.todosLosUsuarios()) {
            if (autor.equalsIgnoreCase(yo)) {
                continue;                       // no cuentan mis propias menciones
            }
            if (!insta.estaVisible(autor)) {
                continue;
            }
            for (Publicacion p : insta.publicacionesDe(autor)) {
                if (!TextoInsta.menciona(p.getTexto(), yo)) {
                    continue;
                }
                String texto = TextoInsta.formato(p);
                if (!yaPuestas.contiene(texto)) {   // sin duplicados
                    yaPuestas.agregarFinal(texto);
                    sb.append(texto).append("\n\n");
                }
            }
        }

        area.setText(sb.length() == 0
                ? "Nadie te ha mencionado todavia."
                : sb.toString());
        area.setCaretPosition(0);
    }
}
