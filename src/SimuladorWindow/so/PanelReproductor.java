package SimuladorWindow.so;

import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.ui.Estilo;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Reproductor de musica (enunciado 3.7).
 *
 * YA FUNCIONA: agregar canciones a la lista desde el navegador de archivos y
 * ver la lista, el hueco de la caratula y el de la descripcion.
 *
 * FALTA (Oscar, Iteracion 3.7): conectar Play / Pause / Stop con un
 * HiloReproductor real y mostrar la caratula y la descripcion de la cancion.
 */
public class PanelReproductor extends JPanel {

    private final DefaultListModel<String> modeloLista = new DefaultListModel<>();
    private final List<File> canciones = new ArrayList<>();

    private final JList<String> lista = new JList<>(modeloLista);
    private final JLabel caratula = new JLabel("Sin caratula", SwingConstants.CENTER);
    private final JTextArea descripcion = new JTextArea(3, 20);

    private HiloReproductor hilo;
    private int indiceActual = -1;

    public PanelReproductor(Usuario usuarioActual, File carpetaRaiz) {
        setLayout(new BorderLayout());
        setBackground(Estilo.PANEL);

        lista.setFont(Estilo.NORMAL);
        JScrollPane scrollLista = new JScrollPane(lista);
        scrollLista.setBorder(Estilo.hundido());
        scrollLista.setPreferredSize(new Dimension(180, 10));
        add(scrollLista, BorderLayout.WEST);

        caratula.setOpaque(true);
        caratula.setBackground(Color.WHITE);
        caratula.setBorder(Estilo.hundido());
        caratula.setFont(Estilo.NORMAL);

        descripcion.setEditable(false);
        descripcion.setFont(Estilo.MONOESPACIADA);
        JScrollPane scrollDesc = new JScrollPane(descripcion);
        scrollDesc.setBorder(Estilo.hundido());

        JPanel centro = new JPanel(new BorderLayout(4, 4));
        centro.setBackground(Estilo.PANEL);
        centro.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        centro.add(caratula, BorderLayout.CENTER);
        centro.add(scrollDesc, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        barra.setRollover(true);
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.SOMBRA));

        JButton btnAgregar = new JButton("Agregar cancion");
        JButton btnPlay = new JButton("Play");
        JButton btnPause = new JButton("Pause");
        JButton btnStop = new JButton("Stop");

        barra.add(btnAgregar);
        barra.add(btnPlay);
        barra.add(btnPause);
        barra.add(btnStop);
        add(barra, BorderLayout.NORTH);

        btnAgregar.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(carpetaRaiz);
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Musica (mp3, wav)", "mp3", "wav"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File cancion = chooser.getSelectedFile();
                canciones.add(cancion);
                modeloLista.addElement(cancion.getName());
            }
        });

        // TODO (Oscar) - Iteracion 3.7: usar HiloReproductor.
        //   Play:  int i = lista.getSelectedIndex();  (si i < 0, avisar)
        //          si no hay hilo -> crear HiloReproductor con canciones.get(i)
        //          y start();  si estaba pausado -> hilo.reanudar();
        //   Pause: hilo.pausar();
        //   Stop:  hilo.detener();  hilo = null;
        //   Ademas: mostrar aqui la caratula (caratula.setIcon(...)) y la
        //           descripcion (descripcion.setText(...)) de la cancion.
        btnPlay.addActionListener(e -> reproducir());
        btnPause.addActionListener(e -> pausar());
        btnStop.addActionListener(e -> detener());
    }

    private void reproducir() {
        int i = lista.getSelectedIndex();
        if (i < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una cancion de la lista.");
            return;
        }
        if (hilo != null && i == indiceActual) {
            hilo.reanudar();
            return;
        }
        detener();
        File cancion = canciones.get(i);
        mostrarInfo(cancion);
        hilo = new HiloReproductor(cancion);
        indiceActual = i;
        hilo.start();
    }

    private void pausar() {
        if (hilo != null) {
            hilo.pausar();
        }
    }

    private void detener() {
        if (hilo != null) {
            hilo.detener();
            hilo = null;
            indiceActual = -1;
        }
    }

    private void mostrarInfo(File cancion) {
        long kb = cancion.length() / 1024;
        descripcion.setText(
                "Cancion: " + cancion.getName()
                        + "\nCarpeta: " + cancion.getParent()
                        + "\nTamano: " + kb + " KB");

        File archivoCaratula = buscarCaratula(cancion.getParentFile());
        if (archivoCaratula != null) {
            ImageIcon icono = new ImageIcon(archivoCaratula.getPath());
            Image escalada = icono.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            caratula.setIcon(new ImageIcon(escalada));
            caratula.setText("");
        } else {
            caratula.setIcon(null);
            caratula.setText("Sin caratula");
        }
    }

    private File buscarCaratula(File carpeta) {
        if (carpeta == null) {
            return null;
        }
        String[] nombresPosibles = {
                "cover.jpg", "cover.jpeg", "cover.png",
                "folder.jpg", "folder.jpeg", "folder.png"
        };
        for (String nombre : nombresPosibles) {
            File f = new File(carpeta, nombre);
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }

    // ya no se necesita en esta clase
    private void pendiente() {
        JOptionPane.showMessageDialog(this,
                "Reproduccion: falta implementarla (ver el comentario TODO en PanelReproductor).");
    }
}
