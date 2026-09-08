package SimuladorWindow.insta;

import SimuladorWindow.estructuras.ListaEnlazada;
import SimuladorWindow.servicios.UsuarioServicio;

import javax.swing.*;
import java.awt.*;

/**
 * Buscar Hashtag (enunciado 4.10).
 *
 * Se escribe una palabra y se muestran todas las publicaciones que llevan ese
 * hashtag, con el formato del timeline. El orden no importa, pero no puede
 * haber publicaciones repetidas: se controla con una ListaEnlazada.
 */
public class PanelBuscarHashtag extends JPanel {

    private final InstaServicio insta;
    private final UsuarioServicio usuarios;

    private final JTextField txtHashtag = new JTextField(20);
    private final JTextArea area = new JTextArea();

    public PanelBuscarHashtag(InstaServicio insta, UsuarioServicio usuarios) {
        this.insta = insta;
        this.usuarios = usuarios;

        setBackground(EstiloInsta.FONDO);
        setLayout(new BorderLayout());
        setBorder(EstiloInsta.margen(20, 24, 20, 24));

        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        arriba.setOpaque(false);
        EstiloInsta.estiloCampo(txtHashtag);
        JButton btnBuscar = EstiloInsta.botonPrimario("Buscar");
        btnBuscar.addActionListener(e -> buscar());
        txtHashtag.addActionListener(e -> buscar());
        JLabel titulo = new JLabel("Explorar hashtag (sin #)");
        titulo.setFont(EstiloInsta.FUERTE);
        arriba.add(titulo);
        arriba.add(txtHashtag);
        arriba.add(btnBuscar);
        add(arriba, BorderLayout.NORTH);

        area.setEditable(false);
        area.setFont(EstiloInsta.NORMAL);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(EstiloInsta.BORDE));
        add(scroll, BorderLayout.CENTER);
    }

    private void buscar() {
        String palabra = txtHashtag.getText().trim();
        if (palabra.startsWith("#")) {
            palabra = palabra.substring(1);
        }
        if (palabra.isEmpty()) {
            area.setText("Escribe una palabra.");
            return;
        }

        ListaEnlazada<String> yaPuestas = new ListaEnlazada<>();
        StringBuilder sb = new StringBuilder();

        for (String autor : insta.todosLosUsuarios()) {
            if (!insta.estaVisible(autor)) {
                continue;
            }
            for (Publicacion p : insta.publicacionesDe(autor)) {
                if (!TextoInsta.tieneHashtag(p.getTexto(), palabra)) {
                    continue;
                }
                String texto = TextoInsta.formato(p);
                if (!yaPuestas.contiene(texto)) {
                    yaPuestas.agregarFinal(texto);
                    sb.append(texto).append("\n\n");
                }
            }
        }

        area.setText(sb.length() == 0
                ? "No hay publicaciones con #" + palabra
                : sb.toString());
        area.setCaretPosition(0);
    }
}
