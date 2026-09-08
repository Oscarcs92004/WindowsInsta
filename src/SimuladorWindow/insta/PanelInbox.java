package SimuladorWindow.insta;

import SimuladorWindow.estructuras.ListaEnlazada;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Inbox: mensajeria privada (enunciado 4.11) y envio de stickers (4.12).
 *
 * A la izquierda la lista de conversaciones; a la derecha el historial de la
 * conversacion elegida y las acciones: enviar texto (max 300), enviar sticker,
 * marcar como leidos y borrar la conversacion.
 */
public class PanelInbox extends JPanel {

    private static final int MAX_MENSAJE = 300;
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final InstaServicio insta;
    private final UsuarioServicio usuarios;
    private final Usuario usuarioActual;

    private final DefaultListModel<String> modeloConv = new DefaultListModel<>();
    private final JList<String> conversaciones = new JList<>(modeloConv);
    private final JPanel historial = new JPanel();
    private final JTextField entrada = new JTextField();

    public PanelInbox(InstaServicio insta, UsuarioServicio usuarios, Usuario usuarioActual) {
        this.insta = insta;
        this.usuarios = usuarios;
        this.usuarioActual = usuarioActual;

        setLayout(new BorderLayout());

        conversaciones.addListSelectionListener(e -> mostrarConversacion());
        JButton btnNueva = new JButton("Nueva conversacion");
        btnNueva.addActionListener(e -> nuevaConversacion());
        JPanel izq = new JPanel(new BorderLayout());
        izq.add(new JScrollPane(conversaciones), BorderLayout.CENTER);
        izq.add(btnNueva, BorderLayout.SOUTH);
        izq.setPreferredSize(new Dimension(160, 0));
        add(izq, BorderLayout.WEST);

        historial.setLayout(new BoxLayout(historial, BoxLayout.Y_AXIS));
        historial.setBackground(Color.WHITE);
        add(new JScrollPane(historial), BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout());
        abajo.add(entrada, BorderLayout.CENTER);
        entrada.addActionListener(e -> enviarTexto());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnEnviar = new JButton("Enviar");
        JButton btnSticker = new JButton("Enviar sticker");
        JButton btnLeidos = new JButton("Marcar leidos");
        JButton btnBorrar = new JButton("Borrar conversacion");
        btnEnviar.addActionListener(e -> enviarTexto());
        btnSticker.addActionListener(e -> enviarSticker());
        btnLeidos.addActionListener(e -> marcarLeidos());
        btnBorrar.addActionListener(e -> borrarConversacion());
        botones.add(btnEnviar);
        botones.add(btnSticker);
        botones.add(btnLeidos);
        botones.add(btnBorrar);
        abajo.add(botones, BorderLayout.SOUTH);
        add(abajo, BorderLayout.SOUTH);

        recargar();
    }

    // -----------------------------------------------------------------

    public void recargar() {
        String seleccion = conversaciones.getSelectedValue();
        modeloConv.clear();

        // Los "otros" usernames que aparecen en mi inbox, sin repetir.
        ListaEnlazada<String> otros = new ListaEnlazada<>();
        String yo = usuarioActual.getUsername();
        for (Mensaje m : insta.inboxDe(yo)) {
            String otro = m.getEmisor().equalsIgnoreCase(yo) ? m.getReceptor() : m.getEmisor();
            if (!otros.contiene(otro)) {
                otros.agregarFinal(otro);
            }
        }
        for (String otro : otros.comoLista()) {
            modeloConv.addElement(otro);
        }

        if (seleccion != null && modeloConv.contains(seleccion)) {
            conversaciones.setSelectedValue(seleccion, true);
        } else {
            limpiarHistorial();
        }
    }

    private void limpiarHistorial() {
        historial.removeAll();
        historial.revalidate();
        historial.repaint();
    }

    private String conversacionActual() {
        return conversaciones.getSelectedValue();
    }

    private void mostrarConversacion() {
        String otro = conversacionActual();
        historial.removeAll();
        if (otro == null) {
            limpiarHistorial();
            return;
        }
        String yo = usuarioActual.getUsername();
        for (Mensaje m : insta.inboxDe(yo)) {
            boolean deEsta = m.getEmisor().equalsIgnoreCase(otro)
                    || m.getReceptor().equalsIgnoreCase(otro);
            if (!deEsta) {
                continue;
            }
            historial.add(filaMensaje(m));
        }
        historial.revalidate();
        historial.repaint();
    }

