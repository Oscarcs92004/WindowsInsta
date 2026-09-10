package SimuladorWindow.insta;

import SimuladorWindow.estructuras.ListaEnlazada;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import java.awt.*;

/**
 * Buscar Profile (enunciado 4.9).
 *
 * Se escribe un texto y se listan los usuarios cuyo username lo contenga
 * (coincidencia parcial). Cada resultado se ve con su avatar, el username y si
 * lo sigues o no, como en el buscador de Instagram. Al pulsar uno se abre su
 * perfil. El resultado se arma en una ListaEnlazada.
 */
public class PanelBuscarPerfil extends JPanel {

    private final InstaServicio insta;
    private final UsuarioServicio usuarios;
    private final Usuario usuarioActual;
    private final PanelInsta panelInsta;

    private final JTextField txtBuscar = new JTextField(20);
    private final DefaultListModel<String> modelo = new DefaultListModel<>();
    private final JList<String> resultados = new JList<>(modelo);

    public PanelBuscarPerfil(InstaServicio insta, UsuarioServicio usuarios,
                             Usuario usuarioActual, PanelInsta panelInsta) {
        this.insta = insta;
        this.usuarios = usuarios;
        this.usuarioActual = usuarioActual;
        this.panelInsta = panelInsta;

        setBackground(EstiloInsta.FONDO);
        setLayout(new BorderLayout());

        EstiloInsta.estiloCampo(txtBuscar);
        EstiloInsta.placeholder(txtBuscar, "Buscar");
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { buscar(); }
        });
        JPanel arriba = new JPanel(new BorderLayout());
        arriba.setOpaque(true);
        arriba.setBackground(EstiloInsta.BLANCO);
        arriba.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloInsta.BORDE),
                EstiloInsta.margen(10, 12, 10, 12)));
        arriba.add(txtBuscar, BorderLayout.CENTER);
        add(arriba, BorderLayout.NORTH);

        resultados.setFixedCellHeight(56);
        resultados.setBackground(EstiloInsta.BLANCO);
        resultados.setCellRenderer(new CeldaResultado());
        resultados.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    entrarAlPerfil();
                }
            }
        });
        JScrollPane scroll = new JScrollPane(resultados);
        scroll.setBorder(null);
        EstiloInsta.scrollFino(scroll);
        add(scroll, BorderLayout.CENTER);

        JButton btnVer = EstiloInsta.botonSecundario("Entrar al perfil");
        btnVer.addActionListener(e -> entrarAlPerfil());
        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        abajo.setOpaque(true);
        abajo.setBackground(EstiloInsta.BLANCO);
        abajo.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloInsta.BORDE));
        abajo.add(btnVer);
        add(abajo, BorderLayout.SOUTH);
    }

    private void buscar() {
        String texto = EstiloInsta.valorReal(txtBuscar).trim().toLowerCase();
        modelo.clear();
        if (texto.isEmpty()) {
            return;
        }

        ListaEnlazada<String> encontrados = new ListaEnlazada<>();
        for (Usuario u : usuarios.listar()) {
            if (!u.getUsername().toLowerCase().contains(texto)) {
                continue;
            }
            if (!insta.estaVisible(u.getUsername())) {
                continue;                       // desactivada: como si no existiera
            }
            if (!encontrados.contiene(u.getUsername())) {
                encontrados.agregarFinal(u.getUsername());
            }
        }

        for (String username : encontrados.comoLista()) {
            modelo.addElement(username);
        }
        if (modelo.isEmpty()) {
            modelo.addElement("");   // fila especial: "sin resultados"
        }
    }

    private void entrarAlPerfil() {
        String username = resultados.getSelectedValue();
        if (username == null || username.isEmpty()) {
            return;
        }
        panelInsta.verPerfilDe(username);
    }

    // -----------------------------------------------------------------

    /** Dibuja cada resultado: avatar + username + "Lo sigues / No lo sigues". */
    private class CeldaResultado extends JPanel implements ListCellRenderer<String> {

        private final JLabel avatar = new JLabel();
        private final JLabel username = new JLabel();
        private final JLabel sigo = new JLabel();

        CeldaResultado() {
            setLayout(new BorderLayout(10, 0));
            setBorder(EstiloInsta.margen(6, 12, 6, 12));
            username.setFont(EstiloInsta.FUERTE);
            username.setForeground(EstiloInsta.TEXTO);
            sigo.setFont(EstiloInsta.CHICA);
            sigo.setForeground(EstiloInsta.TEXTO_GRIS);

            JPanel textos = new JPanel(new GridLayout(2, 1));
            textos.setOpaque(false);
            textos.add(username);
            textos.add(sigo);

            add(avatar, BorderLayout.WEST);
            add(textos, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> lista,
                String valor, int indice, boolean seleccionado, boolean foco) {
            if (valor == null || valor.isEmpty()) {
                avatar.setIcon(null);
                username.setText("(sin resultados)");
                sigo.setText("");
                setBackground(EstiloInsta.BLANCO);
                return this;
            }
            Usuario u = usuarios.buscar(valor);
            avatar.setIcon(EstiloInsta.avatar(u == null ? null : u.getFotoPerfil(), valor, 40));
            username.setText(valor);
            sigo.setText(insta.sigo(usuarioActual.getUsername(), valor)
                    ? "Lo sigues" : "No lo sigues");
            setBackground(seleccionado ? new Color(245, 245, 245) : EstiloInsta.BLANCO);
            return this;
        }
    }
}
