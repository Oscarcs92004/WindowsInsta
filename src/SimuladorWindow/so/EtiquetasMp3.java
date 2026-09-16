package SimuladorWindow.so;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class EtiquetasMp3 {

    private static final int MAXIMO_ETIQUETA = 16 * 1024 * 1024;
    private static final int PORTADA_FRONTAL = 3;

    private String titulo;
    private String artista;
    private String album;
    private byte[] caratula;
    private int tipoCaratula = -1;

    public String getTitulo()   { return titulo; }
    public String getArtista()  { return artista; }
    public String getAlbum()    { return album; }
    public byte[] getCaratula() { return caratula; }

    public static EtiquetasMp3 leer(File cancion) {
        EtiquetasMp3 etiquetas = new EtiquetasMp3();
        if (!cancion.getName().toLowerCase().endsWith(".mp3")) {
            return etiquetas;
        }
        try (InputStream in = new BufferedInputStream(new FileInputStream(cancion))) {
            byte[] cabecera = in.readNBytes(10);
            if (cabecera.length < 10 || cabecera[0] != 'I' || cabecera[1] != 'D' || cabecera[2] != '3') {
                return etiquetas;
            }
            int version = cabecera[3];
            int banderas = cabecera[5] & 0xFF;
            int tamano = entero7(cabecera, 6);
            if (version < 2 || version > 4 || tamano <= 0) {
                return etiquetas;
            }

            byte[] datos = in.readNBytes(Math.min(tamano, MAXIMO_ETIQUETA));
            if (version < 4 && (banderas & 0x80) != 0) {
                datos = quitarSincronizacion(datos);
            }

            int pos = 0;
            if ((banderas & 0x40) != 0 && version == 3 && datos.length >= 4) {
                pos = 4 + entero(datos, 0);
            } else if ((banderas & 0x40) != 0 && version == 4 && datos.length >= 4) {
                pos = entero7(datos, 0);
            }
            etiquetas.leerMarcos(datos, pos, version);
        } catch (IOException | RuntimeException e) {
            return etiquetas;
        }
        return etiquetas;
    }

    private void leerMarcos(byte[] datos, int pos, int version) {
        int largoId = (version == 2) ? 3 : 4;
        int largoCabecera = (version == 2) ? 6 : 10;

        while (pos >= 0 && pos + largoCabecera <= datos.length && datos[pos] != 0) {
            String id = new String(datos, pos, largoId, StandardCharsets.ISO_8859_1);
            int tamano;
            if (version == 2) {
                tamano = ((datos[pos + 3] & 0xFF) << 16) | ((datos[pos + 4] & 0xFF) << 8)
                        | (datos[pos + 5] & 0xFF);
            } else if (version == 3) {
                tamano = entero(datos, pos + 4);
            } else {
                tamano = tamanoMarcoV4(datos, pos);
            }
            int formato = (version == 2) ? 0 : datos[pos + 9] & 0xFF;

            int inicio = pos + largoCabecera;
            if (tamano <= 0 || tamano > datos.length - inicio) {
                return;
            }
            byte[] cuerpo = Arrays.copyOfRange(datos, inicio, inicio + tamano);
            pos = inicio + tamano;

            if (version == 3) {
                if ((formato & 0xC0) != 0) {
                    continue;
                }
                if ((formato & 0x20) != 0) {
                    cuerpo = Arrays.copyOfRange(cuerpo, Math.min(1, cuerpo.length), cuerpo.length);
                }
            } else if (version == 4) {
                if ((formato & 0x0C) != 0) {
                    continue;
                }
                int saltar = ((formato & 0x40) != 0 ? 1 : 0) + ((formato & 0x01) != 0 ? 4 : 0);
                cuerpo = Arrays.copyOfRange(cuerpo, Math.min(saltar, cuerpo.length), cuerpo.length);
                if ((formato & 0x02) != 0) {
                    cuerpo = quitarSincronizacion(cuerpo);
                }
            }
            procesarMarco(id, cuerpo);
        }
    }

    private void procesarMarco(String id, byte[] cuerpo) {
        switch (id) {
            case "TIT2":
            case "TT2":
                if (titulo == null) {
                    titulo = texto(cuerpo);
                }
                break;
            case "TPE1":
            case "TP1":
                if (artista == null) {
                    artista = texto(cuerpo);
                }
                break;
            case "TALB":
            case "TAL":
                if (album == null) {
                    album = texto(cuerpo);
                }
                break;
            case "APIC":
                imagen(cuerpo, saltarHastaCero(cuerpo, 1));
                break;
            case "PIC":
                imagen(cuerpo, 4);
                break;
            default:
                break;
        }
    }

    private void imagen(byte[] cuerpo, int posTipo) {
        if (cuerpo.length == 0 || posTipo >= cuerpo.length) {
            return;
        }
        int codificacion = cuerpo[0] & 0xFF;
        int tipo = cuerpo[posTipo] & 0xFF;
        int inicio = saltarDescripcion(cuerpo, posTipo + 1, codificacion);
        if (inicio >= cuerpo.length) {
            return;
        }
        boolean mejor = caratula == null
                || (tipo == PORTADA_FRONTAL && tipoCaratula != PORTADA_FRONTAL);
        if (mejor) {
            caratula = Arrays.copyOfRange(cuerpo, inicio, cuerpo.length);
            tipoCaratula = tipo;
        }
    }

    private static int saltarDescripcion(byte[] b, int i, int codificacion) {
        if (codificacion == 1 || codificacion == 2) {
            while (i + 1 < b.length && !(b[i] == 0 && b[i + 1] == 0)) {
                i += 2;
            }
            return i + 2;
        }
        return saltarHastaCero(b, i);
    }

    private static int saltarHastaCero(byte[] b, int i) {
        while (i < b.length && b[i] != 0) {
            i++;
        }
        return i + 1;
    }

    private static String texto(byte[] cuerpo) {
        if (cuerpo.length < 2) {
            return null;
        }
        Charset juego;
        switch (cuerpo[0]) {
            case 1:  juego = StandardCharsets.UTF_16;     break;
            case 2:  juego = StandardCharsets.UTF_16BE;   break;
            case 3:  juego = StandardCharsets.UTF_8;      break;
            default: juego = StandardCharsets.ISO_8859_1; break;
        }
        String s = new String(cuerpo, 1, cuerpo.length - 1, juego);
        int fin = s.indexOf('\0');
        if (fin >= 0) {
            s = s.substring(0, fin);
        }
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private static int tamanoMarcoV4(byte[] datos, int pos) {
        int seguro = entero7(datos, pos + 4);
        if (pareceMarco(datos, pos + 10 + seguro)) {
            return seguro;
        }
        int normal = entero(datos, pos + 4);
        return pareceMarco(datos, pos + 10 + normal) ? normal : seguro;
    }

    private static boolean pareceMarco(byte[] datos, int pos) {
        if (pos == datos.length) {
            return true;
        }
        if (pos < 0 || pos > datos.length) {
            return false;
        }
        if (datos[pos] == 0) {
            return true;
        }
        if (pos + 4 > datos.length) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            char c = (char) (datos[pos + i] & 0xFF);
            if (!((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))) {
                return false;
            }
        }
        return true;
    }

    private static byte[] quitarSincronizacion(byte[] datos) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream(datos.length);
        for (int i = 0; i < datos.length; i++) {
            salida.write(datos[i]);
            if ((datos[i] & 0xFF) == 0xFF && i + 1 < datos.length && datos[i + 1] == 0) {
                i++;
            }
        }
        return salida.toByteArray();
    }

    private static int entero(byte[] b, int i) {
        return ((b[i] & 0xFF) << 24) | ((b[i + 1] & 0xFF) << 16)
                | ((b[i + 2] & 0xFF) << 8) | (b[i + 3] & 0xFF);
    }

    private static int entero7(byte[] b, int i) {
        return ((b[i] & 0x7F) << 21) | ((b[i + 1] & 0x7F) << 14)
                | ((b[i + 2] & 0x7F) << 7) | (b[i + 3] & 0x7F);
    }
}
