package miniwindows.excepciones;

/**
 * Se lanza cuando un archivo binario no se puede leer (esta danado o cambio
 * de formato). Es "no comprobada" (extends RuntimeException) porque el usuario
 * no puede hacer nada: se muestra un mensaje y se corta esa operacion.
 */
public class ArchivoCorruptoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ArchivoCorruptoException(String archivo, Throwable causa) {
        super("El archivo '" + archivo + "' no se pudo leer", causa);
    }
}
