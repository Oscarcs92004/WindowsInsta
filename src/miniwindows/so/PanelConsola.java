package miniwindows.so;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import miniwindows.modelo.Usuario;

/**
 * Consola tipo CMD de Windows (enunciado 3.6).
 *
 * Ya funcionan los 7 comandos: mkdir, rm, cd, cd.., dir, date, time.
 * Se apoya en SistemaArchivos para tocar las carpetas.
 */
public class PanelConsola extends JPanel {

    private final SistemaArchivos fs;
    private final JTextArea salida = new JTextArea();
    private final JTextField entrada = new JTextField();

    public PanelConsola(Usuario usuarioActual, File carpetaRaiz) {
        this.fs = new SistemaArchivos(carpetaRaiz);

        setLayout(new BorderLayout());

        salida.setEditable(false);
        salida.setBackground(Color.BLACK);
        salida.setForeground(Color.WHITE);
        salida.setFont(new Font("Monospaced", Font.PLAIN, 13));

        add(new JScrollPane(salida), BorderLayout.CENTER);
        add(entrada, BorderLayout.SOUTH);

        salida.append("Consola de Mini-Windows.\n");
        salida.append("Comandos: mkdir, rm, cd, cd.., dir, date, time\n\n");
        salida.append(fs.rutaActual() + "> ");

        // Cuando el usuario pulsa Enter en la caja de entrada.
        entrada.addActionListener(e -> {
            String linea = entrada.getText();
            entrada.setText("");

            salida.append(linea + "\n");

            String resultado = ejecutar(linea);
            if (!resultado.isEmpty()) {
                salida.append(resultado + "\n");
            }
            salida.append(fs.rutaActual() + "> ");
        });
    }

    /** Recibe la linea completa y devuelve el texto a mostrar. */
    private String ejecutar(String linea) {
        String[] partes = linea.trim().split("\\s+", 2);
        String comando = partes[0];
        String argumento = partes.length > 1 ? partes[1] : "";

        switch (comando) {
            case "":
                return "";
            case "mkdir":
                return fs.crearCarpeta(argumento) ? "" : "No se pudo crear la carpeta";
            case "rm":
                return fs.eliminar(argumento) ? "" : "No se pudo borrar";
            case "cd":
                return fs.cambiar(argumento);
            case "cd..":
                return fs.subirNivel();
            case "dir":
                return fs.listarComoTexto();
            case "date":
                return LocalDate.now().toString();
            case "time":
                return LocalTime.now().toString();
            default:
                return "'" + comando + "' no se reconoce como un comando";
        }
    }
}
