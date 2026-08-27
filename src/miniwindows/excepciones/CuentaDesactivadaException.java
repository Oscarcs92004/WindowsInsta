package miniwindows.excepciones;

/**
 * Se lanza al intentar iniciar sesion o ver una cuenta que esta desactivada.
 * Es "comprobada" (extends Exception): el programa debe manejarla.
 */
public class CuentaDesactivadaException extends Exception {

    private static final long serialVersionUID = 1L;

    public CuentaDesactivadaException(String username) {
        super("La cuenta '" + username + "' esta desactivada");
    }
}
