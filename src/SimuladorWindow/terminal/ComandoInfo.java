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

public class ComandoInfo implements Comando{
    private final SistemaArchivos sistema;
    public ComandoInfo(SistemaArchivos sistema){this.sistema=sistema;}
    @Override
    public String ejecutar(String[] args){
        if(args.length!=1) return "Uso correcto: Info <nombre>";
        return sistema.informacion(args[0]);
    }
}
