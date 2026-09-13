package SimuladorWindow.excepciones;

public class CuentaDesactivadaException extends Exception {

    private static final long serialVersionUID = 1L;

    public CuentaDesactivadaException(String username) {
        super("La cuenta '" + username + "' esta desactivada");
    }
}
