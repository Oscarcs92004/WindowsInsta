package SimuladorWindow.persistencia;

import SimuladorWindow.excepciones.ArchivoCorruptoException;

import java.io.*;

public class ArchivoBinario {

    public static synchronized void guardar(File archivo, Object dato) {
        File carpeta = archivo.getParentFile();
        if (carpeta != null) {
            carpeta.mkdirs();
        }

        try (ObjectOutputStream salida =
                     new ObjectOutputStream(new FileOutputStream(archivo))) {
            salida.writeObject(dato);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo escribir " + archivo, e);
        }
    }

    public static synchronized Object leer(File archivo) {
        try (ObjectInputStream entrada =
                     new ObjectInputStream(new FileInputStream(archivo))) {
            return entrada.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new ArchivoCorruptoException(archivo.getName(), e);
        }
    }
}
