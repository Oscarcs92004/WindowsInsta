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
public class ComandoCopy implements Comando{
    private final SistemaArchivos sistema;
    public ComandoCopy(SistemaArchivos sistema){this.sistema=sistema;}
    @Override
    public String ejecutar(String[] args){
        if(args.length!=2) return "Uso correcto: Copy <origen> <destino>";
        return sistema.copiar(args[0],args[1]) ? "Archivo copiado correctamente." : "Error: No se pudo copiar el archivo.";
    }
}
