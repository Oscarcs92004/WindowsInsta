# Reparto de trabajo — Módulo Mini-Windows

Este documento reparte lo que **falta** del módulo Mini-Windows entre las dos
personas del equipo: **Oscar** y **Alex**. Solo cubre Mini-Windows; INSTA+ (la
Fase 4 del `PLAN_DESARROLLO.md`) se hace después y no entra aquí.

El **qué** y el **por qué** de cada patrón está en `IMPLEMENTACION.md`. El
**orden** general está en `PLAN_DESARROLLO.md`. Este archivo dice **quién hace
qué** y **por dónde seguir en el código**.

---

## 1. Qué ya está hecho

### Núcleo (Fase 0 y 1) — no se toca

- `modelo/` — `Usuario`, `Rol`
- `estructuras/` — `Nodo`, `ListaEnlazada`
- `excepciones/` — las 3 excepciones propias
- `persistencia/` — `ArchivoBinario`
- `servicios/` — `UsuarioServicio` (con `asegurarAdmin()`)
- `pruebas/` — `PruebaNucleo`

### Capa base de la interfaz (ya en `src/`, compila y arranca)

Ya está el **esqueleto de todo Mini-Windows**, con el escritorio funcionando
como un sistema operativo: fondo de pantalla, iconos, y cada herramienta se
abre en su propia ventana interna (se puede mover, cambiar de tamaño,
minimizar y cerrar) con una barra de tareas abajo. Lo que funciona de verdad y
lo que queda como `// TODO` está marcado en cada archivo.

| Archivo | Estado |
|---|---|
| `Main.java` | Listo: crea el admin y abre la ventana en el EDT |
| `ui/VentanaPrincipal.java` | Listo: cambia entre login, registro y escritorio |
| `ui/PanelLogin.java` | Listo: login contra `UsuarioServicio`, con reintento / crear cuenta |
| `ui/PanelRegistro.java` | Casi listo: **falta el campo Foto de perfil** (TODO Alex) |
| `ui/PanelEscritorio.java` | Listo: escritorio tipo sistema operativo — fondo, iconos, ventanas internas (`JInternalFrame`) que se mueven/minimizan/cierran, y barra de tareas abajo con "Inicio", las ventanas abiertas y el reloj |
| `so/Rutas.java` | Listo: carpeta raíz según el rol y creación de las 3 carpetas |
| `so/SistemaArchivos.java` | Listo: `mkdir`, `rm`, `cd`, `cd..`, `dir` |
| `so/PanelConsola.java` | Listo: los 7 comandos del enunciado |
| `so/PanelExplorador.java` | Base: árbol + refrescar + nueva carpeta. **Falta renombrar / copiar / pegar / ordenar / organizar** |
| `so/PanelEditor.java` | Base: abrir y guardar `.txt` (UTF-8). **Falta el formato + `.fmt`** |
| `so/PanelVisor.java` | Base: elegir carpeta + Anterior / Siguiente. **Falta el `SwingWorker`** |
| `so/PanelReproductor.java` | Base: lista de canciones + botones. **Falta el audio con hilo** |
| `so/Reproductor.java`, `so/HiloReproductor.java` | Esqueleto de la interfaz y el hilo (TODO Oscar) |
| `so/TramoFormato.java` | Clase lista para que Oscar guarde el formato del editor |
| `red/Servidor.java`, `red/AtenderCliente.java`, `red/Cliente.java` | Esqueleto de sockets (TODO en la Fase 5) |

Para compilar y arrancar (PowerShell):

```
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out miniwindows.Main
```

Usuarios de prueba (ya están en `datos/usuarios.sop`): `admin` / `admin`.

**Regla de oro:** los paquetes `modelo`, `estructuras`, `persistencia` y
`excepciones` quedan **congelados**. Si alguno necesita cambiar algo ahí, se
avisa al otro **antes** y se hace juntos en una sola pasada.

---

## 2. Cómo trabajar sin chocar

**Alex se encarga de todo lo visual. Oscar se encarga de la lógica, los hilos,
los archivos y la red.** Así casi no tocan los mismos archivos:

| Alex edita | Oscar edita |
|---|---|
| `ui/VentanaPrincipal.java` | `so/PanelExplorador.java` (botones) |
| `ui/PanelLogin.java` | `so/PanelVisor.java` (carga) |
| `ui/PanelRegistro.java` | `so/PanelReproductor.java` + `so/HiloReproductor.java` |
| `ui/PanelEscritorio.java` | `so/SistemaArchivos.java` (si hace falta) |
| `ui/Estilo.java` (nuevo: colores y fuentes) | `so/FormatoTxt.java` (nuevo: guardar/leer `.fmt`) |
| la barra de formato de `so/PanelEditor.java` | todo `red/` |

