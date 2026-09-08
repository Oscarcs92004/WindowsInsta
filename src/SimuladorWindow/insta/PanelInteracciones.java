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

        setLayout(new BorderLayout());
        area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);
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
