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
 * (coincidencia parcial), con el formato: USERNAME - Lo sigo / No lo sigues.
 * Al elegir uno se abre su perfil. El resultado se arma en una ListaEnlazada.
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

        setLayout(new BorderLayout());

        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscar());
        txtBuscar.addActionListener(e -> buscar());
        arriba.add(new JLabel("Buscar persona:"));
        arriba.add(txtBuscar);
        arriba.add(btnBuscar);
        add(arriba, BorderLayout.NORTH);

        add(new JScrollPane(resultados), BorderLayout.CENTER);

        JButton btnVer = new JButton("Entrar al perfil");
        btnVer.addActionListener(e -> entrarAlPerfil());
        add(btnVer, BorderLayout.SOUTH);
    }

    private void buscar() {
        String texto = txtBuscar.getText().trim().toLowerCase();
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
            String sigo = insta.sigo(usuarioActual.getUsername(), u.getUsername())
                    ? "Lo sigo" : "No lo sigues";
            String linea = u.getUsername() + " - " + sigo;
            if (!encontrados.contiene(linea)) {
                encontrados.agregarFinal(linea);
            }
        }

        for (String linea : encontrados.comoLista()) {
            modelo.addElement(linea);
        }
        if (modelo.isEmpty()) {
            modelo.addElement("(sin resultados)");
        }
    }

    private void entrarAlPerfil() {
        String elegido = resultados.getSelectedValue();
        if (elegido == null || elegido.startsWith("(")) {
            JOptionPane.showMessageDialog(this, "Elige un usuario de la lista.");
            return;
        }
        String username = elegido.split(" - ")[0];
        panelInsta.verPerfilDe(username);
    }
}
