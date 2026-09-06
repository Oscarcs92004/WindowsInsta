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
public class ComandoAp implements Comando{
    private final SistemaArchivos sistema;
    public ComandoAp(SistemaArchivos sistema){this.sistema=sistema;}
    @Override
    public String ejecutar(String[] args){
        if(args.length!=1)return "Uso correcto: Ap <archivo.ext>";
        if(sistema.leer(args[0])==null)return "Error: el archivo no existe: "+args[0];
        if(sistema.resolver(args[0]).isDirectory())return "Error: es una carpeta: "+args[0];
        return "MODO_APPEND";
    }
}
