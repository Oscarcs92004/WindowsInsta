package SimuladorWindow.insta;

import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/**
 * Los iconos de la barra inferior de INSTA+, dibujados a mano (linea simple)
 * para que se parezcan a los de la app de Instagram en el telefono:
 * casa, lupa, "+", corazon y persona.
 *
 * Es una clase de solo metodos estaticos.
 */
public final class IconosInsta {

    private IconosInsta() {
    }

    public static final String CASA    = "casa";
    public static final String LUPA    = "lupa";
    public static final String MAS     = "mas";
    public static final String CORAZON = "corazon";
    public static final String PERSONA = "persona";
    public static final String MENSAJE = "mensaje";
    public static final String AVION   = "avion";     // compartir (enviar)
    public static final String GUARDAR = "guardar";   // marcador
    public static final String OPCIONES = "opciones"; // los tres puntos "..."

    /** Icono de {@code tam} px. {@code activo} lo dibuja relleno / mas grueso. */
    public static ImageIcon icono(String nombre, int tam, boolean activo) {
        BufferedImage img = new BufferedImage(tam, tam, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(activo ? EstiloInsta.TEXTO : EstiloInsta.TEXTO_GRIS);
        g.setStroke(new BasicStroke(activo ? 2.4f : 1.8f,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        double p = tam * 0.18;                 // margen
        double s = tam - 2 * p;                // lado util

        switch (nombre) {
            case CASA: {
                Path2D t = new Path2D.Double();
                t.moveTo(p, p + s * 0.45);
                t.lineTo(p + s / 2, p);
                t.lineTo(p + s, p + s * 0.45);
                g.draw(t);
                g.draw(new java.awt.geom.Rectangle2D.Double(
                        p + s * 0.12, p + s * 0.42, s * 0.76, s * 0.58));
                break;
            }
            case LUPA: {
                double d = s * 0.68;
                g.draw(new java.awt.geom.Ellipse2D.Double(p, p, d, d));
                g.draw(new java.awt.geom.Line2D.Double(p + d * 0.75, p + d * 0.75, p + s, p + s));
                break;
            }
            case MAS: {
                g.draw(new java.awt.geom.RoundRectangle2D.Double(p, p, s, s, s * 0.3, s * 0.3));
                g.draw(new java.awt.geom.Line2D.Double(p + s / 2, p + s * 0.25, p + s / 2, p + s * 0.75));
                g.draw(new java.awt.geom.Line2D.Double(p + s * 0.25, p + s / 2, p + s * 0.75, p + s / 2));
                break;
            }
            case CORAZON: {
                Path2D h = corazon(p, p + s * 0.08, s, s * 0.85);
                if (activo) {
                    g.setColor(EstiloInsta.ROJO);
                    g.fill(h);
                } else {
                    g.draw(h);
                }
                break;
            }
            case PERSONA: {
                double d = s * 0.42;
                g.draw(new java.awt.geom.Ellipse2D.Double(p + (s - d) / 2, p, d, d));
                g.draw(new java.awt.geom.Arc2D.Double(p, p + s * 0.45, s, s, 20, 140, java.awt.geom.Arc2D.OPEN));
                break;
            }
            case MENSAJE: {
                g.draw(new java.awt.geom.RoundRectangle2D.Double(p, p + s * 0.1, s, s * 0.7, s * 0.25, s * 0.25));
                Path2D tail = new Path2D.Double();
                tail.moveTo(p + s * 0.25, p + s * 0.8);
                tail.lineTo(p + s * 0.25, p + s);
                tail.lineTo(p + s * 0.5, p + s * 0.8);
                g.draw(tail);
                break;
            }
            case AVION: {
                // Avion de papel (el boton "compartir" de Instagram).
                Path2D av = new Path2D.Double();
                av.moveTo(p + s, p);
                av.lineTo(p, p + s * 0.45);
                av.lineTo(p + s * 0.45, p + s * 0.58);
                av.lineTo(p + s * 0.62, p + s);
                av.closePath();
                g.draw(av);
                g.draw(new java.awt.geom.Line2D.Double(
                        p + s, p, p + s * 0.45, p + s * 0.58));
                break;
            }
            case GUARDAR: {
                // Marcador (bookmark).
                Path2D bm = new Path2D.Double();
                bm.moveTo(p + s * 0.2, p);
                bm.lineTo(p + s * 0.8, p);
                bm.lineTo(p + s * 0.8, p + s);
                bm.lineTo(p + s * 0.5, p + s * 0.72);
                bm.lineTo(p + s * 0.2, p + s);
                bm.closePath();
                if (activo) {
                    g.fill(bm);
                } else {
                    g.draw(bm);
                }
                break;
            }
            case OPCIONES: {
                double d = Math.max(1.6, tam * 0.09);
                for (int i = 0; i < 3; i++) {
                    double cx = p + s * (0.5) + (i - 1) * s * 0.32;
                    g.fill(new java.awt.geom.Ellipse2D.Double(
                            cx - d / 2, p + s / 2 - d / 2, d, d));
                }
                break;
            }
            case "grid": {
                // Cuadrícula de 3x3, como la pestaña de publicaciones del perfil.
                double celda = s / 3;
                for (int fx = 0; fx < 3; fx++) {
                    for (int fy = 0; fy < 3; fy++) {
                        g.draw(new java.awt.geom.Rectangle2D.Double(
                                p + fx * celda, p + fy * celda, celda, celda));
                    }
                }
                break;
            }
            default:
                break;
        }
        g.dispose();
        return new ImageIcon(img);
    }

    private static Path2D corazon(double x, double y, double w, double h) {
        Path2D c = new Path2D.Double();
        c.moveTo(x + w / 2, y + h);
        c.curveTo(x - w * 0.1, y + h * 0.55, x + w * 0.1, y - h * 0.05, x + w / 2, y + h * 0.3);
        c.curveTo(x + w * 0.9, y - h * 0.05, x + w * 1.1, y + h * 0.55, x + w / 2, y + h);
        c.closePath();
        return c;
    }
}
