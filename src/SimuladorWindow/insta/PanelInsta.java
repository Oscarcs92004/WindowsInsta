package SimuladorWindow.insta;

import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;
import SimuladorWindow.ui.VentanaPrincipal;

import javax.swing.*;
import java.awt.*;

/**
 * INSTA+ como una app de telefono (enunciado 4.1 y 4.4, MODO_MOBILE):
 * una sola pantalla vertical con la cabecera "Instagram" arriba, el contenido
 * en el centro (CardLayout, una vista a la vez, sin ventanas nuevas) y la
 * barra de navegacion con iconos abajo, igual que la app real.
 */
public class PanelInsta extends JPanel {

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenedor = new JPanel(cartas);
    private final VentanaPrincipal ventana;

    /** Icono de mensajes de la cabecera; el hilo le pone el numero de no leidos. */
    private JButton botonMensajes;

    private final PanelPerfil panelPerfil;
    private final PanelCrear panelCrear;
    private final PanelTimeline panelTimeline;
    private final PanelInteracciones panelInteracciones;
    private final PanelBuscarPerfil panelBuscarPerfil;
    private final PanelBuscarHashtag panelBuscarHashtag;
    private final PanelInbox panelInbox;
    private final PanelEditarPerfil panelEditar;

    private final JButton[] navBotones = new JButton[5];
    private int navActivo = 0;

    public PanelInsta(InstaServicio insta, UsuarioServicio usuarios,
                      Usuario usuarioActual, VentanaPrincipal ventana) {
        this.ventana = ventana;
        insta.asegurarCarpetaUsuario(usuarioActual.getUsername());

        panelPerfil        = new PanelPerfil(insta, usuarios, usuarioActual, this);
        panelCrear         = new PanelCrear(insta, usuarioActual);
        panelTimeline      = new PanelTimeline(insta, usuarios, usuarioActual);
        panelInteracciones = new PanelInteracciones(insta, usuarioActual);
        panelBuscarPerfil  = new PanelBuscarPerfil(insta, usuarios, usuarioActual, this);
        panelBuscarHashtag = new PanelBuscarHashtag(insta, usuarios);
        panelInbox         = new PanelInbox(insta, usuarios, usuarioActual);
        panelEditar        = new PanelEditarPerfil(insta, usuarios, usuarioActual, this);

        contenedor.setBackground(EstiloInsta.FONDO);
        contenedor.add(envolver(panelPerfil),        "PERFIL");
        contenedor.add(envolver(panelCrear),        "CARGAR");
        contenedor.add(envolver(panelTimeline),      "TIMELINE");
        contenedor.add(envolver(panelInteracciones), "INTERACCIONES");
        contenedor.add(envolver(buscarConPestanas()), "BUSCAR");
        contenedor.add(envolver(panelInbox),         "INBOX");
        contenedor.add(envolver(panelEditar),        "EDITAR");

        setLayout(new BorderLayout());
        setBackground(EstiloInsta.BLANCO);
        add(cabecera(), BorderLayout.NORTH);
        add(contenedor, BorderLayout.CENTER);
        add(barraInferior(), BorderLayout.SOUTH);

        irA("TIMELINE", 0);
        iniciarHiloNotificaciones(insta, usuarioActual.getUsername());
    }

    // -----------------------------------------------------------------
    //  Cabecera: "Instagram" a la izquierda, mensajes a la derecha
    // -----------------------------------------------------------------

