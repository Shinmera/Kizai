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
    
    public FileTransferEvent(DccFileTransfer transfer){this(null, transfer, null, false);}
    public FileTransferEvent(IRC bot, DccFileTransfer transfer){this(bot, transfer, null, false);}
    public FileTransferEvent(IRC bot, DccFileTransfer transfer, Exception ex, boolean finished){
        super(bot);
        this.transfer=transfer;
        this.exception=ex;
        this.finished=finished;
    }
}
