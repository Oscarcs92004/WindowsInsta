package miniwindows.so;

import java.io.Serializable;

/**
 * Un tramo de texto con formato dentro de un archivo del editor.
 *
 * El editor guarda el texto en el .txt normal y, aparte, una lista de estos
 * tramos en un archivo binario paralelo <nombre>.txt.fmt (otra extension
 * propia). Al abrir el .txt se lee el .fmt y se aplica cada tramo.
 *
 * TODO (Oscar) - Iteracion 3.5: usar esta clase al implementar el formato.
 * Los campos son publicos a proposito, para que el codigo del editor quede
 * corto (tramo.inicio, tramo.colorRGB, ...).
 */
public class TramoFormato implements Serializable {

    private static final long serialVersionUID = 1L;

    public int inicio;      // posicion donde empieza el tramo
    public int fin;         // posicion donde termina el tramo
    public String fuente;   // nombre de la fuente, por ejemplo "Arial"
    public int tamano;      // tamano de la letra, por ejemplo 18
    public int colorRGB;    // color del texto como numero RGB
}
