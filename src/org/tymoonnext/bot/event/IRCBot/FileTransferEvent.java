package org.tymoonnext.bot.event.IRCBot;

import org.jibble.pircbot.DccFileTransfer;
import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class FileTransferEvent extends IRCEvent{
    public DccFileTransfer transfer;
    public Exception exception;
    public boolean finished=false;
    
    public FileTransferEvent(IRC bot, DccFileTransfer transfer){super(bot);this.transfer=transfer;}
    public FileTransferEvent(IRC bot, DccFileTransfer transfer, Exception ex, boolean finished){
        super(bot);
        this.transfer=transfer;
        this.exception=ex;
        this.finished=finished;
    }
}
