package SimuladorWindow.insta;

import SimuladorWindow.estructuras.ListaEnlazada;
import SimuladorWindow.excepciones.UsernameDuplicadoException;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.persistencia.ArchivoBinario;
import SimuladorWindow.servicios.UsuarioServicio;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;


public class InstaServicio {

    private final File raiz;
    private final UsuarioServicio usuarioServicio;

    private static final String[] STICKERS_POR_DEFECTO =
            {"Feliz", "Triste", "Corazón", "Risa", "Aplauso"};

    private static final Color[] COLORES_STICKERS = {
            new Color(255, 200, 0),
            new Color(80, 120, 220),
            new Color(220, 60, 90),
            new Color(255, 140, 40),
            new Color(80, 180, 100),
    };

    public InstaServicio(File carpetaDeDatos, UsuarioServicio usuarioServicio) {
        this.raiz = new File(carpetaDeDatos, "INSTA_RAIZ");
        this.usuarioServicio = usuarioServicio;
        carpetaStickersGlobales().mkdirs();
        asegurarStickersGlobales();
    }

    public UsuarioServicio getUsuarioServicio() {
        return usuarioServicio;
    }

    public File carpetaStickersGlobales() {
        return new File(raiz, "stickers_globales");
    }

    public File archivoStickerGlobal(String nombre) {
        return new File(carpetaStickersGlobales(), nombre + ".png");
    }

