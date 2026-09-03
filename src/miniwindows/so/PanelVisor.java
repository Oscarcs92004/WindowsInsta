package miniwindows.so;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import miniwindows.modelo.Usuario;

/**
 * Visor de imagenes (enunciado 3.5).
 *
 * YA FUNCIONA: elegir una carpeta y recorrer sus imagenes con
 * "Anterior" y "Siguiente", sin pasarse de los extremos.
 *
 * FALTA (Oscar, Iteracion 3.6): si la carpeta tiene muchas imagenes, cargar la
 * lista con un SwingWorker para que la ventana no se congele.
 */
public class PanelVisor extends JPanel {

    private final File carpetaRaiz;
    private final List<File> imagenes = new ArrayList<>();
    private int indice = 0;

    private final JLabel etiqueta = new JLabel("Elige una carpeta con imagenes.",
            SwingConstants.CENTER);

    private final JButton btnCarpeta = new JButton("Elegir carpeta");
    private final JButton btnAnterior = new JButton("Anterior");
    private final JButton btnSiguiente = new JButton("Siguiente");

    public PanelVisor(Usuario usuarioActual, File carpetaRaiz) {
        this.carpetaRaiz = carpetaRaiz;

        setLayout(new BorderLayout());
        add(new JScrollPane(etiqueta), BorderLayout.CENTER);

        JToolBar barra = new JToolBar();
        barra.setFloatable(false);

        barra.add(btnCarpeta);
        barra.add(btnAnterior);
        barra.add(btnSiguiente);
        add(barra, BorderLayout.NORTH);

        btnCarpeta.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(carpetaRaiz);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                cargarCarpeta(chooser.getSelectedFile());
            }
        });

        btnAnterior.addActionListener(e -> {
            if (indice > 0) {
                indice--;
                mostrarActual();
            }
        });

        btnSiguiente.addActionListener(e -> {
            if (indice < imagenes.size() - 1) {
                indice++;
                mostrarActual();
            }
        });
    }

    private void cargarCarpeta(File carpeta) {
        btnCarpeta.setEnabled(false);
        btnAnterior.setEnabled(false);
        btnSiguiente.setEnabled(false);

        etiqueta.setIcon(null);
        etiqueta.setText("Cargando imagenes...");

        new CargaCarpetaWorker(carpeta).execute();
    }

    private void mostrarActual() {
        File f = imagenes.get(indice);
        etiqueta.setText("");
        etiqueta.setIcon(new ImageIcon(f.getPath()));
    }

    private class CargaCarpetaWorker extends SwingWorker<List<File>, Void> {

        private final File carpeta;

        CargaCarpetaWorker(File carpeta) {
            this.carpeta = carpeta;
        }

        @Override
        protected List<File> doInBackground() {
            List<File> encontradas = new ArrayList<>();
            File[] hijos = carpeta.listFiles();
            if (hijos != null) {
                for (File f : hijos) {
                    String nombre = f.getName().toLowerCase();
                    if (nombre.endsWith(".png") || nombre.endsWith(".jpg")
                            || nombre.endsWith(".jpeg") || nombre.endsWith(".gif")) {
                        encontradas.add(f);
                    }
                }
            }
            return encontradas;
        }

        // Se ejecuta de vuelta en el EDT: aqui ya es seguro tocar la interfaz.
        @Override
        protected void done() {
            btnCarpeta.setEnabled(true);
            btnAnterior.setEnabled(true);
            btnSiguiente.setEnabled(true);

            try {
                imagenes.clear();
                imagenes.addAll(get());
                indice = 0;

                if (imagenes.isEmpty()) {
                    etiqueta.setIcon(null);
                    etiqueta.setText("No hay imagenes en esa carpeta.");
                } else {
                    mostrarActual();
                }
            } catch (InterruptedException | ExecutionException ex) {
                etiqueta.setIcon(null);
                etiqueta.setText("Error al cargar la carpeta: " + ex.getMessage());
            }
        }
    }
}
