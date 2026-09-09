# Simulador de Sistema Operativo (Mini-Windows) + INSTA+

Proyecto II de Programación II. Una aplicación de escritorio en Java + Swing que
simula un sistema operativo (explorador de archivos, editor de texto con formato,
visor de imágenes, consola de comandos y reproductor de música) y que además
integra INSTA+, una red social estilo Instagram que guarda todo en archivos
binarios.

## Qué necesitas

- JDK 17 o más nuevo (`java -version` y `javac -version` deben responder).

## Cómo compilar y ejecutar

La librería para reproducir `.mp3` está en `lib/jlayer-1.0.1.jar` y hay que
ponerla en el classpath.

Desde la carpeta del proyecto, en PowerShell:

```
javac -encoding UTF-8 -cp "lib/jlayer-1.0.1.jar" -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp "out;lib/jlayer-1.0.1.jar" SimuladorWindow.Main
```

En Git Bash / Linux / Mac (separador `:` en vez de `;`):

```
javac -encoding UTF-8 -cp "lib/jlayer-1.0.1.jar" -d out $(find src -name "*.java")
java -cp "out:lib/jlayer-1.0.1.jar" SimuladorWindow.Main
```

En IntelliJ ya está configurada la librería `lib/`, solo se le da a Run.

La carpeta `datos/` se crea sola la primera vez y guarda todo (usuarios,
publicaciones, mensajes...). No se sube a git.

## Usuario administrador

La primera vez que arranca se crea el usuario `admin` con contraseña `admin`.
El administrador puede crear usuarios y ver las carpetas de todos.

## Servidor de sockets (opcional)

Para que el login pase por la red, arranca el servidor en otra terminal:

```
java -cp "out;lib/jlayer-1.0.1.jar" SimuladorWindow.red.Servidor
```

El login siempre intenta primero por el servidor; si no está encendido, la
aplicación inicia sesión en modo local automáticamente (no hay que marcar nada).

Entiende los comandos `LOGIN`, `POST` y `FOLLOW`.

## Documentación

- `Proyecto_II_MiniWindows.pdf` — el enunciado.
- `IMPLEMENTACION.md` — el qué y el por qué de cada patrón.
- `PLAN_DESARROLLO.md` — el orden de construcción, paso a paso.
- `REPARTO_WINDOWS.md` — reparto de trabajo y checklist de lo hecho.
