package miniwindows.so;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import miniwindows.modelo.Usuario;
import miniwindows.estructuras.ListaEnlazada;

/**
 * Explorador de archivos con JTree (enunciado 3.3).
 *
 * YA FUNCIONA: mostrar el arbol de carpetas, refrescar y crear una carpeta
 * nueva dentro de la carpeta seleccionada.
 *
 * FALTA (ver los TODO de abajo): renombrar, copiar, pegar, ordenar y organizar.
 */
public class PanelExplorador extends JPanel {

    private static final String[] EXT_IMAGENES   = {"jpg","jpeg","png","gif","bmp","svg","webp","ico","tiff"};
    private static final String[] EXT_DOCUMENTOS = {"pdf","doc","docx","xls","xlsx","ppt","pptx","txt","odt","csv","xml","json"};
    private static final String[] EXT_MUSICA     = {"mp3","wav","ogg","flac","aac","m4a","wma","opus"};

    private final File carpetaRaiz;
    private final JTree arbol;

    /** Ruta guardada al pulsar "Copiar", para usarla luego en "Pegar". */
    private File portapapeles;
    private String criterioOrden = "nombre";

    private final JProgressBar barraProgreso;
    private final JLabel       etiquetaProgreso;

    public PanelExplorador(Usuario usuarioActual, File carpetaRaiz) {
        this.carpetaRaiz = carpetaRaiz;

        setLayout(new BorderLayout());

        arbol = new JTree(construirModelo());
        arbol.setCellRenderer(new RendererSoloNombre());
        add(new JScrollPane(arbol), BorderLayout.CENTER);

        barraProgreso    = new JProgressBar(0, 100);
        etiquetaProgreso = new JLabel("  ");
        JPanel panelSur  = new JPanel(new BorderLayout());
        panelSur.add(etiquetaProgreso, BorderLayout.WEST);
        panelSur.add(barraProgreso,    BorderLayout.CENTER);
        barraProgreso.setVisible(false);
        etiquetaProgreso.setVisible(false);
        add(panelSur, BorderLayout.SOUTH);

        JToolBar barra = new JToolBar();
        barra.setFloatable(false);

        JButton btnRefrescar = new JButton("Refrescar");
        JButton btnNueva = new JButton("Nueva carpeta");
        JButton btnRenombrar = new JButton("Renombrar");
        JButton btnCopiar = new JButton("Copiar");
        JButton btnPegar = new JButton("Pegar");
        JComboBox<String> cmbOrden = new JComboBox<>(
                new String[]{"nombre", "fecha", "tipo", "tamaño"});
        JButton   btnOrdenar   = new JButton("Ordenar");
        JButton btnOrganizar = new JButton("Organizar");

        barra.add(btnRefrescar);
        barra.add(btnNueva);
        barra.add(btnRenombrar);
        barra.add(btnCopiar);
        barra.add(btnPegar);
        barra.addSeparator();
        barra.add(new JLabel(" Orden: "));
        barra.add(cmbOrden);
        barra.add(btnOrdenar);
        barra.addSeparator();
        barra.add(btnOrganizar);
        add(barra, BorderLayout.NORTH);

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

        // TODO (Alex) - Iteracion 3.2b: RENOMBRAR
        //   File sel = archivoSeleccionado();
        //   String nuevo = JOptionPane.showInputDialog(...);
        //   sel.renameTo(new File(sel.getParentFile(), nuevo));  luego refrescar();
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

        // TODO (Alex) - Iteracion 3.2b: COPIAR
        //   portapapeles = archivoSeleccionado();   (guardar la ruta y ya)
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

        // TODO (Alex) - Iteracion 3.2b: PEGAR
        //   File destino = new File(carpetaDestino(), portapapeles.getName());
        //   Files.copy(portapapeles.toPath(), destino.toPath(),
        //              StandardCopyOption.REPLACE_EXISTING);
        //   si portapapeles es carpeta, recorrer y copiar cada archivo. refrescar();
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

        // TODO (Alex) - Iteracion 3.2c: ORDENAR
        //   pedir el criterio (nombre / fecha / tipo / tamano) con un JComboBox
        //   y ordenar los hijos con un Comparator<File> antes de pintar el arbol.
        btnOrdenar.addActionListener(e -> {
            criterioOrden = (String) cmbOrden.getSelectedItem();
            refrescar();
        });

        // TODO (Oscar) - Iteracion 3.8: ORGANIZAR
        //   recorrer la carpeta seleccionada y mover cada archivo a las
        //   subcarpetas imagenes/ documentos/ musica/ segun su extension.
        //   Hacerlo con un SwingWorker (no congelar la ventana) y guardar las
        //   rutas de cada tipo en una ListaEnlazada por categoria.
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

        // Listas enlazadas propias (una por categoría) — enunciado 2.4
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

            // 1. Clasificar en listas enlazadas
            for (File f : archivos) {
                String ext = extension(f).toLowerCase();
                if (contiene(EXT_IMAGENES,        ext)) listaImagenes.agregarFinal(f);
                else if (contiene(EXT_DOCUMENTOS, ext)) listaDocumentos.agregarFinal(f);
                else if (contiene(EXT_MUSICA,     ext)) listaMusica.agregarFinal(f);
                else                                     listaOtros.agregarFinal(f);
            }

            int total = archivos.length;
            int movidos = 0;

            // 2. Mover cada lista a su subcarpeta
            movidos = moverLista(listaImagenes,   "imagenes",   total, movidos);
            movidos = moverLista(listaDocumentos, "documentos", total, movidos);
            movidos = moverLista(listaMusica,     "musica",     total, movidos);
            movidos = moverLista(listaOtros,      "otros",      total, movidos);

            setProgress(100);
            return null;
        }

