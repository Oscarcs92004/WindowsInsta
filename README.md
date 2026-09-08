# Simulador de Sistema Operativo (Mini-Windows) + INSTA+

Proyecto II de Programación II. Una aplicación de escritorio en Java + Swing que
simula un sistema operativo (explorador de archivos, editor de texto con formato,
visor de imágenes, consola de comandos y reproductor de música) y que además
integra INSTA+, una red social estilo Instagram que guarda todo en archivos
binarios.

## Qué necesitas

- JDK 17 o más nuevo (`java -version` y `javac -version` deben responder).

## Cómo compilar y ejecutar

Desde la carpeta del proyecto, en PowerShell:

```
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out SimuladorWindow.Main
```

En Git Bash / Linux / Mac:

```
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out SimuladorWindow.Main
```

La carpeta `datos/` se crea sola la primera vez y guarda todo (usuarios,
publicaciones, mensajes...). No se sube a git.

## Usuario administrador

La primera vez que arranca se crea el usuario `admin` con contraseña `admin`.
El administrador puede crear usuarios y ver las carpetas de todos.

## Servidor de sockets (opcional)

Para que el login pase por la red, primero arranca el servidor en otra terminal:

```
java -cp out SimuladorWindow.red.Servidor
```

Entiende los comandos `LOGIN`, `POST` y `FOLLOW`. Si el servidor no está
encendido, la aplicación inicia sesión en modo local.

## Documentación

- `Proyecto_II_MiniWindows.pdf` — el enunciado.
- `IMPLEMENTACION.md` — el qué y el por qué de cada patrón.
- `PLAN_DESARROLLO.md` — el orden de construcción, paso a paso.
- `REPARTO_WINDOWS.md` — reparto de trabajo y checklist de lo hecho.
