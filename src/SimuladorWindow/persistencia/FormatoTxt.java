package SimuladorWindow.persistencia;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class FormatoTxt {

    private static final File CARPETA = new File("datos", "formatos");

    public static void guardar(byte[] contenido, Documento formato) throws EdtException, IOException {
        CARPETA.mkdirs();
        PersistenciaEDT.guardar(formato, archivoDe(contenido));
    }

    public static Documento buscar(byte[] contenido) {
        File archivo = archivoDe(contenido);
        if (!archivo.exists()) {
            return null;
        }
        try {
            return PersistenciaEDT.abrir(archivo);
        } catch (EdtException | IOException e) {
            return null;
        }
    }

    private static File archivoDe(byte[] contenido) {
        try {
            byte[] huella = MessageDigest.getInstance("SHA-256").digest(contenido);
            return new File(CARPETA, HexFormat.of().formatHex(huella) + ".edt");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
