package SimuladorWindow.insta;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Colores, fuentes y componentes con el aspecto de Instagram, en un solo
 * lugar, para que la app se vea parecida a Instagram real.
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

    public static final Font LOGO    = elegirFuente(30, Font.BOLD | Font.ITALIC,
            "Segoe Script", "Brush Script MT", "Lucida Handwriting", "Serif");
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
        g.setClip(new Ellipse2D.Float(0, 0, tam, tam));

        File foto = (rutaFoto == null) ? null : new File(rutaFoto);
        if (foto != null && foto.exists()) {
            Image img = new ImageIcon(rutaFoto).getImage();
            g.drawImage(img, 0, 0, tam, tam, null);
        } else {
            g.setColor(new Color(224, 224, 224));
            g.fillOval(0, 0, tam, tam);
            g.setColor(TEXTO_GRIS);
            g.setFont(new Font("SansSerif", Font.BOLD, tam / 2));
            String inicial = (username == null || username.isEmpty())
                    ? "?" : username.substring(0, 1).toUpperCase();
            FontMetrics fm = g.getFontMetrics();
            int x = (tam - fm.stringWidth(inicial)) / 2;
            int y = (tam - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(inicial, x, y);
        }
        g.setClip(null);
        g.setColor(BORDE);
        g.drawOval(0, 0, tam - 1, tam - 1);
        g.dispose();
        return new ImageIcon(salida);
    }
}
