package miniwindows.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.beans.PropertyVetoException;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import miniwindows.modelo.Rol;
import miniwindows.modelo.Usuario;
import miniwindows.servicios.UsuarioServicio;
import miniwindows.so.PanelConsola;
import miniwindows.so.PanelEditor;
import miniwindows.so.PanelExplorador;
import miniwindows.so.PanelReproductor;
import miniwindows.so.PanelVisor;
import miniwindows.so.Rutas;

/**
 * El escritorio de Mini-Windows: imita un sistema operativo real.
 *
 *  - Al centro un JDesktopPane (el "fondo de pantalla") con iconos.
 *  - Cada herramienta se abre como una ventana interna (JInternalFrame) que
 *    se puede mover, redimensionar, minimizar y cerrar.
 *  - Abajo una barra de tareas con el boton "Inicio", las ventanas abiertas
 *    y el reloj.
 */
public class PanelEscritorio extends JPanel {

    private final VentanaPrincipal ventana;
    private final Usuario usuarioActual;
    private final File carpetaRaiz;

    /** El escritorio pinta el wallpaper del usuario estirado a toda el area. */
    private final JDesktopPane escritorio = new JDesktopPane() {
        private final Image wallpaper = cargarWallpaper();

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (wallpaper != null) {
                g.drawImage(wallpaper, 0, 0, getWidth(), getHeight(), this);
            }
        }
    };
    private final JPanel areaVentanas = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));

    /** Desplazamiento para que las ventanas nuevas no salgan una encima de otra. */
    private int cascada = 0;

    public PanelEscritorio(VentanaPrincipal ventana, UsuarioServicio servicio,
                           Usuario usuarioActual) {
        this.ventana = ventana;
        this.usuarioActual = usuarioActual;

        Rutas.asegurarCarpetasUsuario(usuarioActual);
        this.carpetaRaiz = Rutas.carpetaRaizDe(usuarioActual);

        setLayout(new BorderLayout());

        escritorio.setBackground(Estilo.FONDO_ESCRITORIO);
        add(escritorio, BorderLayout.CENTER);

        crearIconosEscritorio();
        add(crearBarraTareas(), BorderLayout.SOUTH);
    }

    // ------------------------------------------------------------------
    //  Lista de aplicaciones (se usa para los iconos y para el menu Inicio)
    // ------------------------------------------------------------------

    private Map<String, Supplier<JComponent>> aplicaciones() {
        Map<String, Supplier<JComponent>> apps = new LinkedHashMap<>();
        apps.put("Explorador",       () -> new PanelExplorador(usuarioActual, carpetaRaiz));
        apps.put("Editor de texto",  () -> new PanelEditor(usuarioActual, carpetaRaiz));
        apps.put("Visor de imagenes",() -> new PanelVisor(usuarioActual, carpetaRaiz));
        apps.put("Consola",          () -> new PanelConsola(usuarioActual, carpetaRaiz));
        apps.put("Reproductor",      () -> new PanelReproductor(usuarioActual, carpetaRaiz));
        return apps;
    }

    /** Nombre del archivo de icono de cada herramienta (null = sin icono todavia). */
    private static String archivoIcono(String app) {
        switch (app) {
            case "Explorador":        return "file.png";
            case "Editor de texto":   return "notepad.png";
            case "Visor de imagenes": return "camera.png";
            case "Consola":           return "console.png";
            case "Reproductor":       return "musica.png";
            default:                  return null;
        }
    }

    // ------------------------------------------------------------------
    //  Iconos del escritorio
    // ------------------------------------------------------------------

    private void crearIconosEscritorio() {
        JPanel iconos = new JPanel(new GridLayout(0, 1, 10, 12));
        iconos.setOpaque(false);
        iconos.setBounds(15, 15, 130, 380);

        for (Map.Entry<String, Supplier<JComponent>> app : aplicaciones().entrySet()) {
            String nombre = app.getKey();
            Supplier<JComponent> fabrica = app.getValue();

            JButton icono = new JButton("<html><center>" + nombre + "</center></html>");
            icono.setFocusPainted(false);
            // Aspecto de icono de escritorio: sin recuadro, texto claro y debajo.
            icono.setBorderPainted(false);
            icono.setContentAreaFilled(false);
            icono.setForeground(Estilo.TEXTO_CLARO);
            icono.setVerticalTextPosition(SwingConstants.BOTTOM);
            icono.setHorizontalTextPosition(SwingConstants.CENTER);

            ImageIcon img = Iconos.cargar(archivoIcono(nombre), Iconos.GRANDE);
            if (img != null) {
                icono.setIcon(img);
            }

            icono.addActionListener(e -> abrirApp(nombre, fabrica.get()));
            iconos.add(icono);
        }

        // FRAME_CONTENT_LAYER queda por detras de todas las ventanas internas.
        escritorio.add(iconos, JLayeredPane.FRAME_CONTENT_LAYER);
    }

    // ------------------------------------------------------------------
    //  Barra de tareas
    // ------------------------------------------------------------------

    private JComponent crearBarraTareas() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Estilo.BARRA_TAREAS);
        barra.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        barra.setPreferredSize(new Dimension(0, 40));

        JButton inicio = new JButton("Inicio");
        inicio.setFont(inicio.getFont().deriveFont(Font.BOLD));
        inicio.addActionListener(e -> {
            JPopupMenu menu = menuInicio();
            // Mostrar el menu justo encima del boton Inicio.
            menu.show(inicio, 0, -menu.getPreferredSize().height);
        });
        barra.add(inicio, BorderLayout.WEST);

        areaVentanas.setOpaque(false);
        barra.add(areaVentanas, BorderLayout.CENTER);

        JLabel reloj = new JLabel("", JLabel.CENTER);
        reloj.setForeground(Estilo.TEXTO_OSCURO);
        reloj.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)));
        Timer t = new Timer(1000, e -> reloj.setText(
                LocalTime.now().withNano(0) + "   " + LocalDate.now()));
        t.setInitialDelay(0);
        t.start();
        barra.add(reloj, BorderLayout.EAST);

        return barra;
    }

    private JPopupMenu menuInicio() {
        JPopupMenu menu = new JPopupMenu();

        for (Map.Entry<String, Supplier<JComponent>> app : aplicaciones().entrySet()) {
            String nombre = app.getKey();
            Supplier<JComponent> fabrica = app.getValue();
            JMenuItem item = menu.add(nombre);
            ImageIcon img = Iconos.cargar(archivoIcono(nombre), Iconos.PEQUENO);
            if (img != null) {
                item.setIcon(img);
            }
            item.addActionListener(e -> abrirApp(nombre, fabrica.get()));
        }

        menu.addSeparator();

        if (usuarioActual.getRol() == Rol.ADMINISTRADOR) {
            menu.add("Crear usuario").addActionListener(e -> ventana.mostrarRegistro());
        }

        menu.add("Cerrar sesion").addActionListener(e -> {
            int op = JOptionPane.showConfirmDialog(this, "Cerrar sesion?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (op == JOptionPane.YES_OPTION) {
                ventana.mostrarLogin();
            }
        });

        return menu;
    }

    // ------------------------------------------------------------------
    //  Abrir una aplicacion como ventana interna
    // ------------------------------------------------------------------

    private void abrirApp(String titulo, JComponent contenido) {
        ImageIcon icono = Iconos.cargar(archivoIcono(titulo), Iconos.PEQUENO);

        JInternalFrame frame = new JInternalFrame(titulo, true, true, true, true);
        frame.setContentPane(contenido);
        frame.setSize(600, 430);
        frame.setLocation(60 + cascada, 20 + cascada);
        cascada = (cascada + 28) % 170;
        if (icono != null) {
            frame.setFrameIcon(icono);
        }
        frame.setVisible(true);

        escritorio.add(frame);

        // Boton de esta ventana en la barra de tareas.
        JButton botonTarea = new JButton(titulo);
        if (icono != null) {
            botonTarea.setIcon(icono);
        }
        botonTarea.addActionListener(e -> traerAlFrente(frame));
        areaVentanas.add(botonTarea);
        areaVentanas.revalidate();
        areaVentanas.repaint();

        // Al cerrar la ventana, quitar su boton de la barra.
        frame.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                areaVentanas.remove(botonTarea);
                areaVentanas.revalidate();
                areaVentanas.repaint();
            }
        });

        traerAlFrente(frame);
    }

    private void traerAlFrente(JInternalFrame frame) {
        try {
            frame.setIcon(false);      // por si estaba minimizada
            frame.setSelected(true);
            frame.moveToFront();
        } catch (PropertyVetoException ignorado) {
            // la ventana no dejo cambiar el estado; no pasa nada
        }
    }

    // ------------------------------------------------------------------
    //  Wallpaper del escritorio
    // ------------------------------------------------------------------

    /**
     * Carga la imagen de fondo del escritorio. Si no la encuentra devuelve
     * {@code null} y el escritorio se queda con su color teal.
     */
    private static Image cargarWallpaper() {
        String recurso = "/miniwindows/recursos/Wallpaper/windows.jpg";

        // 1. Como recurso del classpath (al ejecutar desde el JAR o el IDE).
        URL url = PanelEscritorio.class.getResource(recurso);
        if (url != null) {
            return new ImageIcon(url).getImage();
        }

        // 2. Desde la carpeta del proyecto (al compilar a mano con javac, que
        //    no copia los recursos a out/).
        File archivo = new File("src" + recurso);
        if (archivo.exists()) {
            return new ImageIcon(archivo.getPath()).getImage();
        }

        return null;
    }
}
