package org.tymoonnext.bot.event.core;

import org.tymoonnext.bot.event.Event;
import org.tymoonnext.bot.meta.Arguments;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.stream.Stream;

/**
 * Event issued on module load.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class ModuleLoadEvent extends Event{
    private Module module;
    
    @Arguments({"origin", "m"})
    public ModuleLoadEvent(Stream origin, Module m){
        super(origin);
        this.module=m;
    }
    
    public Module getModule(){return module;}
}
