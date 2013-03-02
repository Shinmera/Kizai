package org.tymoonnext.bot.module.core;

import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.cmd.CommandRegisterEvent;
import org.tymoonnext.bot.event.cmdgroup.GroupRegisterEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.module.cmd.Command;
import org.tymoonnext.bot.module.cmd.CommandHandler;


/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class CommandModule extends Module{
    private CommandHandler handler;

    public CommandModule(Kizai bot){
        super(bot);
        
        handler = new CommandHandler("Core");
        
        try{bot.bindEvent(CommandRegisterEvent.class, handler, "onCommandRegister");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(CommandEvent.class, handler, "onCommand");}catch(NoSuchMethodException ex){}
    }
    
    /**
     * Register a new-style command for a specific listener. This function is
     * simply a shortcut to make it look neater in-code. All it does is 
     * construct a Command and CommandRegisterEvent instance and send that
     * through the bot.
     * 
     * @param bot The Kizai main instance.
     * @param cmd The command to register for.
     * @param args A list of commands that the argument can posess.
     * @param help An optional help string.
     * @param listener The listener for this command.
     */
    public static void register(Kizai bot, String cmd, String[] args, String help, CommandListener listener){
        bot.event(new CommandRegisterEvent(new Command(cmd, args, help), listener));
    }
    
    
    /**
     * Register a new-style command for a specific listener and group. This
     * function is simply a shortcut to make it look neater in-code. All it does
     * is construct a Command, CommandHandler and GroupRegisterEvent instance
     * and send that through the bot.
     * 
     * @param bot The Kizai main instance.
     * @param group The command group to be included in.
     * @param cmd The command to register for.
     * @param args A list of commands that the argument can posess.
     * @param help An optional help string.
     * @param listener The listener for this command.
     */
    public static void register(Kizai bot, String group, String cmd, String[] args, String help, CommandListener listener){
        //This is unoptimized garbage and should probably be remade some time.
        //Right now I don't give enough of a shit and just want to get it to work though.
        //Yes, yes I know, sad onion and all. Here, have a @TODO.
        CommandHandler handler = new CommandHandler(group+" "+cmd);
        handler.onCommandRegister(new CommandRegisterEvent(new Command(group+" "+cmd, args, help), listener));
        bot.event(new GroupRegisterEvent(group, cmd, handler));
    }
    
    @Override
    public void shutdown(){
        bot.unbindAllEvents(handler);
        bot.unregisterAllCommands(handler);
    }
}