Los **3 únicos puntos donde se cruzan** (se avisan por mensaje antes):

1. **Editor:** Oscar hace `so/FormatoTxt.java` con dos métodos estáticos
   (`guardar(JTextPane, File)` y `cargar(JTextPane, File)`); Alex solo los llama
   desde los `TODO` que ya están en `PanelEditor`.
2. **Explorador:** cuando Oscar termine la lógica, Alex hace el `TreeCellRenderer`
   con iconos por tipo de archivo, en una clase aparte
   `so/RendererArchivos.java`, y Oscar pone la línea
   `arbol.setCellRenderer(new RendererArchivos())`.
3. **Login con sockets (Fase 5):** Oscar cambia una línea de `PanelLogin` para
   usar `Cliente`; se hace al final y juntos.

Cada herramienta es un `JPanel` en `miniwindows.so` con el mismo constructor
`(Usuario usuarioActual, File carpetaRaiz)`. La ventana ya se lo pasa resuelto.

**Nunca subir código que no compile.** `git pull` antes de empezar y antes de
subir. Los paquetes `modelo`, `estructuras`, `persistencia` y `excepciones`
quedan **congelados**.

---

## 3. Alex — lo visual (puedes empezar ya, sin esperar a Oscar)

### A1. `ui/Estilo.java` — la paleta del sistema  (checklist "Diseño visual")

Una clase con constantes que usen todos los paneles, para que el aspecto sea
coherente:

```java
public final class Estilo {
    public static final java.awt.Color FONDO_ESCRITORIO = new java.awt.Color(0, 120, 170);
    public static final java.awt.Color BARRA_TAREAS     = new java.awt.Color(45, 45, 48);
    public static final java.awt.Color TEXTO_CLARO      = java.awt.Color.WHITE;
    public static final java.awt.Font  TITULO  = new java.awt.Font("SansSerif", java.awt.Font.BOLD, 16);
    public static final java.awt.Font  NORMAL  = new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 13);
}
```

Opcional: en `Main`, antes de abrir la ventana, poner el Look&Feel Nimbus
(`UIManager.setLookAndFeel(...)`) para que todo se vea más moderno.

### A2. Pantalla de login estilo sistema operativo  (enunciado 4.2a)

`ui/PanelLogin.java`. Que se vea como la pantalla de bloqueo de Windows:

- Fondo con color de `Estilo`, el título "Mini-Windows" grande y centrado.
- Un recuadro (`JPanel` con `setBorder(BorderFactory.createTitledBorder(...))`)
  con los dos campos y el botón "Entrar".
- Un reloj grande arriba (usa un `javax.swing.Timer` como el del escritorio).

La lógica del login **ya funciona**; solo estás maquetando.

### A3. Pantalla de registro + foto de perfil  (enunciado 4.2b)

`ui/PanelRegistro.java`. Ordena el formulario (etiquetas alineadas, tamaño
cómodo) y agrega en el `TODO (Alex)`:

- Botón "Elegir foto" con un `JFileChooser` (solo `.png` / `.jpg`).
- Un `JLabel` al lado que muestra la miniatura elegida
  (`new ImageIcon(new ImageIcon(ruta).getImage().getScaledInstance(96, 96, ...))`).
- Antes de `servicio.registrar(nuevo)`: `nuevo.setFotoPerfil(rutaElegida);`

### A4. Pulir el escritorio  (enunciado 3.2 / checklist — Iteración 6.4)

`ui/PanelEscritorio.java`. Ya funciona; hazlo parecer un SO de verdad:

- **Iconos con imagen:** pon 5 PNG pequeños en `src/miniwindows/ui/iconos/` y
  cárgalos con `new ImageIcon(getClass().getResource("/miniwindows/ui/iconos/x.png"))`.
  Ponlos en los botones de escritorio y en el menú Inicio.
- **Fondo:** un color de `Estilo` o una imagen de fondo (pintar en
  `paintComponent` de un panel que haga de "wallpaper").
- **Barra de tareas:** resaltar el botón de la ventana que está al frente;
  separar visualmente Inicio / ventanas / reloj.
- **Menú Inicio:** icono al lado de cada opción, separador antes de
  "Cerrar sesión".

