package miniwindows.so;

import java.io.File;

import miniwindows.modelo.Rol;
import miniwindows.modelo.Usuario;

/**
 * Sabe donde vive cada usuario dentro del "disco Z:" simulado.
 *
 * - El disco Z: es la carpeta real  datos/Z
 * - Cada usuario tiene su carpeta   datos/Z/<username>
 * - El administrador ve todo el disco Z: (enunciado 3.2).
 */
public class Rutas {

    /** La raiz de todo el sistema de archivos simulado. */
    public static final File DISCO_Z = new File("datos/Z");

    /**
     * Carpeta que puede ver este usuario:
     * el disco entero si es administrador, o solo su carpeta si es estandar.
     */
    public static File carpetaRaizDe(Usuario u) {
        if (u.getRol() == Rol.ADMINISTRADOR) {
            return DISCO_Z;
        }
        return new File(DISCO_Z, u.getUsername());
    }

    /**
     * Crea, si no existen, la carpeta del usuario y sus 3 carpetas basicas
     * (enunciado 3.2). Se llama al iniciar sesion.
     */
    public static void asegurarCarpetasUsuario(Usuario u) {
        DISCO_Z.mkdirs();

        File carpeta = new File(DISCO_Z, u.getUsername());
        if (!carpeta.exists()) {
            carpeta.mkdirs();
            new File(carpeta, "Mis Documentos").mkdir();
            new File(carpeta, "Musica").mkdir();
            new File(carpeta, "Mis Imagenes").mkdir();
            // TODO (Alex): el enunciado escribe "Musica" y "Mis Imagenes" con
            //   acento. Al compilar hay que pasar  -encoding UTF-8  a javac
            //   (IntelliJ ya lo hace) y entonces se pueden poner los acentos.
        }
    }
}
