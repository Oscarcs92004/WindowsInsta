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
        centro.setBorder(EstiloInsta.margen(28, 28, 28, 28));

        centro.add(cabecera(u));
        centro.add(Box.createVerticalStrut(20));
        JSeparator sep = new JSeparator();
        sep.setAlignmentX(LEFT_ALIGNMENT);
        centro.add(sep);
        centro.add(Box.createVerticalStrut(12));
        JComponent grid = grid(u);
        grid.setAlignmentX(LEFT_ALIGNMENT);
        centro.add(grid);

        add(centro, BorderLayout.NORTH);
        revalidate();
        repaint();
    }

    // -----------------------------------------------------------------

    private JComponent cabecera(Usuario u) {
        boolean esOtro = !u.getUsername().equalsIgnoreCase(usuarioActual.getUsername());

        JLabel foto = new JLabel(EstiloInsta.avatar(u.getFotoPerfil(), u.getUsername(), 140));
        foto.setBorder(EstiloInsta.margen(0, 0, 0, 30));

        JPanel datos = new JPanel();
        datos.setOpaque(false);
        datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));

        // Fila 1: username + boton de accion.
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        fila1.setOpaque(false);
        fila1.setAlignmentX(LEFT_ALIGNMENT);
        JLabel username = new JLabel(u.getUsername());
        username.setFont(EstiloInsta.TITULO);
        username.setForeground(EstiloInsta.TEXTO);
        fila1.add(username);
        fila1.add(botonAccion(u, esOtro));
        datos.add(fila1);
        datos.add(Box.createVerticalStrut(14));

        // Fila 2: contadores.
        int publicaciones = insta.contarPublicaciones(u.getUsername());
        int followers = insta.seguidoresDe(u.getUsername()).size();
        int following = insta.aQuienesSigue(u.getUsername()).size();

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 22, 0));
        fila2.setOpaque(false);
        fila2.setAlignmentX(LEFT_ALIGNMENT);
        fila2.add(contador(publicaciones + " publicaciones", null));
        fila2.add(contador(followers + " followers",
                () -> mostrarLista("Followers de " + u.getUsername(),
                        insta.listaFollowers(u.getUsername()))));
        fila2.add(contador(following + " following",
                () -> mostrarLista("Following de " + u.getUsername(),
                        insta.listaFollowing(u.getUsername()))));
        datos.add(fila2);
        datos.add(Box.createVerticalStrut(12));

        // Fila 3: nombre completo + genero + fecha + estado.
        JLabel nombre = new JLabel(u.getNombreCompleto());
        nombre.setFont(EstiloInsta.FUERTE);
        nombre.setForeground(EstiloInsta.TEXTO);
        nombre.setAlignmentX(LEFT_ALIGNMENT);
        datos.add(nombre);

        JLabel extra = new JLabel(u.getGenero() + " · " + u.getEdad() + " años · desde "
                + u.getFechaRegistro() + (u.isActiva() ? "" : "  · CUENTA INACTIVA"));
        extra.setFont(EstiloInsta.CHICA);
        extra.setForeground(u.isActiva() ? EstiloInsta.TEXTO_GRIS : EstiloInsta.ROJO);
        extra.setAlignmentX(LEFT_ALIGNMENT);
        datos.add(extra);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        cabecera.setAlignmentX(LEFT_ALIGNMENT);
        cabecera.add(foto, BorderLayout.WEST);
        cabecera.add(datos, BorderLayout.CENTER);
        cabecera.setMaximumSize(new Dimension(720, 220));
        return cabecera;
    }

    private JComponent botonAccion(Usuario u, boolean esOtro) {
        if (!esOtro) {
            JButton editar = EstiloInsta.botonSecundario("Editar perfil");
            editar.addActionListener(e -> panelInsta.irAEditarPerfil());
            return editar;
        }

        boolean sigo = insta.sigo(usuarioActual.getUsername(), u.getUsername());
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);

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

        acciones.add(seguir);
        acciones.add(ver);
        return acciones;
    }

    private JComponent contador(String texto, Runnable accion) {
        if (accion == null) {
            JLabel etiqueta = new JLabel(texto);
            etiqueta.setFont(EstiloInsta.NORMAL);
            etiqueta.setForeground(EstiloInsta.TEXTO);
            return etiqueta;
        }
        JButton boton = new JButton(texto);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setFocusPainted(false);
        boton.setMargin(new Insets(0, 0, 0, 0));
        boton.setFont(EstiloInsta.NORMAL);
        boton.setForeground(EstiloInsta.TEXTO);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.addActionListener(e -> accion.run());
        return boton;
    }

    /** Grid de 3 columnas con las imagenes publicadas (enunciado 4.6). */
    private JComponent grid(Usuario u) {
        JPanel grid = new JPanel(new GridLayout(0, ConfigInsta.COLUMNAS_GRID, 4, 4));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(720, Integer.MAX_VALUE));

        int puestas = 0;
        for (Publicacion p : insta.publicacionesDe(u.getUsername())) {
            if (!p.tieneImagen()) {
                continue;
            }
            ImageIcon mini = ConfigInsta.miniatura(new File(p.getRutaImagen()), 220);
            JLabel celda = new JLabel();
            if (mini != null) {
                celda.setIcon(mini);
            } else {
                celda.setText("(imagen)");
            }
            celda.setToolTipText(p.getTexto());
            grid.add(celda);
            puestas++;
        }
        if (puestas == 0) {
            JLabel vacio = new JLabel("Sin imagenes publicadas todavia.");
            vacio.setForeground(EstiloInsta.TEXTO_GRIS);
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT));
            wrap.setOpaque(false);
            wrap.add(vacio);
            return wrap;
        }
        return grid;
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