### A5. Barra de formato del editor  (enunciado 3.4 — Iteración 3.5, parte visual)

`so/PanelEditor.java`, en los `TODO (Oscar/Alex)` de los botones Color / Fuente /
Tamaño. Tú haces **la barra y aplicar el estilo a la selección**; Oscar hace
guardar/leer el `.fmt`.

```java
// Color:
java.awt.Color c = javax.swing.JColorChooser.showDialog(this, "Color", java.awt.Color.BLACK);
if (c != null) aplicar(javax.swing.text.StyleConstants.Foreground, c);

// aplicar(...) usa el StyledDocument sobre el texto seleccionado:
private void aplicar(Object clave, Object valor) {
    javax.swing.text.SimpleAttributeSet at = new javax.swing.text.SimpleAttributeSet();
    at.addAttribute(clave, valor);
    int ini = texto.getSelectionStart();
    int fin = texto.getSelectionEnd();
    texto.getStyledDocument().setCharacterAttributes(ini, fin - ini, at, false);
}
```

Para "Fuente" usa `StyleConstants.FontFamily` con un `JComboBox` de
`GraphicsEnvironment...getAvailableFontFamilyNames()`; para "Tamaño",
`StyleConstants.FontSize` con un `JComboBox` de números.

Al guardar/abrir, llama a `FormatoTxt.guardar(texto, destino)` /
`FormatoTxt.cargar(texto, archivo)` (los hace Oscar).

### A6. Visor de imágenes — que la imagen se ajuste  (enunciado 3.5)

`so/PanelVisor.java`, método `mostrarActual()`: en vez de poner la imagen a
tamaño real, escálala para que quepa en el panel
(`getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH)` respetando la
proporción). La carga en segundo plano la hace Oscar (O2).

### A7. Diálogos de error claros  (Iteración 6.3)

Repaso final de todos los paneles: que cada mensaje de error sea un
`JOptionPane` con texto claro y en español, no un `printStackTrace`. Unifica el
estilo (mismo título, mismo tono).

---

## 4. Oscar — lógica, hilos, archivos y red (puedes empezar ya)

### O1. Explorador: renombrar, copiar, pegar  (enunciado 3.3 — Iteración 3.2b)

`so/PanelExplorador.java`, en los `TODO` de los botones:

- **Renombrar:** `File sel = archivoSeleccionado();` → pedir nombre con
  `JOptionPane.showInputDialog` →
  `sel.renameTo(new File(sel.getParentFile(), nombreNuevo));` → `refrescar();`
- **Copiar:** `portapapeles = archivoSeleccionado();` (el campo ya existe).
- **Pegar:** `Files.copy(portapapeles.toPath(),
  new File(carpetaDestino(), portapapeles.getName()).toPath(),
  StandardCopyOption.REPLACE_EXISTING);` — si es carpeta, recorre y copia cada
  archivo. `refrescar();`

### O2. Explorador: ordenar  (enunciado 3.3 — Iteración 3.2c)

`JComboBox` con "nombre / fecha / tipo / tamaño". Guarda el criterio en un campo
y ordena los hijos antes de pintarlos (en `agregarHijos`, pasa
`carpeta.listFiles()` a un `List<File>` y ordénalo):

```java
switch (criterio) {
    case "nombre": lista.sort(Comparator.comparing(File::getName)); break;
    case "fecha":  lista.sort(Comparator.comparingLong(File::lastModified)); break;
    case "tamano": lista.sort(Comparator.comparingLong(File::length)); break;
    case "tipo":   lista.sort(Comparator.comparing(f -> extension(f))); break;
}
```

### O3. Explorador: "Organizar" con `SwingWorker`  (enunciado 3.3 — Iteración 3.8)

`so/PanelExplorador.java`, `TODO` del botón "Organizar":

- Recorre la carpeta seleccionada y mueve cada archivo a `imagenes/`,
  `documentos/` o `musica/` según su extensión.
- Con `SwingWorker` (código en `IMPLEMENTACION.md` §5): mover en
  `doInBackground()`, progreso en `process()`, `refrescar()` en `done()`.
- Mientras clasifica, guarda las rutas de cada tipo en una **`ListaEnlazada`**
  (una por categoría). El enunciado (2.4) exige usar aquí la lista propia.

### O4. Visor: carga en segundo plano  (enunciado 3.5 — Iteración 3.6)

