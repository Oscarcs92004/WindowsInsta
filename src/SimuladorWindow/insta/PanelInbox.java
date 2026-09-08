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
import java.util.List;

/**
 * Inbox: mensajeria privada (enunciado 4.11) y stickers (4.12), en formato de
 * telefono como el "Direct" de Instagram: primero la lista de conversaciones a
 * pantalla completa y, al tocar una, se abre el chat a pantalla completa con un
 * boton de "Volver".
 */
public class PanelInbox extends JPanel {

    private static final int MAX_MENSAJE = 300;

    private final InstaServicio insta;
    private final UsuarioServicio usuarios;
    private final Usuario usuarioActual;

    private final CardLayout cartas = new CardLayout();

    private final DefaultListModel<String> modeloConv = new DefaultListModel<>();
    private final JList<String> conversaciones = new JList<>(modeloConv);

    private final JPanel historial = new JPanel();
    private final JLabel tituloChat = new JLabel();
    private final JTextField entrada = new JTextField();

    /** Con quien esta abierto el chat ahora mismo (null = viendo la lista). */
    private String chatActual;

    public PanelInbox(InstaServicio insta, UsuarioServicio usuarios, Usuario usuarioActual) {
        this.insta = insta;
        this.usuarios = usuarios;
        this.usuarioActual = usuarioActual;

        setLayout(cartas);
        setBackground(EstiloInsta.BLANCO);
        add(tarjetaLista(), "LISTA");
        add(tarjetaChat(), "CHAT");

        recargar();
    }

    // -----------------------------------------------------------------
    //  Lista de conversaciones
    // -----------------------------------------------------------------

    private JComponent tarjetaLista() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(EstiloInsta.BLANCO);

        JLabel titulo = new JLabel("  Mensajes");
        titulo.setFont(EstiloInsta.FUERTE);
        titulo.setBorder(EstiloInsta.margen(8, 4, 8, 4));
        p.add(titulo, BorderLayout.NORTH);

