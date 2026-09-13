package SimuladorWindow.excepciones;

public class ArchivoCorruptoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ArchivoCorruptoException(String archivo, Throwable causa) {
        super("El archivo '" + archivo + "' no se pudo leer", causa);
    }
}
