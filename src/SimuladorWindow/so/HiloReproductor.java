package SimuladorWindow.so;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class HiloReproductor extends Thread {

    private static final int TAMANO_BLOQUE = 4096;

    private final File cancion;
    private volatile boolean pausado = false;
    private volatile boolean detenido = false;

    private AdvancedPlayer playerMp3;
    private int frameMp3 = 0;

    public HiloReproductor(File cancion) {
        this.cancion = cancion;
    }

    public void pausar() {
        pausado = true;
        if (playerMp3 != null) {
            playerMp3.stop();
        }
    }

    public void reanudar() {
        pausado = false;
    }

    public void detener() {
        detenido = true;
        if (playerMp3 != null) {
            playerMp3.close();
        }
    }

    @Override
    public void run() {
        if (esMp3()) {
            reproducirMp3();
        } else {
            reproducirWav();
        }
    }

    private boolean esMp3() {
        return cancion.getName().toLowerCase().endsWith(".mp3");
    }


    private void reproducirMp3() {
        while (!detenido) {
            if (pausado) {
                dormir(100);
                continue;
            }
            boolean terminoLaCancion = reproducirTramoMp3(frameMp3);
            if (terminoLaCancion) {
                break;
            }
        }
    }

    private boolean reproducirTramoMp3(int desdeFrame) {
        try (FileInputStream fis = new FileInputStream(cancion);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            playerMp3 = new AdvancedPlayer(bis);
            playerMp3.setPlayBackListener(new PlaybackListener() {
                @Override
                public void playbackFinished(PlaybackEvent evt) {
                    frameMp3 = desdeFrame + evt.getFrame();
                }
            });

            playerMp3.play(desdeFrame, Integer.MAX_VALUE);

            return !pausado && !detenido;

        } catch (Exception e) {
            System.err.println("No se pudo reproducir el mp3: " + e.getMessage());
            return true;
        }
    }


    private void reproducirWav() {
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
            System.err.println("Archivo de audio no soportado (usa .mp3 o .wav): "
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
