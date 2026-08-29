package miniwindows.so;

/**
 * Hilo que reproduce la musica en segundo plano (Pilar 3 - hilos), para que
 * el resto de Mini-Windows siga respondiendo mientras suena la cancion.
 *
 * Las banderas son volatile: cuando un hilo las cambia, el otro ve el nuevo
 * valor al instante.
 *
 * TODO (Oscar) - Iteracion 3.7: completar la reproduccion real.
 *   1. Elegir la libreria de audio (JLayer en lib/ es la recomendada; tambien
 *      vale quedarse solo con .wav usando javax.sound.sampled).
 *   2. En el constructor, recibir el File de la cancion y abrir el audio.
 *   3. En run(): ir reproduciendo bloques de audio hasta que se acabe la
 *      cancion o alguien llame a detener().
 */
public class HiloReproductor extends Thread {

    private volatile boolean pausado = false;
    private volatile boolean detenido = false;

    public void pausar() {
        pausado = true;
    }

    public void reanudar() {
        pausado = false;
    }

    public void detener() {
        detenido = true;
    }

    @Override
    public void run() {
        while (!detenido) {
            if (pausado) {
                dormir(100);
                continue;
            }

            // TODO (Oscar): reproducir aqui el siguiente bloque de audio.
            //   Cuando la cancion termine, salir del while (break).
            dormir(100);
        }
    }

    private void dormir(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            detenido = true;
        }
    }
}