    private void asegurarStickersGlobales() {
        for (int i = 0; i < STICKERS_POR_DEFECTO.length; i++) {
            File destino = archivoStickerGlobal(STICKERS_POR_DEFECTO[i]);
            if (destino.exists()) {
                continue;
            }
            BufferedImage img = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(COLORES_STICKERS[i]);
            g.fillOval(4, 4, 88, 88);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 48));
            String inicial = STICKERS_POR_DEFECTO[i].substring(0, 1);
            g.drawString(inicial, 33, 62);
            g.dispose();
            try {
                ImageIO.write(img, "png", destino);
            } catch (IOException e) {
                System.err.println("No se pudo crear el sticker " + destino);
            }
        }
    }

    public File carpetaDe(String username) {
        return new File(raiz, username);
    }

    public File carpetaImagenesDe(String username) {
        return new File(carpetaDe(username), "imagenes");
    }

    public File carpetaVideosDe(String username) {
        return new File(carpetaDe(username), "videos");
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

    public void asegurarCarpetaUsuario(String username) {
        File carpeta = carpetaDe(username);
        boolean esNueva = !carpeta.exists();

        carpeta.mkdirs();
        carpetaImagenesDe(username).mkdirs();
        carpetaVideosDe(username).mkdirs();
        carpetaFoldersPersonalesDe(username).mkdirs();
        carpetaStickersPersonalesDe(username).mkdirs();

        if (esNueva || stickersDe(username).isEmpty()) {
            List<Sticker> stickers = new ArrayList<>();
            for (String nombre : STICKERS_POR_DEFECTO) {
                stickers.add(new Sticker(nombre,
                        archivoStickerGlobal(nombre).getPath()));
            }
            guardarStickers(username, stickers);
        }
    }

    public boolean estaVisible(String username) {
        Usuario u = usuarioServicio.buscar(username);
        return u != null && u.isActiva();
    }

    public List<String> todosLosUsuarios() {
        List<String> nombres = new ArrayList<>();
        for (Usuario u : usuarioServicio.listar()) {
            nombres.add(u.getUsername());
        }
        return nombres;
    }

    public ListaEnlazada<String> listaFollowing(String username) {
        return aListaEnlazada(leerTextos(archivoFollowing(username)));
    }

    public ListaEnlazada<String> listaFollowers(String username) {
        return aListaEnlazada(leerTextos(archivoFollowers(username)));
    }

    public List<String> aQuienesSigue(String username) {
        return listaFollowing(username).comoLista();
    }

    public List<String> seguidoresDe(String username) {
        return listaFollowers(username).comoLista();
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
        ListaEnlazada<String> miFollowing = listaFollowing(yo);
        if (!miFollowing.contiene(otro)) {
            miFollowing.agregarFinal(otro);
            guardarTextos(archivoFollowing(yo), miFollowing.comoLista());
        }

        ListaEnlazada<String> susFollowers = listaFollowers(otro);
        if (!susFollowers.contiene(yo)) {
            susFollowers.agregarFinal(yo);
            guardarTextos(archivoFollowers(otro), susFollowers.comoLista());
        }
    }

    public void dejarDeSeguir(String yo, String otro) {
        ListaEnlazada<String> miFollowing = listaFollowing(yo);
        miFollowing.eliminar(otro);
        guardarTextos(archivoFollowing(yo), miFollowing.comoLista());

        ListaEnlazada<String> susFollowers = listaFollowers(otro);
        susFollowers.eliminar(yo);
        guardarTextos(archivoFollowers(otro), susFollowers.comoLista());
    }

    private static ListaEnlazada<String> aListaEnlazada(List<String> origen) {
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        for (String s : origen) {
            lista.agregarFinal(s);
        }
        return lista;
    }

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

    public List<Mensaje> inboxDe(String username) {
        return leerMensajes(archivoInbox(username));
    }

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

    public int contarNoLeidos(String username) {
        int cuenta = 0;
        for (Mensaje m : inboxDe(username)) {
            if (m.getReceptor().equalsIgnoreCase(username) && !m.isLeido()) {
                cuenta++;
            }
        }
        return cuenta;
    }

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

    public void asegurarCuentasEjemplo() {
        sembrar("noticias", "Canal Noticias", 'F',
                new Color(0x2B, 0x57, 0xC5), new Color(0x4A, 0x9A, 0xD6),
                "Hoy: resumen del día. Lo más importante en cinco titulares #actualidad",
                "Rueda de prensa de esta mañana #actualidad #politica");
        sembrar("moda", "Estilo y Deporte", 'F',
                new Color(0xE0, 0x4A, 0x8A), new Color(0xF8, 0x9A, 0x4A),
                "Nueva colección de tenis para esta temporada #moda #deporte",
                "Tres ideas de look para el finde #moda @cine");
        sembrar("cine", "Sala de Cine", 'M',
                new Color(0x30, 0x2A, 0x6C), new Color(0x8A, 0x3A, 0xB9),
                "Estrenos del fin de semana ya disponibles #entretenimiento #cine",
                "Crítica de la película más taquillera del mes #cine");
    }

    private void sembrar(String username, String nombre, char genero,
                         Color a, Color b, String insta1, String insta2) {
        if (usuarioServicio.buscar(username) != null) {
            return;
        }
        try {
            usuarioServicio.registrar(new Usuario(nombre, genero, username, "1234", 25));
        } catch (UsernameDuplicadoException nuncaPasa) {
            return;
        }
        asegurarCarpetaUsuario(username);

        File avatar = new File(carpetaDe(username), "perfil.png");
        File foto = new File(carpetaImagenesDe(username), "portada.png");
        pintarDegradado(avatar, 200, 200, a, b, nombre.substring(0, 1));
        pintarDegradado(foto, 800, 800, b, a, "");
        Usuario u = usuarioServicio.buscar(username);
        if (u != null) {
            u.setFotoPerfil(avatar.getPath());
            usuarioServicio.actualizar(u);
        }

        publicar(username, new Publicacion(username, insta1, foto.getPath()));
        publicar(username, new Publicacion(username, insta2, null));
    }

    private void pintarDegradado(File destino, int ancho, int alto,
                                 Color a, Color b, String inicial) {
        if (destino.exists()) {
            return;
        }
        BufferedImage img = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new java.awt.GradientPaint(0, 0, a, ancho, alto, b));
        g.fillRect(0, 0, ancho, alto);
        if (inicial != null && !inicial.isEmpty()) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, ancho / 2));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(inicial.toUpperCase(),
                    (ancho - fm.stringWidth(inicial)) / 2,
                    (alto - fm.getHeight()) / 2 + fm.getAscent());
        }
        g.dispose();
        try {
            ImageIO.write(img, "png", destino);
        } catch (IOException e) {
            System.err.println("No se pudo crear la imagen de ejemplo " + destino);
        }
    }

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
}
