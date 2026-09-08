package SimuladorWindow.insta;

import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;
import SimuladorWindow.ui.VentanaPrincipal;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * INSTA+ dentro de una sola vista (enunciado 4.1 y 4.4), con el aspecto de
 * Instagram: barra lateral blanca con el logo y las 9 opciones, y a la
 * derecha un CardLayout que muestra una "pantalla" a la vez, sin ventanas
 * nuevas.
 */
public class PanelInsta extends JPanel {

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenedor = new JPanel(cartas);

    /** Boton "Inbox" de la barra; el hilo de notificaciones le cambia el texto. */
    private JButton botonInbox;
    /** Todos los botones de la barra, para resaltar el que esta activo. */
    private final List<JButton> botonesMenu = new ArrayList<>();
    private JButton botonActivo;

    private final PanelPerfil panelPerfil;
    private final PanelCargarImagenes panelCargar;
    private final PanelTimeline panelTimeline;
    private final PanelInteracciones panelInteracciones;
    private final PanelBuscarPerfil panelBuscarPerfil;
    private final PanelBuscarHashtag panelBuscarHashtag;
    private final PanelInbox panelInbox;
    private final PanelEditarPerfil panelEditar;

    public PanelInsta(InstaServicio insta, UsuarioServicio usuarios,
                      Usuario usuarioActual, VentanaPrincipal ventana) {

        insta.asegurarCarpetaUsuario(usuarioActual.getUsername());

        panelPerfil        = new PanelPerfil(insta, usuarios, usuarioActual, this);
        panelCargar        = new PanelCargarImagenes(insta, usuarioActual);
        panelTimeline      = new PanelTimeline(insta, usuarios, usuarioActual);
        panelInteracciones = new PanelInteracciones(insta, usuarioActual);
        panelBuscarPerfil  = new PanelBuscarPerfil(insta, usuarios, usuarioActual, this);
        panelBuscarHashtag = new PanelBuscarHashtag(insta, usuarios);
        panelInbox         = new PanelInbox(insta, usuarios, usuarioActual);
        panelEditar        = new PanelEditarPerfil(insta, usuarios, usuarioActual);

        contenedor.setBackground(EstiloInsta.FONDO);
        contenedor.add(envolver(panelPerfil),        "PERFIL");
        contenedor.add(envolver(panelCargar),        "CARGAR");
        contenedor.add(envolver(panelTimeline),      "TIMELINE");
        contenedor.add(envolver(panelInteracciones), "INTERACCIONES");
        contenedor.add(envolver(panelBuscarPerfil),  "BUSCAR_PERFIL");
        contenedor.add(envolver(panelBuscarHashtag), "BUSCAR_HASHTAG");
        contenedor.add(envolver(panelInbox),         "INBOX");
        contenedor.add(envolver(panelEditar),        "EDITAR");

        setLayout(new BorderLayout());
        setBackground(EstiloInsta.FONDO);
        add(crearBarraLateral(ventana), BorderLayout.WEST);
        add(contenedor, BorderLayout.CENTER);

        mostrar("PERFIL");
        iniciarHiloNotificaciones(insta, usuarioActual.getUsername());
    }

