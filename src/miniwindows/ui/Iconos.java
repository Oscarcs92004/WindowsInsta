package miniwindows.ui;

import java.awt.Image;
import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * Carga los iconos que estan en {@code src/miniwindows/recursos/Icon} y los
 * devuelve escalados al tamano que pida cada pantalla.
 *
 * Igual que el wallpaper: primero los busca como recurso del classpath (JAR o
 * IDE) y, si no estan, los lee de la carpeta {@code src/} (porque compilar a
 * mano con {@code javac} no copia los recursos a {@code out/}).
 *
 * Es una clase utilitaria: no se crea con {@code new}.
 */
public final class Iconos {

    private Iconos() {
        // Nadie debe instanciar esta clase.
    }

    /** Tamano de los iconos del escritorio. */
    public static final int GRANDE = 32;

    /** Tamano de los iconos del menu Inicio y la barra de tareas. */
    public static final int PEQUENO = 16;

    /**
     * Devuelve el icono {@code nombreArchivo} (por ejemplo {@code "file.png"})
     * escalado a {@code tam} pixeles, o {@code null} si no se encuentra.
     */
    public static ImageIcon cargar(String nombreArchivo, int tam) {
        Image img = imagen(nombreArchivo);
        if (img == null) {
            return null;
        }
        return new ImageIcon(img.getScaledInstance(tam, tam, Image.SCALE_SMOOTH));
    }

    /** La imagen original del icono, sin escalar, o {@code null}. */
    public static Image imagen(String nombreArchivo) {
        if (nombreArchivo == null) {
            return null;
        }
        String recurso = "/miniwindows/recursos/Icon/" + nombreArchivo;

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
