# Cómo implementar Mini-Windows + INSTA+

Este documento explica **cómo se construiría el proyecto y por qué se elige cada
patrón**. Está escrito en lenguaje simple y con ejemplos de código cortos que
solo muestran la "forma" de la solución, no el código final.

Regla que se sigue en todo el documento: **la solución más simple que cumpla el
requisito gana**. Donde el enunciado invita a complicarse, hay un aviso.

---

## 1. Resumen en una página

Se construye **una aplicación de escritorio en Java con Swing** con dos partes:

- **Mini-Windows**: simula un sistema operativo. Tiene explorador de archivos,
  editor de texto con formato, visor de imágenes, consola de comandos y
  reproductor de música. Es multiusuario.
- **INSTA+**: una red social estilo Instagram que vive dentro de Mini-Windows y
  guarda todo en los mismos archivos binarios.

El proyecto obliga a usar cuatro cosas técnicas ("los 4 pilares"):

| Pilar | Para qué sirve |
|---|---|
| Archivos binarios | Guardar datos en disco para que no se pierdan al cerrar |
| Hilos (threads) | Hacer varias cosas a la vez sin congelar la ventana |
| Sockets cliente-servidor | Que varias computadoras hablen por red |
| Listas enlazadas simples propias | Ordenar y recorrer datos en memoria |

Y además: excepciones propias, clases utilitarias de entrada/salida, extensiones
de archivo propias (`.ins`, `.sop`) y un diseño visual tipo sistema operativo.

### La idea de arquitectura: capas

Piensa en una torta de 4 capas. Cada capa solo habla con la de abajo:

```
┌─────────────────────────────────────────┐
│  UI (Swing): ventanas, botones, JTree    │  <- lo que el usuario ve
├─────────────────────────────────────────┤
│  Servicios / Lógica: "crear usuario",    │  <- las reglas del negocio
│  "publicar insta", "seguir a alguien"    │
├─────────────────────────────────────────┤
│  Persistencia: leer/escribir los .ins    │  <- hablar con el disco
├─────────────────────────────────────────┤
│  Modelo: Usuario, Publicacion, Mensaje   │  <- los datos puros
└─────────────────────────────────────────┘
```

**Por qué en capas:** si mañana cambias la ventana, no tocas cómo se guardan los
archivos. Si cambias cómo se guardan los archivos, no tocas la ventana. Cada
problema se arregla en un solo lugar.

---

## 2. Cómo se organiza el proyecto (paquetes)

```
src/miniwindows/
├── modelo/        Usuario, Publicacion, Mensaje, Sticker (datos puros)
├── estructuras/   Nodo, ListaEnlazada (nuestra lista enlazada)
├── persistencia/  ArchivoBinario: el ayudante que lee/escribe los .sop y .ins
├── excepciones/   UsernameDuplicadoException, CuentaDesactivadaException, ...
├── servicios/     UsuarioServicio, InstaServicio, ... (las reglas)
├── red/           Servidor, Cliente, protocolo de sockets
├── so/            Mini-Windows: explorador, consola, editor, visor, música
├── insta/         INSTA+: pantallas de la red social
└── ui/            Ventana principal, componentes compartidos
```

**Por qué separar por responsabilidad y no por módulo** (o sea, no una carpeta
"minitwindows" y otra "insta" con todo adentro): los dos módulos comparten el
modelo, la persistencia y las listas enlazadas. Si separas por módulo,
terminas copiando código. Separando por responsabilidad, `Usuario` y
`ArchivoBinario` se escriben **una vez** y los usan los dos.

---

## 3. Pilar 1 — Archivos binarios

### Qué es (simple)

Un archivo de texto guarda letras que una persona puede leer con el Bloc de
notas. Un **archivo binario** guarda los datos tal como los tiene el programa
por dentro (números, objetos), sin traducirlos a letras. Si lo abres con el
Bloc de notas se ve como basura, pero para el programa es más rápido y ocupa
menos.

En Java, la forma más sencilla es la **serialización**: le pides a Java que
tome un objeto entero (un `Usuario` con todos sus campos) y lo escriba de un
golpe.

### El patrón: un ayudante central que todos usan

