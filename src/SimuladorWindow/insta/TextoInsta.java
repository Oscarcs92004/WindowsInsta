package SimuladorWindow.insta;

import SimuladorWindow.estructuras.ListaEnlazada;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

public final class TextoInsta {

    private TextoInsta() {
    }

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String formato(Publicacion p) {
        String texto = p.getAutor() + " escribio:\n\""
                + p.getTexto() + "\" - " + FECHA.format(p.getFecha());
        if (p.tieneImagen()) {
            texto = texto + "\n[imagen adjunta]";
        }
        if (p.tieneSticker()) {
            texto = texto + "\n[sticker adjunto]";
        }
        return texto;
    }

    public static String hace(LocalDateTime fecha) {
        LocalDateTime ahora = LocalDateTime.now();
        long minutos = ChronoUnit.MINUTES.between(fecha, ahora);
        if (minutos < 1)  return "ahora";
        if (minutos < 60) return "hace " + minutos + " min";
        long horas = minutos / 60;
        if (horas < 24)   return "hace " + horas + " h";
        long dias = horas / 24;
        if (dias < 7)     return "hace " + dias + " d";
        return FECHA.format(fecha);
    }

    public static boolean tieneHashtag(String texto, String palabra) {
        for (String h : hashtags(texto).comoLista()) {
            if (h.equalsIgnoreCase(palabra)) {
                return true;
            }
        }
        return false;
    }

    public static boolean menciona(String texto, String username) {
        for (String m : menciones(texto).comoLista()) {
            if (m.equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    public static ListaEnlazada<String> hashtags(String texto) {
        return palabrasConMarca(texto, '#');
    }

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
