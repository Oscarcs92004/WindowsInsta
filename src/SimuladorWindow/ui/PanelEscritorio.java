package SimuladorWindow.ui;

import SimuladorWindow.insta.InstaServicio;
import SimuladorWindow.insta.PanelInstaApp;
import SimuladorWindow.modelo.Rol;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;
import SimuladorWindow.so.*;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

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
    private final UsuarioServicio servicio;
    private final InstaServicio insta;
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
    private final JPanel areaVentanas = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 1));

    /** Desplazamiento para que las ventanas nuevas no salgan una encima de otra. */
    private int cascada = 0;

    public PanelEscritorio(VentanaPrincipal ventana, UsuarioServicio servicio,
                           InstaServicio insta, Usuario usuarioActual) {
        this.ventana = ventana;
        this.servicio = servicio;
        this.insta = insta;
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
        apps.put("INSTA+",           () -> new PanelInstaApp(insta));
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
            case "INSTA+":            return "camera.png";
            default:                  return null;
        }
    }

    // ------------------------------------------------------------------
    //  Iconos del escritorio
    // ------------------------------------------------------------------

    private void crearIconosEscritorio() {
        JPanel iconos = new JPanel(new GridLayout(0, 1, 0, 6));
        iconos.setOpaque(false);
        iconos.setBounds(14, 14, 118, 470);

        for (Map.Entry<String, Supplier<JComponent>> app : aplicaciones().entrySet()) {
            iconos.add(iconoEscritorio(app.getKey(), app.getValue()));
        }

        // FRAME_CONTENT_LAYER queda por detras de todas las ventanas internas.
        escritorio.add(iconos, JLayeredPane.FRAME_CONTENT_LAYER);
    }

    /**
     * Un icono del escritorio: la imagen arriba y el nombre debajo sobre una
     * placa oscura semitransparente, para que el texto se lea sobre cualquier
     * fondo de pantalla. Se abre con un clic.
     */
    private JComponent iconoEscritorio(String nombre, Supplier<JComponent> fabrica) {
        JLabel imagen = new JLabel();
        imagen.setAlignmentX(Component.CENTER_ALIGNMENT);
        ImageIcon img = Iconos.cargar(archivoIcono(nombre), Iconos.GRANDE);
        if (img != null) {
            imagen.setIcon(img);
        }

        JLabel texto = new JLabel(nombre, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 90));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        texto.setFont(Estilo.NORMAL);
        texto.setForeground(Estilo.TEXTO_CLARO);
        texto.setAlignmentX(Component.CENTER_ALIGNMENT);
        texto.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));

        JPanel celda = new JPanel();
        celda.setOpaque(false);
        celda.setLayout(new BoxLayout(celda, BoxLayout.Y_AXIS));
        celda.add(imagen);
        celda.add(Box.createVerticalStrut(3));
        celda.add(texto);
        celda.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        celda.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                abrirApp(nombre, fabrica.get());
            }
        });
        return celda;
    }

    // ------------------------------------------------------------------
    //  Barra de tareas
    // ------------------------------------------------------------------

    private JComponent crearBarraTareas() {
        JPanel barra = new JPanel(new BorderLayout(4, 0));
        barra.setBackground(Estilo.BARRA_TAREAS);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.LUZ),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        barra.setPreferredSize(new Dimension(0, 32));

        // Boton "Inicio" con la banderita de Windows.
        JButton inicio = new JButton("Inicio", Estilo.iconoWindows(16));
        inicio.setFont(Estilo.SUBTITULO);
        inicio.setIconTextGap(5);
        inicio.setFocusPainted(false);
        inicio.setBackground(Estilo.BARRA_TAREAS);
        inicio.setBorder(BorderFactory.createCompoundBorder(
                Estilo.relieve(), BorderFactory.createEmptyBorder(2, 8, 2, 12)));
        inicio.addActionListener(e -> {
            JPopupMenu menu = menuInicio();
            // Mostrar el menu justo encima del boton Inicio.
            menu.show(inicio, 0, -menu.getPreferredSize().height);
        });

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        izquierda.setOpaque(false);
        izquierda.add(inicio);
        izquierda.add(separadorVertical());
        barra.add(izquierda, BorderLayout.WEST);

        areaVentanas.setOpaque(false);
        barra.add(areaVentanas, BorderLayout.CENTER);

        JLabel reloj = new JLabel(" ", JLabel.CENTER);
        reloj.setFont(Estilo.NORMAL);
        reloj.setForeground(Estilo.TEXTO_OSCURO);
        reloj.setBorder(BorderFactory.createCompoundBorder(
                Estilo.hundido(),
                BorderFactory.createEmptyBorder(2, 10, 2, 10)));
        Runnable ponerHora = () -> {
            reloj.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            reloj.setToolTipText(LocalDate.now().toString());
        };
        ponerHora.run();
        Timer t = new Timer(1000, e -> ponerHora.run());
        t.setInitialDelay(0);
        t.start();

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        derecha.setOpaque(false);
        derecha.add(separadorVertical());
        derecha.add(reloj);
        barra.add(derecha, BorderLayout.EAST);

        return barra;
    }

    /** Una rayita vertical hundida, como las de la barra de tareas de Windows 98. */
    private JComponent separadorVertical() {
        return new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(4, 22);
            }

            @Override
            protected void paintComponent(Graphics g) {
                int h = getHeight();
                g.setColor(Estilo.SOMBRA);
                g.drawLine(1, 2, 1, h - 3);
                g.setColor(Estilo.LUZ);
                g.drawLine(2, 2, 2, h - 3);
            }
        };
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
        if ("INSTA+".equals(titulo)) {
            frame.setSize(404, 780);        // formato de telefono (vertical)
        } else {
            frame.setSize(600, 430);
        }
        frame.setLocation(60 + cascada, 20 + cascada);
        cascada = (cascada + 28) % 170;
        if (icono != null) {
            frame.setFrameIcon(icono);
        }
        frame.setVisible(true);

        escritorio.add(frame);

        // Boton de esta ventana en la barra de tareas.
        JButton botonTarea = new JButton(titulo);
        botonTarea.setFont(Estilo.NORMAL);
        botonTarea.setBackground(Estilo.BARRA_TAREAS);
        botonTarea.setFocusPainted(false);
        botonTarea.setHorizontalAlignment(SwingConstants.LEFT);
        botonTarea.setPreferredSize(new Dimension(150, 24));
        botonTarea.setBorder(BorderFactory.createCompoundBorder(
                Estilo.relieve(), BorderFactory.createEmptyBorder(2, 6, 2, 6)));
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
        String recurso = "/SimuladorWindow/recursos/Wallpaper/windows.jpg";

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
