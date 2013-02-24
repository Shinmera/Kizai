package org.tymoonnext.bot.module.irc;

import NexT.data.DObject;
import java.io.IOException;
import java.util.HashMap;
import org.jibble.pircbot.DccChat;
import org.jibble.pircbot.DccFileTransfer;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.tymoonnext.bot.Commons;
import NexT.data.ConfigLoader;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.IRCBot.*;
import NexT.data.required;
import org.tymoonnext.bot.stream.Stream;

/**
 * IRC class that links the PircBot functions to the event system.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 * @todo Properly respect event cancelling
 */
public class IRC extends PircBot implements Stream{
    public class C extends ConfigLoader{
        public String nick = "KizaiBot";
        public String login = nick;
        public long msgdelay = 1000L;
        @required public String host;
        public int port = 6667;
        public String pass = null;
        public DObject channels = new DObject();
        public String connectcmd = null;
    }
    
    private C config = new C();
    private Kizai bot;
    
    public IRC(Kizai bot, DObject conf) throws IOException, IrcException{
        this.bot = bot;
        config.load(conf);
        setName(config.nick);
        setLogin(config.login);
        setVersion(Commons.getVersionString());
        setMessageDelay(config.msgdelay);
        setAutoNickChange(true);
        connect(config.host, config.port, config.pass);
        for(String channel : ((HashMap<String,DObject>)config.channels.get()).keySet()){
            if((Boolean)config.channels.get(channel).get("autojoin").get()){
                joinChannel(channel);
            }
        }
        if(config.connectcmd != null){
            this.sendRawLine(config.connectcmd);
        }
    }
    
    public void send(String msg, String dst){
        if(dst.equals("*")){
            sendToAll(msg);
        }else{
            bot.event(new SendEvent(this, dst, msg));
            this.sendMessage(dst, msg);
        }
    }
    
    public void sendToAll(String msg){
        for(String s : getChannels()){
            send(msg, s);
        }
    }

    public void close(){
        this.disconnect();
    }

    protected void onConnect(){
        ConnectEvent evt = new ConnectEvent(this, getServer(), getPort(), getPassword());
        bot.event(evt);
        if(!evt.isCancelled())
            super.onConnect();
    }

    protected void onDisconnect(){
        DisconnectEvent evt = new DisconnectEvent(this, getServer(), getPort(), getPassword());
        bot.event(evt);
        if(!evt.isCancelled())
            super.onDisconnect();
    }

