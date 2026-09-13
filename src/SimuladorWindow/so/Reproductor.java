package SimuladorWindow.so;

import java.io.File;


public interface Reproductor {

    void cargar(File cancion);

    void play();

    void pause();

    void stop();
}
