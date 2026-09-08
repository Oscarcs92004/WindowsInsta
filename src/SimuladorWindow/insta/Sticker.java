package SimuladorWindow.insta;

import java.io.Serializable;

/**
 * Un sticker disponible para un usuario (enunciado 4.12).
 *
 * "nombre" es como se llama (Feliz, Triste, ...) y "ruta" es donde esta la
 * imagen del sticker en disco.
 *
 * Implementa Serializable para guardarse en stickers.ins.
 */
public class Sticker implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nombre;
    private final String ruta;

    public Sticker(String nombre, String ruta) {
        this.nombre = nombre;
        this.ruta = ruta;
    }

    public String getNombre() { return nombre; }
    public String getRuta()   { return ruta; }

    @Override
    public String toString() {
        return nombre;
    }
}
