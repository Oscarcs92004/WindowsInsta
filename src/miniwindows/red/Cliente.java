package miniwindows.red;

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
 *
 * TODO (Oscar) - Fase 5: usar esta clase desde PanelLogin
 *   (c.pedir("LOGIN;user;pass")) en lugar de llamar a UsuarioServicio directo.
 *   Si la conexion falla, avisar con un JOptionPane de que no hay servidor.
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
}
