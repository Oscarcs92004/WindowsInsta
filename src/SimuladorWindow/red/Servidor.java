package SimuladorWindow.red;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    public static final int PUERTO = 5000;

    public static void main(String[] args) {
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor escuchando en el puerto " + PUERTO);
            atender(servidor);
        } catch (IOException e) {
            System.out.println("No se pudo iniciar el servidor: " + e.getMessage());
        }
    }

    public static synchronized boolean iniciarEnSegundoPlano() {
        ServerSocket servidor;
        try {
            servidor = new ServerSocket(PUERTO, 50, InetAddress.getLoopbackAddress());
        } catch (IOException e) {
            return false;
        }
        Thread hilo = new Thread(() -> {
            try {
                atender(servidor);
            } catch (IOException e) {
                System.out.println("El servidor se detuvo: " + e.getMessage());
            }
        }, "Servidor");
        hilo.setDaemon(true);
        hilo.start();
        return true;
    }

    private static void atender(ServerSocket servidor) throws IOException {
        while (true) {
            Socket cliente = servidor.accept();
            new Thread(new AtenderCliente(cliente)).start();
        }
    }
}
