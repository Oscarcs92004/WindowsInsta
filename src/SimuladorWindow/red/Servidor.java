package SimuladorWindow.red;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Servidor central de sockets (Pilar 4). Escucha en un puerto fijo y por
 * cada cliente que se conecta lanza un hilo que lo atiende (ver Pilar 3).
 *
 * Se ejecuta aparte de la app:
 *   java -cp out SimuladorWindow.red.Servidor
 *
 * Entiende los comandos LOGIN, POST y FOLLOW (ver AtenderCliente).
 */
public class Servidor {

    public static final int PUERTO = 5000;

    public static void main(String[] args) {
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor escuchando en el puerto " + PUERTO);
            while (true) {
                Socket cliente = servidor.accept();          // espera a alguien
                new Thread(new AtenderCliente(cliente)).start();
            }
        } catch (IOException e) {
            System.out.println("No se pudo iniciar el servidor: " + e.getMessage());
        }
    }
}
