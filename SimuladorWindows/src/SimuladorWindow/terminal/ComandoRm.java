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
public class ComandoRm implements Comando {
    private final SistemaArchivos sistema;
    public ComandoRm(SistemaArchivos sistema){this.sistema=sistema;}
    @Override
    public String ejecutar(String[] args){
        if(args.length!=1)return "Uso correcto: Rm <nombre>";
        return sistema.eliminar(args[0]) ? "Eliminado correctamente: "+args[0] : "Error: no se pudo eliminar: "+args[0];
    }
}