    private JComponent cabecera() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(EstiloInsta.BLANCO);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloInsta.BORDE),
                EstiloInsta.margen(8, 14, 8, 14)));

        JLabel logo = new JLabel("Instagram");
        logo.setFont(EstiloInsta.LOGO.deriveFont(22f));
        logo.setForeground(EstiloInsta.TEXTO);
        barra.add(logo, BorderLayout.WEST);

        botonMensajes = new JButton(IconosInsta.icono(IconosInsta.MENSAJE, 24, false));
        planoIcono(botonMensajes);
        botonMensajes.addActionListener(e -> { panelInbox.recargar(); irA("INBOX", -1); });
        barra.add(botonMensajes, BorderLayout.EAST);
        return barra;
    }

    // -----------------------------------------------------------------
    //  Barra de navegacion inferior (5 iconos, como Instagram movil)
    // -----------------------------------------------------------------

    private JComponent barraInferior() {
        JPanel barra = new JPanel(new GridLayout(1, 5));
        barra.setBackground(EstiloInsta.BLANCO);
        barra.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloInsta.BORDE));

        navBotones[0] = navBoton(IconosInsta.CASA,    () -> { panelTimeline.recargar();      irA("TIMELINE", 0); });
        navBotones[1] = navBoton(IconosInsta.LUPA,    () -> irA("BUSCAR", 1));
        navBotones[2] = navBoton(IconosInsta.MAS,     () -> { panelCrear.recargar();        irA("CARGAR", 2); });
        navBotones[3] = navBoton(IconosInsta.CORAZON, () -> { panelInteracciones.recargar(); irA("INTERACCIONES", 3); });
        navBotones[4] = navBoton(IconosInsta.PERSONA, () -> { panelPerfil.mostrarMio();      irA("PERFIL", 4); });

        for (JButton b : navBotones) {
            barra.add(b);
        }
        return barra;
    }

    private JButton navBoton(String icono, Runnable accion) {
        JButton b = new JButton(IconosInsta.icono(icono, 26, false));
        b.putClientProperty("icono", icono);
        planoIcono(b);
        b.setBorder(EstiloInsta.margen(10, 0, 10, 0));
        b.addActionListener(e -> accion.run());
        return b;
    }

    private void planoIcono(JButton b) {
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // -----------------------------------------------------------------

    private JComponent buscarConPestanas() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(EstiloInsta.NORMAL);
        tabs.addTab("Personas", panelBuscarPerfil);
        tabs.addTab("Hashtags", panelBuscarHashtag);
        return tabs;
    }

    private JScrollPane envolver(JComponent panel) {
        JScrollPane scroll = new JScrollPane(panel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(EstiloInsta.FONDO);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    /**
     * Hilo demonio que cada 5 segundos cuenta los mensajes no leidos y los
     * muestra en el icono de mensajes de la cabecera (enunciado 4.11 -
     * notificacion de mensajes nuevos; checklist - hilo de revision del Inbox).
     */
    private void iniciarHiloNotificaciones(InstaServicio insta, String username) {
        Thread vigilante = new Thread(() -> {
            while (true) {
                int noLeidos = insta.contarNoLeidos(username);
                SwingUtilities.invokeLater(() -> {
                    if (botonMensajes != null) {
                        botonMensajes.setText(noLeidos > 0 ? " " + noLeidos : "");
                    }
                });
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        vigilante.setDaemon(true);
        vigilante.start();
    }

    // -----------------------------------------------------------------

    /** Cambia de vista y marca el icono de la barra inferior (o -1 si ninguno). */
    private void irA(String carta, int indiceNav) {
        cartas.show(contenedor, carta);
        if (indiceNav >= 0 && indiceNav < navBotones.length) {
            navActivo = indiceNav;
        }
        for (int i = 0; i < navBotones.length; i++) {
            String ic = (String) navBotones[i].getClientProperty("icono");
            navBotones[i].setIcon(IconosInsta.icono(ic, 26, i == navActivo));
        }
    }

    /** La usan Buscar e Interacciones para abrir el perfil de otro. */
    public void verPerfilDe(String username) {
        panelPerfil.mostrarPerfilDe(username);
        irA("PERFIL", 4);
    }

    /** La usa el boton "Editar perfil" del propio perfil. */
    public void irAEditarPerfil() {
        panelEditar.recargar();
        irA("EDITAR", -1);
    }

    /** La usa el boton "Volver" de Editar perfil. */
    public void volverAlPerfil() {
        panelPerfil.mostrarMio();
        irA("PERFIL", 4);
    }

    /** La usa el boton "Cerrar sesion" del propio perfil. */
    public void cerrarSesion() {
        int op = JOptionPane.showConfirmDialog(this, "Cerrar sesion?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            ventana.mostrarLogin();
        }
    }
}
