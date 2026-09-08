/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SimuladorWindow.terminal;
import SimuladorWindow.so.SistemaArchivos;
/**
 *
 * @author oscar
 */

public class ComandoRd implements Comando{
    private final SistemaArchivos sistema;
    public ComandoRd(SistemaArchivos sistema){this.sistema=sistema;}
    public String ejecutar(String[] args){
        if(args.length!=1)return "Uso correcto: Rd <archivo.ext>";
        String c=sistema.leer(args[0]);
        if(c==null)return "No se pudo leer: "+args[0]+". No existe o no es un archivo.";
        return c.isEmpty() ? "(No tiene contenido)" : c;
    }
}
