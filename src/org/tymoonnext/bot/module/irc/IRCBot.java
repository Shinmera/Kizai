package org.tymoonnext.bot.module.irc;

import NexT.data.ConfigLoader;
import NexT.data.DObject;
import NexT.data.DParse;
import NexT.data.required;
import NexT.util.Toolkit;
import java.io.File;
import java.io.IOException;
import org.jibble.pircbot.IrcException;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.IRCBot.*;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.Module;

/**
 * IRCBot Module that provides an interface to an IRC server.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 * @see org.tymoonnext.bot.module.irc.IRC
 */
public class IRCBot extends Module implements EventListener{
    public static final File CONFIGFILE = new File(Commons.f_CONFIGDIR, "irc.cfg");
    
    public class C extends ConfigLoader{
        public String cmd = "./";
        @required public DObject server;
    }
    
    private C config = new C();
    private IRC irc;
    
    public IRCBot(Kizai bot) throws IOException, IrcException{
        super(bot);
        config.load(DParse.parse(CONFIGFILE));
        
        irc = new IRC(bot, config.server);
        bot.registerStream(irc);
        try{bot.bindEvent(MessageEvent.class, this, "onMessage");}catch(NoSuchMethodException ex){}
    }

    public void shutdown(){
        bot.unbindAllEvents(this);
        Toolkit.saveStringToFile(DParse.parse(config.save(), true), CONFIGFILE);
    }
    
    public IRC getIRC(){return irc;}
    
    public void onAction(ActionEvent evt){
        irc.sendAction(evt.recipient, evt.action);
    }
    
    public void onChannelChange(ChannelChangeEvent evt){
        switch(evt.change){
            case ChannelChangeEvent.TYPE_REMOVE_BAN:
                
                break;
            case ChannelChangeEvent.TYPE_REMOVE_INVITE_ONLY:
                
                break;
            case ChannelChangeEvent.TYPE_REMOVE_KEY:
                
                break;
            case ChannelChangeEvent.TYPE_REMOVE_LIMIT:
                
                break;
            case ChannelChangeEvent.TYPE_REMOVE_MODERATED:
                
                break;
            case ChannelChangeEvent.TYPE_REMOVE_NO_EXTERNAL_MESSAGES:
                
                break;
            case ChannelChangeEvent.TYPE_REMOVE_PRIVATE:
                
                break;
            case ChannelChangeEvent.TYPE_REMOVE_SECRET:
                
                break;
            case ChannelChangeEvent.TYPE_REMOVE_TOPIC_PROTECTION:
                
                break;
            case ChannelChangeEvent.TYPE_REMOVE_VOICE:
                
                break;
            case ChannelChangeEvent.TYPE_SET_BAN:
                
                break;
            case ChannelChangeEvent.TYPE_SET_INVITE_ONLY:
                
                break;
            case ChannelChangeEvent.TYPE_SET_KEY:
                
                break;
            case ChannelChangeEvent.TYPE_SET_LIMIT:
                
                break;
            case ChannelChangeEvent.TYPE_SET_MODERATED:
                
                break;
            case ChannelChangeEvent.TYPE_SET_NO_EXTERNAL_MESSAGES:
                
                break;
            case ChannelChangeEvent.TYPE_SET_PRIVATE:
                
                break;
            case ChannelChangeEvent.TYPE_SET_SECRET:
                
                break;
            case ChannelChangeEvent.TYPE_SET_TOPIC_PROTECTION:
                
                break;
            case ChannelChangeEvent.TYPE_SET_VOICE:
                
                break;
            default:
                //NOOP.
                break;
        }
    }
    
    public void onChannelInfo(ChannelInfoEvent evt){
        //@TODO: Gingy
    }
    
    public void onChatRequest(ChatRequestEvent evt){
        //@TODO: Gingy
    }
    
    public void onConnect(ConnectEvent evt) throws IOException, IrcException{
        if(evt.host == null)    irc.reconnect();
        else if(evt.port < 0)   irc.connect(evt.host);
        else if(evt.pw == null) irc.connect(evt.host, evt.port);
        else                    irc.connect(evt.host, evt.port, evt.pw);
    }
    
