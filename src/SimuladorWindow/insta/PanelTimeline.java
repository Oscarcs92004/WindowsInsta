package SimuladorWindow.insta;

import SimuladorWindow.estructuras.ListaEnlazada;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Feed / Comentarios / Timeline (enunciado 4.3 y 4.7), con el aspecto de
 * Instagram: una columna centrada de "publicaciones" (avatar + usuario arriba,
 * imagen, y el pie con el texto).
 *
 * Arriba hay una tarjeta para publicar un insta de texto (maximo 140).
 * El feed son mis publicaciones y las de quienes sigo, de la mas nueva a la
 * mas vieja, armadas en una ListaEnlazada propia.
 */
public class PanelTimeline extends JPanel {

    private static final int MAX_TEXTO = 140;
    private static final int ANCHO_FEED = 480;

    private final InstaServicio insta;
    private final UsuarioServicio usuarios;
    private final Usuario usuarioActual;

    private final JTextArea cajaNueva = new JTextArea(2, 24);
    private final JPanel feed = new JPanel();

    public PanelTimeline(InstaServicio insta, UsuarioServicio usuarios, Usuario usuarioActual) {
        this.insta = insta;
        this.usuarios = usuarios;
        this.usuarioActual = usuarioActual;

        setBackground(EstiloInsta.FONDO);
        setLayout(new GridBagLayout());

        feed.setLayout(new BoxLayout(feed, BoxLayout.Y_AXIS));
        feed.setBackground(EstiloInsta.FONDO);
        feed.setBorder(EstiloInsta.margen(16, 0, 16, 0));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.PAGE_START;   // arriba y centrado
        c.weightx = 1;
        c.weighty = 1;
        add(feed, c);

        recargar();
    }

    private JComponent cajaPublicar() {
        JPanel caja = EstiloInsta.tarjeta();
        caja.setLayout(new BorderLayout(8, 8));
        caja.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloInsta.BORDE, 1, true),
                EstiloInsta.margen(12, 12, 12, 12)));
        caja.setAlignmentX(CENTER_ALIGNMENT);
        caja.setMaximumSize(new Dimension(ANCHO_FEED, 130));

        JLabel titulo = new JLabel("Crear publicacion  (max " + MAX_TEXTO + ")");
        titulo.setFont(EstiloInsta.FUERTE);
        titulo.setForeground(EstiloInsta.TEXTO);
        caja.add(titulo, BorderLayout.NORTH);

        cajaNueva.setLineWrap(true);
        cajaNueva.setWrapStyleWord(true);
        EstiloInsta.estiloCampo(cajaNueva);
        caja.add(new JScrollPane(cajaNueva), BorderLayout.CENTER);

        JButton btnPublicar = EstiloInsta.botonPrimario("Publicar");
        btnPublicar.addActionListener(e -> publicar());
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.setOpaque(false);
        derecha.add(btnPublicar);
        caja.add(derecha, BorderLayout.SOUTH);
        return caja;
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
        feed.removeAll();
        feed.add(cajaPublicar());
        feed.add(Box.createVerticalStrut(16));

        boolean hay = false;
        for (Publicacion p : construirTimeline().comoLista()) {
            feed.add(tarjetaPublicacion(p));
            feed.add(Box.createVerticalStrut(16));
            hay = true;
        }
        if (!hay) {
            JLabel vacio = new JLabel("Todavia no hay publicaciones. Publica algo o sigue a alguien.");
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
        JPanel card = EstiloInsta.tarjeta();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(ANCHO_FEED, Integer.MAX_VALUE));

        card.add(cabeceraPublicacion(p));

        if (p.tieneImagen()) {
            ImageIcon icono = ConfigInsta.escalarParaFeed(new File(p.getRutaImagen()));
            JLabel imagen = new JLabel(icono != null ? icono : new ImageIcon(),
                    SwingConstants.CENTER);
            if (icono == null) {
                imagen.setText("[imagen no disponible]");
            }
            imagen.setAlignmentX(LEFT_ALIGNMENT);
            card.add(imagen);
        }

        card.add(pie(p));
        return card;
    }

    private JComponent cabeceraPublicacion(Publicacion p) {
        Usuario autor = usuarios.buscar(p.getAutor());
        String foto = (autor == null) ? null : autor.getFotoPerfil();

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        fila.setOpaque(false);
        fila.setAlignmentX(LEFT_ALIGNMENT);

        fila.add(new JLabel(EstiloInsta.avatar(foto, p.getAutor(), 30)));

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

    private JComponent pie(Publicacion p) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(EstiloInsta.margen(6, 10, 12, 10));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JTextArea texto = new JTextArea();
        texto.setEditable(false);
        texto.setOpaque(false);
        texto.setLineWrap(true);
        texto.setWrapStyleWord(true);
        texto.setFont(EstiloInsta.NORMAL);
        texto.setForeground(EstiloInsta.TEXTO);
        texto.setText(p.getAutor() + "  " + p.getTexto());
        panel.add(texto, BorderLayout.CENTER);
        return panel;
    }
}
