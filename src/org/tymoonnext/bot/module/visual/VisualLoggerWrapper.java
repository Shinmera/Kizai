package org.tymoonnext.bot.module.visual;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.tymoonnext.bot.stream.Stream;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class VisualLoggerWrapper extends Handler{
    private Stream s;
    private Level level = Level.INFO;
    
    public VisualLoggerWrapper(Stream s){
        this.s=s;
    }

    @Override
    public void publish(LogRecord record) {
        if(record.getLevel().intValue() >= level.intValue())
            s.send(record.getMessage(), record.getLevel().getName());
    }

    @Override
    public void flush() {}
    public void close() throws SecurityException {}
    
    public void setLevel(Level lvl){level=lvl;}
    public Level getLevel(){return level;}
}
