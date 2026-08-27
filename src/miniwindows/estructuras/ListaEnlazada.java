package miniwindows.estructuras;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Lista enlazada simple hecha a mano (lo pide el enunciado, seccion 2.4).
 *
 * La lista es una cadena de nodos. Cada Nodo guarda un dato y una flecha
 * ("siguiente") al proximo nodo. El ultimo apunta a null.
 *
 *   cabeza -> [ "a" | *-]--> [ "b" | *-]--> [ "c" | null ]
 *
 * Se recorre siempre igual: se parte de la cabeza y se va saltando de
 * nodo en nodo con "actual = actual.siguiente" hasta llegar a null.
 */
public class ListaEnlazada<T> {

    /** El primer nodo de la lista. Si la lista esta vacia, es null. */
    private Nodo<T> cabeza;

    /** Cuantos elementos tiene la lista ahora mismo. */
    private int tamano;

    /** Agrega un dato al principio de la lista. */
    public void agregarInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.siguiente = cabeza;   // el nuevo apunta al que era primero
        cabeza = nuevo;             // y ahora el nuevo es la cabeza
        tamano++;
    }

    /** Agrega un dato al final de la lista. */
    public void agregarFinal(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);

        if (cabeza == null) {
            // La lista estaba vacia: el nuevo nodo es la cabeza.
            cabeza = nuevo;
        } else {
            // Caminamos hasta el ultimo nodo y enganchamos el nuevo ahi.
            Nodo<T> actual = cabeza;
            while (actual.siguiente != null) {
                actual = actual.siguiente;
            }
            actual.siguiente = nuevo;
        }
        tamano++;
    }

    /** Dice si un dato esta en la lista. */
    public boolean contiene(T dato) {
        Nodo<T> actual = cabeza;
        while (actual != null) {
            // Objects.equals compara sin explotar aunque alguno sea null.
            if (Objects.equals(actual.dato, dato)) {
                return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }

    /**
     * Quita la primera aparicion de un dato.
     * Devuelve true si lo encontro y lo quito, false si no estaba.
     */
    public boolean eliminar(T dato) {
        if (cabeza == null) {
            return false;                       // lista vacia, nada que quitar
        }

        // Caso especial: el dato esta en la cabeza.
        if (Objects.equals(cabeza.dato, dato)) {
            cabeza = cabeza.siguiente;          // la cabeza pasa a ser el segundo
            tamano--;
            return true;
        }

        // Caso general: buscamos el nodo ANTERIOR al que queremos quitar,
        // para poder "saltarlo" con la flecha.
        Nodo<T> actual = cabeza;
        while (actual.siguiente != null) {
            if (Objects.equals(actual.siguiente.dato, dato)) {
                actual.siguiente = actual.siguiente.siguiente;   // salta el nodo
                tamano--;
                return true;
            }
            actual = actual.siguiente;
        }
        return false;                           // no se encontro
    }

    /** Cuantos elementos tiene la lista. */
    public int tamano() {
        return tamano;
    }

    /**
     * Copia los datos de la lista a un ArrayList y lo devuelve.
     *
     * Sirve para recorrer la lista con un for-each normal desde fuera de
     * la clase:  for (T x : lista.comoLista()) { ... }
     *
     * El enunciado (2.4) permite apoyarse en un ArrayList para extraer y
     * ordenar los datos antes de mostrarlos.
     */
    public ArrayList<T> comoLista() {
        ArrayList<T> copia = new ArrayList<>();
        Nodo<T> actual = cabeza;
        while (actual != null) {
            copia.add(actual.dato);
            actual = actual.siguiente;
        }
        return copia;
    }
}
