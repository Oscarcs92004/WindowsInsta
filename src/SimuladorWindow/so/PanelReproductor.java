package SimuladorWindow.so;

import SimuladorWindow.modelo.Rol;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.ui.Estilo;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class PanelReproductor extends JPanel {

    private static final int LADO_CARATULA = 200;

    private final DefaultListModel<String> modeloLista = new DefaultListModel<>();
    private final List<File> canciones = new ArrayList<>();
    private final Usuario usuarioActual;
    private final File carpetaRaiz;
    private final File carpetaMusica;

    private final JList<String> lista = new JList<>(modeloLista);
    private final JLabel caratula = new JLabel("Sin caratula", SwingConstants.CENTER);
    private final JTextArea descripcion = new JTextArea(5, 20);

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
        lista.addListSelectionListener(e -> {
            int i = lista.getSelectedIndex();
            if (!e.getValueIsAdjusting() && i >= 0) {
                mostrarInfo(canciones.get(i));
            }
        });
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
        if (hilo != null && hilo.isAlive() && i == indiceActual) {
            hilo.reanudar();
            return;
        }
        detener();
        File cancion = canciones.get(i);
        hilo = new HiloReproductor(cancion);
        indiceActual = i;
        hilo.start();
    }

    public void reproducirArchivo(File archivo) {
        int indice = -1;
        for (int i = 0; i < canciones.size(); i++) {
            if (canciones.get(i).getAbsoluteFile().equals(archivo.getAbsoluteFile())) {
                indice = i;
                break;
            }
        }
        if (indice < 0) {
            canciones.add(archivo);
            modeloLista.addElement(archivo.getName());
            indice = canciones.size() - 1;
        }
        lista.setSelectedIndex(indice);
        lista.ensureIndexIsVisible(indice);
        reproducir();
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
        EtiquetasMp3 etiquetas = EtiquetasMp3.leer(cancion);

        StringBuilder texto = new StringBuilder();
        texto.append("Cancion: ")
                .append(etiquetas.getTitulo() != null ? etiquetas.getTitulo() : cancion.getName());
        if (etiquetas.getArtista() != null) {
            texto.append("\nArtista: ").append(etiquetas.getArtista());
        }
        if (etiquetas.getAlbum() != null) {
            texto.append("\nAlbum: ").append(etiquetas.getAlbum());
        }
        texto.append("\nArchivo: ").append(cancion.getName());
        texto.append("\nCarpeta: ").append(cancion.getParent());
        texto.append("\nTamano: ").append(cancion.length() / 1024).append(" KB");
        descripcion.setText(texto.toString());
        descripcion.setCaretPosition(0);

        ImageIcon icono = null;
        if (etiquetas.getCaratula() != null) {
            icono = new ImageIcon(etiquetas.getCaratula());
        }
        if (icono == null || icono.getIconWidth() <= 0) {
            File archivoCaratula = buscarCaratula(cancion);
            icono = (archivoCaratula != null) ? new ImageIcon(archivoCaratula.getPath()) : null;
        }

        if (icono != null && icono.getIconWidth() > 0) {
            caratula.setIcon(escalar(icono));
        } else {
            String clave = etiquetas.getAlbum() != null ? etiquetas.getAlbum() : cancion.getName();
            caratula.setIcon(caratulaGenerica(clave));
        }
        caratula.setText("");
    }

    private ImageIcon escalar(ImageIcon icono) {
        int ancho = icono.getIconWidth();
        int alto = icono.getIconHeight();
        double factor = Math.min((double) LADO_CARATULA / ancho, (double) LADO_CARATULA / alto);
        int nuevoAncho = Math.max(1, (int) Math.round(ancho * factor));
        int nuevoAlto = Math.max(1, (int) Math.round(alto * factor));
        return new ImageIcon(icono.getImage()
                .getScaledInstance(nuevoAncho, nuevoAlto, Image.SCALE_SMOOTH));
    }

    private File buscarCaratula(File cancion) {
        File carpeta = cancion.getParentFile();
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

    private ImageIcon caratulaGenerica(String clave) {
        float tono = (clave.hashCode() & 0x7FFFFFFF) % 360 / 360f;
        Color claro = Color.getHSBColor(tono, 0.45f, 0.85f);
        Color oscuro = Color.getHSBColor(tono, 0.60f, 0.45f);

        int lado = LADO_CARATULA;
        BufferedImage img = new BufferedImage(lado, lado, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, claro, lado, lado, oscuro));
        g.fillRect(0, 0, lado, lado);

        g.setColor(new Color(255, 255, 255, 230));
        int cabeza = lado / 5;
        int x = lado / 2 - cabeza / 2;
        int y = lado * 5 / 8;
        g.fill(new Ellipse2D.Double(x - cabeza / 2.0, y, cabeza * 1.3, cabeza));
        int plica = x + (int) (cabeza * 0.8);
        g.fillRect(plica, lado / 4, lado / 25, y - lado / 4 + cabeza / 2);
        Path2D bandera = new Path2D.Double();
        bandera.moveTo(plica, lado / 4.0);
        bandera.curveTo(plica + lado / 5.0, lado / 3.0, plica + lado / 4.0, lado / 2.2,
                plica + lado / 8.0, lado / 1.8);
        bandera.curveTo(plica + lado / 6.0, lado / 2.4, plica + lado / 10.0, lado / 2.8,
                plica, lado / 2.6);
        bandera.closePath();
        g.fill(bandera);
        g.dispose();
        return new ImageIcon(img);
    }
}