Hay **un ayudante `ArchivoBinario`** con el código de leer y escribir en un
solo lugar (como pide el checklist). Cada clase de servicio (`UsuarioServicio`,
`InstaServicio`, ...) sabe el nombre de su archivo y le pide a `ArchivoBinario`
que lo guarde o lo lea.

```java
// persistencia/ArchivoBinario.java
public class ArchivoBinario {

    public static synchronized void guardar(File archivo, Object dato) {
        if (archivo.getParentFile() != null) archivo.getParentFile().mkdirs();
        try (ObjectOutputStream salida =
                 new ObjectOutputStream(new FileOutputStream(archivo))) {
            salida.writeObject(dato);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo escribir " + archivo, e);
        }
    }

    public static synchronized Object leer(File archivo) {
        try (ObjectInputStream entrada =
                 new ObjectInputStream(new FileInputStream(archivo))) {
            return entrada.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new ArchivoCorruptoException(archivo.getName(), e);
        }
    }
}
```

Y cada servicio tiene dos métodos `private` cortos para su archivo:

```java
// dentro de UsuarioServicio.java
private final File archivo;   // apunta a "usuarios.sop"

private List<Usuario> leerUsuarios() {
    if (!archivo.exists()) return new ArrayList<>();
    Object contenido = ArchivoBinario.leer(archivo);
    // el cast es seguro: en este archivo solo guardamos List<Usuario>
    @SuppressWarnings("unchecked")
    List<Usuario> usuarios = (List<Usuario>) contenido;
    return usuarios;
}

private void guardarUsuarios(List<Usuario> usuarios) {
    ArchivoBinario.guardar(archivo, new ArrayList<>(usuarios));
}
```

No hay genéricos raros, ni clases abstractas, ni herencia, ni clases "Dao"
sueltas: el código de streams está una vez en `ArchivoBinario` y cada servicio
tiene un `File` y dos métodos privados de 3 líneas.

> **Nota sobre el nombre del registro de usuarios.** El enunciado lo llama de dos
> formas: `usuarios.sop` (en la sección 2.1 y en el checklist como ejemplo de
> "extensión propia") y `users.ins` (en la sección 4, para INSTA+). Como el
> proyecto dice que INSTA+ "reutiliza la misma lógica de almacenamiento del
> resto del sistema" (sección 1), aquí se usa **un solo registro maestro de
> usuarios**, `usuarios.sop`, y tanto Mini-Windows como INSTA+ leen de él. Si tu
> profesor pide que INSTA+ tenga su propio `users.ins` aparte, se le pasa esa
> otra ruta a un segundo servicio. Confírmalo antes de empezar la Fase 4.

**Por qué así:**

- Cumple el requisito del enunciado de "clases utilitarias para leer y escribir
  los archivos binarios de forma centralizada": el código de streams vive solo
  en `ArchivoBinario`.
- Si la serialización falla, se maneja **en un solo lugar** (ver
  `ArchivoCorruptoException` más abajo).
- Las pantallas le piden datos al servicio y no saben nada de streams.
- Todo se lee de corrido; cualquiera puede explicar qué hace línea por línea.

### `Serializable` y `serialVersionUID`

Cada clase del modelo que se vaya a guardar debe implementar `Serializable` y
declarar un número de versión fijo:

```java
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;
    // ...campos...
}
```

**Por qué el `serialVersionUID`:** es como la etiqueta de versión del formato.
Si un día agregas un campo a `Usuario` y no controlas la versión, Java se
niega a leer los archivos viejos y lanza un error raro. Con el número fijo, tú
decides cuándo cambiar de versión y puedes dar un mensaje claro
(`ArchivoCorruptoException`).

### `RandomAccessFile`: solo cuando de verdad lo necesitas

`ObjectOutputStream` reescribe el archivo entero. Para casi todo eso está bien
(los archivos del proyecto son chicos). `RandomAccessFile` te deja saltar a un
registro concreto y cambiar solo ese.

**Regla simple para decidir:**

- ¿Vas a leer/guardar la lista completa? Usa `ArchivoBinario` con serialización.
- ¿Necesitas cambiar UN registro en un archivo grande muchas veces (por
  ejemplo, marcar un mensaje del Inbox como leído sin reescribir 5000
  mensajes)? Ahí sí vale `RandomAccessFile` con registros de tamaño fijo.

