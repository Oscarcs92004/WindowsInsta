package miniwindows.so;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import miniwindows.modelo.Usuario;

/**
 * Explorador de archivos con JTree (enunciado 3.3).
 *
 * YA FUNCIONA: mostrar el arbol de carpetas, refrescar y crear una carpeta
 * nueva dentro de la carpeta seleccionada.
 *
 * FALTA (ver los TODO de abajo): renombrar, copiar, pegar, ordenar y organizar.
 */
public class PanelExplorador extends JPanel {

    private final File carpetaRaiz;
    private final JTree arbol;

    /** Ruta guardada al pulsar "Copiar", para usarla luego en "Pegar". */
    private File portapapeles;

    public PanelExplorador(Usuario usuarioActual, File carpetaRaiz) {
        this.carpetaRaiz = carpetaRaiz;

        setLayout(new BorderLayout());

        arbol = new JTree(construirModelo());
        arbol.setCellRenderer(new RendererSoloNombre());
        add(new JScrollPane(arbol), BorderLayout.CENTER);

        JToolBar barra = new JToolBar();
        barra.setFloatable(false);

        JButton btnRefrescar = new JButton("Refrescar");
        JButton btnNueva = new JButton("Nueva carpeta");
        JButton btnRenombrar = new JButton("Renombrar");
        JButton btnCopiar = new JButton("Copiar");
        JButton btnPegar = new JButton("Pegar");
        JButton btnOrdenar = new JButton("Ordenar");
        JButton btnOrganizar = new JButton("Organizar");

        barra.add(btnRefrescar);
        barra.add(btnNueva);
        barra.add(btnRenombrar);
        barra.add(btnCopiar);
        barra.add(btnPegar);
        barra.add(btnOrdenar);
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
        btnRenombrar.addActionListener(e -> pendiente("Renombrar"));

        // TODO (Alex) - Iteracion 3.2b: COPIAR
        //   portapapeles = archivoSeleccionado();   (guardar la ruta y ya)
        btnCopiar.addActionListener(e -> pendiente("Copiar"));

        // TODO (Alex) - Iteracion 3.2b: PEGAR
        //   File destino = new File(carpetaDestino(), portapapeles.getName());
        //   Files.copy(portapapeles.toPath(), destino.toPath(),
        //              StandardCopyOption.REPLACE_EXISTING);
        //   si portapapeles es carpeta, recorrer y copiar cada archivo. refrescar();
        btnPegar.addActionListener(e -> pendiente("Pegar"));

        // TODO (Alex) - Iteracion 3.2c: ORDENAR
        //   pedir el criterio (nombre / fecha / tipo / tamano) con un JComboBox
        //   y ordenar los hijos con un Comparator<File> antes de pintar el arbol.
        btnOrdenar.addActionListener(e -> pendiente("Ordenar"));

        // TODO (Oscar) - Iteracion 3.8: ORGANIZAR
        //   recorrer la carpeta seleccionada y mover cada archivo a las
        //   subcarpetas imagenes/ documentos/ musica/ segun su extension.
        //   Hacerlo con un SwingWorker (no congelar la ventana) y guardar las
        //   rutas de cada tipo en una ListaEnlazada por categoria.
        btnOrganizar.addActionListener(e -> pendiente("Organizar"));
    }

    // --------------------- ayudantes que YA sirven -------------------

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
        for (File hijo : hijos) {
            DefaultMutableTreeNode nodoHijo = new DefaultMutableTreeNode(hijo);
            nodo.add(nodoHijo);
            if (hijo.isDirectory()) {
                agregarHijos(nodoHijo, hijo);
            }
        }
    }

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
