package SimuladorWindow.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;


public final class Iconos {

    private Iconos() {
    }

    public static final int GRANDE = 32;

    public static final int PEQUENO = 16;

    public static ImageIcon cargar(String nombreArchivo, int tam) {
        Image img = imagen(nombreArchivo);
        if (img == null) {
            return null;
        }
        return new ImageIcon(img.getScaledInstance(tam, tam, Image.SCALE_SMOOTH));
    }

    public static Image imagen(String nombreArchivo) {
        if (nombreArchivo == null) {
            return null;
        }
        String recurso = "/SimuladorWindow/recursos/Icon/" + nombreArchivo;

        URL url = Iconos.class.getResource(recurso);
        if (url != null) {
            return new ImageIcon(url).getImage();
        }

        File archivo = new File("src" + recurso);
        if (archivo.exists()) {
            return new ImageIcon(archivo.getPath()).getImage();
        }

        return null;
    }
}