Para empezar, usa siempre serialización. Cambia a `RandomAccessFile` solo si
notas que algo va lento de verdad.

### Las extensiones `.ins` y `.sop`

`usuarios.sop`, `insta.ins`, `followers.ins`... son solo **nombres de archivo**.
Por dentro siguen siendo objetos serializados. La extensión propia es un
requisito del checklist del enunciado y sirve para que se note que son archivos
"de este sistema". Se usan dos: `.sop` para los archivos del sistema operativo
(por ejemplo `usuarios.sop`) y `.ins` para los de INSTA+ (los que nombra la
sección 4.3: `following.ins`, `followers.ins`, `insta.ins`, `inbox.ins`,
`stickers.ins`).

---

## 4. Pilar 2 — Listas enlazadas simples

### Qué es (simple)

Imagina una búsqueda del tesoro. Cada papelito tiene **un dato** y **dónde está
el siguiente papelito**. Eso es una lista enlazada: una cadena de nodos.

```
[dato|next] -> [dato|next] -> [dato|next] -> null
  cabeza
```

No necesita decir de antemano cuántos elementos va a tener, y agregar o quitar
uno es solo cambiar un par de flechas.

### La implementación propia

El curso pide una lista enlazada **hecha por ti**, no usar solo `ArrayList`.

```java
// estructuras/Nodo.java
public class Nodo<T> {
    T dato;
    Nodo<T> siguiente;
    Nodo(T dato) { this.dato = dato; }
}
```

```java
// estructuras/ListaEnlazada.java
public class ListaEnlazada<T> {

    private Nodo<T> cabeza;
    private int tamano;

    public void agregarInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.siguiente = cabeza;
        cabeza = nuevo;
        tamano++;
    }

    public void agregarFinal(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (cabeza == null) { cabeza = nuevo; }
        else {
            Nodo<T> actual = cabeza;
            while (actual.siguiente != null) actual = actual.siguiente;
            actual.siguiente = nuevo;
        }
        tamano++;
    }

    public boolean contiene(T dato) {
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (Objects.equals(actual.dato, dato)) return true;
            actual = actual.siguiente;
        }
        return false;
    }

    public boolean eliminar(T dato) { /* recorrer con while y saltar el nodo */ }

    public int tamano() { return tamano; }

    /** Copia los datos a un ArrayList para recorrerlos con un for-each normal. */
    public ArrayList<T> comoLista() {
        ArrayList<T> copia = new ArrayList<>();
        Nodo<T> actual = cabeza;
        while (actual != null) { copia.add(actual.dato); actual = actual.siguiente; }
        return copia;
    }
}
```

Todo se recorre con un `while` normal (`actual = actual.siguiente`), sin `Iterator`
ni clases anónimas. Cuando el resto del programa necesita recorrer la lista con
un `for` corto, llama a `lista.comoLista()` y recorre ese `ArrayList`.

### Dónde se usa (lo pide el enunciado)

- **Timeline de INSTA+**: cada nodo es una publicación; se recorre de la más
  nueva a la más vieja.
- **followers / following**: agregar un seguidor o quitar un "dejar de seguir"
  sin mover el resto.
- **Resultados de búsqueda** (personas y hashtags): se van agregando
  coincidencias, evitando duplicados con `contiene()`.
- **Organizador de archivos**: una lista por categoría (imágenes, documentos,
  música).

### Por qué esto y no `ArrayList` (honesto y corto)

| | Lista enlazada | ArrayList |
|---|---|---|
| Tamaño | Crece sola, nodo por nodo | Reserva bloques y a veces se copia entera |
| Insertar/borrar al inicio | Rápido: cambiar una flecha | Lento: corre todos los demás |
| Llegar al elemento número 500 | Lento: hay que caminar 500 nodos | Rápido: acceso directo por índice |

Para el proyecto da igual el rendimiento (los datos son pocos). Se hace la lista
propia **porque es un requisito académico** y porque encaja bien con "recorrer
de principio a fin" que es lo que hace el timeline.

> **No te compliques:** el enunciado permite usar un `ArrayList` para *ordenar*
> los datos antes de mostrarlos. Ordena con lo que quieras y luego pásalos a tu
> `ListaEnlazada`. No hace falta implementar un algoritmo de ordenamiento sobre
> la lista enlazada.

