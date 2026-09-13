package SimuladorWindow.red;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    public static final int PUERTO = 5000;

    public static void main(String[] args) {
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor escuchando en el puerto " + PUERTO);
            while (true) {
                Socket cliente = servidor.accept();
                new Thread(new AtenderCliente(cliente)).start();
            }
        } catch (IOException e) {
            System.out.println("No se pudo iniciar el servidor: " + e.getMessage());
        }
    }
}
