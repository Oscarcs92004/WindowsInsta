package miniwindows.so;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import miniwindows.modelo.Usuario;

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

    public PanelReproductor(Usuario usuarioActual, File carpetaRaiz) {
        setLayout(new BorderLayout());

        add(new JScrollPane(lista), BorderLayout.WEST);

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(caratula, BorderLayout.CENTER);
        descripcion.setEditable(false);
        centro.add(new JScrollPane(descripcion), BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        JToolBar barra = new JToolBar();
        barra.setFloatable(false);

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
        btnPlay.addActionListener(e -> pendiente());
        btnPause.addActionListener(e -> pendiente());
        btnStop.addActionListener(e -> pendiente());
    }

    private void pendiente() {
        JOptionPane.showMessageDialog(this,
                "Reproduccion: falta implementarla (ver el comentario TODO en PanelReproductor).");
    }
}