    public void onDccChatRequest(DccChatRequestEvent evt){
        //@TODO: Gingy
    }
    
    public void onDccSendRequest(DccSendRequestEvent evt){
        //@TODO: Gingy
    }
    
    public void onDeop(DeopEvent evt){
        irc.deOp(evt.channel, evt.recipient);
    }
    
    public void onDisconnect(DisconnectEvent evt){
        irc.disconnect();
    }
    
    public void onFileTransfer(FileTransferEvent evt){
        //@TODO: Gingy
    }
    
    public void onFinger(FingerEvent evt){
        irc.sendMessage(evt.recipient, "CTCP "+evt.recipient+" FINGER");
    }
    
    public void onIRC(IRCEvent evt){
        //NOOP.
    }
    
    public void onInvite(InviteEvent evt){
        irc.sendInvite(evt.recipient, evt.channel);
    }
    
    public void onJoin(JoinEvent evt){
        irc.joinChannel(evt.channel);
    }
    
    public void onKick(KickEvent evt){
        irc.kick(evt.channel, evt.recipient, evt.reason);
    }
    
    public void onMessage(MessageEvent ev){
        if(ev.getIRC() == null){
            irc.sendMessage(ev.channel, ev.message);
        }else{
            if(ev.message.startsWith(config.cmd)){
                String cmd,args;
                if(ev.message.contains(" ")){
                    cmd = ev.message.substring(config.cmd.length(), ev.message.indexOf(" "));
                    args = ev.message.substring(ev.message.indexOf(" ")+1);
                }else{
                    cmd = ev.message.substring(config.cmd.length());
                    args = null;
                }
                bot.event(new CommandEvent(irc, cmd, args, ev.sender, ev.channel));
            }
        }
    }
    
    public void onMode(ModeEvent evt){
        irc.sendRawLineViaQueue("/mode "+evt.mode+" "+evt.channel);
    }
    
    public void onNick(NickEvent evt){
        irc.changeNick(evt.newNick);
    }
    
    public void onNotice(NoticeEvent evt){
        irc.sendNotice(evt.recipient, evt.notice);
    }
    
    public void onOp(OpEvent evt){
        irc.op(evt.channel, evt.recipient);
    }
    
    public void onPart(PartEvent evt){
        irc.partChannel(evt.channel);
    }
    
    public void onPing(PingEvent evt){
        irc.sendMessage(evt.recipient, "CTCP "+evt.recipient+" PING");
    }
    
    public void onPrivateMessage(PrivateMessageEvent evt){
        irc.sendMessage(evt.sender, evt.message);
    }
    
    public void onQuit(QuitEvent evt){
        irc.quitServer(evt.reason);
    }
    
    public void onSend(SendEvent evt){
        irc.sendMessage(evt.dest, evt.message);
    }
    
    public void onServerPing(ServerPingEvent evt){
        irc.sendRawLineViaQueue("/ping");
    }
    
    public void onServerResponse(ServerResponseEvent evt){
        //NOOP.
    }
    
    public void onTime(TimeEvent evt){
        irc.sendMessage(evt.recipient, "CTCP "+evt.recipient+" TIME");
    }
    
    public void onTopic(TopicEvent evt){
        irc.setTopic(evt.channel, evt.topic);
    }
    
    public void onUnknown(UnknownEvent evt){
        irc.sendRawLineViaQueue(evt.line);
    }
    
    public void onUserList(UserListEvent evt){
        irc.sendRawLineViaQueue("/names "+evt.channel);
    }
    
    public void onUserMode(UserModeEvent evt){
        irc.sendRawLineViaQueue("/mode "+evt.mode+" "+evt.recipient);
    }
    
    public void onVersion(VersionEvent evt){
        irc.sendMessage(evt.recipient, "CTCP "+evt.recipient+" VERSION");
    }
}
