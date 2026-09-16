package SimuladorWindow.so;

import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.ui.Estilo;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PanelVisor extends JPanel {

    private final File carpetaRaiz;
    private final List<File> imagenes = new ArrayList<>();
    private int indice = 0;

    private final JLabel etiqueta = new JLabel("Elige una carpeta con imagenes.",
            SwingConstants.CENTER) {
        @Override
        protected void paintComponent(Graphics g) {
            if (!(getIcon() instanceof ImageIcon)) {
                super.paintComponent(g);
                return;
            }
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());

            Image imagen = ((ImageIcon) getIcon()).getImage();
            int anchoImagen = imagen.getWidth(this);
            int altoImagen = imagen.getHeight(this);
            Insets bordes = getInsets();
            int anchoLibre = getWidth() - bordes.left - bordes.right;
            int altoLibre = getHeight() - bordes.top - bordes.bottom;
            if (anchoImagen <= 0 || altoImagen <= 0 || anchoLibre <= 0 || altoLibre <= 0) {
                return;
            }

            double factor = Math.min(1.0, Math.min((double) anchoLibre / anchoImagen,
                    (double) altoLibre / altoImagen));
            int ancho = Math.max(1, (int) Math.round(anchoImagen * factor));
            int alto = Math.max(1, (int) Math.round(altoImagen * factor));
            int x = bordes.left + (anchoLibre - ancho) / 2;
            int y = bordes.top + (altoLibre - alto) / 2;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(imagen, x, y, ancho, alto, this);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(320, 240);
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(0, 0);
        }
    };
    private final JLabel estado = new JLabel(" ");

    private final JButton btnCarpeta = new JButton("Elegir carpeta");
    private final JButton btnAnterior = new JButton("Anterior");
    private final JButton btnSiguiente = new JButton("Siguiente");

    public PanelVisor(Usuario usuarioActual, File carpetaRaiz) {
        this.carpetaRaiz = carpetaRaiz;

        setLayout(new BorderLayout());
        setBackground(Estilo.PANEL);

        etiqueta.setOpaque(true);
        etiqueta.setBackground(Color.WHITE);
        etiqueta.setBorder(Estilo.hundido());
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(Estilo.PANEL);
        centro.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        centro.add(etiqueta, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        estado.setFont(Estilo.NORMAL);
        estado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.SOMBRA),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        add(estado, BorderLayout.SOUTH);

        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        barra.setRollover(true);
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.SOMBRA));

        barra.add(btnCarpeta);
        barra.add(btnAnterior);
        barra.add(btnSiguiente);
        add(barra, BorderLayout.NORTH);

        btnCarpeta.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(carpetaRaiz);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                cargarCarpeta(chooser.getSelectedFile(), null);
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

    public void abrirImagen(File imagen) {
        cargarCarpeta(imagen.getParentFile(), imagen.getName());
    }

    private void cargarCarpeta(File carpeta, String nombreInicial) {
        btnCarpeta.setEnabled(false);
        btnAnterior.setEnabled(false);
        btnSiguiente.setEnabled(false);

        etiqueta.setIcon(null);
        etiqueta.setText("Cargando imagenes...");

        new CargaCarpetaWorker(carpeta, nombreInicial).execute();
    }

    private void mostrarActual() {
        File f = imagenes.get(indice);
        etiqueta.setText("");
        etiqueta.setIcon(new ImageIcon(f.getPath()));
        estado.setText((indice + 1) + " / " + imagenes.size() + "   -   " + f.getName());
    }

    private class CargaCarpetaWorker extends SwingWorker<List<File>, Void> {

        private final File carpeta;
        private final String nombreInicial;

        CargaCarpetaWorker(File carpeta, String nombreInicial) {
            this.carpeta = carpeta;
            this.nombreInicial = nombreInicial;
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

        @Override
        protected void done() {
            btnCarpeta.setEnabled(true);
            btnAnterior.setEnabled(true);
            btnSiguiente.setEnabled(true);

            try {
                imagenes.clear();
                imagenes.addAll(get());
                indice = 0;
                for (int i = 0; i < imagenes.size(); i++) {
                    if (imagenes.get(i).getName().equals(nombreInicial)) {
                        indice = i;
                        break;
                    }
                }

                if (imagenes.isEmpty()) {
                    etiqueta.setIcon(null);
                    etiqueta.setText("No hay imagenes en esa carpeta.");
                    estado.setText(" ");
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
