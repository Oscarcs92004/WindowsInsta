package miniwindows.so;

import java.io.File;

/**
 * Sistema de archivos simulado sobre carpetas reales (IMPLEMENTACION.md 8).
 *
 * "raiz" es la carpeta tope: el usuario no puede salir de ahi.
 * "carpetaActual" es donde esta parado ahora mismo (la usa la consola).
 *
 * Esta clase ya esta completa; la usan la consola y el explorador.
 */
public class SistemaArchivos {

    private final File raiz;
    private File carpetaActual;

    public SistemaArchivos(File raiz) {
        this.raiz = raiz;
        this.carpetaActual = raiz;
    }

    public File getCarpetaActual() {
        return carpetaActual;
    }

    /** Archivos y carpetas dentro de la carpeta actual. */
    public File[] listar() {
        File[] hijos = carpetaActual.listFiles();
        if (hijos == null) {
            return new File[0];
        }
        return hijos;
    }

    /** mkdir: crea una carpeta dentro de la actual. */
    public boolean crearCarpeta(String nombre) {
        return new File(carpetaActual, nombre).mkdir();
    }

    /** rm: borra una carpeta o archivo (y su contenido) de la carpeta actual. */
    public boolean eliminar(String nombre) {
        return borrarRecursivo(new File(carpetaActual, nombre));
    }

    /** cd: entra a una subcarpeta. Devuelve "" si pudo, o un mensaje de error. */
    public String cambiar(String nombre) {
        File destino = new File(carpetaActual, nombre);
        if (destino.isDirectory()) {
            carpetaActual = destino;
            return "";
        }
        return "No existe la carpeta: " + nombre;
    }

    /** cd.. : sube un nivel, sin pasar de la carpeta raiz. */
    public String subirNivel() {
        if (carpetaActual.equals(raiz)) {
            return "Ya estas en la carpeta raiz";
        }
        carpetaActual = carpetaActual.getParentFile();
        return "";
    }

    /** dir: lista la carpeta actual como texto. */
    public String listarComoTexto() {
        StringBuilder sb = new StringBuilder();
        for (File f : listar()) {
            sb.append(f.isDirectory() ? "<DIR>   " : "        ");
            sb.append(f.getName());
            sb.append("\n");
        }
        return sb.toString();
    }

    /** Ruta de la carpeta actual, para mostrarla como prompt. */
    public String rutaActual() {
        return carpetaActual.getPath();
    }

    // -----------------------------------------------------------------

    private boolean borrarRecursivo(File f) {
        if (f.isDirectory()) {
            File[] hijos = f.listFiles();
            if (hijos != null) {
                for (File h : hijos) {
                    borrarRecursivo(h);
                }
            }
        }
        return f.delete();
    }
}
