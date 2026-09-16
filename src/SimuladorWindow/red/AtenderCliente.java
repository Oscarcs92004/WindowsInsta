package SimuladorWindow.red;

import SimuladorWindow.excepciones.CuentaDesactivadaException;
import SimuladorWindow.insta.InstaServicio;
import SimuladorWindow.insta.Mensaje;
import SimuladorWindow.insta.Publicacion;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;


class AtenderCliente implements Runnable {

    private static final Object CANDADO = new Object();
    private static final File DATOS = new File("datos");
    private static final UsuarioServicio USUARIOS = new UsuarioServicio(DATOS);
    private static final UsuarioServicio USUARIOS_INSTA = new UsuarioServicio(DATOS, "users.ins");
    private static final InstaServicio INSTA = new InstaServicio(DATOS, USUARIOS_INSTA);

    private final Socket socket;

    AtenderCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (Socket s = socket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String linea;
            while ((linea = in.readLine()) != null) {
                out.println(responder(linea));
            }
        } catch (IOException e) {
        }
    }

    private String responder(String linea) {
        try {
            String[] p = separar(linea);
            synchronized (CANDADO) {
                return procesar(p);
            }
        } catch (RuntimeException e) {
            System.out.println("Error en el servidor: " + e);
            return "ERROR;no se pudo procesar la solicitud";
        }
    }

    private static String[] separar(String linea) {
        String[] p = linea.split(";", -1);
        for (int i = 1; i < p.length; i++) {
            p[i] = URLDecoder.decode(p[i], StandardCharsets.UTF_8);
        }
        return p;
    }

    private String procesar(String[] p) {
        switch (p[0]) {
            case "LOGIN":
                return login(USUARIOS, p, false);
            case "LOGIN_INSTA":
                return login(USUARIOS_INSTA, p, true);
            case "POST":
                return post(p);
            case "FOLLOW":
                return follow(p);
            case "UNFOLLOW":
                return unfollow(p);
            case "MSG":
                return mensaje(p);
            default:
                return "ERROR;comando desconocido";
        }
    }

    private String login(UsuarioServicio servicio, String[] p, boolean permitirDesactivada) {
        if (p.length < 3) {
            return "ERROR;faltan datos";
        }
        try {
            Usuario u = servicio.login(p[1], p[2]);
            if (u == null) {
                return "ERROR;credenciales";
            }
            return "OK;bienvenido " + u.getUsername();
        } catch (CuentaDesactivadaException e) {
            return permitirDesactivada ? "OK;cuenta desactivada" : "ERROR;cuenta desactivada";
        }
    }

    private String post(String[] p) {
        if (p.length < 3) {
            return "ERROR;faltan datos";
        }
        if (USUARIOS_INSTA.buscar(p[1]) == null) {
            return "ERROR;usuario no existe";
        }
        String rutaImagen = opcional(p, 3);
        String rutaVideo = opcional(p, 4);
        String rutaSticker = opcional(p, 5);
        INSTA.asegurarCarpetaUsuario(p[1]);
        INSTA.publicar(p[1], new Publicacion(p[1], p[2], rutaImagen, rutaVideo, rutaSticker));
        return "OK;publicado";
    }

    private String follow(String[] p) {
        if (p.length < 3) {
            return "ERROR;faltan datos";
        }
        if (USUARIOS_INSTA.buscar(p[1]) == null || USUARIOS_INSTA.buscar(p[2]) == null) {
            return "ERROR;usuario no existe";
        }
        INSTA.asegurarCarpetaUsuario(p[1]);
        INSTA.asegurarCarpetaUsuario(p[2]);
        INSTA.seguir(p[1], p[2]);
        return "OK;siguiendo a " + p[2];
    }

    private String unfollow(String[] p) {
        if (p.length < 3) {
            return "ERROR;faltan datos";
        }
        if (USUARIOS_INSTA.buscar(p[1]) == null || USUARIOS_INSTA.buscar(p[2]) == null) {
            return "ERROR;usuario no existe";
        }
        INSTA.dejarDeSeguir(p[1], p[2]);
        return "OK;dejaste de seguir a " + p[2];
    }

    private String mensaje(String[] p) {
        if (p.length < 5) {
            return "ERROR;faltan datos";
        }
        if (USUARIOS_INSTA.buscar(p[1]) == null || USUARIOS_INSTA.buscar(p[2]) == null) {
            return "ERROR;usuario no existe";
        }
        String tipo = Mensaje.STICKER.equals(p[3]) ? Mensaje.STICKER : Mensaje.TEXTO;
        INSTA.enviarMensaje(new Mensaje(p[1], p[2], p[4], tipo));
        return "OK;enviado";
    }

    private static String opcional(String[] p, int i) {
        return (i < p.length && !p[i].isEmpty()) ? p[i] : null;
    }
}
