package SimuladorWindow.ui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * La paleta, las fuentes y las piezas visuales de Mini-Windows en un solo lugar.
 *
 * El sistema imita el aspecto de Windows 98: superficies gris "hueso", escritorio
 * verde-azulado (teal), barras de titulo con degradado azul y letra pequena
 * (Tahoma, la de Windows 98).
 *
 * Todos los paneles deben usar estas constantes y estos metodos en vez de crear
 * sus propios {@code new Color(...)}, {@code new Font(...)} o bordes a mano. Asi,
 * si manana cambia el aspecto del sistema, se cambia aqui una vez y se ve en
 * todas las pantallas.
 *
 * Es una clase utilitaria: no se crea con {@code new}.
 */
public final class Estilo {

    private Estilo() {
        // Nadie debe instanciar esta clase.
    }

    // ------------------------------------------------------------------
    //  Colores (paleta clasica de Windows 98)
    // ------------------------------------------------------------------

    /** Fondo del escritorio: el teal clasico, por si no carga el wallpaper. */
    public static final Color FONDO_ESCRITORIO = new Color(0, 128, 128);

    /** Barra de tareas y superficies de ventana: el gris "hueso" de Windows 98. */
    public static final Color BARRA_TAREAS = new Color(212, 208, 200);

    /** Fondo de los paneles de las herramientas (ventanas internas). */
    public static final Color PANEL = new Color(212, 208, 200);

    /** Azul marino de las barras de titulo y las selecciones. */
    public static final Color ACENTO = new Color(0, 0, 128);

    /** Segundo azul del degradado de la barra de titulo activa (Windows 98 Plus!). */
    public static final Color ACENTO_CLARO = new Color(16, 132, 208);

    /** Texto sobre fondos oscuros (barra de titulo azul). */
    public static final Color TEXTO_CLARO = Color.WHITE;

    /** Texto sobre fondos grises (barra de tareas, paneles). */
    public static final Color TEXTO_OSCURO = Color.BLACK;

    /** Rojo para mensajes de error. */
    public static final Color ERROR = new Color(128, 0, 0);

    /** Brillo del relieve 3D (linea de arriba-izquierda). */
    public static final Color LUZ = Color.WHITE;

    /** Sombra media del relieve 3D. */
    public static final Color SOMBRA = new Color(128, 128, 128);

    /** Sombra dura del relieve 3D (linea exterior, casi negra). */
    public static final Color SOMBRA_OSCURA = new Color(64, 64, 64);

    // ------------------------------------------------------------------
    //  Fuentes (Tahoma, la de Windows 98; si no esta, MS Sans Serif)
    // ------------------------------------------------------------------

    /** Titulos grandes (nombre del sistema, encabezados de pantalla). */
    public static final Font TITULO = fuente(Font.BOLD, 18);

    /** Subtitulos, barras de titulo y encabezados de seccion. */
    public static final Font SUBTITULO = fuente(Font.BOLD, 12);

    /** Texto normal de la interfaz. */
    public static final Font NORMAL = fuente(Font.PLAIN, 11);

    /** Fuente de ancho fijo para la consola y el editor. */
    public static final Font MONOESPACIADA = monoespaciada(13);

    private static Font fuente(int estilo, int tam) {
        return primera(estilo, tam, "Tahoma", "MS Sans Serif", "Verdana", "SansSerif");
    }

    private static Font monoespaciada(int tam) {
        return primera(Font.PLAIN, tam, "Consolas", "Lucida Console", "Courier New", "Monospaced");
    }

