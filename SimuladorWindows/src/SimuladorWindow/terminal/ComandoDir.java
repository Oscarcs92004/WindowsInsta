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

public class ComandoDir implements Comando {
    private final SistemaArchivos sistema;
    public ComandoDir(SistemaArchivos sistema){this.sistema=sistema;}
    @Override
    public String ejecutar(String[] args){
        if(args.length!=0) return "Uso correcto: Dir";
        return sistema.listarComoTexto();
    }
}
