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
public class ComandoMkdir implements Comando{
    private final SistemaArchivos sistema;
    public ComandoMkdir(SistemaArchivos sistema){this.sistema=sistema;}
    @Override
    public String ejecutar(String[] args){
        if(args.length!=1)return "Uso correcto: Mkdir <nombre>";
        return sistema.crearCarpeta(args[0]) ? "Carpeta creada correctamente: "+args[0] : "Error: no se pudo crear la carpeta: "+args[0];
    }
}
