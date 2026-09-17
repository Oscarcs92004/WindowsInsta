package SimuladorWindow.insta;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Publicacion implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TEXTO  = "TEXTO";
    public static final String IMAGEN = "IMAGEN";

    private final String autor;
    private final LocalDateTime fecha;
    private final String texto;
    private final String rutaImagen;
    private final String rutaSticker;

    public Publicacion(String autor, String texto, String rutaImagen) {
        this(autor, texto, rutaImagen, null);
    }

    public Publicacion(String autor, String texto, String rutaImagen, String rutaSticker) {
        this.autor = autor;
        this.texto = texto;
        this.rutaImagen = rutaImagen;
        this.rutaSticker = rutaSticker;
        this.fecha = LocalDateTime.now();
    }

    public String getAutor()       { return autor; }
    public LocalDateTime getFecha(){ return fecha; }
    public String getTexto()       { return texto; }
    public String getRutaImagen()  { return rutaImagen; }
    public String getRutaSticker() { return rutaSticker; }

    public boolean tieneImagen()   { return rutaImagen != null; }
    public boolean tieneSticker()  { return rutaSticker != null; }
    public boolean esSoloTexto()   { return rutaImagen == null; }

    public String getTipo() {
        if (rutaImagen != null) return IMAGEN;
        return TEXTO;
    }
}
