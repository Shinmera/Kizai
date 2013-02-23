package org.tymoonnext.bot.event.core;

import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.event.Event;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class ShutdownEvent extends Event{
    public ShutdownEvent(){super(Commons.stdout);}
}
