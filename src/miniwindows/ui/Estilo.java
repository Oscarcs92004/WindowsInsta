package miniwindows.ui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.UIManager;

/**
 * La paleta y las fuentes de Mini-Windows en un solo lugar.
 *
 * El sistema imita el aspecto de Windows 98: superficies grises, escritorio
 * verde-azulado (teal), barras de titulo azul marino y letra pequena.
 *
 * Todos los paneles deben usar estas constantes en vez de crear sus propios
 * {@code new Color(...)} o {@code new Font(...)}. Asi, si manana cambia el
 * aspecto del sistema, se cambia aqui una vez y se ve en todas las pantallas.
 *
 * Es una clase de solo constantes: no se crea con {@code new}.
 */
public final class Estilo {

    private Estilo() {
        // Nadie debe instanciar esta clase.
    }

    // ------------------------------------------------------------------
    //  Colores (paleta clasica de Windows 98)
    // ------------------------------------------------------------------

    /** Fondo del escritorio: el teal clasico, por si no carga el wallpaper. */
    public static final Color FONDO_ESCRITORIO = new Color(0, 128, 128);

    /** Barra de tareas y superficies de ventana: gris plata. */
    public static final Color BARRA_TAREAS = new Color(192, 192, 192);

    /** Fondo de los paneles de las herramientas (ventanas internas). */
    public static final Color PANEL = new Color(192, 192, 192);

    /** Azul marino de las barras de titulo y las selecciones. */
    public static final Color ACENTO = new Color(0, 0, 128);

    /** Texto sobre fondos oscuros (barra de titulo azul). */
    public static final Color TEXTO_CLARO = Color.WHITE;

    /** Texto sobre fondos grises (barra de tareas, paneles). */
    public static final Color TEXTO_OSCURO = Color.BLACK;

    /** Rojo para mensajes de error. */
    public static final Color ERROR = new Color(128, 0, 0);

    // ------------------------------------------------------------------
    //  Fuentes (pequenas, al estilo de MS Sans Serif)
    // ------------------------------------------------------------------

    /** Titulos grandes (nombre "Mini-Windows", encabezados de pantalla). */
    public static final Font TITULO = new Font("SansSerif", Font.BOLD, 18);

    /** Subtitulos y encabezados de seccion. */
    public static final Font SUBTITULO = new Font("SansSerif", Font.BOLD, 12);

    /** Texto normal de la interfaz. */
    public static final Font NORMAL = new Font("SansSerif", Font.PLAIN, 11);

    /** Fuente de ancho fijo para la consola y el editor. */
    public static final Font MONOESPACIADA = new Font("Monospaced", Font.PLAIN, 12);

    // ------------------------------------------------------------------
    //  Look & Feel
    // ------------------------------------------------------------------

    /**
     * Aplica el aspecto clasico de Windows (el de Windows 98 / 2000) a toda la
     * aplicacion. Se llama una sola vez en Main, antes de crear cualquier
     * ventana.
     *
     * Si ese Look & Feel no esta disponible, prueba con el de Windows normal y,
     * como ultimo recurso, se queda con el que venga por defecto sin romper
     * nada.
     */
    public static void aplicarLookAndFeel() {
        String[] candidatos = {
            "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel",
            UIManager.getSystemLookAndFeelClassName(),
        };
        for (String clase : candidatos) {
            try {
                UIManager.setLookAndFeel(clase);
                return;
            } catch (Exception e) {
                // Probamos con el siguiente candidato.
            }
        }
    }
}