    protected void onMessage(String channel, String sender, String login, String hostname, String message){
        MessageEvent evt = new MessageEvent(this, sender, message, channel, hostname, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onMessage(evt.channel, evt.sender, evt.login, evt.host, evt.message);
    }

    protected void onPrivateMessage(String sender, String login, String hostname, String message){
        PrivateMessageEvent evt = new PrivateMessageEvent(this, sender, message, hostname, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onPrivateMessage(evt.sender, evt.login, evt.host, evt.message);
    }

    protected void onAction(String sender, String login, String hostname, String recipient, String action){
        ActionEvent evt = new ActionEvent(this, sender, action, recipient, hostname, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onAction(evt.sender, evt.login, evt.host, evt.recipient, evt.action);
    }

    protected void onJoin(String channel, String sender, String login, String hostname){
        JoinEvent evt = new JoinEvent(this, sender, channel, hostname, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onJoin(evt.channel, evt.sender, evt.login, evt.host);
    }

    protected void onPart(String channel, String sender, String login, String hostname){
        PartEvent evt = new PartEvent(this, sender, channel, hostname, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onPart(evt.channel, evt.sender, evt.login, evt.host);
    }

    protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason){
        KickEvent evt = new KickEvent(this, kickerNick, channel, kickerHostname, kickerLogin, recipientNick, reason);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onKick(evt.channel, evt.sender, evt.login, evt.host, evt.recipient, evt.reason);
    }

    protected void onQuit(String sender, String login, String host, String reason){
        QuitEvent evt = new QuitEvent(this, sender, reason, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onQuit(evt.sender, evt.login, evt.host, evt.reason);
    }

    protected void onNickChange(String oldNick, String login, String hostname, String newNick){
        NickEvent evt = new NickEvent(this, oldNick, hostname, login, newNick);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onNickChange(evt.sender, evt.login, evt.host, evt.newNick);
    }

    protected void onMode(String channel, String sender, String login, String host, String mode){
        ModeEvent evt = new ModeEvent(this, sender, channel, mode, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onMode(evt.channel, evt.sender, evt.login, evt.host, evt.mode);
    }

    protected void onUserMode(String recipientNick, String sender, String login, String host, String mode){
        UserModeEvent evt = new UserModeEvent(this, sender, recipientNick, mode, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onUserMode(evt.recipient, evt.sender, evt.login, evt.host, evt.mode);
    }

    protected void onChannelInfo(String channel, int userCount, String topic){
        ChannelInfoEvent evt = new ChannelInfoEvent(this, channel, userCount, topic);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onChannelInfo(evt.channel, evt.userCount, evt.topic);
    }

    protected void onTopic(String channel, String topic, String setBy, long date, boolean changed){
        TopicEvent evt = new TopicEvent(this, channel, topic, setBy, date, changed);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onTopic(evt.channel, evt.topic, evt.sender, evt.date, evt.changed);
    }

    protected void onUserList(String channel, User[] users){
        UserListEvent evt = new UserListEvent(this, channel, users);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onUserList(evt.channel, evt.users);
    }

    protected void onOp(String channel, String sender, String login, String host, String recipient){
        OpEvent evt = new OpEvent(this, sender, channel, host, login, recipient);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onOp(evt.channel, evt.sender, evt.login, evt.host, evt.recipient);
    }

    protected void onDeop(String channel, String sender, String login, String host, String recipient){
        DeopEvent evt = new DeopEvent(this, sender, channel, host, login, recipient);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onDeop(evt.channel, evt.sender, evt.login, evt.host, evt.recipient);
    }

    protected void onDccChatRequest(String sender, String login, String host, long address, int port){
        DccChatRequestEvent evt = new DccChatRequestEvent(this, sender, host, login, address, port);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onDccChatRequest(evt.sender, evt.login, evt.host, evt.address, evt.port);
    }

    protected void onDccSendRequest(String sender, String login, String host, String filename, long address, int port, int size){
        DccSendRequestEvent evt = new DccSendRequestEvent(this, sender, filename, host, login, address, port, size);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onDccSendRequest(evt.sender, evt.login, evt.host, evt.file, evt.address, evt.port, evt.size);
    }

    protected void onIncomingFileTransfer(DccFileTransfer transfer){
        FileTransferEvent evt = new FileTransferEvent(this, transfer);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onIncomingFileTransfer(evt.transfer);
    }
    
    protected void onFileTransferFinished(DccFileTransfer transfer, Exception e){
        FileTransferEvent evt = new FileTransferEvent(this, transfer, e, true);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onFileTransferFinished(evt.transfer, evt.exception);
    }

    protected void onIncomingChatRequest(DccChat chat){
        ChatRequestEvent evt = new ChatRequestEvent(this, chat);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onIncomingChatRequest(evt.chat);
    }

    protected void onServerPing(String response){
        ServerPingEvent evt = new ServerPingEvent(this, response);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onServerPing(evt.response);
    }

    protected void onFinger(String sender, String login, String host, String recipient){
        FingerEvent evt = new FingerEvent(this, sender, recipient, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onFinger(evt.sender, evt.login, evt.host, evt.recipient);
    }

    protected void onTime(String sender, String login, String host, String recipient){
        TimeEvent evt = new TimeEvent(this, sender, recipient, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onTime(evt.sender, evt.login, evt.host, evt.recipient);
    }

    protected void onVersion(String sender, String login, String host, String recipient){
        VersionEvent evt = new VersionEvent(this, sender, recipient, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onVersion(evt.sender, evt.login, evt.host, evt.recipient);
    }

    protected void onNotice(String sender, String login, String host, String recipient, String notice){
        NoticeEvent evt = new NoticeEvent(this, sender, recipient, notice, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onNotice(evt.sender, evt.login, evt.host, evt.recipient, evt.notice);
    }

    protected void onPing(String sender, String login, String host, String recipient, String pingValue){
        PingEvent evt = new PingEvent(this, sender, recipient, pingValue, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onPing(evt.sender, evt.login, evt.host, evt.recipient, evt.ping);
    }
    
    protected void onInvite(String recipientNick, String sender, String login, String host, String channel){
        InviteEvent evt = new InviteEvent(this, sender, recipientNick, channel, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onInvite(evt.recipient, evt.sender, evt.login, evt.host, evt.channel);
    }
    
    protected void onSetInviteOnly(String channel, String sender, String login, String host){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_INVITE_ONLY, sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onSetInviteOnly(evt.channel, evt.sender, evt.login, evt.host);
    }

    protected void onRemoveInviteOnly(String channel, String sender, String login, String host){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_INVITE_ONLY, sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onRemoveInviteOnly(evt.channel, evt.sender, evt.login, evt.host);
    }

    protected void onSetModerated(String channel, String sender, String login, String host){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_MODERATED, sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onSetModerated(evt.channel, evt.sender, evt.login, evt.host);
    }

    protected void onRemoveModerated(String channel, String sender, String login, String host){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_MODERATED, sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onRemoveModerated(evt.channel, evt.sender, evt.login, evt.host);
    }

    protected void onSetNoExternalMessages(String channel, String sender, String login, String host){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_NO_EXTERNAL_MESSAGES, sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onSetNoExternalMessages(evt.channel, evt.sender, evt.login, evt.host);
    }

    protected void onRemoveNoExternalMessages(String channel, String sender, String login, String host){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_NO_EXTERNAL_MESSAGES, sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onRemoveNoExternalMessages(evt.channel, evt.sender, evt.login, evt.host);
    }

    protected void onSetPrivate(String channel, String sender, String login, String host){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_PRIVATE, sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onSetPrivate(evt.channel, evt.sender, evt.login, evt.host);
    }

    protected void onRemovePrivate(String channel, String sender, String login, String host){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_PRIVATE, sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onRemovePrivate(evt.channel, evt.sender, evt.login, evt.host);
    }

    protected void onSetSecret(String channel, String sender, String login, String host){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_SECRET, sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onSetSecret(evt.channel, evt.sender, evt.login, evt.host);
    }

    protected void onRemoveSecret(String channel, String sender, String login, String host){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_SECRET, sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onRemoveSecret(evt.channel, evt.sender, evt.login, evt.host);
    }

    protected void onSetTopicProtection(String channel, String sender, String login, String host){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_TOPIC_PROTECTION, sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onSetTopicProtection(evt.channel, evt.sender, evt.login, evt.host);
    }

    protected void onRemoveTopicProtection(String channel, String sender, String login, String host){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_TOPIC_PROTECTION, sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onRemoveTopicProtection(evt.channel, evt.sender, evt.login, evt.host);
    }

    protected void onVoice(String channel, String sender, String login, String host, String recipient){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_VOICE, recipient, sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onVoice(evt.channel, evt.sender, evt.login, evt.host, evt.args);
    }

    protected void onDeVoice(String channel, String sender, String login, String host, String recipient){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_VOICE, recipient, sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onDeVoice(evt.channel, evt.sender, evt.login, evt.host, evt.args);
    }

    protected void onSetChannelBan(String channel, String sender, String login, String host, String hostmask){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_BAN, hostmask, sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onSetChannelBan(evt.channel, evt.sender, evt.login, evt.host, evt.args);
    }

    protected void onRemoveChannelBan(String channel, String sender, String login, String host, String hostmask){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_BAN, hostmask, sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onRemoveChannelBan(evt.channel, evt.sender, evt.login, evt.host, evt.args);
    }

    protected void onSetChannelKey(String channel, String sender, String login, String host, String key){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_KEY, key, sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onSetChannelKey(evt.channel, evt.sender, evt.login, evt.host, evt.args);
    }

    protected void onRemoveChannelKey(String channel, String sender, String login, String host, String key){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_KEY, key, sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onRemoveChannelKey(evt.channel, evt.sender, evt.login, evt.host, evt.args);
    }

    protected void onSetChannelLimit(String channel, String sender, String login, String host, int limit){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_LIMIT, limit+"", sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onSetChannelLimit(evt.channel, evt.sender, evt.login, evt.host, Integer.parseInt(evt.args));
    }

    protected void onRemoveChannelLimit(String channel, String sender, String login, String host){
        ChannelChangeEvent evt = new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_LIMIT, sender, host, login);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onRemoveChannelLimit(evt.channel, evt.sender, evt.login, evt.host);
    }

    protected void onServerResponse(int code, String response){
        ServerResponseEvent evt = new ServerResponseEvent(this, code, response);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onServerResponse(evt.code, evt.line);
    }

    protected void onUnknown(String line){
        UnknownEvent evt = new UnknownEvent(this, line);
        bot.event(evt);
        if(!evt.isCancelled())
            super.onUnknown(evt.line);
    }
    
    public String toString(){return "~IRC~";}
}