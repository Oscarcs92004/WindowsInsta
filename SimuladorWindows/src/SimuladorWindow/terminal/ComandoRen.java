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

import java.io.File;

public class ComandoRen implements Comando{
    private final SistemaArchivos sistema;
    public ComandoRen(SistemaArchivos sistema){this.sistema=sistema;}
    @Override
    public String ejecutar(String[] args){
        if(args.length!=2)return "Uso correcto: Ren <actual> <nuevo>";
        return sistema.renombrar(args[0],args[1]) ? "Renombrado correctamente." : "No se pudo renombrar.";
    }
}
