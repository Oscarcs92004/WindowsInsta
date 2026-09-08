package SimuladorWindow.insta;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Una publicacion ("insta") de INSTA+ (enunciado 4.3, 4.6 y 4.7).
 *
 * Igual que en Instagram hay tres tipos:
 *   - TEXTO:  solo texto (rutaImagen y rutaVideo en null), maximo 140 caracteres.
 *   - IMAGEN: una foto con su descripcion (maximo 220).
 *   - VIDEO:  un video / reel con su descripcion (maximo 220).
 *
 * El texto puede llevar #hashtags y @menciones en cualquiera de los tres tipos.
 *
 * Es una clase de datos. Implementa Serializable para guardarse en insta.ins.
 */
public class Publicacion implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TEXTO  = "TEXTO";
    public static final String IMAGEN = "IMAGEN";
    public static final String VIDEO  = "VIDEO";

    private final String autor;
    private final LocalDateTime fecha;
    private final String texto;
    private final String rutaImagen;   // null si no lleva foto
    private final String rutaVideo;    // null si no es un reel

    /** Insta de solo texto o de imagen (compatibilidad con lo anterior). */
    public Publicacion(String autor, String texto, String rutaImagen) {
        this(autor, texto, rutaImagen, null);
    }

    public Publicacion(String autor, String texto, String rutaImagen, String rutaVideo) {
        this.autor = autor;
        this.texto = texto;
        this.rutaImagen = rutaImagen;
        this.rutaVideo = rutaVideo;
        this.fecha = LocalDateTime.now();
    }

    public String getAutor()       { return autor; }
    public LocalDateTime getFecha(){ return fecha; }
    public String getTexto()       { return texto; }
    public String getRutaImagen()  { return rutaImagen; }
    public String getRutaVideo()   { return rutaVideo; }

    public boolean tieneImagen()   { return rutaImagen != null; }
    public boolean esVideo()       { return rutaVideo != null; }
    public boolean esSoloTexto()   { return rutaImagen == null && rutaVideo == null; }

    /** "TEXTO", "IMAGEN" o "VIDEO". */
    public String getTipo() {
        if (rutaVideo != null)  return VIDEO;
        if (rutaImagen != null) return IMAGEN;
        return TEXTO;
    }
}
