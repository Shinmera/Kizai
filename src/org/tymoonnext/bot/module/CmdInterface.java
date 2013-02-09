package org.tymoonnext.bot.module;

import java.util.Scanner;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandEvent;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class CmdInterface extends ThreadedModule{
    private Scanner in;
    
    public CmdInterface(Kizai bot){
        super(bot);
        in = new Scanner(System.in);
    }

    @Override
    public void run(){
        while(in==null){try{Thread.sleep(5);}catch(Exception ex){}}
        while(true){
            String command = in.nextLine();
            if(command.contains(" "))
                bot.command(new CommandEvent(Commons.stdout,
                                             command.substring(0,command.indexOf(' ')),
                                             command.substring(command.indexOf(' ')+1)));
            else
                bot.command(new CommandEvent(Commons.stdout,command));
        }
    }

}
