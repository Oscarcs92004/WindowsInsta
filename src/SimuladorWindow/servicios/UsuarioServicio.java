package SimuladorWindow.servicios;

import SimuladorWindow.excepciones.CuentaDesactivadaException;
import SimuladorWindow.excepciones.UsernameDuplicadoException;
import SimuladorWindow.modelo.Rol;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.persistencia.ArchivoBinario;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class UsuarioServicio {

    private final File archivo;

    public UsuarioServicio(File carpetaDeDatos) {
        this(carpetaDeDatos, "usuarios.sop");
    }

    public UsuarioServicio(File carpetaDeDatos, String nombreArchivo) {
        this.archivo = new File(carpetaDeDatos, nombreArchivo);
    }

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

    public Usuario login(String username, String password)
            throws CuentaDesactivadaException {

        Usuario usuario = buscar(username);

        if (usuario == null) {
            return null;
        }
        if (!usuario.getPassword().equals(password)) {
            return null;
        }
        if (!usuario.isActiva()) {
            throw new CuentaDesactivadaException(username);
        }
        return usuario;
    }

    public List<Usuario> listar() {
        return leerUsuarios();
    }

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

    public Usuario buscar(String username) {
        for (Usuario u : leerUsuarios()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    public void asegurarAdmin() {
        boolean noHayUsuarios = leerUsuarios().isEmpty();
        if (noHayUsuarios) {
            Usuario admin =
                    new Usuario("Administrador", 'M', "admin", "admin", 30);
            admin.setRol(Rol.ADMINISTRADOR);
            try {
                registrar(admin);
            } catch (UsernameDuplicadoException nuncaPasa) {
            }
        }
    }

    private List<Usuario> leerUsuarios() {
        if (!archivo.exists()) {
            return new ArrayList<>();
        }
        Object contenido = ArchivoBinario.leer(archivo);

        @SuppressWarnings("unchecked")
        List<Usuario> usuarios = (List<Usuario>) contenido;
        return usuarios;
    }

    private void guardarUsuarios(List<Usuario> usuarios) {
        ArchivoBinario.guardar(archivo, new ArrayList<>(usuarios));
    }
}
