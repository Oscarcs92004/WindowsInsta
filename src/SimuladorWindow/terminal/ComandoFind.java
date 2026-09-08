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
import SimuladorWindow.so.SistemaArchivos;



public class ComandoFind implements Comando {
    private final SistemaArchivos sistema;
    public ComandoFind(SistemaArchivos sistema){this.sistema=sistema;}
    @Override
    public String ejecutar(String[] args){
        if(args.length!=1) return "Uso correcto: Find <nombre>";
        return sistema.buscar(args[0]);
    }
}
