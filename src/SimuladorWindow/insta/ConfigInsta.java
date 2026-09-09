package SimuladorWindow.insta;

import javax.swing.ImageIcon;
import java.awt.Dimension;
import java.awt.Image;
import java.io.File;

/**
 * Diseño responsive de INSTA+ (enunciado 4.6).
 *
 * El enunciado da tamaños recomendados para la "vista móvil" y dice que el
 * grid de publicaciones tiene 3 columnas. Aquí están esas constantes y un
 * ayudante que escala una imagen según su orientación (cuadrada, vertical u
 * horizontal), manteniendo la proporción de la vista que le toca.
 */
public final class ConfigInsta {

    private ConfigInsta() {
    }

    /** true = se simula la vista de celular. */
    public static final boolean MODO_MOBILE = true;

    /** Columnas del grid de publicaciones del perfil. */
    public static final int COLUMNAS_GRID = 3;

    // Tamaños recomendados por el enunciado (4.6), en px.
    public static final Dimension CUADRADA   = new Dimension(1080, 1080);
    public static final Dimension VERTICAL   = new Dimension(1080, 1350);
    public static final Dimension HORIZONTAL = new Dimension(1080, 566);

    /** Ancho al que se dibuja una imagen en una lista pequeña. */
    private static final int ANCHO_EN_PANTALLA = 260;

    /** Ancho de la imagen dentro de una publicacion del feed (como Instagram). */
    private static final int ANCHO_EN_FEED = 380;

    /** Imagen escalada al tamaño que corresponde a su orientacion (lista chica). */
    public static ImageIcon escalarParaVista(File imagen) {
        return escalar(imagen, ANCHO_EN_PANTALLA);
    }

    /** Imagen escalada para verse dentro de una publicacion del feed. */
    public static ImageIcon escalarParaFeed(File imagen) {
        return escalar(imagen, ANCHO_EN_FEED);
    }

    private static ImageIcon escalar(File imagen, int nuevoAncho) {
        if (imagen == null || !imagen.exists()) {
            return null;
        }
        ImageIcon original = new ImageIcon(imagen.getPath());
        int ancho = original.getIconWidth();
        int alto = original.getIconHeight();
        if (ancho <= 0 || alto <= 0) {
            return null;
        }

        Dimension vista = orientacion(ancho, alto);
        int nuevoAlto = nuevoAncho * vista.height / vista.width;

        Image escalada = original.getImage()
                .getScaledInstance(nuevoAncho, nuevoAlto, Image.SCALE_SMOOTH);
        return new ImageIcon(escalada);
    }

    /** Miniatura cuadrada para el grid de 3 columnas del perfil. */
    public static ImageIcon miniatura(File imagen, int lado) {
        if (imagen == null || !imagen.exists()) {
            return null;
        }
        Image escalada = new ImageIcon(imagen.getPath()).getImage()
                .getScaledInstance(lado, lado, Image.SCALE_SMOOTH);
        return new ImageIcon(escalada);
    }

    private static Dimension orientacion(int ancho, int alto) {
        double razon = (double) ancho / alto;
        if (razon > 1.3) {
            return HORIZONTAL;
        }
        if (razon < 0.9) {
            return VERTICAL;
        }
        return CUADRADA;
    }
}
