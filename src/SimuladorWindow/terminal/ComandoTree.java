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


public class ComandoTree implements Comando{
    private final SistemaArchivos sistema;
    public ComandoTree(SistemaArchivos sistema){this.sistema=sistema;}
    @Override
    public String ejecutar(String[] args){
        if(args.length!=0) return "Uso correcto: Tree";
        return sistema.tree();
    }
}
