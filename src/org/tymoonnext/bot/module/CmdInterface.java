package org.tymoonnext.bot.module;

import java.util.Scanner;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandEvent;
import org.tymoonnext.bot.module.auth.SessionFactory;
import org.tymoonnext.bot.module.auth.User;

/**
 * Primitive Command Line interface
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class CmdInterface extends ThreadedModule{
    private Scanner in;
    private SF sf;
    
    public CmdInterface(Kizai bot){
        super(bot);
        in = new Scanner(System.in);
        sf = new SF(bot);
    }

    @Override
    public void run(){
        while(in==null){try{Thread.sleep(5);}catch(Exception ex){}}
        while(true){
            String command = in.nextLine();
            sf.authenticate(System.getProperty("user.name"));
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
    
    private class SF extends SessionFactory{
        public SF(Kizai bot){super(bot);}
        public void authenticate(String... userinf) {
            if(getUser(userinf[0]) == null){
                addUser(new User(userinf[0]));
            }
        }
    
    }

}
