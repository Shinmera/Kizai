package org.tymoonnext.bot.module.core.ext;

import java.util.TreeMap;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.cmdgroup.GroupRegisterEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.meta.Info;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.module.cmdgroup.CommandGroup;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */

@Info("Handles command grouping by splitting commands into main- and sub-commands before propagating them.")
public class CommandGroupModule extends Module implements EventListener, CommandListener{
    private TreeMap<String, CommandGroup> groups;

    public CommandGroupModule(Kizai bot){
        super(bot);
        
        groups = new TreeMap<String, CommandGroup>();
        
        bot.registerCommand("help", this);
        bot.registerCommand("commands", this);
        try{bot.bindEvent(GroupRegisterEvent.class, this, "onGroupRegister");}catch(NoSuchMethodException ex){}
    }
    
    public void shutdown(){
        bot.unbindAllEvents(this);
        for(CommandGroup group : groups.values()){
            bot.unregisterAllCommands(group);
        }
    }
    
    public void onGroupRegister(GroupRegisterEvent evt){
        if(!groups.containsKey(evt.getGroupName())){
            Commons.log.finer(toString()+" Creating command group "+evt.getGroupName());
            CommandGroup group = new CommandGroup(bot, evt.getGroupName());
            group.onGroupRegisterEvent(evt);
            groups.put(evt.getGroupName(), group);
        }else{
            groups.get(evt.getGroupName()).onGroupRegisterEvent(evt);
        }
    }

    public void onCommand(CommandEvent cmd){
        
    }

}
