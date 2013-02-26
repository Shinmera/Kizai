package org.tymoonnext.bot.event.cmd;

import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.Event;

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
    
    public GroupRegisterEvent(String group, String subcmd, CommandListener listener){this(group, subcmd, listener, false);}
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
