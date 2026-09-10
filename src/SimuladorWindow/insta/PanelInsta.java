package SimuladorWindow.insta;

import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

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
    private final Runnable alCerrarSesion;

    /** La cuenta de INSTA+ con la que se entró (para la foto de la barra). */
    private final Usuario usuarioSesion;

    /** Mientras esta sesion de INSTA+ este activa, el hilo de avisos corre. */
    private volatile boolean sesionActiva = true;

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
                      Usuario usuarioActual, Runnable alCerrarSesion) {
        this.alCerrarSesion = alCerrarSesion;
        this.usuarioSesion = usuarioActual;
        insta.asegurarCarpetaUsuario(usuarioActual.getUsername());

        panelPerfil        = new PanelPerfil(insta, usuarios, usuarioActual, this);
        panelCrear         = new PanelCrear(insta, usuarioActual);
        panelTimeline      = new PanelTimeline(insta, usuarios, usuarioActual, this);
        panelInteracciones = new PanelInteracciones(insta, usuarios, usuarioActual, this);
        panelBuscarPerfil  = new PanelBuscarPerfil(insta, usuarios, usuarioActual, this);
        panelBuscarHashtag = new PanelBuscarHashtag(insta, usuarios, this);
        panelInbox         = new PanelInbox(insta, usuarios, usuarioActual);
        panelEditar        = new PanelEditarPerfil(insta, usuarios, usuarioActual, this);

        contenedor.setBackground(EstiloInsta.FONDO);
        contenedor.add(panelPerfil,             "PERFIL");        // trae su propio scroll
        contenedor.add(envolver(panelCrear),    "CARGAR");
        contenedor.add(panelTimeline,           "TIMELINE");      // trae su propio scroll
        contenedor.add(panelInteracciones,      "INTERACCIONES"); // trae su propio scroll
        contenedor.add(panelBuscar(),           "BUSCAR");
        contenedor.add(envolver(panelInbox),    "INBOX");
        contenedor.add(envolver(panelEditar),   "EDITAR");

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

        barra.add(EstiloInsta.wordmark(24f), BorderLayout.WEST);

        botonMensajes = new JButton(IconosInsta.icono(IconosInsta.MENSAJE, 24, false));
        planoIcono(botonMensajes);
        botonMensajes.setToolTipText("Mensajes");
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
        // El 5º botón muestra tu foto de perfil (como la app real).
        navBotones[4] = navBoton(IconosInsta.PERSONA, () -> { panelPerfil.mostrarMio();      irA("PERFIL", 4); });

        for (JButton b : navBotones) {
            barra.add(b);
        }
        return barra;
    }

    private JButton navBoton(String icono, Runnable accion) {
        JButton b = new JButton(iconoNav(icono, false));
        b.putClientProperty("icono", icono);
        planoIcono(b);
        b.setBorder(EstiloInsta.margen(10, 0, 10, 0));
        b.addActionListener(e -> accion.run());
        return b;
    }

    /** El icono de un botón de la barra inferior según si está activo o no. */
    private Icon iconoNav(String nombre, boolean activo) {
        if (IconosInsta.PERSONA.equals(nombre)) {
            String foto = usuarioSesion.getFotoPerfil();
            return activo
                    ? EstiloInsta.avatarAnillo(foto, usuarioSesion.getUsername(), 28)
                    : EstiloInsta.avatar(foto, usuarioSesion.getUsername(), 26);
        }
        return IconosInsta.icono(nombre, 26, activo);
    }

    private void planoIcono(JButton b) {
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // -----------------------------------------------------------------

    /**
     * La pantalla de Buscar: arriba las pestañas (Personas / Hashtags) y debajo
     * el panel que toque, cambiando con un CardLayout.
     */
    private JComponent panelBuscar() {
        CardLayout cl = new CardLayout();
        JPanel cuerpo = new JPanel(cl);
        cuerpo.add(panelBuscarPerfil, "PERSONAS");   // cada uno trae su propio scroll
        cuerpo.add(panelBuscarHashtag, "HASHTAGS");

        JComponent barra = EstiloInsta.barraSegmentos(new String[] {"Personas", "Hashtags"},
                i -> cl.show(cuerpo, i == 0 ? "PERSONAS" : "HASHTAGS"));

        JPanel todo = new JPanel(new BorderLayout());
        todo.setBackground(EstiloInsta.FONDO);
        todo.add(barra, BorderLayout.NORTH);
        todo.add(cuerpo, BorderLayout.CENTER);
        return todo;
    }

    private JScrollPane envolver(JComponent panel) {
        JScrollPane scroll = new JScrollPane(panel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(EstiloInsta.FONDO);
        EstiloInsta.scrollFino(scroll);
        return scroll;
    }

    /**
     * Hilo demonio que cada 5 segundos cuenta los mensajes no leidos y los
     * muestra en el icono de mensajes de la cabecera (enunciado 4.11 -
     * notificacion de mensajes nuevos; checklist - hilo de revision del Inbox).
     */
    private void iniciarHiloNotificaciones(InstaServicio insta, String username) {
        Thread vigilante = new Thread(() -> {
            while (sesionActiva) {
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
            navBotones[i].setIcon(iconoNav(ic, i == navActivo));
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

    /** La usa el boton "Cerrar sesion" del propio perfil: vuelve al login de
     *  INSTA+ (no cierra la sesion de Windows). */
    public void cerrarSesion() {
        int op = JOptionPane.showConfirmDialog(this, "Cerrar sesion de INSTA+?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            sesionActiva = false;         // corta el hilo de avisos
            alCerrarSesion.run();
        }
    }
}
