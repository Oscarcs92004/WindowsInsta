package SimuladorWindow.insta;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Publicacion implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TEXTO  = "TEXTO";
    public static final String IMAGEN = "IMAGEN";
    public static final String VIDEO  = "VIDEO";

    private final String autor;
    private final LocalDateTime fecha;
    private final String texto;
    private final String rutaImagen;
    private final String rutaVideo;

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

    public String getTipo() {
        if (rutaVideo != null)  return VIDEO;
        if (rutaImagen != null) return IMAGEN;
        return TEXTO;
    }
}
