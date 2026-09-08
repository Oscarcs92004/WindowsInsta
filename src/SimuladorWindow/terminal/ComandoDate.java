/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SimuladorWindow.terminal;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 *
 * @author oscar
 */
public class ComandoDate implements Comando {
    public ComandoDate(Object ignored){}
    @Override
    public String ejecutar(String[] args){
        if(args.length!=0) return "Uso correcto: Date";
        return "Fecha actual: "+new SimpleDateFormat("dd/MM/yyyy").format(new Date());
    }
}
