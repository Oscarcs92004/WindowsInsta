package miniwindows.so;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
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
    private static final int TAMANO_BLOQUE = 4096;
    private final File cancion;
    private volatile boolean pausado = false;
    private volatile boolean detenido = false;

    public HiloReproductor(File cancion) {
        this.cancion = cancion;
    }

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
        try (AudioInputStream entrada = AudioSystem.getAudioInputStream(cancion)) {
            AudioFormat formato = entrada.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, formato);

            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Formato de audio no soportado: " + cancion.getName());
                return;
            }

            try (SourceDataLine linea = (SourceDataLine) AudioSystem.getLine(info)) {
                linea.open(formato);
                linea.start();

                byte[] bloque = new byte[TAMANO_BLOQUE];
                int leidos;

                while (!detenido && (leidos = entrada.read(bloque, 0, bloque.length)) != -1) {

                    while (pausado && !detenido) {
                        dormir(100);
                    }
                    if (detenido) {
                        break;
                    }

                    linea.write(bloque, 0, leidos);
                }

                linea.drain();
            }
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Archivo de audio no soportado (se esperaba .wav): "
                    + cancion.getName());
        } catch (LineUnavailableException e) {
            System.err.println("No se pudo abrir la salida de audio: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error leyendo el archivo de audio: " + e.getMessage());
        }
    }

    private void dormir(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            detenido = true;
            Thread.currentThread().interrupt();
        }
    }
}
