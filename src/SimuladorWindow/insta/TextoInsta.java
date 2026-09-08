package SimuladorWindow.insta;

import SimuladorWindow.estructuras.ListaEnlazada;
import java.time.format.DateTimeFormatter;

/**
 * Ayudantes de texto para INSTA+: sacar los #hashtags y las @menciones de una
 * publicacion y darles el formato del enunciado (4.7).
 *
 * Es una clase de solo metodos estaticos.
 */
public final class TextoInsta {

    private TextoInsta() {
    }

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Formato del timeline (enunciado 4.7):
     *   USERNAME escribio:
     *   "contenido" - fecha
     */
    public static String formato(Publicacion p) {
        String texto = p.getAutor() + " escribio:\n\""
                + p.getTexto() + "\" - " + FECHA.format(p.getFecha());
        if (p.tieneImagen()) {
            texto = texto + "\n[imagen adjunta]";
        }
        return texto;
    }

    /** Devuelve true si el texto contiene el hashtag dado (sin el #). */
    public static boolean tieneHashtag(String texto, String palabra) {
        for (String h : hashtags(texto).comoLista()) {
            if (h.equalsIgnoreCase(palabra)) {
                return true;
            }
        }
        return false;
    }

    /** Devuelve true si el texto menciona a @username. */
    public static boolean menciona(String texto, String username) {
        for (String m : menciones(texto).comoLista()) {
            if (m.equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    /** Todos los #hashtags del texto, sin el #, sin repetidos. */
    public static ListaEnlazada<String> hashtags(String texto) {
        return palabrasConMarca(texto, '#');
    }

    /** Todas las @menciones del texto, sin la @, sin repetidos. */
    public static ListaEnlazada<String> menciones(String texto) {
        return palabrasConMarca(texto, '@');
    }

    private static ListaEnlazada<String> palabrasConMarca(String texto, char marca) {
        ListaEnlazada<String> resultado = new ListaEnlazada<>();
        if (texto == null) {
            return resultado;
        }
        String[] partes = texto.split("\\s+");
        for (String parte : partes) {
            if (parte.length() > 1 && parte.charAt(0) == marca) {
                String limpia = limpiar(parte.substring(1));
                if (!limpia.isEmpty() && !resultado.contiene(limpia)) {
                    resultado.agregarFinal(limpia);
                }
            }
        }
        return resultado;
    }

    /** Se queda solo con letras y numeros (quita comas, puntos, etc.). */
    private static String limpiar(String palabra) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < palabra.length(); i++) {
            char c = palabra.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            } else {
                break;
            }
        }
        return sb.toString();
    }
}
