package SimuladorWindow.so;

import SimuladorWindow.modelo.Rol;
import SimuladorWindow.modelo.Usuario;

import java.io.File;


public class Rutas {

    public static final File DISCO_Z = new File("datos/Z");

    public static File carpetaRaizDe(Usuario u) {
        if (u.getRol() == Rol.ADMINISTRADOR) {
            return DISCO_Z;
        }
        return new File(DISCO_Z, u.getUsername());
    }

    public static void asegurarCarpetasUsuario(Usuario u) {
        DISCO_Z.mkdirs();

        File carpeta = new File(DISCO_Z, u.getUsername());
        if (!carpeta.exists()) {
            carpeta.mkdirs();
            new File(carpeta, "Mis Documentos").mkdir();
            new File(carpeta, "Música").mkdir();
            new File(carpeta, "Mis Imágenes").mkdir();
        }
    }
}
