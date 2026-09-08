package SimuladorWindow.so;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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
        if (raiz == null) {
            throw new IllegalArgumentException("La raiz no puede ser null.");
        }
        this.raiz = raiz.getAbsoluteFile();
        if (!this.raiz.exists()) {
            this.raiz.mkdirs();
        }
        this.carpetaActual = this.raiz;
    }

    public File getCarpetaActual() {
        return carpetaActual;
    }

    public File getRaiz() { return raiz; }

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
        if (!nombreValido(nombre)) {
            return false;
        }
        File carpeta = resolver(nombre);
        if (carpeta.exists()) {
            return false;
        }
        return carpeta.mkdir();
    }
    /** rm: borra una carpeta o archivo (y su contenido) de la carpeta actual. */
    public boolean crearCarpetas(String ruta) {
        if (!nombreValido(ruta)) {
            return false;
        }
        File carpeta = resolver(ruta);
        if (carpeta.exists()) {
            return false;
        }
        return carpeta.mkdirs();
    }
    /** cd: entra a una subcarpeta. Devuelve "" si pudo, o un mensaje de error. */
    public String cambiar(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "Debe indicar una carpeta.";
        }
        if (".".equals(nombre)) {
            return "";
        }
        if ("..".equals(nombre)) {
            return subirNivel();
        }
        File destino = resolver(nombre);
        if (!destino.exists()) {
            return "No existe la carpeta: " + nombre;
        }
        if (!destino.isDirectory()) {
            return "No es una carpeta: " + nombre;
        }
        if (!estaDentroDeRaiz(destino)) {
            return "Acceso denegado.";
        }
        carpetaActual = destino.getAbsoluteFile();
        return "";
    }

    /** cd .. : sube un nivel, sin pasar de la carpeta raiz. */
    public String subirNivel() {
        if (carpetaActual.equals(raiz)) {
            return "Ya estas en la carpeta raiz.";
        }
        File padre = carpetaActual.getParentFile();
        if (padre == null || !estaDentroDeRaiz(padre)) {
            carpetaActual = raiz;
        } else {
            carpetaActual = padre;
        }
        return "";
    }

    /** dir: lista la carpeta actual como texto. */
    public String listarComoTexto() {
        StringBuilder sb = new StringBuilder();
        File[] archivos = listar();
        if (archivos.length == 0) {
            return "La carpeta esta vacia.\n";
        }
        for (File f : archivos) {
            if (f.isDirectory()) {
                sb.append("<DIR>   ");
            } else {
                sb.append("        ");
            }
            sb.append(f.getName());
            if (!f.isDirectory()) {
                sb.append("   ").append(f.length()).append(" bytes");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public boolean crearArchivo(String nombre) {
        if (!nombreValido(nombre)) {
            return false;
        }
        File archivo = resolver(nombre);
        if (archivo.exists()) {
            return false;
        }
        try {
            return archivo.createNewFile();
        } catch (IOException e) {
            return false;
        }
    }

    public boolean escribir(String nombre, String contenido) {
        File archivo = resolver(nombre);
        try {
            File parent = archivo.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            Files.write(archivo.toPath(), contenido.getBytes("UTF-8"));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean agregar(String nombre, String contenido) {
        File archivo = resolver(nombre);
        try {
            File parent = archivo.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(archivo, true);
            fos.write(contenido.getBytes("UTF-8"));
            fos.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public String leer(String nombre) {
        File archivo = resolver(nombre);
        if (!archivo.exists()) {
            return null;
        }
        if (!archivo.isFile()) {
            return null;
        }
        try {
            byte[] datos = Files.readAllBytes(archivo.toPath());
            return new String(datos, "UTF-8");
        } catch (IOException e) {
            return null;
        }
    }

    private boolean borrarRecursivo(File archivo) {
        if (archivo.isDirectory()) {
            File[] hijos = archivo.listFiles();
            if (hijos != null) {
                for (File hijo : hijos) {
                    if (!borrarRecursivo(hijo)) {
                        return false;
                    }
                }
            }
        }
        return archivo.delete();
    }

    public boolean eliminar(String nombre) {
        if (!nombreValido(nombre)) {
            return false;
        }
        File archivo = resolver(nombre);
        if (!archivo.exists()) {
            return false;
        }
        if (archivo.equals(raiz)) {
            return false;
        }
        return borrarRecursivo(archivo);
    }

    public boolean renombrar(String nombreActual, String nuevoNombre) {
        if (!nombreValido(nombreActual)
                || !nombreValido(nuevoNombre)) {
            return false;
        }
        File origen = resolver(nombreActual);
        File destino = new File(origen.getParentFile(), nuevoNombre);
        if (!origen.exists()) {
            return false;
        }
        if (destino.exists()) {
            return false;
        }
        return origen.renameTo(destino);
    }

    public boolean copiar(String origenNombre, String destinoNombre) {
        File origen = resolver(origenNombre);
        File destino = resolver(destinoNombre);
        if (!origen.exists()) {
            return false;
        }
        if (!origen.isFile()) {
            return false;
        }
        try {
            File parent = destino.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            Files.copy(origen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public String buscar(String nombre) {
        StringBuilder resultado = new StringBuilder();
        buscarRecursivo(carpetaActual, nombre.toLowerCase(), resultado);
        if (resultado.length() == 0) {
            return "No se encontraron resultados.";
        }
        return resultado.toString();
    }

    private void buscarRecursivo(File carpeta, String nombre, StringBuilder resultado) {
        File[] archivos = carpeta.listFiles();
        if (archivos == null) {
            return;
        }
        for (File archivo : archivos) {
            if (archivo.getName().toLowerCase().contains(nombre)) {
                resultado.append(archivo.getAbsolutePath()).append("\n");
            }
            if (archivo.isDirectory()) {
                buscarRecursivo(archivo, nombre, resultado);
            }
        }
    }

    public String tree() {
        StringBuilder resultado = new StringBuilder();
        resultado.append(carpetaActual.getName()).append("\n");
        construirTree(carpetaActual, "", resultado);
        return resultado.toString();
    }

    private void construirTree(File carpeta, String prefijo, StringBuilder resultado) {
        File[] hijos = carpeta.listFiles();
        if (hijos == null) {
            return;
        }
        for (int i = 0; i < hijos.length; i++) {
            File hijo = hijos[i];
            boolean ultimo = i == hijos.length - 1;
            resultado.append(prefijo);
            if (ultimo) {
                resultado.append("└── ");
            } else {
                resultado.append("├── ");
            }
            resultado.append(hijo.getName()).append("\n");
            if (hijo.isDirectory()) {
                construirTree(hijo, prefijo + (ultimo ? "    " : "│   "), resultado);
            }
        }
    }

    public String informacion(String nombre) {
        File archivo = resolver(nombre);
        if (!archivo.exists()) {
            return "No existe: " + nombre;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Nombre: ").append(archivo.getName()).append("\n");
        sb.append("Ruta: ").append(archivo.getAbsolutePath()).append("\n");
        sb.append("Tipo: ").append(archivo.isDirectory() ? "Carpeta" : "Archivo").append("\n");
        sb.append("Tamano: ").append(archivo.length()).append(" bytes").append("\n");
        sb.append("Oculto: ").append(archivo.isHidden()).append("\n");
        sb.append("Existe: ").append(archivo.exists()).append("\n");
        return sb.toString();
    }

    /** Ruta de la carpeta actual, para mostrarla como prompt. */
    public String rutaActual() {
        return carpetaActual.getAbsolutePath();
    }

    public String rutaRelativa() {
        String rutaRaiz = raiz.getAbsolutePath();
        String rutaActual = carpetaActual.getAbsolutePath();
        if (rutaActual.equals(rutaRaiz)) {
            return "\\";
        }
        if (rutaActual.startsWith(rutaRaiz)) {
            String relativa = rutaActual.substring(rutaRaiz.length());
            return relativa.replace(File.separatorChar, '\\');
        }
        return "\\";
    }

    public File resolver(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) {
            return carpetaActual;
        }
        ruta = ruta.trim();
        ruta = ruta.replace('\\', File.separatorChar);
        File resultado;
        if (ruta.startsWith(File.separator)) {
            resultado = new File(raiz, ruta.substring(1));
        } else {
            resultado = new File(carpetaActual, ruta);
        }
        try {
            resultado = resultado.getCanonicalFile();
        } catch (IOException e) {
            resultado = resultado.getAbsoluteFile();
        }
        return resultado;
    }

    public boolean estaDentroDeRaiz(File archivo) {
        try {
            File canonico = archivo.getCanonicalFile();
            File raizCanonica = raiz.getCanonicalFile();
            String rutaArchivo = canonico.getPath();
            String rutaRaiz = raizCanonica.getPath();
            return rutaArchivo.equals(rutaRaiz) || rutaArchivo.startsWith(rutaRaiz + File.separator);
        } catch (IOException e) {
            return false;
        }
    }

    private boolean nombreValido(String nombre) {
        if (nombre == null) {
            return false;
        }
        nombre = nombre.trim();
        if (nombre.isEmpty()) {
            return false;
        }
        File archivo = resolver(nombre);
        return estaDentroDeRaiz(archivo);
    }
}
