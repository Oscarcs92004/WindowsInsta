package SimuladorWindow.so;

import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.persistencia.Documento;
import SimuladorWindow.persistencia.EdtException;
import SimuladorWindow.persistencia.PersistenciaEDT;
import SimuladorWindow.persistencia.Tabla;
import SimuladorWindow.so.editor.*;
import SimuladorWindow.ui.Estilo;

import javax.swing.border.EmptyBorder;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.UndoManager;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;


/**
 * Editor de texto con formato (enunciado 3.4).
 *
 * Crea, abre y guarda archivos de texto plano .txt (UTF-8) y, ademas, tiene una
 * barra para dar formato al texto: color, tipo y tamano de fuente, negrita /
 * cursiva / subrayado, alineacion y tablas. Ese formato se guarda en un
 * documento propio .edt (ver el paquete {@code so.editor} y
 * {@code persistencia.PersistenciaEDT}) para que se conserve al reabrir.
 */
public class PanelEditor extends JPanel {


    private final Usuario usuarioActual;
    private final File carpetaRaiz;
    private final JTextPane textPane = new JTextPane();
    private File archivoActual;
    private final GestorFuentes gestorFuentes;
    private final GestorFormatoTexto gestorFormato;
    private final GestorColorTexto gestorColor;
    private final GestorTablas gestorTablas;
    private final UndoManager gestorDeshacer = new UndoManager();
    private final JLabel etiquetaEstado = new JLabel("0 palabras");
    private JComboBox<String> comboFuente;
    private JComboBox<Integer> comboTamano;
    private JToggleButton btnNegrita;
    private JToggleButton btnCursiva;
    private JToggleButton btnSubrayado;
    private JToggleButton btnTachado;
    private JButton btnColor;

    public PanelEditor(Usuario usuarioActual, File carpetaRaiz) {
        this.usuarioActual = usuarioActual;
        this.carpetaRaiz = carpetaRaiz;
        gestorFuentes = new GestorFuentes();
        gestorFormato = new GestorFormatoTexto(textPane);
        gestorColor = new GestorColorTexto(textPane);
        gestorTablas = new GestorTablas();
        setLayout(new BorderLayout());
        construirMenu();
        construirEditor();
        construirEstado();
        configurarAtajos();
        actualizarEstado();
    }

    private void construirMenu() {
        JMenuBar menuBar = new JMenuBar();
        int atajo = InputEvent.CTRL_DOWN_MASK;

        JMenu menuArchivo = new JMenu("Archivo");
        JMenuItem itemNuevo = crearItem("Nuevo", KeyEvent.VK_N, atajo);
        JMenuItem itemAbrir = crearItem("Abrir...", KeyEvent.VK_O, atajo);
        JMenuItem itemGuardar = crearItem("Guardar", KeyEvent.VK_S, atajo);
        JMenuItem itemGuardarComo = crearItem("Guardar como...", KeyEvent.VK_S, atajo | InputEvent.SHIFT_DOWN_MASK);
        JMenuItem itemSalir = new JMenuItem("Cerrar");

        itemNuevo.addActionListener(e -> accionNuevo());
        itemAbrir.addActionListener(e -> accionAbrir());
        itemGuardar.addActionListener(e -> accionGuardar());
        itemGuardarComo.addActionListener(e -> accionGuardarComo());
        itemSalir.addActionListener(e -> accionCerrar());

        menuArchivo.add(itemNuevo);
        menuArchivo.add(itemAbrir);
        menuArchivo.addSeparator();
        menuArchivo.add(itemGuardar);
        menuArchivo.add(itemGuardarComo);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);
        JMenu menuEditar = new JMenu("Editar");
        JMenuItem itemDeshacer = crearItem("Deshacer", KeyEvent.VK_Z, atajo);
        JMenuItem itemRehacer = crearItem("Rehacer", KeyEvent.VK_Y, atajo);
        JMenuItem itemSeleccionarTodo = crearItem("Seleccionar todo", KeyEvent.VK_A, atajo);

        itemDeshacer.addActionListener(e -> {
            if (gestorDeshacer.canUndo()) {
                gestorDeshacer.undo();
            }
        });

        itemRehacer.addActionListener(e -> {
            if (gestorDeshacer.canRedo()) {
                gestorDeshacer.redo();
            }
        });

        itemSeleccionarTodo.addActionListener(
                e -> textPane.selectAll()
        );

        menuEditar.add(itemDeshacer);
        menuEditar.add(itemRehacer);
        menuEditar.addSeparator();
        menuEditar.add(itemSeleccionarTodo);

        JMenu menuFormato = new JMenu("Formato");
        JMenuItem itemColor = new JMenuItem("Color de texto");
        JMenuItem itemFuente = new JMenuItem("Fuente");
        JMenuItem itemTamano = new JMenuItem("Tamaño");

        itemColor.addActionListener(
                e -> seleccionarColor()
        );

        itemFuente.addActionListener(
                e -> seleccionarFuente()
        );

        itemTamano.addActionListener(
                e -> seleccionarTamano()
        );

        menuFormato.add(itemColor);
        menuFormato.add(itemFuente);
        menuFormato.add(itemTamano);

