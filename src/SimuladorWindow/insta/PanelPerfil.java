package SimuladorWindow.insta;

import SimuladorWindow.estructuras.ListaEnlazada;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Perfil de un usuario (enunciado 4.5 y 4.9b), con el aspecto del perfil de
 * Instagram: foto grande a la izquierda, a la derecha el username con el boton
 * de accion, la fila de contadores (publicaciones / followers / following) y el
 * nombre; abajo el grid de 3 columnas con las imagenes publicadas.
 */
public class PanelPerfil extends JPanel {

    private final InstaServicio insta;
    private final UsuarioServicio usuarios;
    private final Usuario usuarioActual;
    private final PanelInsta panelInsta;

    public PanelPerfil(InstaServicio insta, UsuarioServicio usuarios,
                       Usuario usuarioActual, PanelInsta panelInsta) {
        this.insta = insta;
        this.usuarios = usuarios;
        this.usuarioActual = usuarioActual;
        this.panelInsta = panelInsta;
        setBackground(EstiloInsta.FONDO);
        setLayout(new BorderLayout());
        mostrarMio();
    }

    public void mostrarMio() {
        mostrarPerfilDe(usuarioActual.getUsername());
    }

    public void mostrarPerfilDe(String username) {
        removeAll();

        Usuario u = usuarios.buscar(username);
        if (u == null) {
            JLabel aviso = new JLabel("No existe el usuario: " + username);
            aviso.setBorder(EstiloInsta.margen(20, 20, 20, 20));
            add(aviso, BorderLayout.NORTH);
            revalidate();
            repaint();
            return;
        }

        JPanel centro = new JPanel();
        centro.setOpaque(false);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setBorder(EstiloInsta.margen(14, 14, 14, 14));

        for (JComponent parte : cabecera(u)) {
            parte.setAlignmentX(LEFT_ALIGNMENT);
            centro.add(parte);
            centro.add(Box.createVerticalStrut(8));
        }
        JSeparator sep = new JSeparator();
        sep.setAlignmentX(LEFT_ALIGNMENT);
        centro.add(sep);
        centro.add(Box.createVerticalStrut(10));
        JComponent grid = grid(u);
        grid.setAlignmentX(LEFT_ALIGNMENT);
        centro.add(grid);

        add(centro, BorderLayout.NORTH);
        revalidate();
        repaint();
    }

    // -----------------------------------------------------------------

    /** Las piezas de la cabecera del perfil, apiladas (formato telefono). */
    private java.util.List<JComponent> cabecera(Usuario u) {
        boolean esOtro = !u.getUsername().equalsIgnoreCase(usuarioActual.getUsername());
        java.util.List<JComponent> partes = new java.util.ArrayList<>();

        // Fila 1: avatar + los 3 contadores al lado.
        int publicaciones = insta.contarPublicaciones(u.getUsername());
        int followers = insta.seguidoresDe(u.getUsername()).size();
        int following = insta.aQuienesSigue(u.getUsername()).size();

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        fila1.setOpaque(false);
        fila1.add(new JLabel(EstiloInsta.avatar(u.getFotoPerfil(), u.getUsername(), 78)));
        JPanel stats = new JPanel(new GridLayout(1, 3, 6, 0));
        stats.setOpaque(false);
        stats.add(contador(publicaciones, "posts", null));
        stats.add(contador(followers, "followers",
                () -> mostrarLista("Followers de " + u.getUsername(),
                        insta.listaFollowers(u.getUsername()))));
        stats.add(contador(following, "following",
                () -> mostrarLista("Following de " + u.getUsername(),
                        insta.listaFollowing(u.getUsername()))));
        fila1.add(stats);
        partes.add(fila1);

        // Nombre y username.
        JLabel username = new JLabel(u.getUsername());
        username.setFont(EstiloInsta.FUERTE);
        username.setForeground(EstiloInsta.TEXTO);
        partes.add(username);

        JLabel nombre = new JLabel(u.getNombreCompleto());
        nombre.setFont(EstiloInsta.NORMAL);
        nombre.setForeground(EstiloInsta.TEXTO);
        partes.add(nombre);

        JLabel extra = new JLabel(u.getGenero() + " · " + u.getEdad() + " años · desde "
                + u.getFechaRegistro() + (u.isActiva() ? "" : "  · CUENTA INACTIVA"));
        extra.setFont(EstiloInsta.CHICA);
        extra.setForeground(u.isActiva() ? EstiloInsta.TEXTO_GRIS : EstiloInsta.ROJO);
        partes.add(extra);

        // Botones de accion, a lo ancho.
        partes.add(botonAccion(u, esOtro));
        return partes;
    }

    private JComponent botonAccion(Usuario u, boolean esOtro) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila.setOpaque(false);

        if (!esOtro) {
            JButton editar = EstiloInsta.botonSecundario("Editar perfil");
            editar.addActionListener(e -> panelInsta.irAEditarPerfil());
            JButton salir = EstiloInsta.enlace("Cerrar sesion");
            salir.setForeground(EstiloInsta.ROJO);
            salir.addActionListener(e -> panelInsta.cerrarSesion());
            fila.add(editar);
            fila.add(salir);
            return fila;
        }

