package SimuladorWindow.so;

import SimuladorWindow.modelo.Rol;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.ui.Estilo;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class PanelReproductor extends JPanel {

    private final DefaultListModel<String> modeloLista = new DefaultListModel<>();
    private final List<File> canciones = new ArrayList<>();
    private final Usuario usuarioActual;
    private final File carpetaRaiz;
    private final File carpetaMusica;

    private final JList<String> lista = new JList<>(modeloLista);
    private final JLabel caratula = new JLabel("Sin caratula", SwingConstants.CENTER);
    private final JTextArea descripcion = new JTextArea(3, 20);

    private HiloReproductor hilo;
    private int indiceActual = -1;

    private boolean esAudioPermitido(File archivo) {
        String nombre = archivo.getName().toLowerCase();
        return nombre.endsWith(".mp3") || nombre.endsWith(".wav");
    }

    private File obtenerCarpetaMusica() {
        if (usuarioActual.getRol() == Rol.ADMINISTRADOR) {
            return new File(Rutas.DISCO_Z, usuarioActual.getUsername() + File.separator + "Música");
        }
        return new File(carpetaRaiz, "Música");
    }

    private void asegurarCarpetaMusica() {
        if (!carpetaMusica.exists()) {
            carpetaMusica.mkdirs();
        }
    }

    private void cargarCancionesGuardadas() {
        File[] archivos = carpetaMusica.listFiles();
        if (archivos == null) {
            return;
        }

        Arrays.sort(archivos, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        for (File archivo : archivos) {
            if (archivo.isFile() && esAudioPermitido(archivo)) {
                canciones.add(archivo);
                modeloLista.addElement(archivo.getName());
            }
        }
    }

    private void agregarCancion() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(carpetaMusica);
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Musica (mp3, wav)", "mp3", "wav"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File origen = chooser.getSelectedFile();
        File destino = new File(carpetaMusica, origen.getName());

        try {
            if (!origen.getCanonicalFile().equals(destino.getCanonicalFile())) {
                Files.copy(origen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            int indiceExistente = buscarCancion(destino);
            if (indiceExistente >= 0) {
                canciones.set(indiceExistente, destino);
                modeloLista.set(indiceExistente, destino.getName());
            } else {
                canciones.add(destino);
                modeloLista.addElement(destino.getName());
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo guardar la canción: " + ex.getMessage());
        }
    }

    private int buscarCancion(File archivo) {
        for (int i = 0; i < canciones.size(); i++) {
            if (canciones.get(i).getName().equalsIgnoreCase(archivo.getName())) {
                return i;
            }
        }
        return -1;
    }

    public PanelReproductor(Usuario usuarioActual, File carpetaRaiz) {
        this.usuarioActual = usuarioActual;
        this.carpetaRaiz = carpetaRaiz;
        this.carpetaMusica = obtenerCarpetaMusica();
        asegurarCarpetaMusica();
        cargarCancionesGuardadas();

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

        btnAgregar.addActionListener(e -> agregarCancion());
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
}
