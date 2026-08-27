package miniwindows.excepciones;

/**
 * Se lanza al intentar registrar un username que ya existe.
 * Es "comprobada" (extends Exception) porque quien registra debe decidir
 * que hacer: mostrar un mensaje y dejar reintentar.
 */
public class UsernameDuplicadoException extends Exception {

    private static final long serialVersionUID = 1L;

    public UsernameDuplicadoException(String username) {
        super("El username '" + username + "' ya existe");
    }
}
