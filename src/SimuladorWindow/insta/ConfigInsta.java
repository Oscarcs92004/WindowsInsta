package SimuladorWindow.insta;

import javax.swing.ImageIcon;
import java.awt.Dimension;
import java.awt.Image;
import java.io.File;

public final class ConfigInsta {

    private ConfigInsta() {
    }

    public static final boolean MODO_MOBILE = true;

    public static final int COLUMNAS_GRID = 3;

    public static final Dimension CUADRADA   = new Dimension(1080, 1080);
    public static final Dimension VERTICAL   = new Dimension(1080, 1350);
    public static final Dimension HORIZONTAL = new Dimension(1080, 566);

    private static final int ANCHO_EN_PANTALLA = 260;

    private static final int ANCHO_EN_FEED = 392;

    public static ImageIcon escalarParaVista(File imagen) {
        return escalar(imagen, ANCHO_EN_PANTALLA);
    }

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
