package SimuladorWindow.red;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class Cliente {

    private final BufferedReader in;
    private final PrintWriter out;

    public Cliente(String host, int puerto) throws IOException {
        Socket socket = new Socket(host, puerto);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public String pedir(String comando) throws IOException {
        out.println(comando);
        return in.readLine();
    }


    public static String intentar(String comando) {
        try {
            return new Cliente("localhost", Servidor.PUERTO).pedir(comando);
        } catch (IOException e) {
            return null;
        }
    }
}
