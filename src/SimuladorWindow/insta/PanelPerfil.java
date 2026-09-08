package SimuladorWindow.insta;

import SimuladorWindow.estructuras.ListaEnlazada;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Perfil de un usuario (enunciado 4.5 y 4.9b).
 *
 * Muestra los 10 datos que pide el enunciado. Si el perfil es de OTRO usuario,
 * agrega el boton Seguir / Dejar de seguir y "Ver sus publicaciones".
 */
public class PanelPerfil extends JPanel {

    private final InstaServicio insta;
    private final UsuarioServicio usuarios;
    private final Usuario usuarioActual;
    private final PanelInsta panelInsta;

    private String usernameMostrado;

    public PanelPerfil(InstaServicio insta, UsuarioServicio usuarios,
                       Usuario usuarioActual, PanelInsta panelInsta) {
        this.insta = insta;
        this.usuarios = usuarios;
        this.usuarioActual = usuarioActual;
        this.panelInsta = panelInsta;
        setLayout(new BorderLayout());
        mostrarMio();
    }

    public void mostrarMio() {
        mostrarPerfilDe(usuarioActual.getUsername());
    }

    public void mostrarPerfilDe(String username) {
        this.usernameMostrado = username;
        removeAll();

        Usuario u = usuarios.buscar(username);
        if (u == null) {
            add(new JLabel("No existe el usuario: " + username), BorderLayout.NORTH);
            revalidate();
            repaint();
            return;
        }

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.add(crearFoto(u), BorderLayout.WEST);
        cabecera.add(crearDatos(u), BorderLayout.CENTER);
        add(cabecera, BorderLayout.NORTH);

        add(new JScrollPane(crearGrid(u)), BorderLayout.CENTER);

        boolean esOtro = !username.equalsIgnoreCase(usuarioActual.getUsername());
        if (esOtro) {
            add(crearAcciones(u), BorderLayout.SOUTH);
        }

        revalidate();
        repaint();
    }

    private JComponent crearFoto(Usuario u) {
        JLabel foto = new JLabel();
        foto.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        if (u.getFotoPerfil() != null && new File(u.getFotoPerfil()).exists()) {
            ImageIcon icono = new ImageIcon(new ImageIcon(u.getFotoPerfil())
                    .getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH));
            foto.setIcon(icono);
        } else {
            foto.setText("(sin foto)");
        }
        return foto;
    }

    private JComponent crearDatos(Usuario u) {
        String estado = u.isActiva() ? "activa" : "inactiva";
        int followers = insta.seguidoresDe(u.getUsername()).size();
        int following = insta.aQuienesSigue(u.getUsername()).size();
        int publicaciones = insta.contarPublicaciones(u.getUsername());

        JTextArea datos = new JTextArea();
        datos.setEditable(false);
        datos.setOpaque(false);
        datos.setText(
                "Nombre completo: " + u.getNombreCompleto() + "\n"
              + "Username: " + u.getUsername() + "\n"
              + "Edad: " + u.getEdad() + "\n"
              + "Genero: " + u.getGenero() + "\n"
              + "Fecha de registro: " + u.getFechaRegistro() + "\n"
              + "Followers: " + followers + "\n"
              + "Following: " + following + "\n"
              + "Publicaciones: " + publicaciones + "\n"
              + "Estado de la cuenta: " + estado);
        datos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnFollowers = new JButton("Ver followers");
        btnFollowers.addActionListener(e -> mostrarLista(
                "Followers de " + u.getUsername(), insta.listaFollowers(u.getUsername())));
        JButton btnFollowing = new JButton("Ver following");
        btnFollowing.addActionListener(e -> mostrarLista(
                "Following de " + u.getUsername(), insta.listaFollowing(u.getUsername())));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnFollowers);
        botones.add(btnFollowing);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(datos, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    /** Muestra los usernames de una ListaEnlazada en un diálogo. */
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

    /** Grid de 3 columnas con las imagenes publicadas (enunciado 4.6). */
    private JComponent crearGrid(Usuario u) {
        JPanel grid = new JPanel(new GridLayout(0, ConfigInsta.COLUMNAS_GRID, 4, 4));

        int puestas = 0;
        for (Publicacion p : insta.publicacionesDe(u.getUsername())) {
            if (!p.tieneImagen()) {
                continue;
            }
            ImageIcon mini = ConfigInsta.miniatura(new File(p.getRutaImagen()), 110);
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
            grid.add(new JLabel("  (sin imagenes publicadas)"));
        }
        return grid;
    }

    private JComponent crearAcciones(Usuario u) {
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));

        boolean sigo = insta.sigo(usuarioActual.getUsername(), u.getUsername());
        JButton btnSeguir = new JButton(sigo ? "Dejar de seguir" : "Seguir");
        btnSeguir.addActionListener(e -> {
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

        JButton btnVer = new JButton("Ver sus publicaciones");
        btnVer.addActionListener(e -> verPublicaciones(u.getUsername()));

        acciones.add(btnSeguir);
        acciones.add(btnVer);
        return acciones;
    }

    /** Timeline de ese usuario, de la mas reciente a la mas antigua. */
    private void verPublicaciones(String username) {
        ListaEnlazada<Publicacion> lista = new ListaEnlazada<>();
        for (Publicacion p : insta.publicacionesDe(username)) {
            lista.agregarInicio(p);          // agregarInicio -> el ultimo queda primero
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
