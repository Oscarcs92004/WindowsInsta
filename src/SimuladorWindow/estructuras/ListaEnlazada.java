package SimuladorWindow.estructuras;

import java.util.ArrayList;
import java.util.Objects;

public class ListaEnlazada<T> {

    private Nodo<T> cabeza;

    private int tamano;

    public void agregarInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.siguiente = cabeza;   // el nuevo apunta al que era primero
        cabeza = nuevo;             // y ahora el nuevo es la cabeza
        tamano++;
    }

    public void agregarFinal(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);

        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            Nodo<T> actual = cabeza;
            while (actual.siguiente != null) {
                actual = actual.siguiente;
            }
            actual.siguiente = nuevo;
        }
        tamano++;
    }

    public boolean contiene(T dato) {
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (Objects.equals(actual.dato, dato)) {
                return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }

    public boolean eliminar(T dato) {
        if (cabeza == null) {
            return false;
        }

        if (Objects.equals(cabeza.dato, dato)) {
            cabeza = cabeza.siguiente;
            tamano--;
            return true;
        }

        Nodo<T> actual = cabeza;
        while (actual.siguiente != null) {
            if (Objects.equals(actual.siguiente.dato, dato)) {
                actual.siguiente = actual.siguiente.siguiente;
                tamano--;
                return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }

    public int tamano() {
        return tamano;
    }

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
