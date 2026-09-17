package SimuladorWindow.so;

import SimuladorWindow.estructuras.ListaEnlazada;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.ui.Estilo;
import SimuladorWindow.ui.PanelEscritorio;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PanelExplorador extends JPanel {

    private static final String[] EXT_IMAGENES   = {"jpg","jpeg","png","gif","bmp","svg","webp","ico","tiff"};
    private static final String[] EXT_DOCUMENTOS = {"pdf","doc","docx","xls","xlsx","ppt","pptx","txt","odt","csv","xml","json"};
    private static final String[] EXT_MUSICA     = {"mp3","wav","ogg","flac","aac","m4a","wma","opus"};

    private Usuario usuarioActual;
    private final File carpetaRaiz;
    private final JTree arbol;
    private final PanelEscritorio escritorio;

    private File portapapeles;
    private String criterioOrden = "nombre";

    private final JProgressBar barraProgreso;
    private final JLabel       etiquetaProgreso;
    private final JLabel       lblConteo = new JLabel();

    private void abrirEnEditor(File archivo) {
        PanelEditor editor = new PanelEditor(usuarioActual, carpetaRaiz);
        editor.abrirArchivoDesdeExplorador(archivo);
        escritorio.abrirApp("Editor de texto", editor);
    }

    public PanelExplorador(Usuario usuarioActual, File carpetaRaiz, PanelEscritorio escritorio) {
        this.usuarioActual = usuarioActual;
        this.escritorio = escritorio;
        this.carpetaRaiz = carpetaRaiz;

        setLayout(new BorderLayout());
        setBackground(Estilo.PANEL);

        arbol = new JTree(construirModelo());
        arbol.setCellRenderer(new RendererSoloNombre());
        arbol.setBackground(Color.WHITE);
        arbol.setRowHeight(19);
        arbol.setShowsRootHandles(true);
        arbol.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    File archivo = archivoSeleccionado();
                    if (archivo == null || !archivo.isFile()) {
                        return;
                    }
                    String ext = extension(archivo);
                    if (ext.equals("txt")) {
                        abrirEnEditor(archivo);
                    } else if (ext.equals("mp3") || ext.equals("wav")) {
                        escritorio.reproducirCancion(archivo);
                    } else if (ext.equals("png") || ext.equals("jpg")
                            || ext.equals("jpeg") || ext.equals("gif")) {
                        escritorio.verImagen(archivo);
                    }
                }
            }
        });
        JScrollPane scroll = new JScrollPane(arbol);
        scroll.setBorder(Estilo.hundido());
        add(scroll, BorderLayout.CENTER);

        barraProgreso    = new JProgressBar(0, 100);
        etiquetaProgreso = new JLabel("  ");
        lblConteo.setFont(Estilo.NORMAL);
        lblConteo.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        JPanel panelSur  = new JPanel(new BorderLayout());
        panelSur.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.SOMBRA),
                BorderFactory.createEmptyBorder(1, 0, 0, 0)));
        panelSur.add(etiquetaProgreso, BorderLayout.WEST);
        panelSur.add(barraProgreso,    BorderLayout.CENTER);
        panelSur.add(lblConteo,        BorderLayout.EAST);
        barraProgreso.setVisible(false);
        etiquetaProgreso.setVisible(false);
        add(panelSur, BorderLayout.SOUTH);
        actualizarConteo();

        JToolBar barraArchivos = new JToolBar();
        barraArchivos.setFloatable(false);
        barraArchivos.setRollover(true);

        JToolBar barraOrden = new JToolBar();
        barraOrden.setFloatable(false);
        barraOrden.setRollover(true);
        barraOrden.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.SOMBRA));

        JButton btnRefrescar = new JButton("Refrescar");
        JButton btnNueva = new JButton("Nueva carpeta");
        JButton btnNuevoArchivo = new JButton("Nuevo archivo");
        JButton btnRenombrar = new JButton("Renombrar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnCopiar = new JButton("Copiar");
        JButton btnPegar = new JButton("Pegar");
        JButton btnImportar = new JButton("Importar");
        JComboBox<String> cmbOrden = new JComboBox<>(
                new String[]{"nombre", "fecha", "tipo", "tamaño"});
        JButton   btnOrdenar   = new JButton("Ordenar");
        JButton btnOrganizar = new JButton("Organizar");

        barraArchivos.add(btnRefrescar);
        barraArchivos.add(btnNueva);
        barraArchivos.add(btnNuevoArchivo);
        barraArchivos.add(btnRenombrar);
        barraArchivos.add(btnEliminar);
        barraArchivos.add(btnCopiar);
        barraArchivos.add(btnPegar);
        barraArchivos.add(btnImportar);

        barraOrden.add(new JLabel(" Orden: "));
        barraOrden.add(cmbOrden);
        barraOrden.add(btnOrdenar);
        barraOrden.addSeparator();
        barraOrden.add(btnOrganizar);

        JPanel barras = new JPanel(new GridLayout(2, 1));
        barras.add(barraArchivos);
        barras.add(barraOrden);
        add(barras, BorderLayout.NORTH);

        btnRefrescar.addActionListener(e -> refrescar());

        btnNueva.addActionListener(e -> {
            String nombre = JOptionPane.showInputDialog(this, "Nombre de la carpeta:");
            if (nombre != null && !nombre.trim().isEmpty()) {
                File nueva = new File(carpetaDestino(), nombre.trim());
                if (!nueva.mkdir()) {
                    JOptionPane.showMessageDialog(this, "No se pudo crear la carpeta.");
                }
                refrescar();
            }
        });

        btnNuevoArchivo.addActionListener(e -> {
            String nombre = JOptionPane.showInputDialog(this, "Nombre del archivo:");
            if (nombre == null || nombre.trim().isEmpty()) {
                return;
            }
            File nuevo = new File(carpetaDestino(), nombre.trim());
            if (nuevo.exists()) {
                JOptionPane.showMessageDialog(this, "Ya existe un archivo con ese nombre.");
                return;
            }
            try {
                if (!nuevo.createNewFile()) {
                    JOptionPane.showMessageDialog(this, "No se pudo crear el archivo.");
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al crear el archivo: " + ex.getMessage());
            }
            refrescar();
        });

        btnRenombrar.addActionListener(e -> {
            File sel = archivoSeleccionado();
            if (sel == null) {
                JOptionPane.showMessageDialog(this,
                        "Seleccione un archivo o carpeta para renombrar.");
                return;
            }
            String nombreNuevo = JOptionPane.showInputDialog(
                    this, "Nuevo nombre:", sel.getName());
            if (nombreNuevo == null || nombreNuevo.trim().isEmpty()) {
                return;
            }
            File destino = new File(sel.getParentFile(), nombreNuevo.trim());
            if (destino.exists()) {
                JOptionPane.showMessageDialog(this,
                        "Ya existe un archivo con ese nombre.");
                return;
            }
            if (!sel.renameTo(destino)) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo renombrar. Compruebe permisos.");
                return;
            }
            refrescar();
        });

        btnEliminar.addActionListener(e -> {
            File sel = archivoSeleccionado();
            if (sel == null) {
                JOptionPane.showMessageDialog(this,
                        "Seleccione un archivo o carpeta para eliminar.");
                return;
            }
            int resp = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar \"" + sel.getName() + "\"? Esta acción no se puede deshacer.",
                    "Eliminar", JOptionPane.YES_NO_OPTION);
            if (resp != JOptionPane.YES_OPTION) {
                return;
            }
            if (!eliminarRecursivo(sel)) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo eliminar \"" + sel.getName() + "\".");
            }
            refrescar();
        });

        btnCopiar.addActionListener(e -> {
            File sel = archivoSeleccionado();
            if (sel == null) {
                JOptionPane.showMessageDialog(this,
                        "Seleccione un archivo o carpeta para copiar.");
                return;
            }
            portapapeles = sel;
            JOptionPane.showMessageDialog(this,
                    "Copiado: " + sel.getName());
        });

        btnPegar.addActionListener(e -> {
            if (portapapeles == null) {
                JOptionPane.showMessageDialog(this,
                        "No hay nada en el portapapeles. Use Copiar primero.");
                return;
            }
            File destDir = carpetaDestino();
            if (portapapeles.isDirectory()) {
                try {
                    String origenPath  = portapapeles.getCanonicalPath();
                    String destinoPath = destDir.getCanonicalPath();
                    if (destinoPath.equals(origenPath)
                            || destinoPath.startsWith(origenPath + File.separator)) {
                        JOptionPane.showMessageDialog(this,
                                "No se puede pegar una carpeta dentro de sí misma.");
                        return;
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error al verificar rutas: " + ex.getMessage());
                    return;
                }
            }

            try {
                if (portapapeles.isDirectory()) {
                    copiarCarpetaRecursivo(portapapeles,
                            new File(destDir, portapapeles.getName()));
                } else {
                    Files.copy(portapapeles.toPath(),
                            new File(destDir, portapapeles.getName()).toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al pegar: " + ex.getMessage());
            }
            refrescar();
        });

        btnImportar.addActionListener(e -> importar(btnImportar));

        btnOrdenar.addActionListener(e -> {
            criterioOrden = (String) cmbOrden.getSelectedItem();
            refrescar();
        });

        btnOrganizar.addActionListener(e -> {
            File carpetaOrganizar = carpetaDestino();
            int resp = JOptionPane.showConfirmDialog(this,
                    "Se organizará la carpeta:\n" + carpetaOrganizar.getPath()
                            + "\n\nLos archivos se moverán a subcarpetas según su tipo.\n¿Continuar?",
                    "Organizar", JOptionPane.YES_NO_OPTION);
            if (resp != JOptionPane.YES_OPTION) {
                return;
            }
            btnOrganizar.setEnabled(false);
            btnPegar.setEnabled(false);
            barraProgreso.setValue(0);
            barraProgreso.setVisible(true);
            etiquetaProgreso.setVisible(true);

            new OrganizadorWorker(carpetaOrganizar).execute();
        });
    }

    private class OrganizadorWorker extends SwingWorker<Void, String> {

        private final File carpeta;

        private final ListaEnlazada<File> listaImagenes   = new ListaEnlazada<>();
        private final ListaEnlazada<File> listaDocumentos = new ListaEnlazada<>();
        private final ListaEnlazada<File> listaMusica     = new ListaEnlazada<>();
        private final ListaEnlazada<File> listaOtros      = new ListaEnlazada<>();

        OrganizadorWorker(File carpeta) {
            this.carpeta = carpeta;
        }

        @Override
        protected Void doInBackground() throws Exception {
            File[] archivos = carpeta.listFiles(File::isFile);
            if (archivos == null || archivos.length == 0) {
                publish("Sin archivos para organizar.");
                setProgress(100);
                return null;
            }

            for (File f : archivos) {
                String ext = extension(f).toLowerCase();
                if (contiene(EXT_IMAGENES,        ext)) listaImagenes.agregarFinal(f);
                else if (contiene(EXT_DOCUMENTOS, ext)) listaDocumentos.agregarFinal(f);
                else if (contiene(EXT_MUSICA,     ext)) listaMusica.agregarFinal(f);
                else                                     listaOtros.agregarFinal(f);
            }

            int total = archivos.length;
            int movidos = 0;

            movidos = moverLista(listaImagenes,   "imagenes",   total, movidos);
            movidos = moverLista(listaDocumentos, "documentos", total, movidos);
            movidos = moverLista(listaMusica,     "musica",     total, movidos);
            movidos = moverLista(listaOtros,      "otros",      total, movidos);

            setProgress(100);
            return null;
        }

        private int moverLista(ListaEnlazada<File> lista,
                               String nombreSubcarpeta,
                               int total, int movidos) throws IOException {

            if (lista.tamano() == 0) return movidos;

            File sub = new File(carpeta, nombreSubcarpeta);
            sub.mkdirs();

            for (File archivo : lista.comoLista()) {
                File dest = new File(sub, archivo.getName());
                Files.move(archivo.toPath(), dest.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                movidos++;
                int pct = (int) (((double) movidos / total) * 100);
                setProgress(pct);
                publish("Moviendo: " + archivo.getName() + " → " + nombreSubcarpeta + "/");
            }
            return movidos;
        }

        @Override
        protected void process(List<String> mensajes) {
            String ultimo = mensajes.get(mensajes.size() - 1);
            etiquetaProgreso.setText(" " + ultimo);
            barraProgreso.setValue(getProgress());
        }

        @Override
        protected void done() {
            refrescar();
            barraProgreso.setVisible(false);
            etiquetaProgreso.setVisible(false);
            ((JButton) getComponentFromBar("Organizar")).setEnabled(true);
            ((JButton) getComponentFromBar("Pegar")).setEnabled(true);

            System.out.println("── Resumen Organizar ──────────────────");
            System.out.println("  Imágenes   : " + listaImagenes.tamano());
            System.out.println("  Documentos : " + listaDocumentos.tamano());
            System.out.println("  Música     : " + listaMusica.tamano());
            System.out.println("  Otros      : " + listaOtros.tamano());
            System.out.println("────────────────────────────────────────");

            JOptionPane.showMessageDialog(PanelExplorador.this,
                    String.format("Organización completada.\n"
                                    + "  Imágenes   : %d\n"
                                    + "  Documentos : %d\n"
                                    + "  Música     : %d\n"
                                    + "  Otros      : %d",
                            listaImagenes.tamano(),
                            listaDocumentos.tamano(),
                            listaMusica.tamano(),
                            listaOtros.tamano()));
        }

        private Component getComponentFromBar(String texto) {
            Container barras = (Container) PanelExplorador.this.getComponent(2);
            for (Component fila : barras.getComponents()) {
                if (!(fila instanceof JToolBar)) {
                    continue;
                }
                for (Component c : ((JToolBar) fila).getComponents()) {
                    if (c instanceof JButton && texto.equals(((JButton) c).getText())) {
                        return c;
                    }
                }
            }
            return new JButton();
        }
    }


    private void importar(JButton boton) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Importar archivos o carpetas");
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File destino = carpetaDestino();
        ListaEnlazada<File> elegidos = new ListaEnlazada<>();
        ListaEnlazada<File> repetidos = new ListaEnlazada<>();
        for (File origen : chooser.getSelectedFiles()) {
            if (contieneA(origen, destino)) {
                JOptionPane.showMessageDialog(this,
                        "No se puede importar \"" + origen.getName() + "\" dentro de sí misma.");
                continue;
            }
            elegidos.agregarFinal(origen);
            if (new File(destino, origen.getName()).exists()) {
                repetidos.agregarFinal(origen);
            }
        }
        if (elegidos.tamano() == 0) {
            return;
        }

        if (repetidos.tamano() > 0) {
            int op = JOptionPane.showConfirmDialog(this,
                    repetidos.tamano() + " elemento(s) ya existen en \"" + destino.getName()
                            + "\".\n¿Reemplazarlos?",
                    "Importar", JOptionPane.YES_NO_CANCEL_OPTION);
            if (op != JOptionPane.YES_OPTION && op != JOptionPane.NO_OPTION) {
                return;
            }
            if (op == JOptionPane.NO_OPTION) {
                for (File f : repetidos.comoLista()) {
                    elegidos.eliminar(f);
                }
                if (elegidos.tamano() == 0) {
                    return;
                }
            }
        }

        boton.setEnabled(false);
        barraProgreso.setValue(0);
        barraProgreso.setVisible(true);
        etiquetaProgreso.setVisible(true);
        new ImportadorWorker(elegidos, destino, boton).execute();
    }

    private static boolean contieneA(File carpeta, File otro) {
        try {
            String rutaCarpeta = carpeta.getCanonicalPath();
            String rutaOtro = otro.getCanonicalPath();
            return rutaOtro.equals(rutaCarpeta)
                    || rutaOtro.startsWith(rutaCarpeta + File.separator);
        } catch (IOException e) {
            return true;
        }
    }

    private class ImportadorWorker extends SwingWorker<Integer, String> {

        private final ListaEnlazada<File> origenes;
        private final File destino;
        private final JButton boton;
        private int total;
        private int copiados;
        private int fallidos;

        ImportadorWorker(ListaEnlazada<File> origenes, File destino, JButton boton) {
            this.origenes = origenes;
            this.destino = destino;
            this.boton = boton;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            for (File origen : origenes.comoLista()) {
                total += contarArchivos(origen.toPath());
            }
            for (File origen : origenes.comoLista()) {
                copiar(origen.toPath(), new File(destino, origen.getName()).toPath());
            }
            setProgress(100);
            return copiados;
        }

        private int contarArchivos(Path origen) throws IOException {
            int[] cuenta = {0};
            Files.walkFileTree(origen, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path archivo, BasicFileAttributes atributos) {
                    cuenta[0]++;
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path archivo, IOException e) {
                    return FileVisitResult.CONTINUE;
                }
            });
            return cuenta[0];
        }

        private void copiar(Path origen, Path copia) throws IOException {
            Files.walkFileTree(origen, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path carpeta, BasicFileAttributes atributos)
                        throws IOException {
                    Files.createDirectories(copia.resolve(origen.relativize(carpeta)));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path archivo, BasicFileAttributes atributos) {
                    try {
                        Files.copy(archivo, copia.resolve(origen.relativize(archivo)),
                                StandardCopyOption.REPLACE_EXISTING);
                        copiados++;
                        publish("Importando: " + archivo.getFileName());
                    } catch (IOException e) {
                        fallidos++;
                    }
                    setProgress(Math.min(100, (copiados + fallidos) * 100 / Math.max(1, total)));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path archivo, IOException e) {
                    fallidos++;
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        @Override
        protected void process(List<String> mensajes) {
            etiquetaProgreso.setText(" " + mensajes.get(mensajes.size() - 1));
            barraProgreso.setValue(getProgress());
        }

        @Override
        protected void done() {
            refrescar();
            barraProgreso.setVisible(false);
            etiquetaProgreso.setVisible(false);
            boton.setEnabled(true);
            try {
                String mensaje = "Se importaron " + get() + " archivo(s) en \"" + destino.getName() + "\".";
                if (fallidos > 0) {
                    mensaje += "\nNo se pudieron copiar " + fallidos + " archivo(s).";
                }
                JOptionPane.showMessageDialog(PanelExplorador.this, mensaje);
            } catch (InterruptedException | ExecutionException ex) {
                JOptionPane.showMessageDialog(PanelExplorador.this,
                        "Error al importar: " + ex.getMessage());
            }
        }
    }

    private void copiarCarpetaRecursivo(File origen, File destino) throws IOException {
        destino.mkdirs();
        File[] hijos = origen.listFiles();
        if (hijos == null) return;
        for (File hijo : hijos) {
            File dest = new File(destino, hijo.getName());
            if (hijo.isDirectory()) {
                copiarCarpetaRecursivo(hijo, dest);
            } else {
                Files.copy(hijo.toPath(), dest.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private boolean eliminarRecursivo(File archivo) {
        if (archivo.isDirectory()) {
            File[] hijos = archivo.listFiles();
            if (hijos != null) {
                for (File hijo : hijos) {
                    if (!eliminarRecursivo(hijo)) {
                        return false;
                    }
                }
            }
        }
        return archivo.delete();
    }

    private static String extension(File f) {
        String nombre = f.getName();
        int punto = nombre.lastIndexOf('.');
        return (punto >= 0) ? nombre.substring(punto + 1).toLowerCase() : "";
    }

    private static boolean contiene(String[] arr, String ext) {
        for (String s : arr) if (s.equals(ext)) return true;
        return false;
    }

    private File carpetaDestino() {
        File sel = archivoSeleccionado();
        if (sel != null && sel.isDirectory()) {
            return sel;
        }
        return carpetaRaiz;
    }

    private File archivoSeleccionado() {
        DefaultMutableTreeNode nodo =
                (DefaultMutableTreeNode) arbol.getLastSelectedPathComponent();
        if (nodo == null) {
            return null;
        }
        Object valor = nodo.getUserObject();
        if (valor instanceof File) {
            return (File) valor;
        }
        return null;
    }

    private void refrescar() {
        arbol.setModel(construirModelo());
        actualizarConteo();
    }

    private void actualizarConteo() {
        int[] cuenta = {0, 0};
        contar(carpetaRaiz, cuenta);
        lblConteo.setText(cuenta[0] + " carpetas, " + cuenta[1] + " archivos");
    }

    private void contar(File carpeta, int[] cuenta) {
        File[] hijos = carpeta.listFiles();
        if (hijos == null) {
            return;
        }
        for (File hijo : hijos) {
            if (hijo.isDirectory()) {
                cuenta[0]++;
                contar(hijo, cuenta);
            } else {
                cuenta[1]++;
            }
        }
    }

    private DefaultTreeModel construirModelo() {
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode(carpetaRaiz);
        agregarHijos(raiz, carpetaRaiz);
        return new DefaultTreeModel(raiz);
    }

    private void agregarHijos(DefaultMutableTreeNode nodo, File carpeta) {
        File[] hijos = carpeta.listFiles();
        if (hijos == null) {
            return;
        }

        List<File> lista = new ArrayList<>(Arrays.asList(hijos));

        switch (criterioOrden) {
            case "nombre":
                lista.sort(Comparator.comparing(f -> f.getName().toLowerCase()));
                break;
            case "fecha":
                lista.sort(Comparator.comparingLong(File::lastModified));
                break;
            case "tamaño":
                lista.sort(Comparator.comparingLong(File::length));
                break;
            case "tipo":
                lista.sort(Comparator.comparing(PanelExplorador::extension));
                break;
        }

        for (File hijo : lista) {
            DefaultMutableTreeNode nodoHijo = new DefaultMutableTreeNode(hijo);
            nodo.add(nodoHijo);
            if (hijo.isDirectory()) {
                agregarHijos(nodoHijo, hijo);
            }
        }
    }

    private static class RendererSoloNombre extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean exp, boolean leaf, int row, boolean focus) {
            super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, focus);
            Object uo = ((DefaultMutableTreeNode) value).getUserObject();
            if (uo instanceof File) {
                File f = (File) uo;
                setText(f.getName().isEmpty() ? f.getPath() : f.getName());
            }
            return this;
        }
    }
}
