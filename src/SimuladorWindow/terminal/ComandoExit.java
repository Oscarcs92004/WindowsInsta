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
public class ComandoExit implements Comando{
    public ComandoExit(Object ignored){}
    @Override
    public String ejecutar(String[] args){
        return args.length==0 ? "EXIT_APP" : "Uso correcto: Exit";
    }
}
