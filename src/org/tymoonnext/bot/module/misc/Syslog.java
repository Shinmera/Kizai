package org.tymoonnext.bot.module.misc;

import NexT.err.NHandler;
import NexT.util.LimitedSizeStack;
import NexT.util.StringUtils;
import NexT.util.Toolkit;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.cmd.CommandInstanceEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.meta.Info;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.module.cmd.CommandInstance;
import org.tymoonnext.bot.module.core.ext.CommandModule;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
@Info("System log class that allows one to retrieve (and filter) recent log entries.")
public class Syslog extends Module implements CommandListener{
    private final static int STACKSIZE = 500;
    private final LimitedSizeStack<String> logstack;
    private final LimitedSizeStack<Integer> lvlstack;
    private Logger logger;

    public Syslog(Kizai bot){
        super(bot);
        logstack = new LimitedSizeStack<String>(STACKSIZE);
        lvlstack = new LimitedSizeStack<Integer>(STACKSIZE);
        logger = new Logger();
        Commons.log.addHandler(logger);
        
        String[] cmds = {"n[10](INTEGER)",
            "filter[INFO]{OFF|SEVERE|WARNING|INFO|FINE|FINER|FINEST|ALL}",
            "action[PRINT]{PRINT|WRITE|CLEAR}"};
        CommandModule.register(bot, "syslog", cmds, "Get recent messages from the syslog (up to the last 500).", this);
    }
    
    public void shutdown(){
        Commons.log.removeHandler(logger);
    }
    
    public void onCommand(CommandEvent evt){
        if(evt.getCommand().equals("syslog")){
            CommandInstance ci = ((CommandInstanceEvent)evt).get();
            int n = Integer.parseInt(ci.getValue("n"));
            int l = 0;
            if(ci.getValue("filter").equals("NONE"))        l = Level.OFF.intValue();
            else if(ci.getValue("filter").equals("SEVERE")) l = Level.SEVERE.intValue();
            else if(ci.getValue("filter").equals("WARNING"))l = Level.WARNING.intValue();
            else if(ci.getValue("filter").equals("INFO"))   l = Level.INFO.intValue();
            else if(ci.getValue("filter").equals("FINE"))   l = Level.FINE.intValue();
            else if(ci.getValue("filter").equals("FINER"))  l = Level.FINER.intValue();
            else if(ci.getValue("filter").equals("FINEST")) l = Level.FINEST.intValue();
            else if(ci.getValue("filter").equals("ALL"))    l = Level.ALL.intValue();
            
            if(ci.getValue("action").equalsIgnoreCase("print")||
               ci.getValue("action").equalsIgnoreCase("write")){
                
                List<String> list = new ArrayList<String>();
                for(int i=0,c=0;i<lvlstack.size()&&c<n;i++){
                    if(lvlstack.get(i).intValue() >= l){
                        list.add(logstack.get(i));
                        c++;
                    }
                }
                list = Lists.reverse(list);
                
                if(ci.getValue("action").equalsIgnoreCase("print")){
                    evt.getStream().send(toString()+" Last "+n+" available log messages, filtered to "+ci.getValue("filter")+": ", evt.getChannel());
                    for(String s : list){
                        evt.getStream().send(s, evt.getChannel());
                    }
                }else{
                    Toolkit.saveStringToFile(StringUtils.implode(list.toArray(), "\n"), new File(Commons.f_BASEDIR, "syslog.out.log"));
                    evt.getStream().send(toString()+" Last "+n+" available log messages, filtered to "+ci.getValue("filter")+" saved to syslog.out.log", evt.getChannel());
                }
                
                
            }else if(ci.getValue("action").equalsIgnoreCase("clear")){
                logstack.clear();
                lvlstack.clear();
                evt.getStream().send(toString()+" Log cleared.", evt.getChannel());
            }
        }
    }
    
    class Logger extends NHandler{        
        public Logger(){super(Level.ALL);}

        public void publish(LogRecord record){
            logstack.add(sformat(record));
            lvlstack.add(record.getLevel().intValue());
        }
    }
}
