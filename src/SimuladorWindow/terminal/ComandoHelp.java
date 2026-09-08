/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author oscar
 */
package SimuladorWindow.terminal;
public class ComandoHelp implements Comando{
    @Override
    public String ejecutar(String[] argumentos) {
        return "Comandos disponibles:\n"
                + "  Mkdir <nombre>              Crea una nueva carpeta\n"
                + "  Mfile <nombre.ext>          Crea un nuevo archivo\n"
                + "  Rm <nombre>                 Elimina un archivo o carpeta\n"
                + "  Cd <carpeta>                Cambia a la carpeta indicada\n"
                + "  ..                          Regresa a la carpeta anterior\n"
                + "  Dir                         Lista archivos y carpetas\n"
                + "  Date                        Muestra la fecha actual\n"
                + "  Time                        Muestra la hora actual\n"
                + "  Wr <archivo.ext>            Escribe texto en un archivo (EXIT para terminar)\n"
                + "  Rd <archivo.ext>            Lee el contenido de un archivo\n"
                + "  Ap <archivo.ext>            Agrega texto a un archivo (EXIT para terminar)\n"
                + "  Ren <actual> <nuevo>        Renombra un archivo o carpeta\n"
                + "  Copy <origen> <destino>     Copia un archivo\n"
                + "  Find <nombre>               Busca archivos o carpetas por nombre\n"
                + "  Info <nombre>               Muestra informacion de un archivo o carpeta\n"
                + "  Tree                        Muestra el arbol de carpetas y archivos\n"
                + "  Cls                         Limpia la pantalla\n"
                + "  Help                        Muestra esta ayuda\n"
                + "  Exit                        Cierra la aplicacion";
    }
}
