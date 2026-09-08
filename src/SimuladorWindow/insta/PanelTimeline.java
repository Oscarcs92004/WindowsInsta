package SimuladorWindow.insta;

import SimuladorWindow.estructuras.ListaEnlazada;
import SimuladorWindow.modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Comentarios / Timeline (enunciado 4.3 y 4.7).
 *
 * Arriba: caja para publicar un insta de texto (maximo 140 caracteres).
 * Abajo: mis publicaciones y las de quienes sigo, de la mas nueva a la mas
 * vieja, armadas en una ListaEnlazada propia.
 */
public class PanelTimeline extends JPanel {

    private static final int MAX_TEXTO = 140;

    private final InstaServicio insta;
    private final Usuario usuarioActual;

    private final JTextArea cajaNueva = new JTextArea(3, 30);
    private final JTextArea timeline = new JTextArea();

    public PanelTimeline(InstaServicio insta, Usuario usuarioActual) {
        this.insta = insta;
        this.usuarioActual = usuarioActual;

        setLayout(new BorderLayout());

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.setBorder(BorderFactory.createTitledBorder(
                "Nueva publicacion (max " + MAX_TEXTO + " caracteres)"));
        arriba.add(new JScrollPane(cajaNueva), BorderLayout.CENTER);
        JButton btnPublicar = new JButton("Publicar");
        btnPublicar.addActionListener(e -> publicar());
        arriba.add(btnPublicar, BorderLayout.EAST);
        add(arriba, BorderLayout.NORTH);

        timeline.setEditable(false);
        add(new JScrollPane(timeline), BorderLayout.CENTER);

        recargar();
    }

    private void publicar() {
        String texto = cajaNueva.getText().trim();
        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribe algo para publicar.");
            return;
        }
        if (texto.length() > MAX_TEXTO) {
            JOptionPane.showMessageDialog(this,
                    "El texto no puede pasar de " + MAX_TEXTO + " caracteres.");
            return;
        }
        insta.publicar(usuarioActual.getUsername(),
                new Publicacion(usuarioActual.getUsername(), texto, null));
        cajaNueva.setText("");
        recargar();
    }

    public void recargar() {
        timeline.setText(construirTimeline());
        timeline.setCaretPosition(0);
    }

    private String construirTimeline() {
        // 1. Autores: yo + a quienes sigo.
        List<String> autores = new ArrayList<>();
        autores.add(usuarioActual.getUsername());
        autores.addAll(insta.aQuienesSigue(usuarioActual.getUsername()));

        // 2. Junto las publicaciones en un ArrayList para poder ordenarlas.
        List<Publicacion> todas = new ArrayList<>();
        for (String autor : autores) {
            if (!insta.estaVisible(autor)) {
                continue;                       // cuenta desactivada: como si no existiera
            }
            todas.addAll(insta.publicacionesDe(autor));
        }
        todas.sort(Comparator.comparing(Publicacion::getFecha).reversed());

        // 3. Las paso a la ListaEnlazada propia (enunciado 2.4) y las pinto.
        ListaEnlazada<Publicacion> lista = new ListaEnlazada<>();
        for (Publicacion p : todas) {
            lista.agregarFinal(p);
        }

        StringBuilder sb = new StringBuilder();
        for (Publicacion p : lista.comoLista()) {
            sb.append(TextoInsta.formato(p)).append("\n\n");
        }
        if (sb.length() == 0) {
            return "Todavia no hay publicaciones. Publica algo o sigue a alguien.";
        }
        return sb.toString();
    }
}
