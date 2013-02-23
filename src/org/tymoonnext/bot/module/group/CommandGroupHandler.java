package org.tymoonnext.bot.module.group;

import java.util.HashMap;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.group.GroupRegisterEvent;
import org.tymoonnext.bot.module.Module;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class CommandGroupHandler extends Module implements EventListener{
    private HashMap<String, CommandGroup> groups;

    public CommandGroupHandler(Kizai bot){
        super(bot);
        
        groups = new HashMap<String, CommandGroup>();
        
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
            Commons.log.info(toString()+" Creating command group "+evt.getGroupName());
            CommandGroup group = new CommandGroup(bot, evt.getGroupName());
            group.onGroupRegisterEvent(evt);
            groups.put(evt.getGroupName(), group);
        }else{
            groups.get(evt.getGroupName()).onGroupRegisterEvent(evt);
        }
    }

}
