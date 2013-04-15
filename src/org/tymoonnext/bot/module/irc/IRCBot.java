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
import org.tymoonnext.bot.meta.Info;
import org.tymoonnext.bot.module.Module;

/**
 * IRCBot Module that provides an interface to an IRC server.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 * @see org.tymoonnext.bot.module.irc.IRC
 */

@Info("Main module that provides the IRC stream.")
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
        
        try{bot.bindEvent(ActionEvent.class,            this, "onAction");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(ChannelChangeEvent.class,     this, "onChannelChange");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(ChannelInfoEvent.class,       this, "onChannelInfo");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(ChatRequestEvent.class,       this, "onChatRequest");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(ConnectEvent.class,           this, "onConnect");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(DccChatRequestEvent.class,    this, "onDccChatRequest");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(DccSendRequestEvent.class,    this, "onDccSendRequest");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(DeopEvent.class,              this, "onDeop");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(DisconnectEvent.class,        this, "onDisconnect");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(FileTransferEvent.class,      this, "onFileTransfer");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(FingerEvent.class,            this, "onFinger");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(IRCEvent.class,               this, "onIRC");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(InviteEvent.class,            this, "onInvite");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(JoinEvent.class,              this, "onJoin");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(KickEvent.class,              this, "onKick");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(MessageEvent.class,           this, "onMessage");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(ModeEvent.class,              this, "onMode");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(NickEvent.class,              this, "onNick");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(NoticeEvent.class,            this, "onNotice");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(OpEvent.class,                this, "onOp");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(PartEvent.class,              this, "onPart");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(PingEvent.class,              this, "onPing");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(PrivateMessageEvent.class,    this, "onPrivateMessage");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(QuitEvent.class,              this, "onQuit");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(SendEvent.class,              this, "onSend");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(ServerPingEvent.class,        this, "onServerPing");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(ServerResponseEvent.class,    this, "onServerResponse");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(TimeEvent.class,              this, "onTime");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(TopicEvent.class,             this, "onTopic");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(UnknownEvent.class,           this, "onUnknown");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(UserListEvent.class,          this, "onUserList");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(UserModeEvent.class,          this, "onUserMode");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(VersionEvent.class,           this, "onVersion");}catch(NoSuchMethodException ex){}
    }

    public void shutdown(){
        bot.unbindAllEvents(this);
        Toolkit.saveStringToFile(DParse.parse(config.save(), true), CONFIGFILE);
    }
    
    public IRC getIRC(){return irc;}
    
    public void onAction(ActionEvent evt){
        if(evt.getIRC() == null)
            irc.sendAction(evt.recipient, evt.action);
    }
    
    public void onChannelChange(ChannelChangeEvent evt){
        if(evt.getIRC() == null){
            switch(evt.change){
                case ChannelChangeEvent.TYPE_REMOVE_BAN:
                    irc.sendRawLineViaQueue("/mode -b "+evt.channel+" "+evt.args);
                    break;
                case ChannelChangeEvent.TYPE_REMOVE_INVITE_ONLY:
                    irc.sendRawLineViaQueue("/mode -i "+evt.channel);
                    break;
                case ChannelChangeEvent.TYPE_REMOVE_KEY:
                    irc.sendRawLineViaQueue("/mode -k "+evt.channel+" "+evt.args);
                    break;
                case ChannelChangeEvent.TYPE_REMOVE_LIMIT:
                    irc.sendRawLineViaQueue("/mode -l "+evt.channel);
                    break;
                case ChannelChangeEvent.TYPE_REMOVE_MODERATED:
                    irc.sendRawLineViaQueue("/mode -m "+evt.channel);
                    break;
                case ChannelChangeEvent.TYPE_REMOVE_NO_EXTERNAL_MESSAGES:
                    irc.sendRawLineViaQueue("/mode -n "+evt.channel);
                    break;
                case ChannelChangeEvent.TYPE_REMOVE_PRIVATE:
                    irc.sendRawLineViaQueue("/mode  -p "+evt.channel);
                    break;
                case ChannelChangeEvent.TYPE_REMOVE_SECRET:
                    irc.sendRawLineViaQueue("/mode  -s "+evt.channel);
                    break;
                case ChannelChangeEvent.TYPE_REMOVE_TOPIC_PROTECTION:
                    irc.sendRawLineViaQueue("/mode  -t "+evt.channel);
                    break;
                case ChannelChangeEvent.TYPE_REMOVE_VOICE:
                    irc.sendRawLineViaQueue("/mode -v "+evt.channel+" "+evt.args);
                    break;
                case ChannelChangeEvent.TYPE_SET_BAN:
                    irc.sendRawLineViaQueue("/mode +b "+evt.channel+" "+evt.args);
                    break;
                case ChannelChangeEvent.TYPE_SET_INVITE_ONLY:
                    irc.sendRawLineViaQueue("/mode +i "+evt.channel);
                    break;
                case ChannelChangeEvent.TYPE_SET_KEY:
                    irc.sendRawLineViaQueue("/mode +k "+evt.channel+" "+evt.args);
                    break;
                case ChannelChangeEvent.TYPE_SET_LIMIT:
                    irc.sendRawLineViaQueue("/mode +l "+evt.channel);
                    break;
                case ChannelChangeEvent.TYPE_SET_MODERATED:
                    irc.sendRawLineViaQueue("/mode +m "+evt.channel);
                    break;
                case ChannelChangeEvent.TYPE_SET_NO_EXTERNAL_MESSAGES:
                    irc.sendRawLineViaQueue("/mode +n "+evt.channel);
                    break;
                case ChannelChangeEvent.TYPE_SET_PRIVATE:
                    irc.sendRawLineViaQueue("/mode +p "+evt.channel);
                    break;
                case ChannelChangeEvent.TYPE_SET_SECRET:
                    irc.sendRawLineViaQueue("/mode +s "+evt.channel);
                    break;
                case ChannelChangeEvent.TYPE_SET_TOPIC_PROTECTION:
                    irc.sendRawLineViaQueue("/mode +t "+evt.channel);
                    break;
                case ChannelChangeEvent.TYPE_SET_VOICE:
                    irc.sendRawLineViaQueue("/mode +v "+evt.channel+" "+evt.args);
                    break;
                default:
                    Commons.log.severe(toString()+" Received unknown ChannelChangeEvent type: "+evt);
                    //NOOP.
                    break;
            }
        }
    }
    
    public void onChannelInfo(ChannelInfoEvent evt){
        //@TODO: Gingy
    }
    
    public void onChatRequest(ChatRequestEvent evt){
        //@TODO: Gingy
    }
    
    public void onConnect(ConnectEvent evt) throws IOException, IrcException{
        if(evt.getIRC() == null){
            if(evt.host == null)    irc.reconnect();
            else if(evt.port < 0)   irc.connect(evt.host);
            else if(evt.pw == null) irc.connect(evt.host, evt.port);
            else                    irc.connect(evt.host, evt.port, evt.pw);
        }
    }
    
    public void onDccChatRequest(DccChatRequestEvent evt){
        //@TODO: Gingy
    }
    
    public void onDccSendRequest(DccSendRequestEvent evt){
        //@TODO: Gingy
    }
    
    public void onDeop(DeopEvent evt){
        if(evt.getIRC() == null)
            irc.deOp(evt.channel, evt.recipient);
    }
    
    public void onDisconnect(DisconnectEvent evt){
        if(evt.getIRC() == null)
            irc.disconnect();
    }
    
    public void onFileTransfer(FileTransferEvent evt){
        //@TODO: Gingy
    }
    
    public void onFinger(FingerEvent evt){
        if(evt.getIRC() == null)
            irc.sendMessage(evt.recipient, "CTCP "+evt.recipient+" FINGER");
    }
    
    public void onIRC(IRCEvent evt){
        //NOOP.
    }
    
    public void onInvite(InviteEvent evt){
        if(evt.getIRC() == null)
            irc.sendInvite(evt.recipient, evt.channel);
    }
    
    public void onJoin(JoinEvent evt){
        if(evt.getIRC() == null)
            irc.joinChannel(evt.channel);
    }
    
    public void onKick(KickEvent evt){
        if(evt.getIRC() == null)
            irc.kick(evt.channel, evt.recipient, evt.reason);
    }
    
    public void onMessage(MessageEvent ev){
        if(ev.getIRC() == null){
            irc.sendMessage(ev.channel, ev.message);
        }else{
            ev.message += " ";
            String args = ev.message.substring(ev.message.indexOf(" ")+1).trim();
            String cmd = null;
            if(ev.message.startsWith(config.cmd)){
                cmd = ev.message.substring(config.cmd.length(), ev.message.indexOf(" ")).trim();
                
            }else if(ev.message.startsWith(config.server.get("nick")+":")){
                cmd = ev.message.substring(config.server.get("nick").toString().length()+1, ev.message.indexOf(" ")).trim();
            }
            
            if(cmd != null)
                bot.event(new CommandEvent(irc, cmd, args, ev.sender, ev.channel));
        }
    }
    
    public void onMode(ModeEvent evt){
        if(evt.getIRC() == null)
            irc.sendRawLineViaQueue("/mode "+evt.mode+" "+evt.channel);
    }
    
    public void onNick(NickEvent evt){
        if(evt.getIRC() == null)
            irc.changeNick(evt.newNick);
    }
    
    public void onNotice(NoticeEvent evt){
        if(evt.getIRC() == null)
            irc.sendNotice(evt.recipient, evt.notice);
    }
    
    public void onOp(OpEvent evt){
        if(evt.getIRC() == null)
            irc.op(evt.channel, evt.recipient);
    }
    
    public void onPart(PartEvent evt){
        if(evt.getIRC() == null)
            irc.partChannel(evt.channel);
    }
    
    public void onPing(PingEvent evt){
        if(evt.getIRC() == null)
            irc.sendMessage(evt.recipient, "CTCP "+evt.recipient+" PING");
    }
    
    public void onPrivateMessage(PrivateMessageEvent evt){
        if(evt.getIRC() == null){
            irc.sendMessage(evt.sender, evt.message);
        }else{
            if(evt.message.startsWith(config.cmd)){
                String cmd,args;
                if(evt.message.contains(" ")){
                    cmd = evt.message.substring(config.cmd.length(), evt.message.indexOf(" "));
                    args = evt.message.substring(evt.message.indexOf(" ")+1);
                }else{
                    cmd = evt.message.substring(config.cmd.length());
                    args = null;
                }
                bot.event(new CommandEvent(irc, cmd, args, evt.sender, evt.sender));
            }
        }
    }
    
    public void onQuit(QuitEvent evt){
        if(evt.getIRC() == null)
            irc.quitServer(evt.reason);
    }
    
    public void onSend(SendEvent evt){
        if(evt.getIRC() == null)
            irc.sendMessage(evt.dest, evt.message);
    }
    
    public void onServerPing(ServerPingEvent evt){
        if(evt.getIRC() == null)
            irc.sendRawLineViaQueue("/ping");
    }
    
    public void onServerResponse(ServerResponseEvent evt){
        //NOOP.
    }
    
    public void onTime(TimeEvent evt){
        if(evt.getIRC() == null)
            irc.sendMessage(evt.recipient, "CTCP "+evt.recipient+" TIME");
    }
    
    public void onTopic(TopicEvent evt){
        if(evt.getIRC() == null)
            irc.setTopic(evt.channel, evt.topic);
    }
    
    public void onUnknown(UnknownEvent evt){
        if(evt.getIRC() == null)
            irc.sendRawLineViaQueue(evt.line);
    }
    
    public void onUserList(UserListEvent evt){
        if(evt.getIRC() == null)
            irc.sendRawLineViaQueue("/names "+evt.channel);
    }
    
    public void onUserMode(UserModeEvent evt){
        if(evt.getIRC() == null)
            irc.sendRawLineViaQueue("/mode "+evt.mode+" "+evt.recipient);
    }
    
    public void onVersion(VersionEvent evt){
        if(evt.getIRC() == null)
            irc.sendMessage(evt.recipient, "CTCP "+evt.recipient+" VERSION");
    }
}
