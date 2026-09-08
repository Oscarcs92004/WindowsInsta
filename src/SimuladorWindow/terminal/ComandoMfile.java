/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SimuladorWindow.terminal;
import SimuladorWindow.so.SistemaArchivos;
import java.io.File;
import java.io.IOException;
/**
 *
 * @author oscar
 */

import java.io.File;
import java.io.IOException;

public class ComandoMfile implements Comando{
    private final SistemaArchivos sistema;
    public ComandoMfile(SistemaArchivos sistema){this.sistema=sistema;}
    @Override
    public String ejecutar(String[] args){
        if(args.length!=1)return "Uso correcto: Mfile <nombre.ext>";
        File f=sistema.resolver(args[0]);
        if(!sistema.estaDentroDeRaiz(f))return "Error: acceso fuera de la raiz.";
        if(f.exists())return "Error: ya existe: "+args[0];
        try{return f.createNewFile() ? "Archivo creado correctamente: "+args[0] : "No se pudo crear el archivo.";}
        catch(IOException e){return "Error creando el archivo: "+e.getMessage();}
    }
}
