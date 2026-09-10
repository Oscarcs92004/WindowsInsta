package SimuladorWindow.insta;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Colores, fuentes y componentes con el aspecto de Instagram, en un solo
 * lugar, para que INSTA+ se vea parecido a la app real de Instagram.
 *
 * Es una clase de solo metodos estaticos.
 */
public final class EstiloInsta {

    private EstiloInsta() {
    }

    // ------------------------------------------------------------------
    //  Paleta de Instagram
    // ------------------------------------------------------------------

    public static final Color FONDO       = new Color(250, 250, 250);  // #FAFAFA
    public static final Color BLANCO      = Color.WHITE;
    public static final Color BORDE       = new Color(219, 219, 219);  // #DBDBDB
    public static final Color TEXTO       = new Color(38, 38, 38);     // #262626
    public static final Color TEXTO_GRIS  = new Color(142, 142, 142);  // #8E8E8E
    public static final Color AZUL        = new Color(0, 149, 246);    // #0095F6
    public static final Color AZUL_OSCURO = new Color(0, 55, 107);
    public static final Color ROJO        = new Color(237, 73, 86);

    /**
     * El degradado del anillo de las historias de Instagram (amarillo ->
     * naranja -> rosa -> morado -> azul), en diagonal.
     */
    private static final float[] PARADAS_ANILLO = {0f, 0.25f, 0.5f, 0.75f, 1f};
    private static final Color[] COLORES_ANILLO = {
            new Color(0xFE, 0xDA, 0x75), new Color(0xFA, 0x7E, 0x1E),
            new Color(0xD6, 0x29, 0x76), new Color(0x96, 0x2F, 0xBF),
            new Color(0x4F, 0x5B, 0xD5),
    };

    // La fuente del logo: "Brush Script MT" (sin negrita ni cursiva forzada)
    // es la mas parecida a la letra manuscrita del logo real de Instagram.
    public static final Font LOGO    = elegirFuente(30, Font.PLAIN,
            "Brush Script MT", "Segoe Script", "Lucida Handwriting", "Serif");
    public static final Font TITULO  = elegirFuente(22, Font.BOLD, "Segoe UI", "SansSerif");
    public static final Font FUERTE  = elegirFuente(13, Font.BOLD, "Segoe UI", "SansSerif");
    public static final Font NORMAL  = elegirFuente(13, Font.PLAIN, "Segoe UI", "SansSerif");
    public static final Font CHICA   = elegirFuente(11, Font.PLAIN, "Segoe UI", "SansSerif");

    private static Font elegirFuente(int tam, int estilo, String... nombres) {
        String[] disponibles = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        for (String nombre : nombres) {
            for (String d : disponibles) {
                if (d.equalsIgnoreCase(nombre)) {
                    return new Font(nombre, estilo, tam);
                }
            }
        }
        return new Font("SansSerif", estilo, tam);
    }

    // ------------------------------------------------------------------
    //  Botones
    // ------------------------------------------------------------------

    /** Boton azul relleno, como el "Log in" o "Seguir" de Instagram. */
    public static JButton botonPrimario(String texto) {
        return botonRedondo(texto, AZUL, BLANCO, AZUL);
    }

    /** Boton blanco con borde, como "Dejar de seguir" o "Editar perfil". */
    public static JButton botonSecundario(String texto) {
        return botonRedondo(texto, BLANCO, TEXTO, BORDE);
    }