---

## 5. Pilar 3 — Hilos (threads)

### Qué es (simple)

Un hilo es "una mano" del programa haciendo una tarea. Por defecto tu programa
tiene una sola mano. Si esa mano se pone a cargar un archivo grande, no puede
atender los botones y **la ventana se congela**.

En Swing, esa mano única se llama **EDT** (Event Dispatch Thread) y es la que
dibuja la interfaz. Regla de oro: **tareas largas fuera del EDT; tocar
componentes solo desde el EDT**.

### Qué se resuelve con hilos y cómo

**Reproductor de música** → un hilo propio.

La música tiene que sonar mientras usas el resto del sistema. Se lanza un hilo
que reproduce y responde a play/pause/stop con una bandera `volatile`:

```java
public class HiloReproductor extends Thread {
    private volatile boolean pausado = false;
    private volatile boolean detenido = false;

    public void pausar()   { pausado = true; }
    public void reanudar() { pausado = false; }
    public void detener()  { detenido = true; }

    @Override
    public void run() {
        while (!detenido && hayMasAudio()) {
            if (pausado) { dormir(100); continue; }
            reproducirSiguienteBloque();
        }
    }
}
```

`volatile` hace que cuando un hilo cambia la bandera, el otro hilo la vea de
inmediato.

**Organizador de archivos y carga de imágenes** → `SwingWorker`.

Estas tareas terminan y hay que **mostrar el resultado en la ventana**.
`SwingWorker` está hecho justo para eso: `doInBackground()` corre fuera del EDT,
y `done()` / `process()` corren dentro del EDT.

```java
new SwingWorker<Void, File>() {
    @Override
    protected Void doInBackground() {          // hilo de fondo
        for (File f : carpeta.listFiles()) {
            moverASubcarpetaPorTipo(f);
            publish(f);                         // avisa del avance
        }
        return null;
    }
    @Override
    protected void process(List<File> hechos) { // EDT
        barraProgreso.setValue(hechos.size());
    }
    @Override
    protected void done() {                      // EDT
        arbol.recargar();
        JOptionPane.showMessageDialog(null, "Carpeta organizada");
    }
}.execute();
```

**Por qué `SwingWorker` y no un `Thread` a mano:** con `Thread` tendrías que
llamar tú a `SwingUtilities.invokeLater(...)` cada vez que tocas la ventana, y
es fácil equivocarse. `SwingWorker` ya te separa "lo de fondo" de "lo de la
ventana". Es la herramienta a la altura correcta del problema.

**Notificaciones del Inbox** → un hilo demonio que revisa cada cierto tiempo.

```java
Thread vigilante = new Thread(() -> {
    while (true) {
        int noLeidos = instaServicio.contarMensajesNoLeidos(usuarioActual);
        SwingUtilities.invokeLater(() -> iconoInbox.setBadge(noLeidos));
        try { Thread.sleep(5000); } catch (InterruptedException e) { return; }
    }
});
vigilante.setDaemon(true);   // no impide cerrar la app
vigilante.start();
```

Un hilo **demonio** es uno que no impide que el programa se cierre: cuando
cierras todo, este hilo muere solo.

### Cuidado: dos hilos escribiendo el mismo archivo

Si el hilo del Inbox y una pantalla escriben `followers.ins` al mismo tiempo, el
archivo puede quedar a medias y corromperse.

Solución simple: los métodos de `ArchivoBinario` (`guardar` y `leer`) ya están
marcados `synchronized` (míralo en el Pilar 1). Eso significa "solo un hilo a la
vez dentro de este método". Con eso basta para el proyecto.

---

## 6. Pilar 4 — Sockets cliente-servidor

### Qué es (simple)

Un **socket** es un tubo entre dos programas por la red. En cliente-servidor:

- El **servidor** es un programa que se queda esperando en un "número de puerta"
  (un puerto, por ejemplo el 5000).
- Los **clientes** son cada copia de la app que se conecta a esa puerta para
  pedir cosas.

Analogía: el servidor es la cocina de un restaurante; los clientes son las
mesas. Las mesas no entran a la cocina; piden por el mesero y la cocina
responde.

### La arquitectura

**El servidor es el único que toca los archivos `.ins`.** Los clientes nunca
abren los archivos; le piden al servidor que lo haga.