    /** La primera familia de la lista que este instalada; SansSerif si ninguna. */
    private static Font primera(int estilo, int tam, String... familias) {
        Set<String> instaladas = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getAvailableFontFamilyNames()));
        for (String familia : familias) {
            if (instaladas.contains(familia)) {
                return new Font(familia, estilo, tam);
            }
        }
        return new Font("SansSerif", estilo, tam);
    }

    // ------------------------------------------------------------------
    //  Bordes con relieve 3D
    // ------------------------------------------------------------------

    /** Relieve saliente, como la cara de un boton de Windows 98. */
    public static Border relieve() {
        return BorderFactory.createBevelBorder(BevelBorder.RAISED,
                LUZ, BARRA_TAREAS, SOMBRA_OSCURA, SOMBRA);
    }

    /** Relieve hundido, como una caja de texto o un hueco. */
    public static Border hundido() {
        return BorderFactory.createBevelBorder(BevelBorder.LOWERED,
                SOMBRA, SOMBRA_OSCURA, LUZ, BARRA_TAREAS);
    }

    /** Borde exterior de un cuadro de dialogo (linea oscura + relieve). */
    public static Border ventana() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SOMBRA_OSCURA),
                relieve());
    }

    /** Recuadro con titulo, como los "group box" de Windows 98. */
    public static Border grupo(String titulo) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(LUZ, SOMBRA), titulo),
                BorderFactory.createEmptyBorder(2, 6, 4, 6));
    }

    /** Deja una caja de texto con el aspecto hundido y blanco de Windows 98. */
    public static void aplicarCampo(JTextComponent campo) {
        campo.setFont(NORMAL);
        campo.setBackground(Color.WHITE);
        campo.setBorder(BorderFactory.createCompoundBorder(
                hundido(),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)));
    }

    // ------------------------------------------------------------------
    //  Botones
    // ------------------------------------------------------------------

    /** Un boton gris con relieve y letra pequena, al estilo de Windows 98. */
    public static JButton boton(String texto) {
        JButton b = new JButton(texto);
        b.setFont(NORMAL);
        b.setForeground(TEXTO_OSCURO);
        b.setBackground(PANEL);
        b.setFocusPainted(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                relieve(),
                BorderFactory.createEmptyBorder(4, 14, 4, 14)));
        return b;
    }

    // ------------------------------------------------------------------
    //  Linea separadora con relieve
    // ------------------------------------------------------------------

    /** Una linea horizontal hundida (gris + blanco) para separar zonas. */
    public static JComponent separador() {
        return new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(1, 2);
            }

            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(SOMBRA);
                g.drawLine(0, 0, getWidth(), 0);
                g.setColor(LUZ);
                g.drawLine(0, 1, getWidth(), 1);
            }
        };
    }

    // ------------------------------------------------------------------
    //  Barra de titulo azul con degradado
    // ------------------------------------------------------------------

    public static JComponent barraTitulo(String texto) {
        return new BarraTitulo(texto, null);
    }

    public static JComponent barraTitulo(String texto, Icon icono) {
        return new BarraTitulo(texto, icono);
    }

    /** Pinta el degradado azul, el titulo y tres botoncitos decorativos. */
    private static final class BarraTitulo extends JPanel {

        private final String texto;
        private final Icon icono;

        BarraTitulo(String texto, Icon icono) {
            this.texto = texto;
            this.icono = icono;
            setPreferredSize(new Dimension(10, 22));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Degradado azul marino -> azul cielo.
            g2.setPaint(new GradientPaint(0, 0, ACENTO, w, 0, ACENTO_CLARO));
            g2.fillRect(0, 0, w, h);

            int x = 5;
            if (icono != null) {
                icono.paintIcon(this, g2, x, (h - icono.getIconHeight()) / 2);
                x += icono.getIconWidth() + 5;
            }

            g2.setFont(SUBTITULO);
            g2.setColor(TEXTO_CLARO);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(texto, x, (h - fm.getHeight()) / 2 + fm.getAscent());

            // Tres botoncitos a la derecha (solo decoracion).
            int s = 14;
            int top = (h - s) / 2;
            int bx = w - 4 - s;
            control(g2, bx, top, s, 'x');
            bx -= s + 2;
            control(g2, bx, top, s, 'O');
            bx -= s + 2;
            control(g2, bx, top, s, '_');

            g2.dispose();
        }

        private void control(Graphics2D g2, int x, int y, int s, char tipo) {
            g2.setColor(PANEL);
            g2.fillRect(x, y, s, s);
            g2.setColor(LUZ);
            g2.drawLine(x, y, x + s - 2, y);
            g2.drawLine(x, y, x, y + s - 2);
            g2.setColor(SOMBRA_OSCURA);
            g2.drawLine(x + s - 1, y, x + s - 1, y + s - 1);
            g2.drawLine(x, y + s - 1, x + s - 1, y + s - 1);

            g2.setColor(Color.BLACK);
            int m = 3;
            if (tipo == '_') {
                g2.drawLine(x + m, y + s - m - 1, x + s - m - 1, y + s - m - 1);
            } else if (tipo == 'O') {
                g2.drawRect(x + m, y + m, s - 2 * m - 1, s - 2 * m - 1);
            } else {
                g2.drawLine(x + m, y + m, x + s - m - 1, y + s - m - 1);
                g2.drawLine(x + s - m - 1, y + m, x + m, y + s - m - 1);
            }
        }
    }

    // ------------------------------------------------------------------
    //  Sombra suave para las "ventanas" que flotan sobre el escritorio
    // ------------------------------------------------------------------

    public static Border bordeSombra(int grosor) {
        return new BordeSombra(grosor);
    }

    private static final class BordeSombra extends AbstractBorder {

        private final int grosor;

        BordeSombra(int grosor) {
            this.grosor = grosor;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, grosor, grosor);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            for (int i = 0; i < grosor; i++) {
                int alfa = Math.max(6, 55 - (i * 45 / grosor));
                g2.setColor(new Color(0, 0, 0, alfa));
                g2.drawLine(x + grosor, y + h - grosor + i, x + w - 1, y + h - grosor + i);
                g2.drawLine(x + w - grosor + i, y + grosor, x + w - grosor + i, y + h - 1);
            }
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    //  Banderita de Windows (cuatro cuadros de colores, un poco inclinados)
    // ------------------------------------------------------------------

    public static ImageIcon iconoWindows(int tam) {
        BufferedImage img = new BufferedImage(tam, tam, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Ligera inclinacion, para que parezca una bandera ondeando.
        g2.shear(0, -0.14);
        g2.translate(0, tam * 0.14);

        int m = Math.max(1, tam / 12);
        int c = (tam - m) / 2;
        g2.setColor(new Color(235, 62, 45));   g2.fillRect(0, 0, c, c);
        g2.setColor(new Color(122, 190, 40));  g2.fillRect(c + m, 0, tam - c - m, c);
        g2.setColor(new Color(30, 130, 225));  g2.fillRect(0, c + m, c, tam - c - m);
        g2.setColor(new Color(250, 190, 30));  g2.fillRect(c + m, c + m, tam - c - m, tam - c - m);

        g2.dispose();
        return new ImageIcon(img);
    }

    // ------------------------------------------------------------------
    //  Look & Feel
    // ------------------------------------------------------------------

    /**
     * Aplica el aspecto clasico de Windows (el de Windows 98 / 2000) a toda la
     * aplicacion. Se llama una sola vez en Main, antes de crear cualquier
     * ventana.
     *
     * Si ese Look & Feel no esta disponible, prueba con el de Windows normal y,
     * como ultimo recurso, se queda con el que venga por defecto sin romper
     * nada.
     */
    public static void aplicarLookAndFeel() {
        String[] candidatos = {
            "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel",
            UIManager.getSystemLookAndFeelClassName(),
        };
        for (String clase : candidatos) {
            try {
                UIManager.setLookAndFeel(clase);
                break;
            } catch (Exception e) {
                // Probamos con el siguiente candidato.
            }
        }

        // Que los cuadros de dialogo (JOptionPane) usen la fuente pequena de
        // Windows 98. No tocamos "Label.font" ni "Button.font" globales para no
        // cambiar el aspecto de INSTA+, que imita a Instagram.
        UIManager.put("OptionPane.messageFont", NORMAL);
        UIManager.put("OptionPane.buttonFont", NORMAL);
    }
}
