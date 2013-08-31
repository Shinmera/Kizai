package org.tymoonnext.bot.module.misc;

import NexT.Commons;
import java.util.logging.Level;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.module.core.ext.CommandModule;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Calc extends Module implements CommandListener{
    private Evaluator evaluator = new Evaluator();

    public Calc(Kizai bot){
        super(bot);
        CommandModule.register(bot, "calc", "expression".split(" "), "Evaluate mathematical expressions.", this);
    }
    
    @Override
    public void shutdown() {
        bot.unregisterAllCommands(this);
    }

    @Override
    public void onCommand(CommandEvent cmd) {
        try{
            cmd.getStream().send("Result: " + evaluator.evaluate(cmd.getArgs()), cmd.getChannel());
        }catch(EvaluationException ex){
            Commons.log.log(Level.WARNING, "Failed to evaluate '"+cmd.getArgs()+"'.", ex);
            cmd.getStream().send("Failed to evaluate: "+ex.getMessage(), cmd.getChannel());
        }
    }
}
