package SimuladorWindow.insta;

import javax.swing.*;
import java.awt.*;

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

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
