package SimuladorWindow.pruebas;

import SimuladorWindow.estructuras.ListaEnlazada;
import SimuladorWindow.excepciones.CuentaDesactivadaException;
import SimuladorWindow.excepciones.UsernameDuplicadoException;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.persistencia.ArchivoBinario;
import SimuladorWindow.servicios.UsuarioServicio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class PruebaNucleo {

    public static void main(String[] args) {
        probarListaEnlazada();
        probarGuardadoBinario();
        probarServicioDeUsuarios();
        System.out.println("\nTodas las pruebas del nucleo pasaron.");
    }


    private static void probarListaEnlazada() {
        System.out.println("== Lista enlazada ==");

        ListaEnlazada<String> lista = new ListaEnlazada<>();
        lista.agregarFinal("a");
        lista.agregarFinal("b");
        lista.agregarInicio("z");


        String recorrido = "";
        for (String dato : lista.comoLista()) {
            recorrido = recorrido + dato + " ";
        }

        comprobar(recorrido.trim().equals("z a b"), "el recorrido es 'z a b'");
        comprobar(lista.tamano() == 3, "el tamano es 3");
        comprobar(lista.contiene("b"), "la lista contiene 'b'");
        comprobar(lista.eliminar("a"), "se elimina 'a'");
        comprobar(lista.tamano() == 2, "ahora el tamano es 2");
        comprobar(!lista.contiene("a"), "ya no contiene 'a'");
    }



    private static void probarGuardadoBinario() {
        System.out.println("\n== Guardado binario ==");

        File archivo = new File("datos", "prueba.sop");
        borrarSiExiste(archivo);


        List<Usuario> original = new ArrayList<>();
        original.add(new Usuario("Ana Perez", 'F', "ana", "1234", 20));
        original.add(new Usuario("Beto Ruiz", 'M', "beto", "abcd", 22));
        ArchivoBinario.guardar(archivo, original);

        @SuppressWarnings("unchecked")
        List<Usuario> leidos = (List<Usuario>) ArchivoBinario.leer(archivo);

        comprobar(leidos.size() == 2, "se leyeron 2 usuarios del archivo");
        comprobar(leidos.get(0).getUsername().equals("ana"), "el primero es 'ana'");

        borrarSiExiste(archivo);
    }



    private static void probarServicioDeUsuarios() {
        System.out.println("\n== Servicio de usuarios ==");

        File carpeta = new File("datos");
        borrarSiExiste(new File(carpeta, "usuarios.sop"));
        UsuarioServicio servicio = new UsuarioServicio(carpeta);

        servicio.asegurarAdmin();
        comprobar(servicio.buscar("admin") != null,
                "asegurarAdmin crea el usuario 'admin'");

        try {
            servicio.registrar(new Usuario("Carla Diaz", 'F', "carla", "pass", 25));
            System.out.println("  ok  se registra 'carla'");
        } catch (UsernameDuplicadoException e) {
            comprobar(false, "el registro de 'carla' no deberia fallar");
        }

        boolean fueRechazado = false;
        try {
            servicio.registrar(new Usuario("Otra Carla", 'F', "carla", "x", 30));
        } catch (UsernameDuplicadoException e) {
            fueRechazado = true;
        }
        comprobar(fueRechazado, "registrar 'carla' dos veces se rechaza");

        try {
            comprobar(servicio.login("carla", "pass") != null,
                    "login con la clave correcta devuelve el usuario");
            comprobar(servicio.login("carla", "mala") == null,
                    "login con la clave mala devuelve null");
        } catch (CuentaDesactivadaException e) {
            comprobar(false, "el login no deberia lanzar excepcion aqui");
        }
    }

    private static void comprobar(boolean condicion, String descripcion) {
        if (condicion) {
            System.out.println("  ok  " + descripcion);
        } else {
            System.out.println("  FALLA  " + descripcion);
            throw new AssertionError("Fallo la prueba: " + descripcion);
        }
    }

    private static void borrarSiExiste(File archivo) {
        if (archivo.exists() && !archivo.delete()) {
            System.out.println("  (aviso) no se pudo borrar " + archivo);
        }
    }
}
