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

import java.util.Calendar;

public class ComandoTime implements Comando{
    @Override
    public String ejecutar(String[] args){
        if(args.length!=0) return "Uso correcto: Time";
        return "Hora actual: "+new SimpleDateFormat("HH:mm:ss").format(new Date());
    }
}
