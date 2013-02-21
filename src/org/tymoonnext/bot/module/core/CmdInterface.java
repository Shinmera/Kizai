package org.tymoonnext.bot.module.core;

import NexT.data.DObject;
import java.util.Scanner;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.ThreadedModule;

/**
 * Primitive Command Line interface
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
                bot.event(new CommandEvent(Commons.stdout,
                                            command.substring(0,command.indexOf(' ')),
                                            command.substring(command.indexOf(' ')+1),
                                            System.getProperty("user.name")));
            else
                bot.event(new CommandEvent(Commons.stdout,
                                            command,
                                            null,
                                            System.getProperty("user.name")));
        }
    }

}
