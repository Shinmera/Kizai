package org.tymoonnext.bot.event;

import org.tymoonnext.bot.event.core.CommandEvent;

/**
 * Command listener interface
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public interface CommandListener {
    public void onCommand(CommandEvent cmd);
}
