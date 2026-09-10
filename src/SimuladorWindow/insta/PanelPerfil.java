package SimuladorWindow.insta;

import SimuladorWindow.estructuras.ListaEnlazada;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.format.DateTimeFormatter;

/**
 * Perfil de un usuario (enunciado 4.5 y 4.9b), con el aspecto del perfil de
 * Instagram: arriba el avatar y los tres contadores (publicaciones, followers,
 * following); debajo el nombre y los datos; luego el botón de acción; y al final
 * el grid de 3 columnas con las fotos y los reels publicados.
 */
public class PanelPerfil extends JPanel {

    private static final int LADO_CELDA = 129;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final InstaServicio insta;
    private final UsuarioServicio usuarios;
    private final Usuario usuarioActual;
    private final PanelInsta panelInsta;

    private final PanelFeed cuerpo = new PanelFeed();

    public PanelPerfil(InstaServicio insta, UsuarioServicio usuarios,
                       Usuario usuarioActual, PanelInsta panelInsta) {
        this.insta = insta;
        this.usuarios = usuarios;
        this.usuarioActual = usuarioActual;
        this.panelInsta = panelInsta;

        setBackground(EstiloInsta.BLANCO);
        setLayout(new BorderLayout());

        cuerpo.setBackground(EstiloInsta.BLANCO);
        JScrollPane scroll = new JScrollPane(cuerpo,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(EstiloInsta.BLANCO);
        EstiloInsta.scrollFino(scroll);
        add(scroll, BorderLayout.CENTER);

        mostrarMio();
    }

    public void mostrarMio() {
        mostrarPerfilDe(usuarioActual.getUsername());
    }

    public void mostrarPerfilDe(String username) {
        cuerpo.removeAll();

        Usuario u = usuarios.buscar(username);
        if (u == null) {
            JLabel aviso = new JLabel("No existe el usuario: " + username);
            aviso.setBorder(EstiloInsta.margen(20, 16, 20, 16));
            cuerpo.add(aviso);
            cuerpo.revalidate();
            cuerpo.repaint();
            return;
        }

        boolean esOtro = !u.getUsername().equalsIgnoreCase(usuarioActual.getUsername());

        cuerpo.add(bloqueCabecera(u));
        cuerpo.add(fila(botonesAccion(u, esOtro), 12, 14, 12, 14));
        cuerpo.add(barraPestana());
        cuerpo.add(grid(u));
        cuerpo.add(Box.createVerticalGlue());

        cuerpo.revalidate();
        cuerpo.repaint();
    }

    // -----------------------------------------------------------------
    //  Cabecera: avatar + contadores + nombre + datos
    // -----------------------------------------------------------------

    private JComponent bloqueCabecera(Usuario u) {
        int publicaciones = insta.contarPublicaciones(u.getUsername());
        int followers = insta.seguidoresDe(u.getUsername()).size();
        int following = insta.aQuienesSigue(u.getUsername()).size();

        JPanel fila1 = new JPanel(new BorderLayout(14, 0));
        fila1.setOpaque(false);
        fila1.add(new JLabel(EstiloInsta.avatarAnillo(u.getFotoPerfil(), u.getUsername(), 82)),
                BorderLayout.WEST);

        JPanel stats = new JPanel(new GridLayout(1, 3));
        stats.setOpaque(false);
        stats.add(contador(publicaciones, "publicaciones", null));
        stats.add(contador(followers, "followers",
                () -> mostrarLista("Followers", insta.listaFollowers(u.getUsername()))));
        stats.add(contador(following, "following",
                () -> mostrarLista("Following", insta.listaFollowing(u.getUsername()))));
        fila1.add(stats, BorderLayout.CENTER);

        JPanel datos = new JPanel();
        datos.setOpaque(false);
        datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));

        JLabel username = new JLabel(u.getUsername());
        username.setFont(EstiloInsta.FUERTE);
        username.setForeground(EstiloInsta.TEXTO);
        username.setAlignmentX(LEFT_ALIGNMENT);
        datos.add(username);

        JLabel nombre = new JLabel(u.getNombreCompleto());
        nombre.setFont(EstiloInsta.NORMAL);
        nombre.setForeground(EstiloInsta.TEXTO);
        nombre.setAlignmentX(LEFT_ALIGNMENT);
        datos.add(nombre);

