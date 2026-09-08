package SimuladorWindow.insta;

import SimuladorWindow.estructuras.ListaEnlazada;
import SimuladorWindow.modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Comentarios / Timeline (enunciado 4.3 y 4.7).
 *
 * Arriba: caja para publicar un insta de texto (maximo 140 caracteres).
 * Abajo: mis publicaciones y las de quienes sigo, de la mas nueva a la mas
 * vieja, armadas en una ListaEnlazada propia. Las imagenes se muestran
 * escaladas segun su orientacion (ConfigInsta, diseño responsive 4.6).
 */
public class PanelTimeline extends JPanel {

    private static final int MAX_TEXTO = 140;

    private final InstaServicio insta;
    private final Usuario usuarioActual;

    private final JTextArea cajaNueva = new JTextArea(3, 30);
    private final JPanel lista = new JPanel();

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

        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setBackground(Color.WHITE);
        add(new JScrollPane(lista), BorderLayout.CENTER);

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
        lista.removeAll();

        for (Publicacion p : construirTimeline().comoLista()) {
            lista.add(filaPublicacion(p));
            lista.add(new JSeparator());
        }
        if (lista.getComponentCount() == 0) {
            lista.add(new JLabel("Todavia no hay publicaciones. Publica algo o sigue a alguien."));
        }
        lista.revalidate();
        lista.repaint();
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

    private JComponent filaPublicacion(Publicacion p) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JTextArea texto = new JTextArea(TextoInsta.formato(p));
        texto.setEditable(false);
        texto.setOpaque(false);
        fila.add(texto, BorderLayout.NORTH);

        if (p.tieneImagen()) {
            ImageIcon icono = ConfigInsta.escalarParaVista(new File(p.getRutaImagen()));
            JLabel imagen = new JLabel();
            if (icono != null) {
                imagen.setIcon(icono);
            } else {
                imagen.setText("[imagen no disponible]");
            }
            fila.add(imagen, BorderLayout.CENTER);
        }
        return fila;
    }
}