**Por qué:** si dos computadoras abrieran el mismo archivo por red, se pisarían.
Con un solo servidor dueño de los archivos, no hay conflicto.

```
Cliente A ─┐
Cliente B ─┼── (red) ──> Servidor ──> usuarios.sop, insta.ins, followers.ins...
Cliente C ─┘
```

### El servidor: un hilo por cliente

Aquí se reutiliza el Pilar 3. El servidor acepta conexiones en un bucle y por
cada cliente lanza un hilo que lo atiende:

```java
public class Servidor {
    public static void main(String[] args) throws IOException {
        try (ServerSocket servidor = new ServerSocket(5000)) {
            System.out.println("Servidor escuchando en el puerto 5000");
            while (true) {
                Socket cliente = servidor.accept();      // espera a alguien
                new Thread(new AtenderCliente(cliente)).start();
            }
        }
    }
}
```

```java
class AtenderCliente implements Runnable {
    private final Socket socket;
    AtenderCliente(Socket socket) { this.socket = socket; }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                 new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String linea;
            while ((linea = in.readLine()) != null) {
                out.println(procesar(linea));   // responde una línea
            }
        } catch (IOException e) {
            // el cliente se desconectó
        }
    }

    private String procesar(String linea) {
        String[] p = linea.split(";");
        switch (p[0]) {
            case "LOGIN":  return login(p[1], p[2]);
            case "POST":   return publicar(p[1]);
            case "FOLLOW": return seguir(p[1]);
            default:       return "ERROR;comando desconocido";
        }
    }
}
```

### El protocolo: texto simple por líneas

Cliente manda: `LOGIN;patito;1234`
Servidor responde: `OK;bienvenido` o `ERROR;credenciales`

**Por qué texto y no objetos serializados por el socket:** el texto lo puedes
probar a mano con `telnet localhost 5000` y ver qué pasa. Si algo falla, lo lees
directamente. Con objetos binarios tendrías que depurar a ciegas.

### El cliente

```java
public class Cliente {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    public Cliente(String host, int puerto) throws IOException {
        socket = new Socket(host, puerto);
        in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public String pedir(String comando) throws IOException {
        out.println(comando);
        return in.readLine();
    }
}
```

### Versión mínima que cumple el requisito

No hace falta que TODO pase por la red desde el día uno. Basta con:

1. `Servidor` corriendo en `localhost`.
2. `Cliente` conectado.
3. Tres comandos reales de punta a punta: `LOGIN`, `POST`, `FOLLOW`.

Con eso el pilar de sockets está demostrado. Los demás comandos (`INBOX`,
`SEARCH`, ...) se agregan con el mismo molde: una línea más en el `switch`.

> **No te compliques:** para entregar y defender el proyecto, `localhost` es
> suficiente. Si quieres probar entre dos computadoras, solo cambias
> `"localhost"` por la IP del servidor y abres el puerto en el firewall. No
> necesitas un pool de hilos ni librerías de red; `new Thread(...)` por cliente
> está bien para la cantidad de usuarios de una demo.

---

## 7. Excepciones propias

El enunciado pide **al menos 3**. Se crean estas:

```java
public class UsernameDuplicadoException extends Exception {
    public UsernameDuplicadoException(String username) {
        super("El username '" + username + "' ya existe");
    }
}

public class CuentaDesactivadaException extends Exception {
    public CuentaDesactivadaException(String username) {
        super("La cuenta '" + username + "' está desactivada");
    }
}

public class ArchivoCorruptoException extends RuntimeException {
    public ArchivoCorruptoException(String archivo, Throwable causa) {
        super("El archivo '" + archivo + "' no se pudo leer", causa);
    }
}
```

**Por qué `UsernameDuplicadoException` y `CuentaDesactivadaException` son
comprobadas** (`extends Exception`): son situaciones **normales** que el programa
debe manejar (mostrar un mensaje y dejar reintentar). Al ser comprobadas, Java
te obliga a no olvidarte de ellas.

**Por qué `ArchivoCorruptoException` es `RuntimeException`:** si un `.ins` está
corrupto, no hay mucho que el usuario pueda hacer; se muestra un mensaje y se
corta esa operación. No tiene sentido obligar a cada método a declararla.

Dónde se lanzan:

