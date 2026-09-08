# Plan de desarrollo paso a paso — Mini-Windows + INSTA+

Este documento te lleva de la mano desde un proyecto vacío hasta el proyecto
terminado. Está pensado para alguien de **primer año**: los pasos son pequeños y
cada bloque de código es corto.

La idea central es el **ciclo de desarrollo**. En vez de escribir todo y probar
al final (que siempre sale mal), repites este ciclo muchas veces:

```
1. Escribo un pedacito pequeño
2. Lo compilo
3. Lo pruebo (que haga lo que espero)
4. Confirmo que funciona
5. Hago un commit (guardo el avance en git)
   -> vuelvo al paso 1
```

Cada "iteración" de este plan es una vuelta completa a ese ciclo. Si algo falla
en el paso 2 o 3, arréglalo antes de seguir. Nunca acumules código roto.

> Referencia: el **qué** y el **por qué** de cada patrón está en
> `IMPLEMENTACION.md`. Este documento es el **cuándo** y el **en qué orden**.

---

## Reglas globales (léelas una vez)

- **Texto siempre en UTF-8.** Windows por defecto no usa UTF-8 y los acentos y la
  `ñ` se rompen. Cada vez que leas o escribas texto, pásalo explícito:

  ```java
  Files.writeString(ruta, contenido, java.nio.charset.StandardCharsets.UTF_8);
  String s = Files.readString(ruta, java.nio.charset.StandardCharsets.UTF_8);
  ```

- **Dónde vive cada cosa en disco** (todo dentro de `datos/`, que no va a git):

  ```
  datos/
  ├── usuarios.sop            registro maestro de usuarios (lo usan los 2 módulos)
  ├── Z/                      el "disco Z:" del sistema operativo
  │   ├── admin/
  │   └── <usuario>/          Mis Documentos, Música, Mis Imágenes
  └── INSTA_RAIZ/             la red social INSTA+
      ├── stickers_globales/
      └── <username>/         following.ins, followers.ins, insta.ins,
                              inbox.ins, stickers.ins, /imagenes, ...
  ```

  El enunciado usa dos nombres para el registro de usuarios: `usuarios.sop`
  (sección 2.1 y checklist) y `users.ins` (sección 4). Como INSTA+ "reutiliza la
  misma lógica de almacenamiento del resto del sistema", aquí se usa **uno solo**,
  `usuarios.sop`. Si tu profesor exige un `users.ins` aparte para INSTA+, se le
  pasa esa otra ruta a un segundo servicio. Pregúntale antes de la Fase 4.

- **Librerías externas:** van en una carpeta `lib/` en la raíz del proyecto. Para
  que IntelliJ las use: *File → Project Structure → Libraries → +* y eliges el
  JAR. `lib/` **sí** va a git (para que a tu compañero le funcione igual).

- **Un cambio que funciona = un commit.** No acumules.

## Cómo dividir el trabajo (2 personas)

Llamemos a las dos personas **P1** y **P2**. El orden de las fases no cambia;
lo que cambia es quién hace cada cosa.

| Momento | P1 | P2 |
|---|---|---|
| **Fase 0** | Prepara el repo, `.gitignore`, `Main.java`, sube todo a `main`. | Clona el repo, verifica que compila y corre en su máquina. |
| **Fase 1 (núcleo)** | `modelo/`, `persistencia/`, `excepciones/`, `UsuarioServicio`, admin por defecto. | `estructuras/` (`Nodo`, `ListaEnlazada`) y escribe los `main` de prueba que verifican lo de P1. |
| **Fase 2 (login)** | Ventana principal, login, registro, carpetas del usuario. | Revisa y prueba; mientras, empieza a leer la Fase 4. |
| **Fase 3 — Mini-Windows** | **Toda la Fase 3** (explorador, consola, editor, visor, reproductor, organizar). | — |
| **Fase 4 — INSTA+** | — | **Toda la Fase 4** (pantalla única, perfil, publicar, timeline, seguir, buscar, inbox, stickers, cuentas de ejemplo). |
| **Fase 5 — Sockets** | Servidor (`Servidor`, `AtenderCliente`, comandos). | Cliente (`Cliente`) y cambiar las pantallas para que usen el cliente. |
| **Fase 6 — Hilos y pulido** | Hilo de notificaciones del Inbox, revisar `synchronized`. | Excepciones conectadas a diálogos, pulido visual. |
| **Fase 7 — Cierre** | Empaquetar el JAR, tag de entrega. | Recorrer la checklist contra el PDF, escribir el README. |

Las Fases 3 y 4 son el grueso del trabajo y se hacen **al mismo tiempo**: P1 en
el paquete `so/`, P2 en el paquete `insta/`. Casi no se tocan.

**Regla de oro para no chocar:** cuando termine la Fase 1, los paquetes
`modelo`, `estructuras`, `persistencia` y `excepciones` quedan **congelados**. Si
uno de los dos necesita cambiar algo ahí (por ejemplo, agregar un campo a
`Usuario`), lo avisa al otro **antes** de tocarlo, para hacerlo juntos en una
sola pasada.

**Flujo de git para 2 (lo más simple):**

El enunciado no pide nada de git, así que no hace falta ramas. Como cada uno
trabaja en su propio paquete, los dos pueden trabajar directo sobre `main`:

```
git pull                 # ANTES de empezar: traes lo de tu compañero
# ...trabajas, commits pequeños, que siempre compile...
git pull                 # por si el otro subió algo mientras tanto
git push                 # subes lo tuyo
```

**Acuerdo mínimo entre los dos:**

- Nunca hacer `push` con código que no compila.
- Antes de empezar una tarea, un mensaje corto: "voy con el editor de texto".
- `git pull` seguido, para no acumular diferencias.
- Si al hacer `pull` sale un conflicto, es que ambos tocaron el mismo archivo:
  ábranlo juntos, dejen la versión buena, `git add` y `git commit`. Respetando
  la regla de oro esto casi no pasa.

