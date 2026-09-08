package SimuladorWindow.insta;

import SimuladorWindow.excepciones.UsernameDuplicadoException;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.persistencia.ArchivoBinario;
import SimuladorWindow.servicios.UsuarioServicio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Todo lo de INSTA+ que toca disco, en un solo lugar (mismo molde que
 * UsuarioServicio): guarda las rutas de los archivos .ins de cada usuario y
 * tiene metodos cortos que llaman a ArchivoBinario para leerlos y guardarlos.
 *
 * Estructura en disco (enunciado 4.3):
 *
 *   datos/INSTA_RAIZ/
 *   ├── stickers_globales/
 *   └── <username>/
 *       ├── following.ins   followers.ins   insta.ins   inbox.ins   stickers.ins
 *       ├── imagenes/
 *       ├── folders_personales/
 *       └── stickers_personales/
 *
 * El registro maestro de usuarios sigue siendo usuarios.sop (lo maneja
 * UsuarioServicio); INSTA+ lo reutiliza en vez de tener su propio users.ins.
 */
public class InstaServicio {

    private final File raiz;                 // datos/INSTA_RAIZ
    private final UsuarioServicio usuarioServicio;

    /** Los 5 stickers que todos tienen desde el principio (enunciado 4.12). */
    private static final String[] STICKERS_POR_DEFECTO =
            {"Feliz", "Triste", "Corazon", "Risa", "Aplauso"};

    public InstaServicio(File carpetaDeDatos, UsuarioServicio usuarioServicio) {
        this.raiz = new File(carpetaDeDatos, "INSTA_RAIZ");
        this.usuarioServicio = usuarioServicio;
        new File(raiz, "stickers_globales").mkdirs();
    }

    // -----------------------------------------------------------------
    //  Rutas
    // -----------------------------------------------------------------

    public File carpetaDe(String username) {
        return new File(raiz, username);
    }

    public File carpetaImagenesDe(String username) {
        return new File(carpetaDe(username), "imagenes");
    }

    public File carpetaFoldersPersonalesDe(String username) {
        return new File(carpetaDe(username), "folders_personales");
    }

    public File carpetaStickersPersonalesDe(String username) {
        return new File(carpetaDe(username), "stickers_personales");
    }

    private File archivoFollowing(String username) { return new File(carpetaDe(username), "following.ins"); }
    private File archivoFollowers(String username) { return new File(carpetaDe(username), "followers.ins"); }
    private File archivoInsta(String username)     { return new File(carpetaDe(username), "insta.ins"); }
    private File archivoInbox(String username)     { return new File(carpetaDe(username), "inbox.ins"); }
    private File archivoStickers(String username)  { return new File(carpetaDe(username), "stickers.ins"); }

    /**
     * Crea la carpeta del usuario y sus 3 subcarpetas, y le registra los 5
     * stickers por defecto si todavia no los tiene. Se llama al iniciar sesion.
     */
    public void asegurarCarpetaUsuario(String username) {
        File carpeta = carpetaDe(username);
        boolean esNueva = !carpeta.exists();

        carpeta.mkdirs();
        carpetaImagenesDe(username).mkdirs();
        carpetaFoldersPersonalesDe(username).mkdirs();
        carpetaStickersPersonalesDe(username).mkdirs();

        if (esNueva || stickersDe(username).isEmpty()) {
            List<Sticker> stickers = new ArrayList<>();
            for (String nombre : STICKERS_POR_DEFECTO) {
                stickers.add(new Sticker(nombre,
                        "src/SimuladorWindow/recursos/Icon/file.png"));
            }
            guardarStickers(username, stickers);
        }
    }

    // -----------------------------------------------------------------
    //  Cuenta desactivada = como si no existiera (enunciado 4.13).
    //  Un solo metodo; lo llaman el timeline y las busquedas.
    // -----------------------------------------------------------------

    public boolean estaVisible(String username) {
        Usuario u = usuarioServicio.buscar(username);
        return u != null && u.isActiva();
    }

    // -----------------------------------------------------------------
    //  Seguir / dejar de seguir (enunciado 4.5 y 4.9b)
    // -----------------------------------------------------------------

    public List<String> aQuienesSigue(String username) {
        return leerTextos(archivoFollowing(username));
    }

    public List<String> seguidoresDe(String username) {
        return leerTextos(archivoFollowers(username));
    }

    public boolean sigo(String yo, String otro) {
        for (String u : aQuienesSigue(yo)) {
            if (u.equalsIgnoreCase(otro)) {
                return true;
            }
        }
        return false;
    }

    public void seguir(String yo, String otro) {
        List<String> miFollowing = aQuienesSigue(yo);
        if (!contieneIgnoreCase(miFollowing, otro)) {
            miFollowing.add(otro);
            guardarTextos(archivoFollowing(yo), miFollowing);
        }

        List<String> susFollowers = seguidoresDe(otro);
        if (!contieneIgnoreCase(susFollowers, yo)) {   // sin duplicados (enunciado 4.3)
            susFollowers.add(yo);
            guardarTextos(archivoFollowers(otro), susFollowers);
        }
    }

    public void dejarDeSeguir(String yo, String otro) {
        List<String> miFollowing = aQuienesSigue(yo);
        quitarIgnoreCase(miFollowing, otro);
        guardarTextos(archivoFollowing(yo), miFollowing);

        List<String> susFollowers = seguidoresDe(otro);
        quitarIgnoreCase(susFollowers, yo);
        guardarTextos(archivoFollowers(otro), susFollowers);
    }

