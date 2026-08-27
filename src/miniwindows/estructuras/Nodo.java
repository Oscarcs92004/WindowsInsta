package miniwindows.estructuras;

/**
 * Un eslabon de la ListaEnlazada.
 *
 * Guarda un dato y una flecha ("siguiente") al proximo eslabon.
 * El ultimo nodo de la lista tiene siguiente = null.
 *
 * Los campos no son privados a proposito: solo ListaEnlazada (que esta
 * en el mismo paquete) los usa, y asi el codigo de la lista se lee mas
 * corto (actual.dato, actual.siguiente).
 */
public class Nodo<T> {

    T dato;
    Nodo<T> siguiente;

    Nodo(T dato) {
        this.dato = dato;
    }
}
