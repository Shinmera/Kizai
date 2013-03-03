package org.tymoonnext.bot.module.core;

import java.util.Scanner;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.auth.UserVerifyEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.meta.Info;
import org.tymoonnext.bot.module.ThreadedModule;
import org.tymoonnext.bot.module.auth.SessionImplementor;

/**
 * Primitive Command Line interface
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */

@Info("Simple module allowing you to execute commands through the command line.")
public class CmdInterface extends ThreadedModule{
    private Scanner in;
    private StdSessionImplementor sessions;
    
    public CmdInterface(Kizai bot){
        super(bot);
        in = new Scanner(System.in);
        sessions = new StdSessionImplementor(bot);
    }

    @Override
    public void run(){
        while(in==null){try{Thread.sleep(5);}catch(Exception ex){}}
        while(true){
            String command = in.nextLine();
            Commons.log.fine(toString()+" Received line: "+command);
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

    class StdSessionImplementor extends SessionImplementor{
        public StdSessionImplementor(Kizai bot){
            super(bot);
        }
        
        public void onUserVerify(UserVerifyEvent evt) {
            if(evt.getStream() == Commons.stdout)evt.getUser().activateSession();
        }
        
    }
}