        String genero = (u.getGenero() == 'F') ? "Mujer" : (u.getGenero() == 'M') ? "Hombre"
                : String.valueOf(u.getGenero());
        JLabel bio = new JLabel(genero + " · " + u.getEdad() + " años · Se unió el "
                + u.getFechaRegistro().format(FECHA));
        bio.setFont(EstiloInsta.CHICA);
        bio.setForeground(EstiloInsta.TEXTO_GRIS);
        bio.setAlignmentX(LEFT_ALIGNMENT);
        datos.add(bio);

        // Estado de la cuenta (enunciado 4.5): siempre visible.
        JLabel estado = new JLabel(u.isActiva() ? "Cuenta activa" : "Cuenta desactivada");
        estado.setFont(EstiloInsta.CHICA);
        estado.setForeground(u.isActiva() ? EstiloInsta.TEXTO_GRIS : EstiloInsta.ROJO);
        estado.setAlignmentX(LEFT_ALIGNMENT);
        datos.add(estado);

        JPanel bloque = new JPanel();
        bloque.setOpaque(false);
        bloque.setLayout(new BoxLayout(bloque, BoxLayout.Y_AXIS));
        bloque.setBorder(EstiloInsta.margen(14, 14, 6, 14));
        fila1.setAlignmentX(LEFT_ALIGNMENT);
        datos.setAlignmentX(LEFT_ALIGNMENT);
        bloque.add(fila1);
        bloque.add(Box.createVerticalStrut(10));
        bloque.add(datos);
        return capar(bloque);
    }

    /** Un contador del perfil: número en negrita y la etiqueta debajo. */
    private JComponent contador(int numero, String etiqueta, Runnable accion) {
        JLabel n = new JLabel(String.valueOf(numero), SwingConstants.CENTER);
        n.setFont(EstiloInsta.FUERTE.deriveFont(16f));
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

    // -----------------------------------------------------------------
    //  Botones de acción
    // -----------------------------------------------------------------

    private JComponent botonesAccion(Usuario u, boolean esOtro) {
        JPanel fila = new JPanel(new GridBagLayout());
        fila.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 6);
        c.gridy = 0;

        if (!esOtro) {
            JButton editar = EstiloInsta.botonSecundario("Editar perfil");
            editar.addActionListener(e -> panelInsta.irAEditarPerfil());
            c.gridx = 0; c.weightx = 1; fila.add(editar, c);

            JButton opciones = EstiloInsta.botonSecundario("");
            opciones.setIcon(IconosInsta.icono(IconosInsta.OPCIONES, 18, true));
            opciones.addActionListener(e -> {
                JPopupMenu menu = new JPopupMenu();
                JMenuItem salir = new JMenuItem("Cerrar sesión");
                salir.addActionListener(x -> panelInsta.cerrarSesion());
                menu.add(salir);
                menu.show(opciones, 0, opciones.getHeight());
            });
            c.gridx = 1; c.weightx = 0; c.insets = new Insets(0, 0, 0, 0); fila.add(opciones, c);
            return fila;
        }

        boolean sigo = insta.sigo(usuarioActual.getUsername(), u.getUsername());
        JButton seguir = sigo
                ? EstiloInsta.botonSecundario("Siguiendo")
                : EstiloInsta.botonPrimario("Seguir");
        seguir.addActionListener(e -> {
            if (sigo) {
                int op = JOptionPane.showConfirmDialog(this,
                        "¿Dejar de seguir a " + u.getUsername() + "?",
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

        c.gridx = 0; c.weightx = 1; fila.add(seguir, c);
        c.gridx = 1; c.weightx = 1; c.insets = new Insets(0, 0, 0, 0); fila.add(ver, c);
        return fila;
    }

    /** La barra con el icono de cuadrícula (una sola pestaña, decorativa). */
    private JComponent barraPestana() {
        JLabel grid = new JLabel(IconosInsta.icono("grid", 22, true), SwingConstants.CENTER);
        grid.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 2, 0, EstiloInsta.TEXTO),
                EstiloInsta.margen(6, 0, 6, 0)));
        grid.setOpaque(true);
        grid.setBackground(EstiloInsta.BLANCO);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(EstiloInsta.BLANCO);
        wrap.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloInsta.BORDE));
        wrap.add(grid, BorderLayout.CENTER);
        return capar(wrap);
    }

    // -----------------------------------------------------------------
    //  Grid de publicaciones (enunciado 4.6: 3 columnas)
    // -----------------------------------------------------------------

    private JComponent grid(Usuario u) {
        JPanel grid = new JPanel(new GridLayout(0, ConfigInsta.COLUMNAS_GRID, 2, 2));
        grid.setOpaque(false);
        grid.setBorder(EstiloInsta.margen(2, 0, 2, 0));

        int puestas = 0;
        for (Publicacion p : insta.publicacionesDe(u.getUsername())) {
            if (p.esSoloTexto()) {
                continue;                        // los instas de texto no van al grid
            }
            grid.add(celdaGrid(p));
            puestas++;
        }
        if (puestas == 0) {
            JLabel vacio = new JLabel("Sin fotos ni reels publicados todavía.", SwingConstants.CENTER);
            vacio.setForeground(EstiloInsta.TEXTO_GRIS);
            vacio.setBorder(EstiloInsta.margen(30, 12, 30, 12));
            return capar(vacio);
        }
        // Rellena la última fila para que las celdas queden cuadradas.
        while (puestas % ConfigInsta.COLUMNAS_GRID != 0) {
            JPanel hueco = new JPanel();
            hueco.setOpaque(false);
            grid.add(hueco);
            puestas++;
        }
        return capar(grid);
    }

    private JComponent celdaGrid(Publicacion p) {
        JLabel celda = new JLabel("", SwingConstants.CENTER);
        celda.setPreferredSize(new Dimension(LADO_CELDA, LADO_CELDA));
        celda.setOpaque(true);
        celda.setToolTipText(p.getTexto());

        if (p.esVideo()) {
            celda.setText("<html><center>REEL<br>&#9654;</center></html>");
            celda.setBackground(new Color(30, 30, 30));
            celda.setForeground(Color.WHITE);
        } else {
            ImageIcon mini = ConfigInsta.miniatura(new File(p.getRutaImagen()), LADO_CELDA);
            celda.setBackground(new Color(235, 235, 235));
            if (mini != null) {
                celda.setIcon(mini);
            } else {
                celda.setText("(imagen)");
                celda.setForeground(EstiloInsta.TEXTO_GRIS);
            }
        }
        return celda;
    }

    // -----------------------------------------------------------------
    //  Ayudantes de maquetación
    // -----------------------------------------------------------------

    /** Envuelve un componente para que ocupe el ancho pero no se estire a lo alto. */
    private JComponent capar(JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(EstiloInsta.BLANCO);
        wrap.setAlignmentX(LEFT_ALIGNMENT);
        wrap.add(c, BorderLayout.CENTER);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrap.getPreferredSize().height));
        return wrap;
    }

    private JComponent fila(JComponent c, int arriba, int izq, int abajo, int der) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(EstiloInsta.BLANCO);
        wrap.setAlignmentX(LEFT_ALIGNMENT);
        wrap.setBorder(EstiloInsta.margen(arriba, izq, abajo, der));
        wrap.add(c, BorderLayout.CENTER);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrap.getPreferredSize().height));
        return wrap;
    }

    // -----------------------------------------------------------------

    private void mostrarLista(String titulo, ListaEnlazada<String> lista) {
        StringBuilder sb = new StringBuilder();
        for (String username : lista.comoLista()) {
            sb.append(username).append("\n");
        }
        if (sb.length() == 0) {
            sb.append("(nadie todavía)");
        }
        JTextArea area = new JTextArea(sb.toString(), 12, 18);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), titulo,
                JOptionPane.PLAIN_MESSAGE);
    }

    /** Timeline de ese usuario, de la más reciente a la más antigua. */
    private void verPublicaciones(String username) {
        ListaEnlazada<Publicacion> lista = new ListaEnlazada<>();
        for (Publicacion p : insta.publicacionesDe(username)) {
            lista.agregarInicio(p);          // el último queda primero
        }

        PanelFeed columna = new PanelFeed();
        boolean hay = false;
        for (Publicacion p : lista.comoLista()) {
            columna.add(new TarjetaPublicacion(p, usuarios, 380, panelInsta::verPerfilDe));
            hay = true;
        }
        if (!hay) {
            columna.add(new JLabel("  Este usuario no tiene publicaciones."));
        }
        columna.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(columna,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(400, 560));
        scroll.setBorder(null);
        EstiloInsta.scrollFino(scroll);
        JOptionPane.showMessageDialog(this, scroll,
                "Publicaciones de " + username, JOptionPane.PLAIN_MESSAGE);
    }
}
