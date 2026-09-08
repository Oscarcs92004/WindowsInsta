package SimuladorWindow.servicios;

import SimuladorWindow.excepciones.CuentaDesactivadaException;
import SimuladorWindow.excepciones.UsernameDuplicadoException;
import SimuladorWindow.modelo.Rol;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.persistencia.ArchivoBinario;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    /** Registro de usuarios de Mini-Windows (usuarios.sop). */
    public UsuarioServicio(File carpetaDeDatos) {
        this(carpetaDeDatos, "usuarios.sop");
    }

    /**
     * Igual, pero con otro archivo. INSTA+ lo usa con "users.ins" para tener
     * sus cuentas aparte de las de Mini-Windows (una sesion de Windows puede
     * usar dos cuentas de INSTA+ distintas).
     */
    public UsuarioServicio(File carpetaDeDatos, String nombreArchivo) {
        this.archivo = new File(carpetaDeDatos, nombreArchivo);
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

    /** La lista con todos los usuarios del sistema (para buscar, INSTA+, etc.). */
    public List<Usuario> listar() {
        return leerUsuarios();
    }

    /**
     * Guarda los cambios hechos a un usuario ya existente (editar perfil,
     * activar/desactivar la cuenta). Busca por username y lo reemplaza.
     */
    public void actualizar(Usuario cambiado) {
        List<Usuario> usuarios = leerUsuarios();
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getUsername().equalsIgnoreCase(cambiado.getUsername())) {
                usuarios.set(i, cambiado);
                guardarUsuarios(usuarios);
                return;
            }
        }
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
