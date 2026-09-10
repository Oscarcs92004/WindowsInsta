package SimuladorWindow.insta;

import javax.swing.*;
import java.awt.*;

/**
 * Una columna vertical (BoxLayout) pensada para ir dentro de un JScrollPane.
 *
 * Implementa Scrollable para que su ancho sea siempre el del área visible: así
 * las tarjetas del feed ocupan todo el ancho y nunca aparece una barra de
 * desplazamiento horizontal. Lo usan el Timeline, Interacciones y Buscar
 * Hashtag, que muestran una lista de TarjetaPublicacion.
 */
public class PanelFeed extends JPanel implements Scrollable {

    public PanelFeed() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(EstiloInsta.FONDO);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle v, int orientacion, int direccion) {
        return 24;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle v, int orientacion, int direccion) {
        return v.height - 24;
    }

    /** true: el contenido se ajusta al ancho visible (no scroll horizontal). */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
