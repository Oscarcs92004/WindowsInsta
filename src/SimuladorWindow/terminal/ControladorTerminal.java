/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SimuladorWindow.terminal;
import SimuladorWindow.modelo.Usuario;
import SimuladorWindow.so.Rutas;
import SimuladorWindow.so.SistemaArchivos;
/**
 *
 * @author oscar
 */
public class ControladorTerminal {private final SistemaArchivos sistemaArchivos;
    private final InterpreteComandos interprete;
    private final Usuario usuario;
    public ControladorTerminal(Usuario usuario){
        if(usuario==null) throw new IllegalArgumentException("El usuario no puede ser null.");
        this.usuario=usuario;
        Rutas.asegurarCarpetasUsuario(usuario);
        sistemaArchivos=new SistemaArchivos(Rutas.carpetaRaizDe(usuario));
        interprete=new InterpreteComandos(sistemaArchivos);
    }
    public String ejecutar(String comando){return interprete.ejecutar(comando);}
    public SistemaArchivos getSistemaArchivos(){return sistemaArchivos;}
    public Usuario getUsuario(){return usuario;}
    public String getRutaActual(){return sistemaArchivos.rutaActual();}
}
