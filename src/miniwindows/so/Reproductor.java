package miniwindows.so;

import java.io.File;

/**
 * Lo que sabe hacer un reproductor de musica (enunciado 3.7).
 *
 * Se deja como interfaz para que la parte visual (PanelReproductor) no
 * dependa de la libreria de audio que se elija al final. La implementacion
 * real, con hilo, la hace Oscar en la Iteracion 3.7.
 */
public interface Reproductor {

    void cargar(File cancion);

    void play();

    void pause();

    void stop();
}
