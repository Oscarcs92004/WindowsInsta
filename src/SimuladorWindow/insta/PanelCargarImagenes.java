package SimuladorWindow.insta;

import SimuladorWindow.modelo.Usuario;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Cargar imagenes (enunciado 4.6).
 *
 * El usuario elige una imagen, le pone una descripcion (maximo 220 caracteres,
 * puede llevar #hashtags y @menciones) y opcionalmente una carpeta personal.
 * La imagen se copia a la carpeta /imagenes de su cuenta y queda registrada
 * en insta.ins como una Publicacion con ruta de imagen + descripcion.
 */
public class PanelCargarImagenes extends JPanel {

    private static final int MAX_DESCRIPCION = 220;

    private final InstaServicio insta;
    private final Usuario usuarioActual;

    private final JTextField txtRuta = new JTextField(25);
    private final JTextField txtCarpeta = new JTextField(15);
    private final JTextArea txtDescripcion = new JTextArea(4, 25);
    private File imagenElegida;

    public PanelCargarImagenes(InstaServicio insta, Usuario usuarioActual) {
        this.insta = insta;
        this.usuarioActual = usuarioActual;

        setBackground(EstiloInsta.FONDO);
        setLayout(new GridBagLayout());
        setBorder(EstiloInsta.margen(24, 28, 24, 28));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 4, 6, 4);
        c.anchor = GridBagConstraints.WEST;

        txtRuta.setEditable(false);
        EstiloInsta.estiloCampo(txtRuta);
        EstiloInsta.estiloCampo(txtCarpeta);
        EstiloInsta.estiloCampo(txtDescripcion);
        JButton btnElegir = EstiloInsta.botonSecundario("Elegir imagen");
        btnElegir.addActionListener(e -> elegirImagen());

        int fila = 0;
        c.gridx = 0; c.gridy = fila; add(new JLabel("Imagen:"), c);
        c.gridx = 1; add(txtRuta, c);
        c.gridx = 2; add(btnElegir, c);
        fila++;
        c.gridx = 0; c.gridy = fila; add(new JLabel("Carpeta personal (opcional):"), c);
        c.gridx = 1; add(txtCarpeta, c);
        fila++;
        c.gridx = 0; c.gridy = fila; add(new JLabel("Descripcion (max " + MAX_DESCRIPCION + "):"), c);
        c.gridx = 1; add(new JScrollPane(txtDescripcion), c);
        fila++;
        JButton btnSubir = EstiloInsta.botonPrimario("Publicar imagen");
        btnSubir.addActionListener(e -> subir());
        c.gridx = 1; c.gridy = fila; add(btnSubir, c);
    }

    public void recargar() {
        txtRuta.setText("");
        txtCarpeta.setText("");
        txtDescripcion.setText("");
        imagenElegida = null;
    }

    private void elegirImagen() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Imagenes (png, jpg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            imagenElegida = chooser.getSelectedFile();
            txtRuta.setText(imagenElegida.getPath());
        }
    }

    private void subir() {
        if (imagenElegida == null) {
            JOptionPane.showMessageDialog(this, "Primero elige una imagen.");
            return;
        }
        String descripcion = txtDescripcion.getText().trim();
        if (descripcion.length() > MAX_DESCRIPCION) {
            JOptionPane.showMessageDialog(this,
                    "La descripcion no puede pasar de " + MAX_DESCRIPCION + " caracteres.");
            return;
        }

        String usuario = usuarioActual.getUsername();
        File destino = new File(insta.carpetaImagenesDe(usuario), imagenElegida.getName());
        try {
            insta.carpetaImagenesDe(usuario).mkdirs();
            Files.copy(imagenElegida.toPath(), destino.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            String carpetaPersonal = txtCarpeta.getText().trim();
            if (!carpetaPersonal.isEmpty()) {
                File carpeta = new File(insta.carpetaFoldersPersonalesDe(usuario), carpetaPersonal);
                carpeta.mkdirs();
                Files.copy(imagenElegida.toPath(),
                        new File(carpeta, imagenElegida.getName()).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo copiar la imagen: " + ex.getMessage());
            return;
        }

        insta.publicar(usuario, new Publicacion(usuario, descripcion, destino.getPath()));
        JOptionPane.showMessageDialog(this, "Imagen publicada.");
        recargar();
    }
}