- `UsernameDuplicadoException`: al crear cuenta, si el username ya está en
  `usuarios.sop`.
- `CuentaDesactivadaException`: al intentar iniciar sesión o al ver un perfil
  desactivado.
- `ArchivoCorruptoException`: dentro de `ArchivoBinario.leer()` cuando la
  deserialización falla.

---

## 8. Módulo Mini-Windows: decisiones concretas

### Sistema de archivos simulado: usa carpetas reales

`Z:\` no existe de verdad. La forma **simple**: elegir una carpeta real del
proyecto, por ejemplo `datos/Z/`, y que esa sea la raíz. Todo lo que el usuario
"crea" dentro de Mini-Windows son carpetas y archivos reales dentro de
`datos/Z/`.

**Por qué:**

- Puedes usar `java.io.File` de verdad: `mkdir()`, `renameTo()`, `listFiles()`,
  `lastModified()`, `length()` ya vienen hechos.
- El `JTree` se llena leyendo esa carpeta real.
- Copiar/pegar es copiar bytes de un `File` a otro.

Simular un sistema de archivos **dentro** de un archivo binario sería muchísimo
más código (tabla de bloques, rutas, permisos...) y más fácil de romper. Esta es
**la decisión más importante de "no sobre-ingenierizar"** de todo el proyecto.

```java
public class SistemaArchivos {
    private final File raiz;           // datos/Z
    private File carpetaActual;        // para la consola

    public SistemaArchivos(File raiz) {
        this.raiz = raiz;
        this.carpetaActual = raiz;
    }
    public File[] listar()            { return carpetaActual.listFiles(); }
    public boolean crearCarpeta(String n) { return new File(carpetaActual, n).mkdir(); }
    // renombrar, copiar, pegar, cd, ...
}
```

### Multiusuario

- Un usuario del sistema se guarda en `usuarios.sop`.
- Al crearlo: se crea `datos/Z/<usuario>/` con las carpetas `Mis Documentos`,
  `Música` y `Mis Imágenes`.
- Rol como `enum`:

```java
public enum Rol { ADMINISTRADOR, ESTANDAR }
```

- El chequeo de permisos es un `if` simple: si `rol == ESTANDAR`, la raíz que ve
  es `datos/Z/<usuario>/`; si es `ADMINISTRADOR`, la raíz es `datos/Z/`.

### Consola de comandos

Son 7 comandos fijos (`mkdir`, `rm`, `cd`, `cd..`, `dir`, `date`, `time`). Un
`switch` sobre la primera palabra:

```java
public String ejecutar(String linea) {
    String[] partes = linea.trim().split("\\s+", 2);
    String comando = partes[0];

    String argumento = "";
    if (partes.length > 1) {
        argumento = partes[1];
    }

    switch (comando) {
        case "mkdir":
            if (fs.crearCarpeta(argumento)) return "";
            return "No se pudo crear";
        case "rm":
            if (fs.eliminar(argumento)) return "";
            return "No se pudo borrar";
        case "cd":   return fs.cambiar(argumento);
        case "cd..": return fs.subirNivel();
        case "dir":  return fs.listarComoTexto();
        case "date": return LocalDate.now().toString();
        case "time": return LocalTime.now().toString();
        default:     return "'" + comando + "' no se reconoce";
    }
}
```

**Por qué no un "patrón Command" con un registro de comandos:** ese patrón sirve
cuando los comandos cambian mucho o los cargas dinámicamente. Aquí son 7 y no
van a cambiar. El `switch` es más corto, se lee de una y no esconde nada.

### Editor de texto con formato

Usa `JTextPane`, que sí soporta colores y fuentes (a diferencia de `JTextArea`).

El texto se guarda en el `.txt`. El **formato** (color, fuente, tamaño por
tramo) se guarda aparte en un archivo binario paralelo, por ejemplo
`micarta.txt` + `micarta.txt.fmt`. El `.fmt` es una lista serializada de tramos:

```java
public class TramoFormato implements Serializable {
    private static final long serialVersionUID = 1L;
    int inicio, fin;
    String fuente;
    int tamano;
    int colorRGB;
}
```

Al abrir: se lee el `.txt`, luego se aplica cada `TramoFormato` al
`StyledDocument`.

**Por qué así (lo pide el enunciado):** la sección 3.4 dice literalmente
"archivos .txt" y que "este formato debe conservarse y visualizarse cada vez que
el archivo se vuelva a abrir". Si guardaras el contenido como RTF, el archivo ya
no sería un `.txt` de verdad. Con el archivo paralelo, el `.txt` sigue siendo
texto plano normal y el formato se conserva. Además, el `.fmt` es otra
**extensión propia**, que también es un requisito del checklist.

### Visor de imágenes

Un `JLabel` con `ImageIcon`, una lista de las imágenes de la carpeta y un índice:

```java
private List<File> imagenes;
private int i = 0;

