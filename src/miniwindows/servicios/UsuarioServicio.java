package miniwindows.servicios;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import miniwindows.excepciones.CuentaDesactivadaException;
import miniwindows.excepciones.UsernameDuplicadoException;
import miniwindows.modelo.Rol;
import miniwindows.modelo.Usuario;
import miniwindows.persistencia.ArchivoBinario;

/**
 * Todo lo relacionado con los usuarios del sistema, en un solo lugar:
 *
 *   - Las reglas: registrar sin repetir el username, iniciar sesion y
 *     crear el administrador la primera vez.
 *   - Guardar y leer la lista de usuarios en el archivo "usuarios.sop".
 *
 * La ventana de login y (mas adelante) el servidor de sockets llaman a
 * esta misma clase, para que las reglas esten escritas una sola vez.
 */
public class UsuarioServicio {

    /** El archivo binario donde vive la lista con TODOS los usuarios. */
    private final File archivo;

    public UsuarioServicio(File carpetaDeDatos) {
        this.archivo = new File(carpetaDeDatos, "usuarios.sop");
    }

    /**
     * Agrega un usuario nuevo y guarda la lista.
     * Si ya existe alguien con ese username, no guarda nada y avisa con
     * una excepcion.
     */
    public void registrar(Usuario nuevo) throws UsernameDuplicadoException {
        List<Usuario> usuarios = leerUsuarios();

        for (Usuario u : usuarios) {
            boolean mismoUsername =
                    u.getUsername().equalsIgnoreCase(nuevo.getUsername());
            if (mismoUsername) {
                throw new UsernameDuplicadoException(nuevo.getUsername());
            }
        }

        usuarios.add(nuevo);
        guardarUsuarios(usuarios);
    }

    /**
     * Intenta iniciar sesion. Devuelve:
     *   - el Usuario, si el username y la contrasena son correctos;
     *   - null, si el username no existe o la contrasena no coincide.
     * Lanza CuentaDesactivadaException si la cuenta esta desactivada.
     */
    public Usuario login(String username, String password)
            throws CuentaDesactivadaException {

        Usuario usuario = buscar(username);

        if (usuario == null) {
            return null;                          // no existe ese username
        }
        if (!usuario.getPassword().equals(password)) {
            return null;                          // la contrasena no coincide
        }
        if (!usuario.isActiva()) {
            throw new CuentaDesactivadaException(username);
        }
        return usuario;                           // todo bien
    }

    /**
     * Busca un usuario por su username, sin distinguir mayusculas de
     * minusculas. Devuelve null si no lo encuentra.
     */
    public Usuario buscar(String username) {
        for (Usuario u : leerUsuarios()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Crea el usuario administrador por defecto, pero solo si el archivo
     * todavia no tiene ningun usuario (la primera vez que se arranca).
     */
    public void asegurarAdmin() {
        boolean noHayUsuarios = leerUsuarios().isEmpty();
        if (noHayUsuarios) {
            Usuario admin =
                    new Usuario("Administrador", 'M', "admin", "admin", 30);
            admin.setRol(Rol.ADMINISTRADOR);
            try {
                registrar(admin);
            } catch (UsernameDuplicadoException nuncaPasa) {
                // Imposible: acabamos de ver que la lista estaba vacia.
            }
        }
    }

    // -----------------------------------------------------------------
    //  Guardar y leer el archivo.
    //  Son privados: solo esta clase los usa. El codigo de streams de
    //  verdad esta en la clase ArchivoBinario.
    // -----------------------------------------------------------------

    /** Lee la lista de usuarios del archivo. Si aun no existe, lista vacia. */
    private List<Usuario> leerUsuarios() {
        if (!archivo.exists()) {
            return new ArrayList<>();
        }
        Object contenido = ArchivoBinario.leer(archivo);

        // El cast es seguro: en "usuarios.sop" solo guardamos List<Usuario>.
        @SuppressWarnings("unchecked")
        List<Usuario> usuarios = (List<Usuario>) contenido;
        return usuarios;
    }

    /** Guarda la lista de usuarios en el archivo, pisando lo anterior. */
    private void guardarUsuarios(List<Usuario> usuarios) {
        ArchivoBinario.guardar(archivo, new ArrayList<>(usuarios));
    }
}