        conversaciones.setFont(EstiloInsta.NORMAL);
        conversaciones.setFixedCellHeight(40);
        conversaciones.setBorder(EstiloInsta.margen(4, 8, 4, 8));
        conversaciones.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && conversaciones.getSelectedValue() != null) {
                abrirChat(conversaciones.getSelectedValue());
            }
        });
        JScrollPane scroll = new JScrollPane(conversaciones);
        scroll.setBorder(null);
        p.add(scroll, BorderLayout.CENTER);

        JButton nueva = EstiloInsta.botonPrimario("Nueva conversacion");
        nueva.addActionListener(e -> nuevaConversacion());
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        sur.setOpaque(false);
        sur.add(nueva);
        p.add(sur, BorderLayout.SOUTH);
        return p;
    }

    // -----------------------------------------------------------------
    //  Un chat
    // -----------------------------------------------------------------

    private JComponent tarjetaChat() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(EstiloInsta.BLANCO);

        // Cabecera: volver + nombre + acciones.
        JButton volver = EstiloInsta.enlace("< Volver");
        volver.addActionListener(e -> { chatActual = null; cartas.show(this, "LISTA"); });
        tituloChat.setFont(EstiloInsta.FUERTE);

        JButton leidos = EstiloInsta.enlace("Leidos");
        leidos.addActionListener(e -> marcarLeidos());
        JButton borrar = EstiloInsta.enlace("Borrar");
        borrar.setForeground(EstiloInsta.ROJO);
        borrar.addActionListener(e -> borrarConversacion());

        JPanel cab = new JPanel(new BorderLayout());
        cab.setOpaque(false);
        cab.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloInsta.BORDE),
                EstiloInsta.margen(4, 6, 4, 6)));
        JPanel izqCab = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        izqCab.setOpaque(false);
        izqCab.add(volver);
        izqCab.add(tituloChat);
        cab.add(izqCab, BorderLayout.WEST);
        JPanel derCab = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        derCab.setOpaque(false);
        derCab.add(leidos);
        derCab.add(borrar);
        cab.add(derCab, BorderLayout.EAST);
        p.add(cab, BorderLayout.NORTH);

        historial.setLayout(new BoxLayout(historial, BoxLayout.Y_AXIS));
        historial.setBackground(EstiloInsta.BLANCO);
        JScrollPane scroll = new JScrollPane(historial,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        p.add(scroll, BorderLayout.CENTER);

        // Barra de escribir: campo + Enviar + Sticker.
        EstiloInsta.estiloCampo(entrada);
        entrada.addActionListener(e -> enviarTexto());
        JButton enviar = EstiloInsta.botonPrimario("Enviar");
        enviar.addActionListener(e -> enviarTexto());
        JButton sticker = EstiloInsta.botonSecundario("Sticker");
        sticker.addActionListener(e -> enviarSticker());

        JPanel abajo = new JPanel(new BorderLayout(6, 0));
        abajo.setOpaque(false);
        abajo.setBorder(EstiloInsta.margen(6, 6, 6, 6));
        abajo.add(entrada, BorderLayout.CENTER);
        JPanel bots = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        bots.setOpaque(false);
        bots.add(sticker);
        bots.add(enviar);
        abajo.add(bots, BorderLayout.EAST);
        p.add(abajo, BorderLayout.SOUTH);
        return p;
    }

    // -----------------------------------------------------------------

    public void recargar() {
        modeloConv.clear();
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

        if (chatActual != null && modeloConv.contains(chatActual)) {
            pintarConversacion();
        } else {
            chatActual = null;
            conversaciones.clearSelection();
            cartas.show(this, "LISTA");
        }
    }

    private void abrirChat(String otro) {
        chatActual = otro;
        tituloChat.setText(otro);
        pintarConversacion();
        cartas.show(this, "CHAT");
    }

    private void pintarConversacion() {
        historial.removeAll();
        String yo = usuarioActual.getUsername();
        for (Mensaje m : insta.inboxDe(yo)) {
            boolean deEsta = m.getEmisor().equalsIgnoreCase(chatActual)
                    || m.getReceptor().equalsIgnoreCase(chatActual);
            if (deEsta) {
                historial.add(filaMensaje(m));
            }
        }
        historial.revalidate();
        historial.repaint();
    }

    /** Una burbuja del chat: a la derecha si la envie yo, a la izquierda si no. */
    private JComponent filaMensaje(Mensaje m) {
        boolean mio = m.getEmisor().equalsIgnoreCase(usuarioActual.getUsername());

        JPanel burbuja = new JPanel();
        burbuja.setLayout(new BoxLayout(burbuja, BoxLayout.Y_AXIS));
        burbuja.setBackground(mio ? EstiloInsta.AZUL : new Color(239, 239, 239));
        burbuja.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        mio ? EstiloInsta.AZUL : EstiloInsta.BORDE, 1, true),
                EstiloInsta.margen(6, 10, 6, 10)));

        if (m.esSticker()) {
            JLabel img = new JLabel();
            File archivo = new File(m.getTexto());
            if (archivo.exists()) {
                img.setIcon(new ImageIcon(new ImageIcon(m.getTexto()).getImage()
                        .getScaledInstance(96, 96, java.awt.Image.SCALE_SMOOTH)));
            } else {
                img.setText("[sticker: " + archivo.getName() + "]");
            }
            burbuja.add(img);
        } else {
            JLabel texto = new JLabel("<html><body style='width:200px'>"
                    + m.getTexto().replace("<", "&lt;") + "</body></html>");
            texto.setForeground(mio ? Color.WHITE : EstiloInsta.TEXTO);
            texto.setFont(EstiloInsta.NORMAL);
            burbuja.add(texto);
        }

        JLabel hora = new JLabel(TextoInsta.hace(m.getFechaHora())
                + (mio && !m.isLeido() ? "  · enviado" : "")
                + (!mio && !m.isLeido() ? "  · nuevo" : ""));
        hora.setFont(EstiloInsta.CHICA);
        hora.setForeground(mio ? new Color(220, 235, 255) : EstiloInsta.TEXTO_GRIS);
        burbuja.add(hora);

        JPanel fila = new JPanel(new FlowLayout(mio ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 4));
        fila.setOpaque(false);
        fila.add(burbuja);
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
        abrirChat(otro);
    }

    private void enviarTexto() {
        if (chatActual == null) {
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
        insta.enviarMensaje(new Mensaje(usuarioActual.getUsername(), chatActual,
                texto, Mensaje.TEXTO));
        entrada.setText("");
        pintarConversacion();
    }

    private void enviarSticker() {
        if (chatActual == null) {
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
                insta.enviarMensaje(new Mensaje(usuarioActual.getUsername(), chatActual,
                        s.getRuta(), Mensaje.STICKER));
                pintarConversacion();
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
        if (chatActual == null) {
            return;
        }
        String yo = usuarioActual.getUsername();
        List<Mensaje> inbox = insta.inboxDe(yo);
        for (Mensaje m : inbox) {
            if (m.getEmisor().equalsIgnoreCase(chatActual) && m.getReceptor().equalsIgnoreCase(yo)) {
                m.setLeido(true);
            }
        }
        insta.guardarInbox(yo, inbox);
        pintarConversacion();
    }

    private void borrarConversacion() {
        if (chatActual == null) {
            return;
        }
        int op = JOptionPane.showConfirmDialog(this,
                "Borrar toda la conversacion con " + chatActual + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) {
            return;
        }
        String yo = usuarioActual.getUsername();
        String otro = chatActual;
        List<Mensaje> inbox = insta.inboxDe(yo);
        inbox.removeIf(m -> m.getEmisor().equalsIgnoreCase(otro)
                || m.getReceptor().equalsIgnoreCase(otro));
        insta.guardarInbox(yo, inbox);
        chatActual = null;
        recargar();
    }
}