        menuBar.add(menuArchivo);
        menuBar.add(menuEditar);
        menuBar.add(menuFormato);
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(menuBar, BorderLayout.NORTH);
        construirBarraHerramientas(panelSuperior);
        add(panelSuperior, BorderLayout.NORTH);
    }

    private JMenuItem crearItem(String texto, int tecla, int modificadores) {
        JMenuItem item = new JMenuItem(texto);
        item.setAccelerator(KeyStroke.getKeyStroke(tecla, modificadores));
        return item;
    }

    private void construirBarraHerramientas(JPanel panelSuperior) {
        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        barra.setRollover(true);
        comboFuente = new JComboBox<>();
        comboFuente.setToolTipText("Tipo de fuente");
        comboFuente.setPreferredSize(new Dimension(180, 30));
        try {
            String[] fuentes = gestorFuentes.obtenerNombres();
            for (String fuente : fuentes) {
                comboFuente.addItem(fuente);
            }
        } catch (Exception ex) {
            comboFuente.addItem("Arial");
            comboFuente.addItem("Serif");
            comboFuente.addItem("SansSerif");
            comboFuente.addItem("Monospaced");
        }

        if (comboFuente.getItemCount() > 0) {
            comboFuente.setSelectedItem("Arial");
            if (comboFuente.getSelectedIndex() < 0) {
                comboFuente.setSelectedIndex(0);
            }
        }

        comboFuente.addActionListener(e -> {
            String fuente = (String) comboFuente.getSelectedItem();
            if (fuente != null) {
                aplicarFuente(fuente);
            }
        });
        comboTamano = new JComboBox<>(new Integer[]{8, 9, 10, 11, 12, 14, 16, 18, 20, 24, 28, 32, 36, 48, 60, 72});
        comboTamano.setSelectedItem(14);
        comboTamano.setPreferredSize(new Dimension(75, 30));
        comboTamano.setToolTipText("Tamaño de fuente");
        comboTamano.addActionListener(e -> {
            Integer tamano = (Integer) comboTamano.getSelectedItem();
            if (tamano != null) {
                aplicarTamano(tamano);
            }
        });

        btnNegrita = new JToggleButton("N");
        btnNegrita.setToolTipText("Negrita");
        btnNegrita.setPreferredSize(new Dimension(38, 30));

        btnNegrita.addActionListener(
                e -> gestorFormato.alternarNegrita()
        );

        btnCursiva = new JToggleButton("I");
        btnCursiva.setToolTipText("Cursiva");
        btnCursiva.setPreferredSize(new Dimension(38, 30));

        btnCursiva.addActionListener(
                e -> gestorFormato.alternarCursiva()
        );

        btnSubrayado = new JToggleButton("S");
        btnSubrayado.setToolTipText("Subrayado");
        btnSubrayado.setPreferredSize(new Dimension(38, 30));

        btnSubrayado.addActionListener(
                e -> gestorFormato.alternarSubrayado()
        );

        btnTachado = new JToggleButton("T");
        btnTachado.setToolTipText("Tachado");
        btnTachado.setPreferredSize(new Dimension(38, 30));

        btnTachado.addActionListener(
                e -> gestorFormato.alternarTachado()
        );

        btnColor = new JButton();
        btnColor.setToolTipText("Color de texto");
        btnColor.setPreferredSize(new Dimension(45, 30));
        btnColor.setBackground(Color.BLACK);

        btnColor.addActionListener(
                e -> seleccionarColor()
        );

        JButton btnTabla =
                new JButton("▦ Tabla");

        btnTabla.setPreferredSize(
                new Dimension(85, 30)
        );

        btnTabla.addActionListener(
                e -> insertarTabla()
        );

        barra.add(comboFuente);
        barra.add(comboTamano);
        barra.addSeparator();
        barra.add(btnNegrita);
        barra.add(btnCursiva);
        barra.add(btnSubrayado);
        barra.add(btnTachado);
        barra.addSeparator();
        barra.add(btnColor);
        barra.addSeparator();
        barra.add(btnTabla);
        panelSuperior.add(barra, BorderLayout.SOUTH);
    }

    private void construirEditor() {
        textPane.setFont(new Font("Arial", Font.PLAIN, 14));

        textPane.setMargin(new Insets(40, 50, 40, 50));
        textPane.setBackground(Color.WHITE);
        textPane.setForeground(Color.BLACK);
        textPane.setCaretColor(Color.BLACK);
        JScrollPane scrollPane = new JScrollPane(textPane);
        JPanel marco = new JPanel(new BorderLayout());
        marco.setBackground(Estilo.PANEL);
        marco.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        scrollPane.setBorder(Estilo.hundido());
        marco.add(scrollPane, BorderLayout.CENTER);
        add(marco, BorderLayout.CENTER);

        textPane.getDocument().addUndoableEditListener(gestorDeshacer);

        textPane.getDocument().addDocumentListener(
                        new javax.swing.event.DocumentListener() {
                            @Override
                            public void insertUpdate(
                                    javax.swing.event.DocumentEvent e) {actualizarEstado();
                            }
                            @Override
                            public void removeUpdate(
                                    javax.swing.event.DocumentEvent e) {actualizarEstado();
                            }
                            @Override
                            public void changedUpdate(
                                    javax.swing.event.DocumentEvent e) {actualizarEstado();
                            }
                        }
                );
    }

    private void construirEstado() {
        JPanel panelEstado = new JPanel(new BorderLayout());

        panelEstado.setBackground(Estilo.PANEL);
        panelEstado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.SOMBRA),
                new EmptyBorder(3, 8, 3, 8)));

        etiquetaEstado.setFont(Estilo.NORMAL);
        panelEstado.add(etiquetaEstado, BorderLayout.WEST);

        add(panelEstado, BorderLayout.SOUTH);
    }

    private void actualizarEstado() {
        String contenido = textPane.getText().trim();

        if (contenido.isEmpty()) {
            etiquetaEstado.setText("0 palabras");
            return;
        }

        String[] palabras = contenido.split("\\s+");

        etiquetaEstado.setText(palabras.length + " palabras");
    }

    private void aplicarFuente(String fuente) {
        int inicio = textPane.getSelectionStart();

        int fin = textPane.getSelectionEnd();

        StyledDocument doc = textPane.getStyledDocument();

        SimpleAttributeSet atributos = new SimpleAttributeSet();

        StyleConstants.setFontFamily(atributos, fuente);

        if (inicio == fin) {
            textPane.setCharacterAttributes(atributos, false);
        } else {
            doc.setCharacterAttributes(inicio, fin - inicio, atributos, false);
        }
    }

    private void aplicarTamano(int tamano) {
        int inicio = textPane.getSelectionStart();
        int fin = textPane.getSelectionEnd();
        StyledDocument doc = textPane.getStyledDocument();
        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setFontSize(atributos, tamano);

        if (inicio == fin) {
            textPane.setCharacterAttributes(atributos, false);
        } else {
            doc.setCharacterAttributes(inicio, fin - inicio, atributos, false);
        }
    }

    private void seleccionarColor() {
        Color color = JColorChooser.showDialog(this, "Seleccionar color", Color.BLACK);
        if (color == null) {
            return;
        }

        btnColor.setBackground(color);
        gestorColor.aplicarColor(color);
    }

    private void seleccionarFuente() {
        if (comboFuente != null) {
            comboFuente.requestFocus();
            comboFuente.showPopup();
        }
    }

    private void seleccionarTamano() {
        if (comboTamano != null) {
            comboTamano.requestFocus();
            comboTamano.showPopup();
        }
    }

    private void insertarTabla() {
        DialogoTabla dialogo = new DialogoTabla();

        if (!dialogo.mostrar(this)) {
            return;
        }

        gestorTablas.insertar(textPane, dialogo.getFilas(), dialogo.getColumnas());
    }

    private void accionNuevo() {
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Deseas crear un documento nuevo?", "Nuevo documento", JOptionPane.YES_NO_OPTION);

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        textPane.setText("");
        archivoActual = null;
        actualizarEstado();
    }

    private void accionAbrir() {
        JFileChooser chooser = crearChooser();

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = chooser.getSelectedFile();
        try {
            Documento documento = PersistenciaEDT.abrir(archivo);
            PersistenciaEDT.aplicarA(documento, textPane.getStyledDocument());
            archivoActual = archivo;
            actualizarEstado();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir el documento:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionGuardar() {
        if (archivoActual == null) {
            accionGuardarComo();
            return;
        }
        guardarDocumento(archivoActual);
    }

    private void accionGuardarComo() {
        JFileChooser chooser = crearChooser();
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = chooser.getSelectedFile();

        if (!archivo.getName().toLowerCase().endsWith(".edt")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".edt");
        }
        guardarDocumento(archivo);
    }

    private void guardarDocumento(File archivo) {
        try {
            Documento documento = PersistenciaEDT.desdeStyledDocument(textPane.getStyledDocument());
            PersistenciaEDT.guardar(documento, archivo);
            archivoActual = archivo;
            JOptionPane.showMessageDialog(this, "Documento guardado correctamente.", "Guardar", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el documento:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JFileChooser crearChooser() {
        JFileChooser chooser = new JFileChooser(carpetaRaiz);

        chooser.setFileFilter(new FileNameExtensionFilter("Documentos EDT (*.edt)", "edt"));

        return chooser;
    }

    private void accionCerrar() {
        Container padre = getParent();

        if (padre != null) {
            padre.remove(this);
            padre.revalidate();
            padre.repaint();
        }
    }

    private void configurarAtajos() {
        InputMap inputMap = textPane.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = textPane.getActionMap();
        int ctrl = InputEvent.CTRL_DOWN_MASK;
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, ctrl), "negrita");
        actionMap.put("negrita", new AbstractAction() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        btnNegrita.doClick();
                    }
                }
        );
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, ctrl), "cursiva");
        actionMap.put("cursiva", new AbstractAction() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        btnCursiva.doClick();
                    }
                }
        );
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, ctrl), "subrayado");
        actionMap.put("subrayado", new AbstractAction() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        btnSubrayado.doClick();
                    }
                }
        );
    }
}
