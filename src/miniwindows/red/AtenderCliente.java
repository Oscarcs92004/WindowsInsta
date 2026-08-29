package miniwindows.red;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Atiende a UN cliente conectado. Corre en su propio hilo (lo lanza Servidor).
 *
 * Lee lineas de texto del cliente, las procesa y responde otra linea.
 * Protocolo: comandos separados por ";", por ejemplo  LOGIN;ana;1234
 */
class AtenderCliente implements Runnable {

    private final Socket socket;

    AtenderCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String linea;
            while ((linea = in.readLine()) != null) {
                out.println(procesar(linea));
            }
        } catch (IOException e) {
            // el cliente se desconecto; no hay nada que hacer
        }
    }

    private String procesar(String linea) {
        String[] p = linea.split(";");

        switch (p[0]) {
            case "LOGIN":
                // TODO (Alex) - Fase 5: crear aqui un UsuarioServicio (el
                //   servidor es el unico que abre usuarios.sop), llamar a
                //   login(p[1], p[2]) y devolver "OK;bienvenido" o
                //   "ERROR;credenciales" / "ERROR;cuenta desactivada".
                return "ERROR;LOGIN todavia no implementado";
            default:
                return "ERROR;comando desconocido";
        }
    }
}