        /** Mueve todos los elementos de una ListaEnlazada a la subcarpeta dada. */
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

        // Llamado en el EDT con los mensajes publicados
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
            // Re-habilitar botones
            ((JButton) getComponentFromBar("Organizar")).setEnabled(true);
            ((JButton) getComponentFromBar("Pegar")).setEnabled(true);

            // Imprimir resumen en consola (útil para depuración)
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

        /** Busca un botón por texto dentro de la barra de herramientas. */
        private Component getComponentFromBar(String texto) {
            JToolBar bar = (JToolBar) PanelExplorador.this.getComponent(2); // NORTH
            for (Component c : bar.getComponents()) {
                if (c instanceof JButton && texto.equals(((JButton) c).getText())) {
                    return c;
                }
            }
            return new JButton(); // fallback seguro
        }
    }

    // --------------------- ayudantes que YA sirven -------------------

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

    private static String extension(File f) {
        String nombre = f.getName();
        int punto = nombre.lastIndexOf('.');
        return (punto >= 0) ? nombre.substring(punto + 1).toLowerCase() : "";
    }

    private static boolean contiene(String[] arr, String ext) {
        for (String s : arr) if (s.equals(ext)) return true;
        return false;
    }

    /** Carpeta donde crear/pegar: la seleccionada si es carpeta, si no la raiz. */
    private File carpetaDestino() {
        File sel = archivoSeleccionado();
        if (sel != null && sel.isDirectory()) {
            return sel;
        }
        return carpetaRaiz;
    }

    /** El File del nodo seleccionado en el arbol, o null si no hay seleccion. */
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

        for (File hijo : hijos) {
            DefaultMutableTreeNode nodoHijo = new DefaultMutableTreeNode(hijo);
            nodo.add(nodoHijo);
            if (hijo.isDirectory()) {
                agregarHijos(nodoHijo, hijo);
            }
        }
    }

    //ya no se ocupa en esta clase
    private void pendiente(String accion) {
        JOptionPane.showMessageDialog(this,
                accion + ": falta implementarlo (ver el comentario TODO en PanelExplorador).");
    }

    /** Hace que el arbol muestre solo el nombre del archivo, no la ruta entera. */
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
