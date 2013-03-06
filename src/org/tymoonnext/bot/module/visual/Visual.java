package org.tymoonnext.bot.module.visual;

import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.module.Module;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Visual extends Module{
    private VisualBase baseFrame;
    
    public Visual(Kizai bot){
        super(bot);
        
        baseFrame = new VisualBase(bot);
    }

    @Override
    public void shutdown() {
        
    }
}
