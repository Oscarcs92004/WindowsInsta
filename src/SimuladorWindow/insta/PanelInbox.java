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
import java.time.LocalDateTime;
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

    private final DefaultListModel<Conversacion> modeloConv = new DefaultListModel<>();
    private final JList<Conversacion> conversaciones = new JList<>(modeloConv);

    private final JPanel historial = new JPanel();
    private final JLabel tituloChat = new JLabel();
    private final JLabel avatarChat = new JLabel();
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

        JLabel titulo = new JLabel(usuarioActual.getUsername());
        titulo.setFont(EstiloInsta.FUERTE.deriveFont(15f));
        JButton nuevaIcono = new JButton(IconosInsta.icono(IconosInsta.MENSAJE, 22, false));
        nuevaIcono.setContentAreaFilled(false);
        nuevaIcono.setBorderPainted(false);
        nuevaIcono.setFocusPainted(false);
        nuevaIcono.setToolTipText("Nueva conversación");
        nuevaIcono.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nuevaIcono.addActionListener(e -> nuevaConversacion());
        JPanel cabLista = new JPanel(new BorderLayout());
        cabLista.setBackground(EstiloInsta.BLANCO);
        cabLista.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloInsta.BORDE),
                EstiloInsta.margen(8, 12, 8, 12)));
        cabLista.add(titulo, BorderLayout.WEST);
        cabLista.add(nuevaIcono, BorderLayout.EAST);
        p.add(cabLista, BorderLayout.NORTH);

        conversaciones.setFixedCellHeight(64);
        conversaciones.setBackground(EstiloInsta.BLANCO);
        conversaciones.setCellRenderer(new CeldaConversacion());
        conversaciones.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                Conversacion c = conversaciones.getSelectedValue();
                if (c != null) {
                    abrirChat(c.otro);
                }
            }
        });
        JScrollPane scroll = new JScrollPane(conversaciones);
        scroll.setBorder(null);
        EstiloInsta.scrollFino(scroll);
        p.add(scroll, BorderLayout.CENTER);

        JButton nueva = EstiloInsta.botonPrimario("Nueva conversación");
        nueva.addActionListener(e -> nuevaConversacion());
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        sur.setOpaque(true);
        sur.setBackground(EstiloInsta.BLANCO);
        sur.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloInsta.BORDE));
        sur.add(nueva);
        p.add(sur, BorderLayout.SOUTH);
        return p;
    }

    /** Resumen de una conversación para la lista: con quién y el último mensaje. */
    private static class Conversacion {
        final String otro;
        final String ultimo;
        final LocalDateTime fecha;
        final boolean hayNoLeidos;

        Conversacion(String otro, String ultimo, LocalDateTime fecha, boolean hayNoLeidos) {
            this.otro = otro;
            this.ultimo = ultimo;
            this.fecha = fecha;
            this.hayNoLeidos = hayNoLeidos;
        }
    }

    /** Dibuja una fila de la lista: avatar + nombre + último mensaje + hora. */
    private class CeldaConversacion extends JPanel implements ListCellRenderer<Conversacion> {

        private final JLabel avatar = new JLabel();
        private final JLabel nombre = new JLabel();
        private final JLabel previo = new JLabel();
        private final JLabel hora = new JLabel();
        private final JLabel punto = new JLabel("●");

        CeldaConversacion() {
            setLayout(new BorderLayout(10, 0));
            setBorder(EstiloInsta.margen(8, 12, 8, 12));
            nombre.setFont(EstiloInsta.FUERTE);
            nombre.setForeground(EstiloInsta.TEXTO);
            previo.setFont(EstiloInsta.CHICA);
            previo.setForeground(EstiloInsta.TEXTO_GRIS);
            hora.setFont(EstiloInsta.CHICA);
            hora.setForeground(EstiloInsta.TEXTO_GRIS);
            punto.setForeground(EstiloInsta.AZUL);

            JPanel textos = new JPanel(new GridLayout(2, 1));
            textos.setOpaque(false);
            textos.add(nombre);
            textos.add(previo);

            JPanel der = new JPanel(new BorderLayout());
            der.setOpaque(false);
            hora.setHorizontalAlignment(SwingConstants.RIGHT);
            punto.setHorizontalAlignment(SwingConstants.RIGHT);
            der.add(hora, BorderLayout.NORTH);
            der.add(punto, BorderLayout.SOUTH);

            add(avatar, BorderLayout.WEST);
            add(textos, BorderLayout.CENTER);
            add(der, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Conversacion> lista,
                Conversacion c, int indice, boolean seleccionado, boolean foco) {
            Usuario u = usuarios.buscar(c.otro);
            avatar.setIcon(EstiloInsta.avatar(u == null ? null : u.getFotoPerfil(), c.otro, 44));
            nombre.setText(c.otro);
            String texto = c.ultimo.length() > 34 ? c.ultimo.substring(0, 33) + "…" : c.ultimo;
            previo.setText(texto.isEmpty() ? "Sticker" : texto);
            hora.setText(c.fecha == null ? "" : TextoInsta.hace(c.fecha));
            punto.setVisible(c.hayNoLeidos);
            setBackground(seleccionado ? new Color(245, 245, 245) : EstiloInsta.BLANCO);
            return this;
        }
    }

    // -----------------------------------------------------------------
    //  Un chat
    // -----------------------------------------------------------------

    private JComponent tarjetaChat() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(EstiloInsta.BLANCO);

        // Cabecera: volver + avatar + nombre + acciones.
        JButton volver = new JButton("‹");
        volver.setFont(EstiloInsta.TITULO);
        volver.setContentAreaFilled(false);
        volver.setBorderPainted(false);
        volver.setFocusPainted(false);
        volver.setForeground(EstiloInsta.TEXTO);
        volver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        volver.addActionListener(e -> { chatActual = null; cartas.show(this, "LISTA"); });
        tituloChat.setFont(EstiloInsta.FUERTE);

        JButton acciones = new JButton(IconosInsta.icono(IconosInsta.OPCIONES, 20, true));
        acciones.setContentAreaFilled(false);
        acciones.setBorderPainted(false);
        acciones.setFocusPainted(false);
        acciones.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        acciones.addActionListener(e -> {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem mLeidos = new JMenuItem("Marcar como leídos");
            mLeidos.addActionListener(x -> marcarLeidos());
            JMenuItem mBorrar = new JMenuItem("Borrar conversación");
            mBorrar.addActionListener(x -> borrarConversacion());
            menu.add(mLeidos);
            menu.add(mBorrar);
            menu.show(acciones, 0, acciones.getHeight());
        });

        JPanel cab = new JPanel(new BorderLayout());
        cab.setOpaque(false);
        cab.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloInsta.BORDE),
                EstiloInsta.margen(4, 6, 4, 6)));
        JPanel izqCab = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        izqCab.setOpaque(false);
        izqCab.add(volver);
        izqCab.add(avatarChat);
        izqCab.add(tituloChat);
        cab.add(izqCab, BorderLayout.WEST);
        cab.add(acciones, BorderLayout.EAST);
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
        String yo = usuarioActual.getUsername();
        List<Mensaje> inbox = insta.inboxDe(yo);

        // Un nodo por cada persona con la que hay conversación (sin repetir).
        ListaEnlazada<String> otros = new ListaEnlazada<>();
        for (Mensaje m : inbox) {
            String otro = m.getEmisor().equalsIgnoreCase(yo) ? m.getReceptor() : m.getEmisor();
            if (!otros.contiene(otro)) {
                otros.agregarFinal(otro);
            }
        }

        modeloConv.clear();
        boolean sigueChatActual = false;
        for (String otro : otros.comoLista()) {
            Mensaje ultimo = null;
            boolean noLeidos = false;
            for (Mensaje m : inbox) {
                boolean deEsta = m.getEmisor().equalsIgnoreCase(otro)
                        || m.getReceptor().equalsIgnoreCase(otro);
                if (!deEsta) {
                    continue;
                }
                ultimo = m;      // los mensajes están en orden de envío
                if (m.getReceptor().equalsIgnoreCase(yo) && !m.isLeido()) {
                    noLeidos = true;
                }
            }
            String texto = (ultimo == null || ultimo.esSticker()) ? "" : ultimo.getTexto();
            modeloConv.addElement(new Conversacion(otro, texto,
                    ultimo == null ? null : ultimo.getFechaHora(), noLeidos));
            if (otro.equals(chatActual)) {
                sigueChatActual = true;
            }
        }

        if (chatActual != null && sigueChatActual) {
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
        Usuario u = usuarios.buscar(otro);
        avatarChat.setIcon(EstiloInsta.avatar(u == null ? null : u.getFotoPerfil(), otro, 30));
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

    /** Una burbuja del chat: a la derecha si la envié yo, a la izquierda si no. */
    private JComponent filaMensaje(Mensaje m) {
        boolean mio = m.getEmisor().equalsIgnoreCase(usuarioActual.getUsername());

        JComponent contenido;
        if (m.esSticker()) {
            JLabel img = new JLabel();
            File archivo = new File(m.getTexto());
            if (archivo.exists()) {
                img.setIcon(new ImageIcon(new ImageIcon(m.getTexto()).getImage()
                        .getScaledInstance(110, 110, java.awt.Image.SCALE_SMOOTH)));
            } else {
                img.setText("[sticker: " + archivo.getName() + "]");
            }
            contenido = img;                       // los stickers van sin burbuja
        } else {
            JPanel burbuja = EstiloInsta.burbuja(mio ? EstiloInsta.AZUL : new Color(239, 239, 239));
            JLabel texto = EstiloInsta.textoHtml(EstiloInsta.escaparHtml(m.getTexto()), 210);
            texto.setForeground(mio ? Color.WHITE : EstiloInsta.TEXTO);
            burbuja.add(texto);
            contenido = burbuja;
        }

        JLabel hora = new JLabel(TextoInsta.hace(m.getFechaHora())
                + (mio ? (m.isLeido() ? "  · visto" : "  · enviado") : ""));
        hora.setFont(EstiloInsta.CHICA);
        hora.setForeground(EstiloInsta.TEXTO_GRIS);

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        contenido.setAlignmentX(mio ? RIGHT_ALIGNMENT : LEFT_ALIGNMENT);
        hora.setAlignmentX(mio ? RIGHT_ALIGNMENT : LEFT_ALIGNMENT);
        col.add(contenido);
        col.add(Box.createVerticalStrut(2));
        col.add(hora);

        JPanel fila = new JPanel(new FlowLayout(mio ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 5));
        fila.setOpaque(false);
        fila.add(col);
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