    /** Texto en azul sin recuadro, como un enlace. */
    public static JButton enlace(String texto) {
        JButton b = new JButton(texto);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setForeground(AZUL);
        b.setFont(FUERTE);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JButton botonRedondo(String texto, Color fondo, Color texeto, Color borde) {
        JButton b = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? mezclar(fondo, Color.BLACK, 0.08f) : fondo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (!borde.equals(fondo)) {
                    g2.setColor(borde);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setForeground(texeto);
        b.setFont(FUERTE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static Color mezclar(Color a, Color b, float t) {
        return new Color(
                (int) (a.getRed()   * (1 - t) + b.getRed()   * t),
                (int) (a.getGreen() * (1 - t) + b.getGreen() * t),
                (int) (a.getBlue()  * (1 - t) + b.getBlue()  * t));
    }

    // ------------------------------------------------------------------
    //  Campos de texto
    // ------------------------------------------------------------------

    public static void estiloCampo(JTextComponent campo) {
        campo.setBackground(FONDO);
        campo.setForeground(TEXTO);
        campo.setFont(NORMAL);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(9, 10, 9, 10)));
    }

    /**
     * Texto gris de ayuda dentro del campo (como los "placeholder" de las
     * webs) que se queda hasta que el usuario escribe algo.
     * Solo para JTextField/JPasswordField.
     */
    public static void placeholder(JTextField campo, String ayuda) {
        final char eco = (campo instanceof JPasswordField)
                ? ((JPasswordField) campo).getEchoChar() : 0;

        Runnable poner = () -> {
            campo.putClientProperty("placeholder", Boolean.TRUE);
            campo.setText(ayuda);
            campo.setForeground(TEXTO_GRIS);
            if (campo instanceof JPasswordField) {
                ((JPasswordField) campo).setEchoChar((char) 0);
            }
        };
        Runnable quitar = () -> {
            if (Boolean.TRUE.equals(campo.getClientProperty("placeholder"))) {
                campo.putClientProperty("placeholder", Boolean.FALSE);
                campo.setText("");
                campo.setForeground(TEXTO);
                if (campo instanceof JPasswordField && eco != 0) {
                    ((JPasswordField) campo).setEchoChar(eco);
                }
            }
        };

        poner.run();

        campo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                quitar.run();
            }
        });
        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (valorReal(campo).isEmpty()) {
                    poner.run();
                }
            }
        });
    }

    /** El texto real del campo, o "" si solo tiene el placeholder. */
    public static String valorReal(JTextField campo) {
        if (Boolean.TRUE.equals(campo.getClientProperty("placeholder"))) {
            return "";
        }
        return campo.getText();
    }

    // ------------------------------------------------------------------
    //  Tarjeta blanca con borde (una "publicacion", un recuadro de login)
    // ------------------------------------------------------------------

    public static JPanel tarjeta() {
        JPanel p = new JPanel();
        p.setBackground(BLANCO);
        p.setBorder(BorderFactory.createLineBorder(BORDE, 1, true));
        return p;
    }

    public static Border margen(int arriba, int izq, int abajo, int der) {
        return BorderFactory.createEmptyBorder(arriba, izq, abajo, der);
    }

    /**
     * Un panel con las esquinas redondeadas y un color de fondo, para las
     * burbujas del chat del Inbox.
     */
    public static JPanel burbuja(Color fondo) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fondo);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(margen(7, 12, 7, 12));
        return p;
    }

    /**
     * Una etiqueta de texto que corta de línea a lo ancho indicado. Swing
     * ignora el {@code width} de CSS en un JLabel, pero sí respeta el ancho de
     * una celda de tabla, así que el texto se mete dentro de una.
     *
     * {@code contenidoHtml} puede llevar {@code <b>}, {@code <font color>}, etc.
     */
    public static JLabel textoHtml(String contenidoHtml, int ancho) {
        JLabel l = new JLabel("<html><table cellpadding='0' cellspacing='0'><tr><td width='"
                + ancho + "'>" + contenidoHtml + "</td></tr></table></html>");
        l.setFont(NORMAL);
        l.setForeground(TEXTO);
        return l;
    }

    /** Escapa los caracteres que romperían el HTML de una etiqueta. */
    public static String escaparHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Pinta de azul los #hashtags y las @menciones, como Instagram. */
    public static String resaltarTags(String textoYaEscapado) {
        StringBuilder sb = new StringBuilder();
        for (String palabra : textoYaEscapado.split(" ")) {
            if (palabra.startsWith("#") || palabra.startsWith("@")) {
                sb.append("<font color='#0095F6'>").append(palabra).append("</font>");
            } else {
                sb.append(palabra);
            }
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    // ------------------------------------------------------------------
    //  Icono de la app para el escritorio de Windows 98
    // ------------------------------------------------------------------

    /**
     * Un cuadrado redondeado con el degradado de Instagram (amarillo -> rosa ->
     * morado) y el dibujo de una camara en blanco, como el icono real de la app.
     */
    public static ImageIcon iconoApp(int tam) {
        BufferedImage img = new BufferedImage(tam, tam, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int r = Math.max(4, tam / 4);
        g.setPaint(new GradientPaint(0, tam, new Color(254, 218, 117),
                tam, 0, new Color(129, 52, 175)));
        g.fillRoundRect(0, 0, tam - 1, tam - 1, r, r);

        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(Math.max(1.5f, tam / 12f)));
        int m = tam / 5;
        g.drawRoundRect(m, m, tam - 2 * m, tam - 2 * m, tam / 5, tam / 5);
        int c = tam / 4;
        g.drawOval((tam - c) / 2, (tam - c) / 2, c, c);
        int p = Math.max(2, tam / 12);
        g.fillOval(tam - m - p - 1, m + 2, p, p);

        g.dispose();
        return new ImageIcon(img);
    }

    // ------------------------------------------------------------------
    //  Avatar circular
    // ------------------------------------------------------------------

    /**
     * Devuelve la foto de perfil recortada en circulo. Si no hay foto, dibuja
     * un circulo gris con la inicial del username.
     */
    public static ImageIcon avatar(String rutaFoto, String username, int tam) {
        BufferedImage salida = new BufferedImage(tam, tam, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = salida.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        dibujarCirculoAvatar(g, rutaFoto, username, 0, 0, tam);
        g.setColor(BORDE);
        g.drawOval(0, 0, tam - 1, tam - 1);
        g.dispose();
        return new ImageIcon(salida);
    }

    /**
     * El mismo avatar pero rodeado del anillo con el degradado de las historias
     * de Instagram (amarillo -> rosa -> morado). Se usa en la fila de historias
     * y en la cabecera del feed.
     */
    public static ImageIcon avatarAnillo(String rutaFoto, String username, int tam) {
        BufferedImage salida = new BufferedImage(tam, tam, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = salida.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int grosorAnillo = Math.max(2, tam / 16);
        int hueco = Math.max(1, tam / 22);           // el aro blanco entre anillo y foto

        // Anillo: circulo relleno con el degradado en diagonal.
        g.setPaint(new LinearGradientPaint(0, tam, tam, 0, PARADAS_ANILLO, COLORES_ANILLO));
        g.fillOval(0, 0, tam, tam);

        // Hueco blanco.
        int d2 = tam - 2 * grosorAnillo;
        g.setColor(BLANCO);
        g.fillOval(grosorAnillo, grosorAnillo, d2, d2);

        // La foto, mas adentro.
        int off = grosorAnillo + hueco;
        int dFoto = tam - 2 * off;
        dibujarCirculoAvatar(g, rutaFoto, username, off, off, dFoto);

        g.dispose();
        return new ImageIcon(salida);
    }

    /**
     * El avatar con un círculo azul con un "+" abajo a la derecha, como el
     * "Tu historia" de Instagram.
     */
    public static ImageIcon avatarMasBadge(String rutaFoto, String username, int tam) {
        BufferedImage salida = new BufferedImage(tam, tam, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = salida.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int d = (int) (tam * 0.82);
        dibujarCirculoAvatar(g, rutaFoto, username, (tam - d) / 2, 0, d);
        g.setColor(BORDE);
        g.drawOval((tam - d) / 2, 0, d - 1, d - 1);

        int badge = tam / 3;
        int bx = tam - badge;
        int by = tam - badge;
        g.setColor(BLANCO);
        g.fillOval(bx - 2, by - 2, badge + 4, badge + 4);
        g.setColor(AZUL);
        g.fillOval(bx, by, badge, badge);
        g.setColor(BLANCO);
        g.setStroke(new BasicStroke(Math.max(1.4f, tam / 22f)));
        g.drawLine(bx + badge / 2, by + badge / 4, bx + badge / 2, by + badge * 3 / 4);
        g.drawLine(bx + badge / 4, by + badge / 2, bx + badge * 3 / 4, by + badge / 2);

        g.dispose();
        return new ImageIcon(salida);
    }

    /** Dibuja la foto (o el circulo gris con la inicial) recortada en circulo. */
    private static void dibujarCirculoAvatar(Graphics2D g, String rutaFoto,
                                             String username, int x, int y, int tam) {
        Shape antes = g.getClip();
        g.setClip(new Ellipse2D.Float(x, y, tam, tam));

        File foto = (rutaFoto == null) ? null : new File(rutaFoto);
        if (foto != null && foto.exists()) {
            g.drawImage(new ImageIcon(rutaFoto).getImage(), x, y, tam, tam, null);
        } else {
            g.setColor(new Color(224, 224, 224));
            g.fillOval(x, y, tam, tam);
            g.setColor(TEXTO_GRIS);
            g.setFont(new Font("SansSerif", Font.BOLD, tam / 2));
            String inicial = (username == null || username.isEmpty())
                    ? "?" : username.substring(0, 1).toUpperCase();
            FontMetrics fm = g.getFontMetrics();
            g.drawString(inicial, x + (tam - fm.stringWidth(inicial)) / 2,
                    y + (tam - fm.getHeight()) / 2 + fm.getAscent());
        }
        g.setClip(antes);
    }

    // ------------------------------------------------------------------
    //  El logotipo "Instagram" como etiqueta
    // ------------------------------------------------------------------

    public static JLabel wordmark(float tam) {
        JLabel l = new JLabel("Instagram");
        l.setFont(LOGO.deriveFont(tam));
        l.setForeground(TEXTO);
        return l;
    }

    // ------------------------------------------------------------------
    //  Barra de desplazamiento fina (para que no se vea la gruesa de
    //  Windows 98 dentro de INSTA+)
    // ------------------------------------------------------------------

    public static void scrollFino(JScrollPane scroll) {
        scroll.getVerticalScrollBar().setUI(new BarraFina());
        scroll.getHorizontalScrollBar().setUI(new BarraFina());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
        scroll.getVerticalScrollBar().setUnitIncrement(18);
    }

    /** Un pulgar gris redondeado, sin flechas ni fondo, como en las apps de movil. */
    private static final class BarraFina extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(193, 193, 193);
        }

        @Override
        protected JButton createDecreaseButton(int o) { return botonCero(); }

        @Override
        protected JButton createIncreaseButton(int o) { return botonCero(); }

        private JButton botonCero() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            // sin pista: transparente
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            int m = 2;
            g2.fill(new RoundRectangle2D.Float(r.x + m, r.y + m,
                    r.width - 2 * m, r.height - 2 * m, 8, 8));
            g2.dispose();
        }
    }
}
