package miniwindows.persistencia;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;

import miniwindows.excepciones.ArchivoCorruptoException;

/**
 * Ayudante para guardar y leer objetos en archivos binarios.
 *
 * Toda clase que maneje un archivo .sop o .ins pasa por aqui, asi el codigo
 * de "abrir el stream, escribir o leer, cerrar" esta escrito UNA sola vez
 * (el checklist del enunciado pide utilidades de E/S centralizadas).
 *
 * "Binario" significa que se guardan los bytes tal cual los tiene el
 * programa, no como texto legible. Java lo hace con la serializacion:
 * writeObject(...) toma un objeto entero y lo escribe; readObject() lo
 * vuelve a armar al leer.
 */
public class ArchivoBinario {

    /**
     * Guarda un objeto en el archivo, pisando lo que hubiera antes.
     * Si la carpeta del archivo no existe, la crea.
     *
     * synchronized = solo un hilo a la vez puede estar dentro de este
     * metodo, para que dos hilos no escriban el mismo archivo a la vez
     * y lo dejen a medias.
     */
    public static synchronized void guardar(File archivo, Object dato) {
        File carpeta = archivo.getParentFile();
        if (carpeta != null) {
            carpeta.mkdirs();
        }

        // try-with-resources: el stream se cierra solo al salir del try,
        // aunque ocurra un error.
        try (ObjectOutputStream salida =
                     new ObjectOutputStream(new FileOutputStream(archivo))) {
            salida.writeObject(dato);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo escribir " + archivo, e);
        }
    }

    /**
     * Lee y devuelve el objeto que estaba guardado en el archivo.
     * Si el archivo esta danado o cambio de formato, lanza
     * ArchivoCorruptoException.
     */
    public static synchronized Object leer(File archivo) {
        try (ObjectInputStream entrada =
                     new ObjectInputStream(new FileInputStream(archivo))) {
            return entrada.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new ArchivoCorruptoException(archivo.getName(), e);
        }
    }
}