        boolean sigo = insta.sigo(usuarioActual.getUsername(), u.getUsername());
        JButton seguir = sigo
                ? EstiloInsta.botonSecundario("Dejar de seguir")
                : EstiloInsta.botonPrimario("Seguir");
        seguir.addActionListener(e -> {
            if (sigo) {
                int op = JOptionPane.showConfirmDialog(this,
                        "Dejar de seguir a " + u.getUsername() + "?",
                        "Confirmar", JOptionPane.YES_NO_OPTION);
                if (op != JOptionPane.YES_OPTION) {
                    return;
                }
                insta.dejarDeSeguir(usuarioActual.getUsername(), u.getUsername());
            } else {
                insta.seguir(usuarioActual.getUsername(), u.getUsername());
            }
            mostrarPerfilDe(u.getUsername());
        });

        JButton ver = EstiloInsta.botonSecundario("Ver publicaciones");
        ver.addActionListener(e -> verPublicaciones(u.getUsername()));

        fila.add(seguir);
        fila.add(ver);
        return fila;
    }

    /** Un contador del perfil: numero en negrita y la etiqueta debajo. */
    private JComponent contador(int numero, String etiqueta, Runnable accion) {
        JLabel n = new JLabel(String.valueOf(numero), SwingConstants.CENTER);
        n.setFont(EstiloInsta.FUERTE);
        n.setForeground(EstiloInsta.TEXTO);
        JLabel t = new JLabel(etiqueta, SwingConstants.CENTER);
        t.setFont(EstiloInsta.CHICA);
        t.setForeground(EstiloInsta.TEXTO_GRIS);

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(n, BorderLayout.CENTER);
        p.add(t, BorderLayout.SOUTH);
        if (accion != null) {
            p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            p.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    accion.run();
                }
            });
        }
        return p;
    }

    /** Grid de 3 columnas con las fotos y los reels publicados (enunciado 4.6). */
    private JComponent grid(Usuario u) {
        JPanel grid = new JPanel(new GridLayout(0, ConfigInsta.COLUMNAS_GRID, 3, 3));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(360, Integer.MAX_VALUE));

        int puestas = 0;
        for (Publicacion p : insta.publicacionesDe(u.getUsername())) {
            if (p.esSoloTexto()) {
                continue;                        // los instas de texto no van al grid
            }
            grid.add(celdaGrid(p));
            puestas++;
        }
        if (puestas == 0) {
            JLabel vacio = new JLabel("Sin fotos ni reels publicados todavia.");
            vacio.setForeground(EstiloInsta.TEXTO_GRIS);
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT));
            wrap.setOpaque(false);
            wrap.add(vacio);
            return wrap;
        }
        return grid;
    }

    private JComponent celdaGrid(Publicacion p) {
        if (p.esVideo()) {
            JLabel celda = new JLabel("<html><center>REEL<br>&#9654;</center></html>",
                    SwingConstants.CENTER);
            celda.setOpaque(true);
            celda.setBackground(new Color(30, 30, 30));
            celda.setForeground(Color.WHITE);
            celda.setPreferredSize(new Dimension(112, 112));
            celda.setToolTipText(p.getTexto());
            return celda;
        }
        ImageIcon mini = ConfigInsta.miniatura(new File(p.getRutaImagen()), 112);
        JLabel celda = new JLabel();
        if (mini != null) {
            celda.setIcon(mini);
        } else {
            celda.setText("(imagen)");
        }
        celda.setToolTipText(p.getTexto());
        return celda;
    }

    // -----------------------------------------------------------------

    private void mostrarLista(String titulo, ListaEnlazada<String> lista) {
        StringBuilder sb = new StringBuilder();
        for (String username : lista.comoLista()) {
            sb.append(username).append("\n");
        }
        if (sb.length() == 0) {
            sb.append("(ninguno)");
        }
        JTextArea area = new JTextArea(sb.toString(), 12, 20);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), titulo,
                JOptionPane.PLAIN_MESSAGE);
    }

    /** Timeline de ese usuario, de la mas reciente a la mas antigua. */
    private void verPublicaciones(String username) {
        ListaEnlazada<Publicacion> lista = new ListaEnlazada<>();
        for (Publicacion p : insta.publicacionesDe(username)) {
            lista.agregarInicio(p);          // el ultimo queda primero
        }

        StringBuilder sb = new StringBuilder();
        for (Publicacion p : lista.comoLista()) {
            sb.append(TextoInsta.formato(p)).append("\n\n");
        }
        if (sb.length() == 0) {
            sb.append("Este usuario no tiene publicaciones.");
        }

        JTextArea area = new JTextArea(sb.toString(), 20, 40);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "Publicaciones de " + username, JOptionPane.PLAIN_MESSAGE);
    }
}