    /** Una linea del chat: texto normal, o la imagen si es un sticker. */
    private JComponent filaMensaje(Mensaje m) {
        String encabezado = FECHA.format(m.getFechaHora()) + "  " + m.getEmisor()
                + (m.isLeido() ? "" : "  (no leido)");

        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
        fila.add(new JLabel(encabezado), BorderLayout.NORTH);

        if (m.esSticker()) {
            JLabel img = new JLabel();
            File archivo = new File(m.getTexto());
            if (archivo.exists()) {
                img.setIcon(new ImageIcon(new ImageIcon(m.getTexto()).getImage()
                        .getScaledInstance(96, 96, java.awt.Image.SCALE_SMOOTH)));
            } else {
                img.setText("[sticker: " + archivo.getName() + "]");
            }
            fila.add(img, BorderLayout.CENTER);
        } else {
            JLabel texto = new JLabel(m.getTexto());
            fila.add(texto, BorderLayout.CENTER);
        }
        return fila;
    }

    // -----------------------------------------------------------------

    private void nuevaConversacion() {
        String otro = JOptionPane.showInputDialog(this, "Username del otro usuario:");
        if (otro == null || otro.trim().isEmpty()) {
            return;
        }
        otro = otro.trim();
        if (usuarios.buscar(otro) == null) {
            JOptionPane.showMessageDialog(this, "No existe ese usuario.");
            return;
        }
        if (!modeloConv.contains(otro)) {
            modeloConv.addElement(otro);
        }
        conversaciones.setSelectedValue(otro, true);
    }

    private void enviarTexto() {
        String otro = conversacionActual();
        if (otro == null) {
            JOptionPane.showMessageDialog(this, "Elige una conversacion.");
            return;
        }
        String texto = entrada.getText().trim();
        if (texto.isEmpty()) {
            return;
        }
        if (texto.length() > MAX_MENSAJE) {
            JOptionPane.showMessageDialog(this,
                    "El mensaje no puede pasar de " + MAX_MENSAJE + " caracteres.");
            return;
        }
        insta.enviarMensaje(new Mensaje(usuarioActual.getUsername(), otro,
                texto, Mensaje.TEXTO));
        entrada.setText("");
        mostrarConversacion();
    }

    private void enviarSticker() {
        String otro = conversacionActual();
        if (otro == null) {
            JOptionPane.showMessageDialog(this, "Elige una conversacion.");
            return;
        }
        List<Sticker> misStickers = insta.stickersDe(usuarioActual.getUsername());

        Object[] opciones = new Object[misStickers.size() + 1];
        for (int i = 0; i < misStickers.size(); i++) {
            opciones[i] = misStickers.get(i).getNombre();
        }
        opciones[misStickers.size()] = "Importar sticker...";

        Object elegido = JOptionPane.showInputDialog(this, "Elige un sticker:",
                "Stickers", JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
        if (elegido == null) {
            return;
        }
        if ("Importar sticker...".equals(elegido)) {
            importarSticker();
            return;
        }
        for (Sticker s : misStickers) {
            if (s.getNombre().equals(elegido)) {
                insta.enviarMensaje(new Mensaje(usuarioActual.getUsername(), otro,
                        s.getRuta(), Mensaje.STICKER));
                mostrarConversacion();
                return;
            }
        }
    }

    private void importarSticker() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Imagenes (png, jpg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File origen = chooser.getSelectedFile();
        String nombre = origen.getName().toLowerCase();
        if (!nombre.endsWith(".png") && !nombre.endsWith(".jpg") && !nombre.endsWith(".jpeg")) {
            JOptionPane.showMessageDialog(this, "El sticker debe ser .png o .jpg");
            return;
        }
        String usuario = usuarioActual.getUsername();
        File carpeta = insta.carpetaStickersPersonalesDe(usuario);
        carpeta.mkdirs();
        File destino = new File(carpeta, origen.getName());
        try {
            Files.copy(origen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo importar: " + ex.getMessage());
            return;
        }
        insta.agregarSticker(usuario, new Sticker(origen.getName(), destino.getPath()));
        JOptionPane.showMessageDialog(this, "Sticker importado.");
    }

    private void marcarLeidos() {
        String otro = conversacionActual();
        if (otro == null) {
            return;
        }
        String yo = usuarioActual.getUsername();
        List<Mensaje> inbox = insta.inboxDe(yo);
        for (Mensaje m : inbox) {
            if (m.getEmisor().equalsIgnoreCase(otro) && m.getReceptor().equalsIgnoreCase(yo)) {
                m.setLeido(true);
            }
        }
        insta.guardarInbox(yo, inbox);
        mostrarConversacion();
    }

    private void borrarConversacion() {
        String otro = conversacionActual();
        if (otro == null) {
            return;
        }
        int op = JOptionPane.showConfirmDialog(this,
                "Borrar toda la conversacion con " + otro + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) {
            return;
        }
        String yo = usuarioActual.getUsername();
        List<Mensaje> inbox = insta.inboxDe(yo);
        inbox.removeIf(m -> m.getEmisor().equalsIgnoreCase(otro)
                || m.getReceptor().equalsIgnoreCase(otro));
        insta.guardarInbox(yo, inbox);
        recargar();
        limpiarHistorial();
    }
}
