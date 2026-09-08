package SimuladorWindow.so;

import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.terminal.ControladorTerminal;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Consola tipo CMD de Windows (enunciado 3.6).
 *
 * Ya funcionan los 7 comandos: mkdir, rm, cd, cd.., dir, date, time.
 * Se apoya en SistemaArchivos para tocar las carpetas.
 */
public class PanelConsola extends JPanel {

    private final ControladorTerminal controlador;
    private final SistemaArchivos fs;
    private final JTextArea salida = new JTextArea();
    private final JTextField entrada = new JTextField();

    private boolean modoEdicion = false;
    private boolean modoAppend = false;
    private String archivoEdicion = null;
    private final StringBuilder contenidoEdicion = new StringBuilder();

    private void cerrarVentanaConsola() {
        Container padre = getParent();
        while (padre != null && !(padre instanceof JInternalFrame)) {
            padre = padre.getParent();
        }
        if (padre instanceof JInternalFrame) {
            ((JInternalFrame) padre).dispose();
        }
    }

    private void procesarEntrada() {
        String linea = entrada.getText();
        entrada.setText("");
        if (modoEdicion) {
            procesarLineaEdicion(linea);
            return;
        }
        salida.append(fs.rutaActual() + "> " + linea + "\n");
        String resultado = controlador.ejecutar(linea);
        if ("CLS".equals(resultado)) {
            salida.setText("");
            mostrarPrompt();
            return;
        }
        if ("EXIT_APP".equals(resultado)) {
            cerrarVentanaConsola();
            return;
        }
        if ("MODO_ESCRITURA".equals(resultado) || "MODO_APPEND".equals(resultado)) {
            iniciarEdicion(linea, "MODO_APPEND".equals(resultado));
            return;
        }
        if (!resultado.isEmpty()) {
            salida.append(resultado + (resultado.endsWith("\n") ? "" : "\n"));
        }
        mostrarPrompt();
    }

    private void iniciarEdicion(String comando, boolean append) {
        String[] partes = comando.trim().split("\\s+");
        archivoEdicion = partes[1];
        modoAppend = append;
        modoEdicion = true;
        contenidoEdicion.setLength(0);
        salida.append(append ? "--- MODO APPEND ---\n" : "--- MODO ESCRITURA ---\n");
        salida.append("Escriba el contenido linea por linea.\n");
        salida.append("Escriba EXIT en una linea separada para guardar y salir.\n");
        salida.append("------------------------------------------------\n");
        entrada.requestFocusInWindow();
    }

    private void procesarLineaEdicion(String linea) {
        if ("EXIT".equalsIgnoreCase(linea.trim())) {
            String contenido = contenidoEdicion.toString();
            boolean ok;
            if (modoAppend) {
                String existente = fs.leer(archivoEdicion);
                if (existente != null && !existente.isEmpty() && !contenido.isEmpty()) {
                    contenido = System.lineSeparator() + contenido;
                }
                ok = fs.agregar(archivoEdicion, contenido);
            } else {
                ok = fs.escribir(archivoEdicion, contenido);
            }
            finalizarEdicion(ok);
            return;
        }
        salida.append(linea + "\n");
        contenidoEdicion.append(linea).append(System.lineSeparator());
    }

    private void finalizarEdicion(boolean ok) {
        if (ok) {
            salida.append("Archivo \"" + archivoEdicion + "\" guardado correctamente.\n");
        } else {
            salida.append("Error: no se pudo guardar \"" + archivoEdicion + "\".\n");
        }
        modoEdicion = false;
        modoAppend = false;
        archivoEdicion = null;
        contenidoEdicion.setLength(0);
        mostrarPrompt();
    }

    private void mostrarPrompt() {
        salida.append(fs.rutaActual() + "> ");
        salida.setCaretPosition(salida.getDocument().getLength());
    }

    public PanelConsola(Usuario usuarioActual, File carpetaRaiz) {
        controlador = new ControladorTerminal(usuarioActual);
        this.fs = new SistemaArchivos(carpetaRaiz);

        setLayout(new BorderLayout());

        salida.setEditable(false);
        salida.setBackground(Color.BLACK);
        salida.setForeground(Color.WHITE);
        salida.setCaretColor(Color.WHITE);
        salida.setFont(new Font("Monospaced", Font.PLAIN, 13));

        entrada.setBackground(Color.BLACK);
        entrada.setForeground(Color.WHITE);
        entrada.setCaretColor(Color.WHITE);
        entrada.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        add(new JScrollPane(salida), BorderLayout.CENTER);
        add(entrada, BorderLayout.SOUTH);

        salida.append("Consola de Simulador Windows.\n");
        salida.append("Escriba Help para ver los comandos.\n\n");
        salida.append(fs.rutaActual() + "> ");
        mostrarPrompt();

        // Cuando el usuario pulsa Enter en la caja de entrada.
        entrada.addActionListener(e -> {
            procesarEntrada();
        });
    }
}