Si más adelante quieren que `main` esté siempre perfecto, pueden usar ramas
(`git checkout -b tarea` → trabajar → `git merge` a `main`), pero para dos
personas en paquetes separados no es necesario.

---

## Índice

- [Fase 0 — Preparar el terreno](#fase-0--preparar-el-terreno)
- [Fase 1 — El núcleo (sin ventanas)](#fase-1--el-núcleo-sin-ventanas)
- [Fase 2 — La primera ventana (login)](#fase-2--la-primera-ventana-login)
- [Fase 3 — Mini-Windows](#fase-3--mini-windows)
- [Fase 4 — INSTA+ en local](#fase-4--insta-en-local)
- [Fase 5 — Red con sockets](#fase-5--red-con-sockets)
- [Fase 6 — Hilos de fondo y pulido](#fase-6--hilos-de-fondo-y-pulido)
- [Fase 7 — Cierre y entrega](#fase-7--cierre-y-entrega)

---

## Fase 0 — Preparar el terreno

Objetivo de la fase: dejar el proyecto listo y **entender el ciclo** con un
ejemplo tonto antes de tocar nada serio.

### Iteración 0.1 — Herramientas

Necesitas:

- **JDK** (Java Development Kit) 17 o más nuevo. Comprueba en una terminal:

  ```
  java -version
  javac -version
  ```

  Los dos deben responder con un número de versión. Si `javac` no existe,
  tienes el JRE pero no el JDK; instala el JDK.

- **IntelliJ IDEA** (Community sirve). Ya hay un proyecto abierto.
- **Git**. Comprueba: `git --version`.

**Cómo probar:** los tres comandos de arriba responden sin error.

**Commit:** todavía no hay nada que guardar.

### Iteración 0.2 — Estructura de carpetas

Dentro del proyecto vas a tener:

```
MiniWindows/
├── src/                 <- todo el código Java
│   └── Main.java
├── lib/                 <- JARs de librerías externas; SÍ va a git
├── datos/               <- se crea sola al ejecutar; NO va a git
│   ├── Z/               <- la "unidad Z:" del sistema simulado
│   └── INSTA_RAIZ/      <- datos de la red social INSTA+
├── IMPLEMENTACION.md
├── PLAN_DESARROLLO.md
└── .gitignore
```

Por ahora solo existe `src/Main.java`. Lo demás lo iremos creando.

**Cómo probar:** `ls src` muestra `Main.java`.

### Iteración 0.3 — Arreglar `Main.java` y entender compilar/ejecutar

El `Main.java` que generó IntelliJ usa una sintaxis nueva y experimental
(`void main()` sin clase, `IO.println`). Para un proyecto normal usamos la
forma estándar. Reemplázalo por:

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Mini-Windows arranca");
    }
}
```

Ahora, **las dos formas de ejecutarlo**:

- **En IntelliJ:** botón ▶ (Run) al lado de `main`.
- **En terminal**, parado en la carpeta del proyecto:

  ```
  javac -d out src/Main.java      # compila: crea out/Main.class
  java -cp out Main               # ejecuta la clase Main que está en out/
  ```

  `javac` traduce tu `.java` a `.class` (bytecode). `java` corre ese `.class`.
  `-d out` dice "pon los .class en la carpeta out". `-cp out` dice "busca las
  clases en out".

**Cómo probar:** al ejecutar aparece `Mini-Windows arranca`.

**Commit:**

```
git add -A
git commit -m "Usar main estandar y documentar como compilar"
```

### Iteración 0.4 — `.gitignore` y primer commit limpio

Hay cosas que **no** deben ir a git: los `.class` compilados y los datos que
genera el programa. Revisa que `.gitignore` incluya (agrégalo si falta):

```
out/
datos/
```

**Cómo probar:** `git status` no muestra `out/` ni `datos/` como cambios.

**Commit:**

```
git add -A
git commit -m "Ignorar carpetas out y datos"
```

### Iteración 0.5 — El ciclo completo con un ejemplo tonto

Vamos a hacer una vuelta entera al ciclo con algo sin importancia, solo para
sentir el ritmo. Agrega un método y pruébalo:

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("2 + 3 = " + sumar(2, 3));
    }

    static int sumar(int a, int b) {
        return a + b;
    }
}
```

1. Escribí el pedacito (`sumar`).
2. Compilo: `javac -d out src/Main.java`.
3. Pruebo: `java -cp out Main` → debe imprimir `2 + 3 = 5`.
4. Funciona.
5. Commit: `git commit -am "Ejemplo: metodo sumar"`.

Ese es **exactamente** el ritmo que vas a repetir cientos de veces. Lo único que
cambia es que el "pedacito" será cada vez más interesante.

---

## Fase 1 — El núcleo (sin ventanas)

Objetivo de la fase: tener los datos, la lista enlazada, las excepciones y el
guardado en disco **funcionando y probados**, sin nada de interfaz gráfica.
Probar cosas sin ventana es más rápido y te enseña si la lógica está bien.

### Iteración 1.0 — Organizar en paquetes

Ya vamos a tener varias clases, así que las agrupamos en **paquetes** (carpetas
con significado). Crea estas carpetas dentro de `src/`:

```
src/miniwindows/
├── modelo/
├── estructuras/
├── excepciones/
├── persistencia/
└── servicios/
```

Y mueve `Main.java` a `src/miniwindows/Main.java`, agregándole la primera línea:

```java
package miniwindows;

public class Main {
    public static void main(String[] args) {
        System.out.println("Mini-Windows arranca");
    }
}
```

A partir de ahora, para compilar **todo** el proyecto:

```
javac -d out $(find src -name "*.java")
java -cp out miniwindows.Main
```

(En Windows PowerShell: `javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName`)

**Cómo probar:** compila sin errores y sigue imprimiendo el mensaje.

**Commit:** `git commit -am "Organizar codigo en paquetes"`

### Iteración 1.1 — La clase `Usuario`

Una clase de datos pura: campos, constructor, getters. Nada de lógica.

```java
package miniwindows.modelo;

import java.io.Serializable;
import java.time.LocalDate;

public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombreCompleto;
    private char genero;              // 'M' o 'F'
    private String username;          // único en el sistema
    private String password;
    private LocalDate fechaRegistro;
    private int edad;
    private boolean activa = true;
    private String fotoPerfil;        // ruta a la imagen

    public Usuario(String nombreCompleto, char genero, String username,
                   String password, int edad) {
        this.nombreCompleto = nombreCompleto;
        this.genero = genero;
        this.username = username;
        this.password = password;
        this.edad = edad;
        this.fechaRegistro = LocalDate.now();
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean isActiva()   { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
    // ...los demás getters que vayas necesitando...
}
```

**Cómo probar:** en `Main`, crea uno e imprímelo:

```java
Usuario u = new Usuario("Ana Pérez", 'F', "ana", "1234", 20);
System.out.println(u.getUsername() + " creada el " + /* getter fecha */ "");
```

Compila y corre: debe imprimir `ana creada el ...`.

**Commit:** `git commit -am "Modelo Usuario"`

### Iteración 1.2 — La lista enlazada

Copia `Nodo<T>` y `ListaEnlazada<T>` de `IMPLEMENTACION.md` (sección 4) a
`src/miniwindows/estructuras/`.

**Cómo probar** con un `main` de prueba:

```java
ListaEnlazada<String> l = new ListaEnlazada<>();
l.agregarFinal("a");
l.agregarFinal("b");
l.agregarInicio("z");
for (String s : l.comoLista()) System.out.print(s + " ");   // z a b
System.out.println("\ntamaño = " + l.tamano());             // 3
System.out.println(l.contiene("b"));                        // true
```

Corre y verifica que la salida sea exactamente `z a b`, `tamaño = 3`, `true`.
La lista se recorre desde fuera con `l.comoLista()` (un `ArrayList` con los
datos); por dentro, `contiene` y `eliminar` usan un `while` normal.

**Commit:** `git commit -am "Estructura ListaEnlazada propia"`

### Iteración 1.3 — Las excepciones propias

Crea las 3 clases de `IMPLEMENTACION.md` (sección 7) en
`src/miniwindows/excepciones/`. No hay nada que probar todavía: solo deben
compilar.

**Commit:** `git commit -am "Excepciones propias del sistema"`

### Iteración 1.4 — El ayudante `ArchivoBinario`

En `persistencia/ArchivoBinario.java`, copia el ayudante de la sección 3 de
`IMPLEMENTACION.md`: dos métodos estáticos, `guardar(File, Object)` y
`leer(File)`. Sin genéricos, sin herencia, sin clases "Dao".

**Cómo probar** con un `main` de prueba:

```java
File archivo = new File("datos", "prueba.sop");

List<Usuario> original = new ArrayList<>();
original.add(new Usuario("Ana Perez", 'F', "ana", "1234", 20));
ArchivoBinario.guardar(archivo, original);

@SuppressWarnings("unchecked")
List<Usuario> leidos = (List<Usuario>) ArchivoBinario.leer(archivo);
System.out.println("Leido: " + leidos.get(0).getUsername());   // Leido: ana
```

Debe crear `datos/prueba.sop` (ábrelo con un editor: se ve como basura, es
binario) e imprimir `Leido: ana`.

**Commit:** `git commit -am "Persistencia binaria: ayudante ArchivoBinario"`

### Iteración 1.5 — `UsuarioServicio` (reglas + su archivo)

Una sola clase con: las reglas (registrar sin duplicar username, login) y dos
métodos `private` para guardar/leer su archivo `usuarios.sop` (que por dentro
llaman a `ArchivoBinario`). Copia la clase completa de `IMPLEMENTACION.md`
sección 3 + sección 7.

Forma resumida:

```java
public class UsuarioServicio {

    private final File archivo;   // "usuarios.sop"

    public UsuarioServicio(File carpetaDeDatos) {
        this.archivo = new File(carpetaDeDatos, "usuarios.sop");
    }

    public void registrar(Usuario nuevo) throws UsernameDuplicadoException {
        List<Usuario> usuarios = leerUsuarios();
        for (Usuario u : usuarios) {
            if (u.getUsername().equalsIgnoreCase(nuevo.getUsername())) {
                throw new UsernameDuplicadoException(nuevo.getUsername());
            }
        }
        usuarios.add(nuevo);
        guardarUsuarios(usuarios);
    }

    public Usuario login(String username, String password)
            throws CuentaDesactivadaException {
        Usuario u = buscar(username);
        if (u == null || !u.getPassword().equals(password)) return null;
        if (!u.isActiva()) throw new CuentaDesactivadaException(username);
        return u;
    }

    public Usuario buscar(String username) { /* recorre leerUsuarios() */ }

    // privados: leerUsuarios() y guardarUsuarios(lista) llaman a ArchivoBinario
}
```

**Cómo probar** los 3 caminos:

```java
File carpeta = new File("datos");
// borra datos/usuarios.sop antes para empezar limpio
UsuarioServicio serv = new UsuarioServicio(carpeta);

try {
    serv.registrar(new Usuario("Ana Perez", 'F', "ana", "1234", 20));
    System.out.println("registro 1: OK");
    serv.registrar(new Usuario("Otra Ana", 'F', "ana", "xxxx", 30));
    System.out.println("registro 2: NO deberia llegar aqui");
} catch (UsernameDuplicadoException e) {
    System.out.println("registro 2 rechazado: " + e.getMessage());
}

if (serv.login("ana", "1234") != null) {
    System.out.println("login OK");
} else {
    System.out.println("login fallo");
}

if (serv.login("ana", "mala") == null) {
    System.out.println("login rechazado");
} else {
    System.out.println("login ACEPTADO (mal, no deberia)");
}
```

Salida esperada: `registro 1: OK`, `registro 2 rechazado: ...`, `login OK`,
`login rechazado`.

**Commit:** `git commit -am "UsuarioServicio: registro sin duplicados y login"`

### Iteración 1.6 — Usuario administrador por defecto

El enunciado pide que al primer arranque **ya exista un usuario administrador**.
Se hace con una comprobación simple: si `usuarios.sop` no tiene a nadie, se crea.
Es un método más de `UsuarioServicio`:

```java
public void asegurarAdmin() {
    if (leerUsuarios().isEmpty()) {
        Usuario admin = new Usuario("Administrador", 'M', "admin", "admin", 30);
        admin.setRol(Rol.ADMINISTRADOR);      // el resto son ESTANDAR
        try {
            registrar(admin);
        } catch (UsernameDuplicadoException nuncaPasa) {
            // imposible: la lista estaba vacia
        }
    }
}
```

Llama a `serv.asegurarAdmin()` una vez al arrancar el programa, antes de mostrar
el login.

**Cómo probar:** borra `datos/usuarios.sop`, arranca → puedes entrar con
`admin` / `admin`. Arranca otra vez → no se crea un segundo admin.

**Commit:** `git commit -am "Crear usuario admin por defecto al primer arranque"`

### Fin de Fase 1

Ya tienes un backend completo y probado sin una sola ventana. Todo lo que sigue
se apoya en esto.

---

## Fase 2 — La primera ventana (login)

Objetivo: una ventana Swing con login y registro conectada al
`UsuarioServicio`. Aquí conoces el **EDT** (ver glosario de `IMPLEMENTACION.md`).

### Iteración 2.1 — Una ventana que abre

```java
package miniwindows.ui;

import javax.swing.*;

public class VentanaPrincipal extends JFrame {
    public VentanaPrincipal() {
        setTitle("Mini-Windows");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);   // centrada
    }
}
```

Y el arranque correcto de una app Swing (todo dentro del EDT):

```java
package miniwindows;

import javax.swing.SwingUtilities;
import miniwindows.ui.VentanaPrincipal;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
```

**Cómo probar:** al ejecutar aparece una ventana vacía de 900x600 centrada.
Cerrarla termina el programa.

**Commit:** `git commit -am "Ventana principal vacia"`

### Iteración 2.2 — Panel de login

Un panel con dos campos y un botón. Al pulsar, llama a `serv.login(...)`.

```java
JTextField txtUser = new JTextField(15);
JPasswordField txtPass = new JPasswordField(15);
JButton btnEntrar = new JButton("Entrar");

btnEntrar.addActionListener(e -> {
    try {
        Usuario u = servicio.login(txtUser.getText(),
                                   new String(txtPass.getPassword()));
        if (u == null) {
            // enunciado 4.2a: ofrecer reintentar o ir a crear cuenta
            int op = JOptionPane.showConfirmDialog(this,
                "Usuario o contraseña incorrectos.\n¿Quieres crear una cuenta nueva?",
                "Error", JOptionPane.YES_NO_OPTION);
            if (op == JOptionPane.YES_OPTION) mostrarPanelRegistro();
        } else {
            JOptionPane.showMessageDialog(this, "Bienvenido, " + u.getUsername());
            // más adelante: mostrar el escritorio
        }
    } catch (CuentaDesactivadaException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage());
    }
});
```

**Cómo probar a mano:** entra con `ana` / `1234` (creado en Fase 1) → saludo.
Entra con `ana` / `mala` → mensaje de error que te ofrece reintentar o ir a
crear cuenta.

**Commit:** `git commit -am "Pantalla de login conectada al servicio"`

### Iteración 2.3 — Pantalla de registro

Un formulario con los campos del `Usuario`. Al enviar, `serv.registrar(...)` y
maneja `UsernameDuplicadoException` con un `JOptionPane`.

**Cómo probar:** registra `carlos` → puedes iniciar sesión con él. Intenta
registrar `carlos` otra vez → mensaje "ya existe".

**Commit:** `git commit -am "Pantalla de registro"`

### Iteración 2.4 — Carpetas del usuario al entrar

Cuando el login es correcto, si es la primera vez, crea su espacio:

```java
File carpetaUser = new File("datos/Z/" + u.getUsername());
if (!carpetaUser.exists()) {
    carpetaUser.mkdirs();
    new File(carpetaUser, "Mis Documentos").mkdir();
    new File(carpetaUser, "Música").mkdir();
    new File(carpetaUser, "Mis Imágenes").mkdir();
}
```

**Cómo probar:** entra con un usuario nuevo → aparecen las 3 carpetas dentro de
`datos/Z/<usuario>/`.

**Commit:** `git commit -am "Crear carpetas base del usuario al iniciar sesion"`

### Iteración 2.5 — Barra administradora principal

El enunciado (sección 3.2) pide "una barra administradora principal desde la
cual se acceda a todas las herramientas". Tras el login se muestra el escritorio
de Mini-Windows con una barra (un `JMenuBar` o un `JToolBar`) con botones para:
Explorador, Editor de texto, Visor de imágenes, Consola, Reproductor, y (solo si
el usuario es `ADMINISTRADOR`) Crear usuario. Por ahora los botones abren
paneles vacíos; se irán llenando en la Fase 3.

**Cómo probar:** entra como `admin` → ves la barra con todas las herramientas y
el botón "Crear usuario". Entra como usuario estándar → la barra no muestra
"Crear usuario".

**Commit:** `git commit -am "Barra administradora principal del escritorio"`

---

## Fase 3 — Mini-Windows

Objetivo: el simulador de sistema operativo. Cada herramienta es una iteración.
Todas viven en el paquete `miniwindows.so`.

### Iteración 3.1 — `SistemaArchivos`

Copia la clase `SistemaArchivos` de `IMPLEMENTACION.md` (sección 8). Trabaja
sobre `datos/Z/` (admin) o `datos/Z/<usuario>/` (estándar).

**Cómo probar** sin ventana: un `main` que llame a `crearCarpeta("Prueba")` y
`listar()`, y verifica que la carpeta aparece en disco.

**Commit:** `git commit -am "SistemaArchivos sobre carpetas reales"`

### Iteración 3.2 — Explorador con `JTree`

Un `JTree` que muestra la carpeta raíz del usuario y se puede expandir. Al hacer
clic en un archivo, guarda cuál está seleccionado.

**Cómo probar:** abre el explorador → ves tus carpetas; creas una carpeta con un
botón → aparece al refrescar.

**Commit:** `git commit -am "Explorador de archivos con JTree"`

### Iteración 3.2b — Gestión de archivos (enunciado 3.3)

El enunciado pide poder **renombrar, crear, copiar y pegar** archivos o
carpetas. Cada acción es un botón o un menú contextual sobre el nodo
seleccionado del `JTree`:

- Crear: `new File(carpeta, nombre).mkdir()` o `.createNewFile()`.
- Renombrar: `origen.renameTo(new File(origen.getParentFile(), nuevoNombre))`.
- Copiar: guarda la ruta del `File` seleccionado en una variable.
- Pegar: `Files.copy(origen, destino, ...)` (si es carpeta, recorre y copia
  cada archivo).

**Cómo probar:** crea `Carpeta1`, renómbrala a `Viajes`, copia un `.txt` de otra
carpeta y pégalo dentro → el archivo aparece en `Viajes` y sigue en su sitio
original.

**Commit:** `git commit -am "Explorador: crear, renombrar, copiar y pegar"`

### Iteración 3.2c — Ordenar archivos (enunciado 3.3)

El enunciado pide ordenar por **nombre, fecha, tipo o tamaño**. Un `JComboBox`
con esas 4 opciones; al cambiar, se reordena la lista de hijos antes de pintar
el árbol:

```java
switch (criterio) {
    case "nombre": archivos.sort(Comparator.comparing(File::getName)); break;
    case "fecha":  archivos.sort(Comparator.comparingLong(File::lastModified)); break;
    case "tamaño": archivos.sort(Comparator.comparingLong(File::length)); break;
    case "tipo":   archivos.sort(Comparator.comparing(f -> extension(f))); break;
}
```

**Cómo probar:** cambia el criterio a "tamaño" → los archivos se reordenan del
más pequeño al más grande.

**Commit:** `git commit -am "Explorador: ordenar por nombre, fecha, tipo y tamano"`

### Iteración 3.3 — Consola de comandos

Copia el `switch` de comandos de `IMPLEMENTACION.md` (sección 8). Una caja de
texto muestra la salida; una línea de entrada recibe comandos. Muestra siempre
la carpeta actual como prompt (`Z:\Ana>`).

**Cómo probar:** `mkdir test`, `dir` (aparece `test`), `cd test`, `date`,
`cd..`. Cada uno hace lo suyo.

**Commit:** `git commit -am "Consola de comandos estilo CMD"`

### Iteración 3.4 — Editor de texto (texto plano primero)

`JTextPane` dentro de la ventana. Botones "Abrir" y "Guardar" que leen/escriben
un `.txt` normal. **Todavía sin formato.**

**Cómo probar:** escribe algo, guarda como `nota.txt`, ciérralo, ábrelo →
aparece el texto.

**Commit:** `git commit -am "Editor de texto: abrir y guardar .txt"`

### Iteración 3.5 — Formato del editor

El enunciado (sección 3.4) exige archivos **`.txt`** y que el formato (color,
tipo y tamaño de fuente **como mínimo**) se conserve al reabrir. Se hace así: el
texto va en el `.txt` normal y el formato va aparte en un `.fmt` binario con la
lista de `TramoFormato` (ver `IMPLEMENTACION.md` sección 8). Al abrir, se lee el
`.txt` y se aplican los tramos.

Agrega la barra de herramientas con las tres opciones que pide el enunciado:
color del texto, tipo de fuente y tamaño de fuente.

**Cómo probar:** pon una palabra en rojo y tamaño grande, guarda, cierra, abre →
la palabra sigue en rojo y grande.

**Commit:** `git commit -am "Editor: formato persistente con archivo .fmt"`

### Iteración 3.6 — Visor de imágenes

`JLabel` + `ImageIcon`, lista de imágenes de una carpeta, botones Anterior /
Siguiente (ver `IMPLEMENTACION.md` sección 8). Si la carpeta tiene muchas
imágenes, cárgalas con un `SwingWorker` para que la ventana no se congele
(el enunciado 2.2 menciona la carga de imágenes como caso de hilo).

**Cómo probar:** mete 3 imágenes en "Mis Imágenes", abre el visor → puedes
navegar entre las 3 y no se pasa de los extremos.

**Commit:** `git commit -am "Visor de imagenes con Anterior/Siguiente"`

### Iteración 3.7 — Reproductor de música (primer hilo)

> **Decisión pendiente: cómo reproducir MP3.** Java puro solo reproduce `.wav`
> (con `javax.sound.sampled`). Para `.mp3` hay que elegir una opción cuando
> llegues aquí:
> - **JLayer** (javazoom): un JAR pequeño que pones en `lib/`. Es lo más común
>   para este tipo de proyecto y no afecta al resto del código. *Recomendada.*
> - **JavaFX** `MediaPlayer`: reproduce MP3 pero ya no viene con el JDK; hay que
>   instalarlo y configurarlo aparte.
> - **Solo WAV**: cero librerías, pero tendrías que tener las canciones en
>   `.wav`.
>
> Sea cual sea, la interfaz `Reproductor` (`play/pause/stop/cargar`) no cambia;
> solo cambia la clase que la implementa por dentro. Por eso puedes dejar esta
> iteración para más adelante sin frenar el resto.

Interfaz `Reproductor` + la implementación que elijas arriba. La reproducción
corre en `HiloReproductor` (ver `IMPLEMENTACION.md` secciones 5 y 8). El panel
muestra, como pide el enunciado (3.7): botones **Play / Pause / Stop**, la
**lista de canciones**, la **carátula** de la canción actual y su
**descripción**. Los `.mp3` se eligen desde el navegador de archivos. La
carátula y la descripción se pueden guardar en un `.ins` de canciones o leerse
de los metadatos del archivo.

**Cómo probar:** dale Play a una canción → suena, se ve su carátula y
descripción, **y** puedes seguir usando la ventana (abrir el explorador,
escribir en la consola) sin que se congele.

**Commit:** `git commit -am "Reproductor de musica en su propio hilo"`

### Iteración 3.8 — Organizar carpeta con `SwingWorker`

La función "Organizar": recorre una carpeta y mueve cada archivo a una
subcarpeta según su tipo (imágenes / documentos / música). Se hace con
`SwingWorker` (ver `IMPLEMENTACION.md` sección 5).

Mientras clasifica, guarda las rutas de cada categoría en una `ListaEnlazada`
(una por tipo). El enunciado (2.4) cuenta este organizador como uno de los
lugares donde **hay que usar la lista enlazada propia**.

**Cómo probar:** llena una carpeta con archivos mezclados, pulsa "Organizar" →
la ventana no se congela, una barra avanza, y al final los archivos quedan
ordenados en subcarpetas.

**Commit:** `git commit -am "Organizar carpeta en segundo plano con SwingWorker"`

---

## Fase 4 — INSTA+ en local

Objetivo: la red social completa, **sin red todavía** (todo lee y escribe
archivos locales). Paquete `miniwindows.insta`.

### Iteración 4.1 — Ventana única con `CardLayout`

Un `JPanel` con `CardLayout` y 9 paneles vacíos (Perfil, Cargar imágenes,
Timeline, Interacciones, Buscar Profile, Buscar Hashtag, Inbox, Editar perfil,
Cerrar sesión). Un menú lateral cambia de carta (ver `IMPLEMENTACION.md`
sección 9).

**Cómo probar:** haces clic en cada opción del menú y cambia el panel mostrado,
sin abrir ventanas nuevas.

**Commit:** `git commit -am "INSTA+: pantalla unica con CardLayout"`

### Iteración 4.2 — Modelos y servicio de INSTA+

Clases `Publicacion`, `Mensaje`, `Sticker` (todas `Serializable` con
`serialVersionUID`). Y un `InstaServicio` con el mismo molde que
`UsuarioServicio`: guarda las rutas de los archivos del usuario (`insta.ins`,
`inbox.ins`, `following.ins`, `followers.ins`, `stickers.ins`) y tiene métodos
`private` cortos que llaman a `ArchivoBinario` para guardar/leer cada uno.

**Cómo probar:** un `main` que guarda 2 publicaciones y las vuelve a leer.

**Commit:** `git commit -am "INSTA+: modelos y servicio binario"`

### Iteración 4.2b — Panel de Perfil

El enunciado (sección 4.5) dice exactamente qué mostrar. El panel "Perfil" pinta:
foto de perfil, nombre completo, username, edad, género, fecha de registro,
cantidad de followers, cantidad de following, cantidad de publicaciones y estado
de la cuenta (activa/inactiva). Cuando el perfil es de **otro** usuario, además
un botón **Seguir / Dejar de seguir**.

Los contadores se sacan del tamaño de `followers.ins`, `following.ins` e
`insta.ins` de ese usuario.

**Cómo probar:** abre tu perfil → ves tus 10 datos. Los contadores coinciden con
lo que tienes en los archivos.

**Commit:** `git commit -am "INSTA+: panel de perfil"`

### Iteración 4.3 — Publicar un insta (texto)

Panel "Comentarios / Nueva publicación": caja de texto (**máx. 140** caracteres,
enunciado 4.3), botón Publicar que agrega un `Publicacion` a `insta.ins` del
usuario. El texto puede llevar `@usuario` y `#hashtag` dentro.

**Cómo probar:** publica "hola @ana #saludo" → aparece un registro nuevo;
reinicia la app → sigue ahí.

**Commit:** `git commit -am "INSTA+: publicar insta de texto"`

### Iteración 4.3b — Cargar imágenes

Es un flujo aparte del insta de texto (enunciado 4.6). Al subir una imagen el
usuario:

- copia el archivo a la carpeta `/imagenes` de su cuenta;
- puede asignarla a una **carpeta personal** (`/folders_personales/<nombre>`);
- escribe una **descripción de máx. 220 caracteres** (distinta del límite de 140
  del texto);
- la descripción puede llevar `#hashtags` y etiquetar con `@username`.

Queda registrada en `insta.ins` como una `Publicacion` con ruta de imagen +
descripción.

**Cómo probar:** sube una foto con descripción y un `#tag` → aparece en tu
timeline con la imagen, y el `#tag` la encuentra en "Buscar Hashtag".

**Commit:** `git commit -am "INSTA+: cargar imagenes con descripcion, hashtags y etiquetas"`

### Iteración 4.4 — Timeline con `ListaEnlazada`

Copia `timelineDe(Usuario)` de `IMPLEMENTACION.md` (sección 9). Recorre tus
instas y los de quienes sigues, los mete en una `ListaEnlazada`, ordena por
fecha descendente y los pinta con el formato `USERNAME escribió: "..." — fecha`.

**Cómo probar:** con 2 usuarios que se siguen y varios instas, el timeline los
muestra del más nuevo al más viejo.

**Commit:** `git commit -am "INSTA+: timeline con lista enlazada"`

### Iteración 4.5 — Seguir / dejar de seguir

Al ver otro perfil: botón que agrega tu username a `followers.ins` del otro y el
suyo a tu `following.ins`. "Dejar de seguir" pide confirmación (enunciado 4.9b),
y puede manejarse con un `boolean` "activo" dentro de `following.ins`.

**Cómo probar:** sigue a alguien → su contador de followers sube y sus instas
aparecen en tu timeline. Deja de seguir → confirma y desaparecen.

**Commit:** `git commit -am "INSTA+: seguir y dejar de seguir"`

### Iteración 4.6 — Buscar personas y hashtags

Buscar personas (enunciado 4.9a): coincidencia parcial en el username
(`contiene` texto). El resultado se lista con el formato
`USERNAME — Lo sigo / No lo sigues`.

Buscar hashtag (4.10): recorre todas las publicaciones buscando `#palabra`, sin
duplicados. Ambos resultados se arman en una `ListaEnlazada`.

**Cómo probar:** buscar "ana" encuentra `ana` y `anabel`, cada uno con su
etiqueta de "lo sigo / no lo sigues". Buscar `#viaje` muestra solo instas con
ese hashtag, cada uno una sola vez.

**Commit:** `git commit -am "INSTA+: busqueda de personas y hashtags"`

### Iteración 4.6b — Entrar a un perfil y ver sus publicaciones

Desde el resultado de "Buscar personas", al escribir un username existente
(enunciado 4.9b) se abre su perfil (reutiliza el panel de la Iteración 4.2b) con
sus datos generales y los contadores. Se ofrece la opción **Ver sus
publicaciones**, que muestra su timeline completo, de la más reciente a la más
antigua.

**Cómo probar:** busca `noticias`, entra a su perfil → ves sus datos y, al pulsar
"Ver sus publicaciones", su lista de instas ordenada por fecha.

**Commit:** `git commit -am "INSTA+: entrar a un perfil ajeno y ver sus publicaciones"`

### Iteración 4.7 — Interacciones (menciones)

Panel que muestra las publicaciones de otros donde aparece `@tu_username`, sin
duplicados.

**Cómo probar:** que otro usuario te mencione → aparece en tu panel de
Interacciones.

**Commit:** `git commit -am "INSTA+: panel de interacciones"`

### Iteración 4.8 — Inbox

Lista de conversaciones + vista de una conversación. Enviar texto (máx. 300)
agrega un `Mensaje` a `inbox.ins`. Marcar como leído cambia el estado. Borrar
conversación elimina esos mensajes.

**Cómo probar:** manda un mensaje de A a B, entra como B → lo ve, lo marca
leído, el estado cambia y persiste.

**Commit:** `git commit -am "INSTA+: inbox de mensajeria privada"`

### Iteración 4.9 — Stickers

5 stickers por defecto registrados en `stickers.ins`. Importar valida `.png` /
`.jpg`, copia a `/stickers_personales` y registra. En el Inbox, "Enviar sticker"
guarda un `Mensaje` con tipo `STICKER`.

**Cómo probar:** envía un sticker en una conversación → se ve la imagen en el
chat, no el texto.

**Commit:** `git commit -am "INSTA+: sistema de stickers"`

### Iteración 4.10 — Editar perfil (datos)

Opción "Editar perfil" del menú (enunciado 4.4 y 4.13). Un formulario con los
datos que el usuario puede cambiar (nombre, edad, foto de perfil, contraseña).
Al guardar, `UsuarioServicio` actualiza ese usuario en `usuarios.sop`. Para eso
hace falta un método nuevo en el servicio, p. ej. `actualizar(Usuario)`, que
recorre la lista, reemplaza al que tiene ese username y vuelve a guardar.

**Cómo probar:** cambia tu edad y tu foto, guarda, cierra sesión y vuelve a
entrar → los cambios siguen.

**Commit:** `git commit -am "INSTA+: editar datos del perfil"`

### Iteración 4.10b — Activar / desactivar cuenta

También dentro de "Editar perfil" (enunciado 4.13): botón que cambia `activa`.
Desactivar pide confirmación; si ya está desactivada, reactivar es automático y
sin confirmación. El método `estaVisible(username)` (un solo lugar, ver
`IMPLEMENTACION.md` sección 9) hace que una cuenta desactivada no salga en
búsquedas ni timeline y que ninguna de sus publicaciones se muestre.

**Cómo probar:** desactiva una cuenta → desaparece de búsquedas y sus instas se
van del timeline de todos. Reactívala → vuelve.

**Commit:** `git commit -am "INSTA+: activar y desactivar cuenta"`

### Iteración 4.10c — Cerrar sesión

Opción "Cerrar sesión" del menú (enunciado 4.14): tras confirmar, se cierra la
sesión y se vuelve a la pantalla de Log In.

**Cómo probar:** pulsa "Cerrar sesión" → confirmación → vuelves al login y ya no
puedes ver los paneles sin volver a entrar.

**Commit:** `git commit -am "INSTA+: cerrar sesion"`

### Iteración 4.11 — Cuentas de ejemplo con contenido

El enunciado pide que "Buscar Profile" tenga resultados desde el primer uso, así
que se crean **3 cuentas temáticas** si no existen todavía. Mismo patrón que el
admin: comprobar y sembrar.

```java
public void asegurarCuentasEjemplo() {
    sembrar("noticias", "Canal Noticias", "Hoy: resumen del día #actualidad");
    sembrar("moda", "Estilo y Deporte", "Nueva colección de tenis #moda #deporte");
    sembrar("cine", "Sala de Cine", "Estrenos del fin de semana #entretenimiento");
}

private void sembrar(String username, String nombre, String primerInsta) {
    if (usuarioServicio.buscar(username) != null) return;   // ya existe
    Usuario u = new Usuario(nombre, 'F', username, "1234", 25);
    // registrar, crear su carpeta en INSTA_RAIZ y guardar 1-2 publicaciones
}
```

Llama a `asegurarCuentasEjemplo()` una vez al arrancar, junto a `asegurarAdmin()`.

**Cómo probar:** borra `datos/INSTA_RAIZ/`, arranca, entra a INSTA+ y busca
"cine" o "#moda" → aparecen las cuentas y sus publicaciones.

**Commit:** `git commit -am "INSTA+: cuentas de ejemplo con contenido tematico"`

---

## Fase 5 — Red con sockets

Objetivo: que las operaciones clave pasen por un servidor central. Se hace
**después** de que todo funciona en local, para no depurar dos cosas a la vez.
Paquete `miniwindows.red`.

### Iteración 5.1 — Servidor "eco"

Un `ServerSocket` en el puerto 5000 que devuelve en mayúsculas lo que reciba.
Todavía de un cliente a la vez.

**Cómo probar:** corre el servidor y en otra terminal `telnet localhost 5000`
(o un cliente mínimo). Escribe `hola` → responde `HOLA`.

**Commit:** `git commit -am "Servidor de sockets: eco basico"`

### Iteración 5.2 — Un hilo por cliente

Envuelve la atención de cada cliente en `new Thread(new AtenderCliente(socket))`
(ver `IMPLEMENTACION.md` sección 6).

**Cómo probar:** abre dos `telnet` a la vez → los dos reciben respuesta sin
bloquearse.

**Commit:** `git commit -am "Servidor: un hilo por cliente"`

### Iteración 5.3 — Clase `Cliente`

Copia `Cliente` de `IMPLEMENTACION.md` (sección 6): `Socket` + `pedir(String)`
que manda una línea y lee la respuesta.

**Cómo probar:** un `main` que hace `new Cliente("localhost", 5000).pedir("hola")`
e imprime `HOLA`.

**Commit:** `git commit -am "Clase Cliente para hablar con el servidor"`

### Iteración 5.4 — Comando `LOGIN` real

En el servidor, cambia el `switch`: `LOGIN;user;pass` llama al `UsuarioServicio`
(el servidor es el único que abre `usuarios.sop`) y responde `OK;...` o `ERROR;...`.
La pantalla de login ahora usa `Cliente.pedir(...)` en vez de llamar al servicio
directo.

**Cómo probar:** con el servidor encendido, inicia sesión desde la app → entra.
Apaga el servidor → la app avisa que no hay conexión.

**Commit:** `git commit -am "Login a traves del servidor"`

### Iteración 5.5 — Comandos `POST` y `FOLLOW`

Mismo molde: una línea más en el `switch` del servidor por cada uno. Publicar y
seguir ahora pasan por el servidor.

**Cómo probar:** desde dos copias de la app conectadas al mismo servidor, A
publica y B lo ve; B sigue a A y el contador sube en ambas.

**Commit:** `git commit -am "Publicar y seguir a traves del servidor"`

> El resto de comandos (INBOX, SEARCH...) se pueden migrar igual si te sobra
> tiempo. Con LOGIN + POST + FOLLOW el pilar de sockets ya está demostrado.

---

## Fase 6 — Hilos de fondo y pulido

### Iteración 6.1 — Notificaciones del Inbox

El hilo demonio de `IMPLEMENTACION.md` (sección 5): cada 5 segundos cuenta los
mensajes no leídos y actualiza un contador en el ícono del Inbox.

**Cómo probar:** con la app abierta como B, haz que A le mande un mensaje → en
unos segundos aparece el aviso en B sin tocar nada.

**Commit:** `git commit -am "Hilo de notificaciones del inbox"`

### Iteración 6.2 — Revisar el acceso concurrente

Confirma que los dos métodos de `ArchivoBinario` (`guardar` y `leer`) están
`synchronized`. Prueba: dos hilos escribiendo `followers.ins` a la vez muchas
veces; al final el archivo debe leerse sin `ArchivoCorruptoException`.

**Commit:** `git commit -am "Asegurar acceso sincronizado a los archivos binarios"`

### Iteración 6.3 — Conectar las excepciones a la interfaz

Repasa que cada `catch` de una excepción propia muestre un `JOptionPane` claro,
no un `printStackTrace` en la consola.

**Commit:** `git commit -am "Mensajes de error claros para el usuario"`

### Iteración 6.4 — Pulido visual tipo sistema operativo

Barra superior con las herramientas, iconos, una paleta de colores coherente.
Sin exagerar: que **se note** que imita un sistema operativo.

**Commit:** `git commit -am "Pulido visual estilo sistema operativo"`

---

## Fase 7 — Cierre y entrega

### Iteración 7.1 — Checklist contra el enunciado

Revisa uno por uno los requisitos obligatorios (punto 5 del PDF):

- [ ] Archivos binarios en todo el almacenamiento
- [ ] Hilos en: reproductor, organizador, notificaciones del Inbox
- [ ] Sockets cliente-servidor funcionando (al menos LOGIN, POST, FOLLOW)
- [ ] Lista enlazada propia usada en timeline, followers/following, búsquedas
- [ ] Clase utilitaria de E/S centralizada (`ArchivoBinario`)
- [ ] Extensiones propias (`.ins`, `.sop`, `.fmt`)
- [ ] 3+ excepciones propias, lanzadas y capturadas de verdad
- [ ] Diseño visual tipo sistema operativo; INSTA+ en una sola vista
- [ ] Usuario administrador por defecto al primer arranque
- [ ] 3 cuentas de ejemplo con contenido para "Buscar Profile"
- [ ] Acentos y `ñ` se guardan y se leen bien (UTF-8) en archivos y nombres

Para cada casilla, abre el archivo donde está y compruébalo.

### Iteración 7.2 — Empaquetar

Genera un JAR ejecutable (en IntelliJ: *Build → Build Artifacts*, o
`File → Project Structure → Artifacts → JAR from modules with dependencies`).

**Cómo probar:** `java -jar MiniWindows.jar` abre la app en una máquina donde
solo está instalado Java.

**Commit:** `git commit -am "Configuracion de artefacto JAR"`

### Iteración 7.3 — Repaso final y tag

- Lee el `README` (si lo pide el curso) con: qué es, cómo compilar, cómo correr
  el servidor, cómo correr la app, usuario admin por defecto.
- Marca la versión de entrega:

  ```
  git tag entrega-final
  ```

---

## Consejos para no atascarte

- **Un cambio, un commit.** Si un commit toca 12 archivos, probablemente hiciste
  demasiado de una vez.
- **Si no compila, para.** No sigas escribiendo encima de un error.
- **Prueba lo pequeño antes de lo grande.** Un `main` de prueba de 6 líneas te
  ahorra una hora de clicar en la ventana.
- **Copia los patrones de `IMPLEMENTACION.md`,** pero escribe tú los getters,
  los `for` y los `if`. Ahí es donde se aprende.
- **Cuando algo funcione, haz el commit enseguida.** Es tu punto de retorno si
  el siguiente cambio lo rompe.