    // -----------------------------------------------------------------
    //  Publicaciones (enunciado 4.3, 4.6, 4.7)
    // -----------------------------------------------------------------

    public List<Publicacion> publicacionesDe(String username) {
        return leerPublicaciones(archivoInsta(username));
    }

    public void publicar(String username, Publicacion publicacion) {
        List<Publicacion> lista = publicacionesDe(username);
        lista.add(publicacion);
        guardarPublicaciones(username, lista);
    }

    public int contarPublicaciones(String username) {
        return publicacionesDe(username).size();
    }

    // -----------------------------------------------------------------
    //  Inbox (enunciado 4.11)
    // -----------------------------------------------------------------

    public List<Mensaje> inboxDe(String username) {
        return leerMensajes(archivoInbox(username));
    }

    /** Guarda el mensaje en el inbox del que envia y en el del que recibe. */
    public void enviarMensaje(Mensaje mensaje) {
        List<Mensaje> deEmisor = inboxDe(mensaje.getEmisor());
        deEmisor.add(mensaje);
        guardarInbox(mensaje.getEmisor(), deEmisor);

        List<Mensaje> deReceptor = inboxDe(mensaje.getReceptor());
        deReceptor.add(mensaje);
        guardarInbox(mensaje.getReceptor(), deReceptor);
    }

    public void guardarInbox(String username, List<Mensaje> mensajes) {
        ArchivoBinario.guardar(archivoInbox(username), new ArrayList<>(mensajes));
    }

    /** Cuantos mensajes recibidos y sin leer tiene el usuario (para el aviso). */
    public int contarNoLeidos(String username) {
        int cuenta = 0;
        for (Mensaje m : inboxDe(username)) {
            if (m.getReceptor().equalsIgnoreCase(username) && !m.isLeido()) {
                cuenta++;
            }
        }
        return cuenta;
    }

    // -----------------------------------------------------------------
    //  Stickers (enunciado 4.12)
    // -----------------------------------------------------------------

    public List<Sticker> stickersDe(String username) {
        return leerStickers(archivoStickers(username));
    }

    public void agregarSticker(String username, Sticker sticker) {
        List<Sticker> lista = stickersDe(username);
        lista.add(sticker);
        guardarStickers(username, lista);
    }

    private void guardarStickers(String username, List<Sticker> stickers) {
        ArchivoBinario.guardar(archivoStickers(username), new ArrayList<>(stickers));
    }

    // -----------------------------------------------------------------
    //  Cuentas de ejemplo (enunciado 4.9)
    // -----------------------------------------------------------------

    public void asegurarCuentasEjemplo() {
        sembrar("noticias", "Canal Noticias", 'F',
                "Hoy: resumen del dia #actualidad");
        sembrar("moda", "Estilo y Deporte", 'F',
                "Nueva coleccion de tenis #moda #deporte");
        sembrar("cine", "Sala de Cine", 'M',
                "Estrenos del fin de semana #entretenimiento");
    }

    private void sembrar(String username, String nombre, char genero, String primerInsta) {
        if (usuarioServicio.buscar(username) != null) {
            return;                          // ya existe
        }
        try {
            usuarioServicio.registrar(new Usuario(nombre, genero, username, "1234", 25));
        } catch (UsernameDuplicadoException nuncaPasa) {
            return;
        }
        asegurarCarpetaUsuario(username);
        publicar(username, new Publicacion(username, primerInsta, null));
    }

    // -----------------------------------------------------------------
    //  Guardar / leer los archivos .ins (mismo estilo que UsuarioServicio)
    // -----------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private List<String> leerTextos(File archivo) {
        if (!archivo.exists()) {
            return new ArrayList<>();
        }
        return (List<String>) ArchivoBinario.leer(archivo);
    }

    private void guardarTextos(File archivo, List<String> textos) {
        ArchivoBinario.guardar(archivo, new ArrayList<>(textos));
    }

    @SuppressWarnings("unchecked")
    private List<Publicacion> leerPublicaciones(File archivo) {
        if (!archivo.exists()) {
            return new ArrayList<>();
        }
        return (List<Publicacion>) ArchivoBinario.leer(archivo);
    }

    private void guardarPublicaciones(String username, List<Publicacion> lista) {
        ArchivoBinario.guardar(archivoInsta(username), new ArrayList<>(lista));
    }

    @SuppressWarnings("unchecked")
    private List<Mensaje> leerMensajes(File archivo) {
        if (!archivo.exists()) {
            return new ArrayList<>();
        }
        return (List<Mensaje>) ArchivoBinario.leer(archivo);
    }

    @SuppressWarnings("unchecked")
    private List<Sticker> leerStickers(File archivo) {
        if (!archivo.exists()) {
            return new ArrayList<>();
        }
        return (List<Sticker>) ArchivoBinario.leer(archivo);
    }

    // -----------------------------------------------------------------

    private static boolean contieneIgnoreCase(List<String> lista, String texto) {
        for (String s : lista) {
            if (s.equalsIgnoreCase(texto)) {
                return true;
            }
        }
        return false;
    }

    private static void quitarIgnoreCase(List<String> lista, String texto) {
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).equalsIgnoreCase(texto)) {
                lista.remove(i);
                return;
            }
        }
    }
}
