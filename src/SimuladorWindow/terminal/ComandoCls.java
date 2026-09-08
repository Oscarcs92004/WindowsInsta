/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SimuladorWindow.terminal;
/**
 *
 * @author oscar
 */
public class ComandoCls implements Comando{
    public ComandoCls(Object ignored){}
    @Override
    public String ejecutar(String[] args){
        return args.length==0 ? "CLS" : "Uso correcto: Cls";
    }
}
