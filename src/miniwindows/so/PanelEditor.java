package miniwindows.so;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;

import miniwindows.modelo.Usuario;

/**
 * Editor de texto (enunciado 3.4).
 *
 * YA FUNCIONA: crear, abrir y guardar archivos .txt (texto plano, UTF-8).
 *
 * FALTA (Oscar, Iteracion 3.5): la barra de formato (color, tipo y tamano de
 * fuente) y guardar ese formato en un archivo paralelo <nombre>.txt.fmt para
 * que se conserve al reabrir. Ver la clase TramoFormato.
 */
public class PanelEditor extends JPanel {

    private final File carpetaRaiz;
    private final JTextPane texto = new JTextPane();
    private File archivoAbierto;

    public PanelEditor(Usuario usuarioActual, File carpetaRaiz) {
        this.carpetaRaiz = carpetaRaiz;

        setLayout(new BorderLayout());
        add(new JScrollPane(texto), BorderLayout.CENTER);

        JToolBar barra = new JToolBar();
        barra.setFloatable(false);

        JButton btnNuevo = new JButton("Nuevo");
        JButton btnAbrir = new JButton("Abrir");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnColor = new JButton("Color");
        JButton btnFuente = new JButton("Fuente");
        JButton btnTamano = new JButton("Tamano");

        barra.add(btnNuevo);
        barra.add(btnAbrir);
        barra.add(btnGuardar);
        barra.addSeparator();
        barra.add(btnColor);
        barra.add(btnFuente);
        barra.add(btnTamano);
        add(barra, BorderLayout.NORTH);

        btnNuevo.addActionListener(e -> {
            texto.setText("");
            archivoAbierto = null;
        });
        btnAbrir.addActionListener(e -> abrir());
        btnGuardar.addActionListener(e -> guardar());

        // TODO (Oscar) - Iteracion 3.5: formato del editor.
        //   Color:  Color c = JColorChooser.showDialog(...);  aplicar a la seleccion.
        //   Fuente: elegir el nombre de la fuente y aplicarlo a la seleccion.
        //   Tamano: elegir el tamano y aplicarlo a la seleccion.
        //   Para aplicar formato a un tramo se usa un StyledDocument y
        //   SimpleAttributeSet (StyleConstants.setForeground / setFontFamily /
        //   setFontSize). Luego guardar la lista de TramoFormato en el .fmt.
        btnColor.addActionListener(e -> pendiente());
        btnFuente.addActionListener(e -> pendiente());
        btnTamano.addActionListener(e -> pendiente());
    }

    private void abrir() {
        JFileChooser chooser = new JFileChooser(carpetaRaiz);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = chooser.getSelectedFile();
        try {
            String contenido = Files.readString(archivo.toPath(), StandardCharsets.UTF_8);
            texto.setText(contenido);
            archivoAbierto = archivo;
            // TODO (Oscar): aqui tambien hay que leer el .fmt y aplicar el formato.
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir el archivo.");
        }
    }

    private void guardar() {
        File destino = archivoAbierto;

        if (destino == null) {
            JFileChooser chooser = new JFileChooser(carpetaRaiz);
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            destino = chooser.getSelectedFile();
            if (!destino.getName().toLowerCase().endsWith(".txt")) {
                destino = new File(destino.getParentFile(), destino.getName() + ".txt");
            }
        }

        try {
            Files.writeString(destino.toPath(), texto.getText(), StandardCharsets.UTF_8);
            archivoAbierto = destino;
            JOptionPane.showMessageDialog(this, "Guardado: " + destino.getName());
            // TODO (Oscar): aqui tambien hay que guardar el .fmt con el formato.
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el archivo.");
        }
    }

    private void pendiente() {
        JOptionPane.showMessageDialog(this,
                "Formato: falta implementarlo (ver el comentario TODO en PanelEditor).");
    }
}