`so/PanelVisor.java`, `TODO` de `cargarCarpeta(...)`: mueve el recorrido de la
carpeta a un `SwingWorker` (`doInBackground()` arma la lista de imágenes,
`done()` pinta la primera). Así no se congela con carpetas grandes.

### O5. Reproductor de música con hilo  (enunciado 3.7 — Iteración 3.7)

- **Decisión de equipo antes de empezar:** `.mp3` con **JLayer** (JAR en `lib/`,
  recomendada), **JavaFX** o **solo `.wav`** (`javax.sound.sampled`, sin
  librería). La interfaz `Reproductor` no cambia.
- `so/HiloReproductor.java`: en `run()`, reproducir bloques de audio hasta que
  acabe la canción o alguien llame a `detener()`.
- `so/PanelReproductor.java`, `TODO`: conectar Play / Pause / Stop con el hilo
  (`start()`, `pausar()`, `reanudar()`, `detener()`) y mostrar la carátula
  (`caratula.setIcon(...)`) y la descripción (`descripcion.setText(...)`).

### O6. Editor: guardar y leer el formato  (enunciado 3.4 — Iteración 3.5)

Clase nueva `so/FormatoTxt.java` con dos métodos estáticos:

```java
// Recorre el StyledDocument, arma una lista de TramoFormato y la guarda
// en <archivo>.fmt con ArchivoBinario.
public static void guardar(JTextPane texto, File txt) { ... }

// Si existe <archivo>.fmt, lo lee con ArchivoBinario y aplica cada
// TramoFormato al StyledDocument del JTextPane.
public static void cargar(JTextPane texto, File txt) { ... }
```

`TramoFormato` ya existe (`inicio, fin, fuente, tamano, colorRGB`). Alex llama a
estos métodos desde `PanelEditor`.

### O7. Sockets — servidor y `LOGIN`  (Fase 5)

Se hace **al final**. Archivos en `red/` (ya hay esqueleto):

- `Servidor.java`: ya escucha en el 5000 y lanza un hilo por cliente.
- `AtenderCliente.procesar(...)`, `case "LOGIN"`: crea un `UsuarioServicio` (el
  servidor es el único que abre `usuarios.sop`), llama a `login(p[1], p[2])` y
  responde `"OK;bienvenido"` o `"ERROR;credenciales"`.
- En `PanelLogin` (con Alex, una línea): usar
  `new Cliente("localhost", Servidor.PUERTO).pedir("LOGIN;user;pass")` en vez de
  llamar a `UsuarioServicio` directo; si falla la conexión, avisar con
  `JOptionPane`.
- Probar con `telnet localhost 5000` → `LOGIN;admin;admin` → `OK;...`.

`POST` y `FOLLOW` son de INSTA+.

### O8. Iconos por tipo de archivo en el árbol  (con Alex)

Cuando termines O1–O3: Alex hace `so/RendererArchivos.java` (un
`TreeCellRenderer` que pone un icono según la extensión); tú añades
`arbol.setCellRenderer(new RendererArchivos());` en el constructor.

---

## 5. Checklist de Mini-Windows (revisar al terminar)

- [x] Ventana de login y registro conectadas a `UsuarioServicio`
- [x] Admin por defecto al primer arranque (`asegurarAdmin()`)
- [x] Cada usuario entra solo a su carpeta; el admin ve todas (`Rutas`)
- [x] Las 3 carpetas se crean solas al iniciar sesión
- [x] Escritorio tipo sistema operativo: iconos, ventanas internas y barra de tareas
- [x] "Crear usuario" solo visible para el administrador; cerrar sesión
- [x] Consola con `mkdir`, `rm`, `cd`, `cd..`, `dir`, `date`, `time` y prompt
- [ ] Explorador: renombrar, copiar, pegar; ordenar por nombre / fecha / tipo / tamaño
- [ ] "Organizar" en segundo plano con `SwingWorker` y `ListaEnlazada` por tipo
- [ ] Editor `.txt` con color, fuente y tamaño que se conservan al reabrir (`.fmt`)
- [ ] Visor de imágenes con carga en `SwingWorker`
- [ ] Reproductor con Play / Pause / Stop, lista, carátula y descripción, en su hilo
- [ ] Sockets: `LOGIN` de punta a punta por el servidor
- [ ] Foto de perfil en el registro
- [ ] Acentos y `ñ` se guardan y se leen bien (UTF-8)
- [ ] Todos los errores se muestran con `JOptionPane`, no con `printStackTrace`
- [x] La interfaz se parece a un sistema operativo real (falta pulir iconos y colores)