void siguiente() { if (i < imagenes.size() - 1) mostrar(++i); }
void anterior()  { if (i > 0) mostrar(--i); }
void mostrar(int k) { etiqueta.setIcon(new ImageIcon(imagenes.get(k).getPath())); }
```

### Reproductor de música

Interfaz mínima y una implementación con la librería de audio que uses:

```java
public interface Reproductor {
    void play();
    void pause();
    void stop();
    void cargar(File mp3);
}
```

La reproducción real corre en el hilo del Pilar 3. La lista de canciones,
carátula y descripción son datos que sacas del archivo o de un `.ins` de
canciones.

**Sobre la librería (decisión pendiente):** Java puro solo reproduce `.wav`
(`javax.sound.sampled`). Para `.mp3` se elige más adelante entre **JLayer**
(un JAR en `lib/`, la opción más simple y recomendada), **JavaFX MediaPlayer**
(no viene con el JDK, hay que instalarlo aparte) o **quedarse solo con `.wav`**.
La interfaz `Reproductor` no cambia con esa decisión; solo cambia la clase que
la implementa.

---

## 9. Módulo INSTA+: decisiones concretas

### Una sola pantalla: `CardLayout`

El enunciado insiste en que INSTA+ funciona en **una sola ventana**, sin abrir
ventanas nuevas. `CardLayout` es un layout que apila paneles como cartas y
muestra una a la vez:

```java
CardLayout cartas = new CardLayout();
JPanel contenedor = new JPanel(cartas);
contenedor.add(new PanelPerfil(),      "PERFIL");
contenedor.add(new PanelTimeline(),    "TIMELINE");
contenedor.add(new PanelBuscar(),      "BUSCAR");
// ...las 9 opciones...

// cambiar de "pantalla":
cartas.show(contenedor, "TIMELINE");
```

**Por qué `CardLayout`:** hace exactamente lo que pide el enunciado (cambiar de
vista sin ventanas emergentes) con 3 líneas. No necesitas un framework de
navegación.

### Estructura de archivos por usuario

Al registrarse se crea `INSTA_RAIZ/<username>/` con:

| Archivo | Contenido |
|---|---|
| `following.ins` | usernames que el usuario sigue |
| `followers.ins` | usernames que lo siguen (sin duplicados) |
| `insta.ins` | sus publicaciones (autor, fecha, texto ≤ 140) |
| `inbox.ins` | mensajes privados enviados y recibidos |
| `stickers.ins` | stickers disponibles para ese usuario |

Más las subcarpetas `/imagenes`, `/folders_personales`,
`/stickers_personales`. Y a nivel general: el registro maestro `usuarios.sop`
(ver la nota de la sección 3) y la carpeta `stickers_globales/`.

El `InstaServicio` maneja esos `.ins` con el mismo molde que `UsuarioServicio`:
guarda las rutas de los archivos y tiene métodos privados cortos que llaman a
`ArchivoBinario`.

### Timeline, Interacciones, Buscar, Hashtag

Todos siguen el mismo molde:

1. Leer los `.ins` que hagan falta (los tuyos + los de a quienes sigues).
2. Recorrerlos y meter las coincidencias en una `ListaEnlazada`.
3. Evitar duplicados con `contiene()`.
4. Ordenar por fecha si aplica (timeline: más nueva primero).

```java
public ListaEnlazada<Publicacion> timelineDe(Usuario u) {
    ListaEnlazada<Publicacion> resultado = new ListaEnlazada<>();

    List<String> autores = instaServicio.aQuienesSigue(u.getUsername());
    autores.add(u.getUsername());                 // también mis propios instas

    for (String autor : autores) {
        if (!usuarioServicio.estaVisible(autor)) continue;   // salta desactivados
        for (Publicacion p : instaServicio.publicacionesDe(autor)) {
            resultado.agregarFinal(p);
        }
    }
    return ordenarPorFechaDesc(resultado);
}
```

### Cuenta desactivada: un solo punto de filtrado

Una cuenta desactivada "se comporta como si no existiera" en búsquedas y
timeline. La tentación es poner `if (usuario.estaActiva())` en cada pantalla.
**Mejor:** un solo método:

```java
public boolean estaVisible(String username) {
    Usuario u = buscar(username);
    return u != null && u.isActiva();
}
```

Y todas las búsquedas y el timeline llaman a `estaVisible(...)`. Si mañana
cambia la regla de visibilidad, se cambia **en un lugar**. Esto es "arreglar el
problema a la altura correcta" en vez de repartir parches.

### Stickers, Inbox, activar/desactivar, responsive

- **Stickers**: 5 por defecto (`Feliz`, `Triste`, `Corazón`, `Risa`,
  `Aplauso`). Importar valida que sea `.png` o `.jpg`, copia el archivo a
  `/stickers_personales` y agrega un registro a `stickers.ins`.
- **Inbox**: cada mensaje es un registro en `inbox.ins` con emisor, receptor,
  fecha, texto (≤ 300), tipo (`TEXTO` o `STICKER`) y estado (`LEIDO` /
  `NO_LEIDO`). Marcar como leído = cambiar el campo y volver a guardar.
- **Activar/desactivar**: un `boolean activa` en `Usuario`. Desactivar pide
  confirmación; reactivar no.
- **Responsive**: una constante controla el escalado de las imágenes.

```java
public final class Config {
    public static final boolean MODO_MOBILE = true;
    public static final int COLUMNAS_GRID = 3;               // grid de publicaciones

