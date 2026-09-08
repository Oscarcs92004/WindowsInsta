package SimuladorWindow.red;

import SimuladorWindow.excepciones.CuentaDesactivadaException;
import SimuladorWindow.insta.InstaServicio;
import SimuladorWindow.insta.Publicacion;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Atiende a UN cliente conectado. Corre en su propio hilo (lo lanza Servidor).
 *
 * Lee lineas de texto del cliente, las procesa y responde otra linea.
 * Protocolo: comandos separados por ";", por ejemplo  LOGIN;ana;1234
 *
 * El servidor es el unico que abre los archivos del sistema (usuarios.sop,
 * los .ins): los clientes le piden a el que haga las operaciones.
 */
class AtenderCliente implements Runnable {

    private final Socket socket;
    /** Cuentas de Mini-Windows (usuarios.sop): para el comando LOGIN. */
    private final UsuarioServicio usuarios;
    /** Cuentas de INSTA+ (users.ins): para POST y FOLLOW. */
    private final UsuarioServicio usuariosInsta;
    private final InstaServicio insta;

    AtenderCliente(Socket socket) {
        this.socket = socket;
        File datos = new File("datos");
        this.usuarios = new UsuarioServicio(datos);
        this.usuariosInsta = new UsuarioServicio(datos, "users.ins");
        this.insta = new InstaServicio(datos, usuariosInsta);
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
                return login(p);
            case "POST":
                return post(p);
            case "FOLLOW":
                return follow(p);
            default:
                return "ERROR;comando desconocido";
        }
    }

    private String login(String[] p) {
        if (p.length < 3) {
            return "ERROR;faltan datos";
        }
        try {
            Usuario u = usuarios.login(p[1], p[2]);
            if (u == null) {
                return "ERROR;credenciales";
            }
            return "OK;bienvenido " + u.getUsername();
        } catch (CuentaDesactivadaException e) {
            return "ERROR;cuenta desactivada";
        }
    }

    private String post(String[] p) {
        if (p.length < 3) {
            return "ERROR;faltan datos";
        }
        if (usuariosInsta.buscar(p[1]) == null) {
            return "ERROR;usuario no existe";
        }
        insta.asegurarCarpetaUsuario(p[1]);
        insta.publicar(p[1], new Publicacion(p[1], p[2], null));
        return "OK;publicado";
    }

    private String follow(String[] p) {
        if (p.length < 3) {
            return "ERROR;faltan datos";
        }
        if (usuariosInsta.buscar(p[1]) == null || usuariosInsta.buscar(p[2]) == null) {
            return "ERROR;usuario no existe";
        }
        insta.asegurarCarpetaUsuario(p[1]);
        insta.asegurarCarpetaUsuario(p[2]);
        insta.seguir(p[1], p[2]);
        return "OK;siguiendo a " + p[2];
    }
}
