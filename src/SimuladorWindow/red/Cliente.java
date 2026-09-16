package SimuladorWindow.red;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


public class Cliente implements AutoCloseable {

    private static final int ESPERA_MS = 5000;

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    public Cliente(String host, int puerto) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, puerto), ESPERA_MS);
        socket.setSoTimeout(ESPERA_MS);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
    }

    public String pedir(String comando) throws IOException {
        out.println(comando);
        return in.readLine();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }


    public static String intentar(String comando) {
        String respuesta = pedirAlServidor(comando);
        if (respuesta == null && Servidor.iniciarEnSegundoPlano()) {
            respuesta = pedirAlServidor(comando);
        }
        return respuesta;
    }

    public static String enviar(String comando, String... campos) {
        StringBuilder linea = new StringBuilder(comando);
        for (String campo : campos) {
            linea.append(';');
            if (campo != null) {
                linea.append(URLEncoder.encode(campo, StandardCharsets.UTF_8));
            }
        }
        return intentar(linea.toString());
    }

    public static boolean esOk(String respuesta) {
        return respuesta != null && respuesta.startsWith("OK");
    }

    public static String motivo(String respuesta) {
        if (respuesta == null) {
            return "No hay conexión con el servidor.";
        }
        int separador = respuesta.indexOf(';');
        return (separador < 0) ? respuesta : respuesta.substring(separador + 1);
    }

    private static String pedirAlServidor(String comando) {
        try (Cliente cliente = new Cliente("localhost", Servidor.PUERTO)) {
            return cliente.pedir(comando);
        } catch (IOException e) {
            return null;
        }
    }
}
