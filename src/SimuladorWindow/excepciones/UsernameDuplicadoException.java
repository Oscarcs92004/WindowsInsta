package SimuladorWindow.excepciones;

public class UsernameDuplicadoException extends Exception {

    private static final long serialVersionUID = 1L;

    public UsernameDuplicadoException(String username) {
        super("El username '" + username + "' ya existe");
    }
}
