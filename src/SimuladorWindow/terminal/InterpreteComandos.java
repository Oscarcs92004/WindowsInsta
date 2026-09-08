/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SimuladorWindow.terminal;
import java.util.HashMap;
import SimuladorWindow.so.SistemaArchivos;
import java.util.Map;
/**
 *
 * @author oscar
 */
public class InterpreteComandos {
    private final Map<String,Comando> comandos=new HashMap<>();
    private final SistemaArchivos sistema;
    public InterpreteComandos(SistemaArchivos sistema){
        this.sistema=sistema;
        comandos.put("mkdir",new ComandoMkdir(sistema));
        comandos.put("mfile",new ComandoMfile(sistema));
        comandos.put("rm",new ComandoRm(sistema));
        comandos.put("..",new ComandoAnterior(sistema));
        comandos.put("cd..",new ComandoAnterior(sistema));
        comandos.put("date",new ComandoDate(sistema));
        comandos.put("time",new ComandoTime());
        comandos.put("wr",new ComandoWr(sistema));
        comandos.put("ap",new ComandoAp(sistema));
        comandos.put("copy",new ComandoCopy(sistema));
        comandos.put("info",new ComandoInfo(sistema));
        comandos.put("cls",new ComandoCls(sistema));
        comandos.put("exit",new ComandoExit(sistema));
        comandos.put("cd",new ComandoCd(sistema));
        comandos.put("dir",new ComandoDir(sistema));
        comandos.put("rd",new ComandoRd(sistema));
        comandos.put("ren",new ComandoRen(sistema));
        comandos.put("find",new ComandoFind(sistema));
        comandos.put("tree",new ComandoTree(sistema));
        comandos.put("help",new ComandoHelp());
    }
    public String ejecutar(String entrada){
        if(entrada==null || entrada.trim().isEmpty()) return "";
        String[] partes=entrada.trim().split("\\s+");
        String nombre=partes[0].toLowerCase();
        Comando c=comandos.get(nombre);
        if(c==null) return "'"+partes[0]+"' no se reconoce como un comando interno o externo.";
        String[] args=new String[partes.length-1];
        System.arraycopy(partes,1,args,0,args.length);
        return c.ejecutar(args);
    }
}
