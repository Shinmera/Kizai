package org.tymoonnext.bot.event.cmdgroup;

import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.Event;
import org.tymoonnext.bot.meta.Arguments;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class GroupRegisterEvent extends Event{
    private String group;
    private String subcmd;
    private CommandListener listener;
    private boolean force;
    
    @Arguments({"group", "subcmd", "listener"})
    public GroupRegisterEvent(String group, String subcmd, CommandListener listener){this(group, subcmd, listener, false);}
    
    @Arguments({"group", "subcmd", "listener", "force"})
    public GroupRegisterEvent(String group, String subcmd, CommandListener listener, boolean force){
        super(Commons.stdout);
        this.group=group.toLowerCase();
        this.subcmd=subcmd.toLowerCase();
        this.listener=listener;
        this.force=force;
    }
    
    public String getGroupName(){return group;}
    public String getSubCommand(){return subcmd;}
    public CommandListener getListener(){return listener;}
    public boolean isForced(){return force;}
}