    // tamaños recomendados por el enunciado (4.6), en px:
    public static final Dimension CUADRADA   = new Dimension(1080, 1080);
    public static final Dimension VERTICAL   = new Dimension(1080, 1350);
    public static final Dimension HORIZONTAL = new Dimension(1080, 566);
}
```

Al mostrar una imagen se escala a la `Dimension` que corresponda según su
orientación. No hace falta un sistema de temas ni media queries; es una
constante y un `if`.

---

## 10. Orden sugerido de construcción

Construye de abajo hacia arriba. Cada hito debe compilar y probarse antes de
seguir.

1. **Base**: `modelo/` + `ArchivoBinario` + `excepciones/` + `ListaEnlazada` +
   `UsuarioServicio`. Pruébalo con un `main` que guarde y lea usuarios.
2. **Cuentas**: login, registro (con `UsernameDuplicadoException`), creación de
   las carpetas del usuario.
3. **Mini-Windows**: explorador (`JTree` sobre `datos/Z/`), consola, editor,
   visor de imágenes, reproductor.
4. **INSTA+ en local** (sin red todavía): perfil, cargar imágenes, timeline,
   búsquedas, inbox, stickers. Aquí ya usas la `ListaEnlazada` de verdad.
5. **Sockets**: `Servidor` + `Cliente` en `localhost`, y mueve `LOGIN`, `POST`
   y `FOLLOW` para que pasen por el servidor.
6. **Hilos de fondo y pulido**: `SwingWorker` del organizador, hilo demonio de
   notificaciones del Inbox, detalles visuales del "sistema operativo".

---

## 11. Glosario mínimo

- **EDT (Event Dispatch Thread)**: el único hilo de Swing que dibuja la ventana
  y atiende clics. No lo bloquees.
- **Serialización**: convertir un objeto entero en bytes para guardarlo, y al
  revés.
- **Servicio**: clase que junta las reglas de un tema (usuarios, INSTA+, ...) y
  el guardar/leer de su archivo. El resto del programa le pide cosas a ella y
  no toca archivos ni streams.
- **Socket**: el tubo de comunicación entre dos programas por la red.
- **Hilo demonio**: hilo que no impide cerrar el programa; muere cuando la app
  termina.
- **`volatile`**: marca una variable para que todos los hilos vean su último
  valor al instante.
- **`synchronized`**: "solo un hilo a la vez dentro de este método".
