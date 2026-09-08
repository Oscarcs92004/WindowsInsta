package SimuladorWindow.insta;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Una publicacion ("insta") de INSTA+ (enunciado 4.3 y 4.6).
 *
 * Sirve para los dos casos:
 *   - insta de solo texto: rutaImagen queda en null.
 *   - insta con imagen: rutaImagen apunta a la foto y el texto es la descripcion.
 *
 * Es una clase de datos. Implementa Serializable para guardarse en insta.ins.
 */
public class Publicacion implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String autor;
    private final LocalDateTime fecha;
    private final String texto;
    private final String rutaImagen;   // null si es un insta de solo texto

    public Publicacion(String autor, String texto, String rutaImagen) {
        this.autor = autor;
        this.texto = texto;
        this.rutaImagen = rutaImagen;
        this.fecha = LocalDateTime.now();
    }

    public String getAutor()      { return autor; }
    public LocalDateTime getFecha(){ return fecha; }
    public String getTexto()      { return texto; }
    public String getRutaImagen() { return rutaImagen; }

    public boolean tieneImagen()  { return rutaImagen != null; }
}
