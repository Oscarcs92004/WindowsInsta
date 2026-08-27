package miniwindows.pruebas;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import miniwindows.estructuras.ListaEnlazada;
import miniwindows.excepciones.CuentaDesactivadaException;
import miniwindows.excepciones.UsernameDuplicadoException;
import miniwindows.modelo.Usuario;
import miniwindows.persistencia.ArchivoBinario;
import miniwindows.servicios.UsuarioServicio;

/**
 * Prueba manual del nucleo (Fase 1).
 *
 * NO es la aplicacion. Solo comprueba que la lista enlazada, el guardado
 * binario y el servicio de usuarios funcionan, antes de montar la ventana.
 *
 * Se ejecuta con:
 *   java -cp out miniwindows.pruebas.PruebaNucleo
 *
 * Cada linea imprime "ok" si pasa; si algo falla, el programa se detiene
 * ahi y muestra que fallo.
 */
public class PruebaNucleo {

    public static void main(String[] args) {
        probarListaEnlazada();
        probarGuardadoBinario();
        probarServicioDeUsuarios();
        System.out.println("\nTodas las pruebas del nucleo pasaron.");
    }

    // -----------------------------------------------------------------

    private static void probarListaEnlazada() {
        System.out.println("== Lista enlazada ==");

        ListaEnlazada<String> lista = new ListaEnlazada<>();
        lista.agregarFinal("a");
        lista.agregarFinal("b");
        lista.agregarInicio("z");   // este queda de primero

        // Recorremos la lista y juntamos sus datos en un texto.
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

    // -----------------------------------------------------------------

    private static void probarGuardadoBinario() {
        System.out.println("\n== Guardado binario ==");

        File archivo = new File("datos", "prueba.sop");
        borrarSiExiste(archivo);

        // Armamos una lista, la guardamos y la volvemos a leer.
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

    // -----------------------------------------------------------------

    private static void probarServicioDeUsuarios() {
        System.out.println("\n== Servicio de usuarios ==");

        File carpeta = new File("datos");
        borrarSiExiste(new File(carpeta, "usuarios.sop"));
        UsuarioServicio servicio = new UsuarioServicio(carpeta);

        // Admin por defecto.
        servicio.asegurarAdmin();
        comprobar(servicio.buscar("admin") != null,
                "asegurarAdmin crea el usuario 'admin'");

        // Registro normal.
        try {
            servicio.registrar(new Usuario("Carla Diaz", 'F', "carla", "pass", 25));
            System.out.println("  ok  se registra 'carla'");
        } catch (UsernameDuplicadoException e) {
            comprobar(false, "el registro de 'carla' no deberia fallar");
        }

        // Registrar el mismo username otra vez debe fallar.
        boolean fueRechazado = false;
        try {
            servicio.registrar(new Usuario("Otra Carla", 'F', "carla", "x", 30));
        } catch (UsernameDuplicadoException e) {
            fueRechazado = true;
        }
        comprobar(fueRechazado, "registrar 'carla' dos veces se rechaza");

        // Login.
        try {
            comprobar(servicio.login("carla", "pass") != null,
                    "login con la clave correcta devuelve el usuario");
            comprobar(servicio.login("carla", "mala") == null,
                    "login con la clave mala devuelve null");
        } catch (CuentaDesactivadaException e) {
            comprobar(false, "el login no deberia lanzar excepcion aqui");
        }
    }

    // ------------------------- ayudantes ------------------------------

    /** Imprime "ok" o "FALLA" y, si falla, detiene el programa. */
    private static void comprobar(boolean condicion, String descripcion) {
        if (condicion) {
            System.out.println("  ok  " + descripcion);
        } else {
            System.out.println("  FALLA  " + descripcion);
            throw new AssertionError("Fallo la prueba: " + descripcion);
        }
    }

    /** Borra un archivo si existe (para empezar cada prueba limpia). */
    private static void borrarSiExiste(File archivo) {
        if (archivo.exists() && !archivo.delete()) {
            System.out.println("  (aviso) no se pudo borrar " + archivo);
        }
    }
}
