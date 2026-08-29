package miniwindows.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.beans.PropertyVetoException;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
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

    private final JDesktopPane escritorio = new JDesktopPane();
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

        escritorio.setBackground(new Color(0, 120, 170));
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

    // ------------------------------------------------------------------
    //  Iconos del escritorio
    // ------------------------------------------------------------------

    private void crearIconosEscritorio() {
        JPanel iconos = new JPanel(new GridLayout(0, 1, 10, 10));
        iconos.setOpaque(false);
        iconos.setBounds(15, 15, 160, 260);

        for (Map.Entry<String, Supplier<JComponent>> app : aplicaciones().entrySet()) {
            String nombre = app.getKey();
            Supplier<JComponent> fabrica = app.getValue();

            JButton icono = new JButton("<html><center>" + nombre + "</center></html>");
            icono.setFocusPainted(false);
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
        barra.setBackground(new Color(45, 45, 48));
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
        reloj.setForeground(Color.WHITE);
        reloj.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
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
            menu.add(nombre).addActionListener(e -> abrirApp(nombre, fabrica.get()));
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
        JInternalFrame frame = new JInternalFrame(titulo, true, true, true, true);
        frame.setContentPane(contenido);
        frame.setSize(600, 430);
        frame.setLocation(60 + cascada, 20 + cascada);
        cascada = (cascada + 28) % 170;
        frame.setVisible(true);

        escritorio.add(frame);

        // Boton de esta ventana en la barra de tareas.
        JButton botonTarea = new JButton(titulo);
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
}