    /**
     * Hilo demonio que cada 5 segundos cuenta los mensajes no leidos y muestra
     * el aviso en el boton "Inbox" (enunciado 4.11 - notificacion de mensajes
     * nuevos; checklist - hilo de revision del Inbox).
     */
    private void iniciarHiloNotificaciones(InstaServicio insta, String username) {
        Thread vigilante = new Thread(() -> {
            while (true) {
                int noLeidos = insta.contarNoLeidos(username);
                String texto = noLeidos > 0 ? "Inbox (" + noLeidos + ")" : "Inbox";
                SwingUtilities.invokeLater(() -> {
                    if (botonInbox != null) {
                        botonInbox.setText(texto);
                    }
                });
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        vigilante.setDaemon(true);   // no impide cerrar la aplicacion
        vigilante.start();
    }

    private JScrollPane envolver(JComponent panel) {
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(EstiloInsta.FONDO);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // -----------------------------------------------------------------
    //  Barra lateral estilo Instagram
    // -----------------------------------------------------------------

    private JComponent crearBarraLateral(VentanaPrincipal ventana) {
        JPanel barra = new JPanel();
        barra.setLayout(new BoxLayout(barra, BoxLayout.Y_AXIS));
        barra.setBackground(EstiloInsta.BLANCO);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, EstiloInsta.BORDE),
                EstiloInsta.margen(24, 14, 14, 14)));
        barra.setPreferredSize(new Dimension(210, 0));

        JLabel logo = new JLabel("Simulador W.");
        logo.setFont(EstiloInsta.LOGO.deriveFont(19f));
        logo.setForeground(EstiloInsta.TEXTO);
        logo.setAlignmentX(LEFT_ALIGNMENT);
        logo.setBorder(EstiloInsta.margen(0, 6, 20, 0));
        barra.add(logo);

        agregarItem(barra, "Inicio",         () -> { panelTimeline.recargar(); mostrar("TIMELINE"); });
        botonInbox = agregarItem(barra, "Inbox", () -> { panelInbox.recargar(); mostrar("INBOX"); });
        agregarItem(barra, "Buscar",         () -> mostrar("BUSCAR_PERFIL"));
        agregarItem(barra, "Explorar tags",  () -> mostrar("BUSCAR_HASHTAG"));
        agregarItem(barra, "Interacciones",  () -> { panelInteracciones.recargar(); mostrar("INTERACCIONES"); });
        agregarItem(barra, "Crear",          () -> { panelCargar.recargar(); mostrar("CARGAR"); });
        agregarItem(barra, "Perfil",         () -> { panelPerfil.mostrarMio(); mostrar("PERFIL"); });
        agregarItem(barra, "Editar perfil",  () -> { panelEditar.recargar(); mostrar("EDITAR"); });

        barra.add(Box.createVerticalGlue());

        JButton salir = agregarItem(barra, "Cerrar sesion", () -> cerrarSesion(ventana));
        salir.setForeground(EstiloInsta.ROJO);

        return barra;
    }

    private JButton agregarItem(JPanel barra, String texto, Runnable accion) {
        JButton item = new JButton(texto);
        item.setHorizontalAlignment(SwingConstants.LEFT);
        item.setFont(EstiloInsta.NORMAL);
        item.setForeground(EstiloInsta.TEXTO);
        item.setBackground(EstiloInsta.BLANCO);
        item.setBorder(EstiloInsta.margen(10, 6, 10, 6));
        item.setContentAreaFilled(false);
        item.setOpaque(true);
        item.setFocusPainted(false);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.setAlignmentX(LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        item.addActionListener(e -> {
            marcarActivo(item);
            accion.run();
        });
        barra.add(item);
        botonesMenu.add(item);
        return item;
    }

    private void marcarActivo(JButton item) {
        if (botonActivo != null) {
            botonActivo.setFont(EstiloInsta.NORMAL);
            botonActivo.setBackground(EstiloInsta.BLANCO);
        }
        item.setFont(EstiloInsta.FUERTE);
        item.setBackground(new Color(240, 240, 240));
        botonActivo = item;
    }

    private void cerrarSesion(VentanaPrincipal ventana) {
        int op = JOptionPane.showConfirmDialog(this, "Cerrar sesion?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            ventana.mostrarLogin();
        }
    }

    // -----------------------------------------------------------------

    private void mostrar(String carta) {
        cartas.show(contenedor, carta);
    }

    /** La usan Buscar Profile e Interacciones para abrir el perfil de otro. */
    public void verPerfilDe(String username) {
        panelPerfil.mostrarPerfilDe(username);
        mostrar("PERFIL");
    }

    /** La usa el boton "Editar perfil" del propio perfil. */
    public void irAEditarPerfil() {
        panelEditar.recargar();
        mostrar("EDITAR");
    }
}
