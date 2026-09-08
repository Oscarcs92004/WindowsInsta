package SimuladorWindow.red;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Cliente de sockets: se conecta al servidor y le manda comandos de texto.
 *
 *   Cliente c = new Cliente("localhost", Servidor.PUERTO);
 *   String respuesta = c.pedir("LOGIN;ana;1234");
 */
public class Cliente {

    private final BufferedReader in;
    private final PrintWriter out;

    public Cliente(String host, int puerto) throws IOException {
        Socket socket = new Socket(host, puerto);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    /** Manda un comando y devuelve la respuesta del servidor (una linea). */
    public String pedir(String comando) throws IOException {
        out.println(comando);
        return in.readLine();
    }

    /**
     * Se conecta a localhost, manda el comando y devuelve la respuesta.
     * Si el servidor no esta encendido, devuelve null (no lanza excepcion),
     * para que quien llame pueda seguir en modo local.
     */
    public static String intentar(String comando) {
        try {
            return new Cliente("localhost", Servidor.PUERTO).pedir(comando);
        } catch (IOException e) {
            return null;   // no hay servidor
        }
    }
}
