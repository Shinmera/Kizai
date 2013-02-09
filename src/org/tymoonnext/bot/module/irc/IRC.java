package org.tymoonnext.bot.module.irc;

import org.tymoonnext.bot.event.IRCBot.FileTransferEvent;
import org.tymoonnext.bot.event.IRCBot.DccSendRequestEvent;
import org.tymoonnext.bot.event.IRCBot.PartEvent;
import org.tymoonnext.bot.event.IRCBot.UnknownEvent;
import org.tymoonnext.bot.event.IRCBot.ModeEvent;
import org.tymoonnext.bot.event.IRCBot.UserListEvent;
import org.tymoonnext.bot.event.IRCBot.KickEvent;
import org.tymoonnext.bot.event.IRCBot.InviteEvent;
import org.tymoonnext.bot.event.IRCBot.FingerEvent;
import org.tymoonnext.bot.event.IRCBot.DisconnectEvent;
import org.tymoonnext.bot.event.IRCBot.ChannelChangeEvent;
import org.tymoonnext.bot.event.IRCBot.UserModeEvent;
import org.tymoonnext.bot.event.IRCBot.ChannelInfoEvent;
import org.tymoonnext.bot.event.IRCBot.ServerResponseEvent;
import org.tymoonnext.bot.event.IRCBot.ChatRequestEvent;
import org.tymoonnext.bot.event.IRCBot.NoticeEvent;
import org.tymoonnext.bot.event.IRCBot.DeopEvent;
import org.tymoonnext.bot.event.IRCBot.PrivateMessageEvent;
import org.tymoonnext.bot.event.IRCBot.ServerPingEvent;
import org.tymoonnext.bot.event.IRCBot.VersionEvent;
import org.tymoonnext.bot.event.IRCBot.DccChatRequestEvent;
import org.tymoonnext.bot.event.IRCBot.JoinEvent;
import org.tymoonnext.bot.event.IRCBot.ActionEvent;
import org.tymoonnext.bot.event.IRCBot.PingEvent;
import org.tymoonnext.bot.event.IRCBot.TimeEvent;
import org.tymoonnext.bot.event.IRCBot.SendEvent;
import org.tymoonnext.bot.event.IRCBot.ConnectEvent;
import org.tymoonnext.bot.event.IRCBot.MessageEvent;
import org.tymoonnext.bot.event.IRCBot.TopicEvent;
import org.tymoonnext.bot.event.IRCBot.OpEvent;
import org.tymoonnext.bot.event.IRCBot.QuitEvent;
import org.tymoonnext.bot.event.IRCBot.NickEvent;
import NexT.data.DObject;
import java.io.IOException;
import java.util.HashMap;
import org.jibble.pircbot.DccChat;
import org.jibble.pircbot.DccFileTransfer;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.stream.Stream;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class IRC extends PircBot implements Stream{
    private Kizai bot;
    
    public IRC(Kizai bot, DObject config) throws IOException, IrcException{
        this.bot = bot;
        DObject server = config.get("server");
        setLogin(config.get("login").toString());
        setName(config.get("nick").toString());
        connect(server.get("host").toString(), (Integer)server.get("port").get(), server.get("pass").toString());

        DObject channels = server.get("channels");
        for(String channel : ((HashMap<String,DObject>)channels.get()).keySet()){
            if((Boolean)channels.get(channel).get("autojoin").get()){
                joinChannel(channel);
            }
        }
    }
    
    public void send(String msg, String dst){
        bot.event(new SendEvent(this, dst, msg));
        this.sendMessage(dst, msg);
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
        super.onConnect();
        bot.event(new DisconnectEvent(this));
    }

    protected void onDisconnect(){
        super.onDisconnect();
        bot.event(new ConnectEvent(this));
    }

    protected void onMessage(String channel, String sender, String login, String hostname, String message){
        super.onMessage(channel, sender, login, hostname, message);
        bot.event(new MessageEvent(this, sender, message, channel, hostname, login));
    }

    protected void onPrivateMessage(String sender, String login, String hostname, String message){
        super.onPrivateMessage(sender, login, hostname, message);
        bot.event(new PrivateMessageEvent(this, sender, message, hostname, login));
    }

    protected void onAction(String sender, String login, String hostname, String target, String action){
        super.onAction(sender, login, hostname, target, action);
        bot.event(new ActionEvent(this, sender, action, target, hostname, login));
    }

    protected void onJoin(String channel, String sender, String login, String hostname){
        super.onJoin(channel, sender, login, hostname);
        bot.event(new JoinEvent(this, sender, channel, hostname, login));
    }

    protected void onPart(String channel, String sender, String login, String hostname){
        super.onPart(channel, sender, login, hostname);
        bot.event(new PartEvent(this, sender, channel, hostname, login));
    }

    protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason){
        super.onKick(channel, kickerNick, kickerLogin, kickerHostname, recipientNick, reason);
        bot.event(new KickEvent(this, kickerNick, channel, kickerHostname, kickerLogin, recipientNick, reason));
    }

    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason){
        super.onQuit(sourceNick, sourceLogin, sourceHostname, reason);
        bot.event(new QuitEvent(this, sourceNick, reason, sourceHostname, sourceLogin));
    }

    protected void onNickChange(String oldNick, String login, String hostname, String newNick){
        super.onNickChange(oldNick, login, hostname, newNick);
        bot.event(new NickEvent(this, oldNick, hostname, login, newNick));
    }

    protected void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode){
        super.onMode(channel, sourceNick, sourceLogin, sourceHostname, mode);
        bot.event(new ModeEvent(this, sourceNick, channel, mode, sourceHostname, sourceLogin));
    }

    protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode){
        super.onUserMode(targetNick, sourceNick, sourceLogin, sourceHostname, mode);
        bot.event(new UserModeEvent(this, sourceNick, targetNick, mode, sourceHostname, sourceLogin));
    }

    protected void onChannelInfo(String channel, int userCount, String topic){
        super.onChannelInfo(channel, userCount, topic);
        bot.event(new ChannelInfoEvent(this, channel, userCount, topic));
    }

    protected void onTopic(String channel, String topic, String setBy, long date, boolean changed){
        super.onTopic(channel, topic, setBy, date, changed);
        bot.event(new TopicEvent(this, channel, topic, setBy, date, changed));
    }

    protected void onUserList(String channel, User[] users){
        super.onUserList(channel, users);
        bot.event(new UserListEvent(this, channel, users));
    }

    protected void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient){
        super.onOp(channel, sourceNick, sourceLogin, sourceHostname, recipient);
        bot.event(new OpEvent(this, sourceNick, channel, sourceHostname, sourceLogin, recipient));
    }

    protected void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient){
        super.onDeop(channel, sourceNick, sourceLogin, sourceHostname, recipient);
        bot.event(new DeopEvent(this, sourceNick, channel, sourceHostname, sourceLogin, recipient));
    }

    protected void onDccChatRequest(String sourceNick, String sourceLogin, String sourceHostname, long address, int port){
        super.onDccChatRequest(sourceNick, sourceLogin, sourceHostname, address, port);
        bot.event(new DccChatRequestEvent(this, sourceNick, sourceHostname, sourceLogin, address, port));
    }

    protected void onDccSendRequest(String sourceNick, String sourceLogin, String sourceHostname, String filename, long address, int port, int size){
        super.onDccSendRequest(sourceNick, sourceLogin, sourceHostname, filename, address, port, size);
        bot.event(new DccSendRequestEvent(this, sourceNick, filename, sourceHostname, sourceLogin, address, port, size));
    }

    protected void onIncomingFileTransfer(DccFileTransfer transfer){
        super.onIncomingFileTransfer(transfer);
        bot.event(new FileTransferEvent(this, transfer));
    }
    
    protected void onFileTransferFinished(DccFileTransfer transfer, Exception e){
        super.onFileTransferFinished(transfer, e);
        bot.event(new FileTransferEvent(this, transfer, e, true));
    }

    protected void onIncomingChatRequest(DccChat chat){
        super.onIncomingChatRequest(chat);
        bot.event(new ChatRequestEvent(this, chat));
    }

    protected void onServerPing(String response){
        super.onServerPing(response);
        bot.event(new ServerPingEvent(this, response));
    }

    protected void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target){
        super.onFinger(sourceNick, sourceLogin, sourceHostname, target);
        bot.event(new FingerEvent(this, sourceNick, target, sourceHostname, sourceLogin));
    }

    protected void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target){
        super.onTime(sourceNick, sourceLogin, sourceHostname, target);
        bot.event(new TimeEvent(this, sourceNick, target, sourceHostname, sourceLogin));
    }

    protected void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target){
        super.onVersion(sourceNick, sourceLogin, sourceHostname, target);
        bot.event(new VersionEvent(this, sourceNick, target, sourceHostname, sourceLogin));
    }

    protected void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice){
        super.onNotice(sourceNick, sourceLogin, sourceHostname, target, notice);
        bot.event(new NoticeEvent(this, sourceNick, target, notice, sourceHostname, sourceLogin));
    }

    protected void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue){
        super.onPing(sourceNick, sourceLogin, sourceHostname, target, pingValue);
        bot.event(new PingEvent(this, sourceNick, target, pingValue, sourceHostname, sourceLogin));
    }
    
    protected void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel){
        super.onInvite(targetNick, sourceNick, sourceLogin, sourceHostname, channel);
        bot.event(new InviteEvent(this, sourceNick, targetNick, channel, sourceHostname, sourceLogin));
    }
    
    protected void onSetInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname){
        super.onSetInviteOnly(channel, sourceNick, sourceLogin, sourceHostname);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_INVITE_ONLY, sourceNick, sourceHostname, sourceLogin));
    }

    protected void onRemoveInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname){
        super.onRemoveInviteOnly(channel, sourceNick, sourceLogin, sourceHostname);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_INVITE_ONLY, sourceNick, sourceHostname, sourceLogin));
    }

    protected void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname){
        super.onSetModerated(channel, sourceNick, sourceLogin, sourceHostname);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_MODERATED, sourceNick, sourceHostname, sourceLogin));
    }

    protected void onRemoveModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname){
        super.onRemoveModerated(channel, sourceNick, sourceLogin, sourceHostname);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_MODERATED, sourceNick, sourceHostname, sourceLogin));
    }

    protected void onSetNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname){
        super.onSetNoExternalMessages(channel, sourceNick, sourceLogin, sourceHostname);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_NO_EXTERNAL_MESSAGES, sourceNick, sourceHostname, sourceLogin));
    }

    protected void onRemoveNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname){
        super.onRemoveNoExternalMessages(channel, sourceNick, sourceLogin, sourceHostname);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_NO_EXTERNAL_MESSAGES, sourceNick, sourceHostname, sourceLogin));
    }

    protected void onSetPrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname){
        super.onSetPrivate(channel, sourceNick, sourceLogin, sourceHostname);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_PRIVATE, sourceNick, sourceHostname, sourceLogin));
    }

    protected void onRemovePrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname){
        super.onRemovePrivate(channel, sourceNick, sourceLogin, sourceHostname);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_PRIVATE, sourceNick, sourceHostname, sourceLogin));
    }

    protected void onSetSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname){
        super.onSetSecret(channel, sourceNick, sourceLogin, sourceHostname);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_SECRET, sourceNick, sourceHostname, sourceLogin));
    }

    protected void onRemoveSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname){
        super.onRemoveSecret(channel, sourceNick, sourceLogin, sourceHostname);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_SECRET, sourceNick, sourceHostname, sourceLogin));
    }

    protected void onSetTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname){
        super.onSetTopicProtection(channel, sourceNick, sourceLogin, sourceHostname);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_TOPIC_PROTECTION, sourceNick, sourceHostname, sourceLogin));
    }

    protected void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname){
        super.onRemoveTopicProtection(channel, sourceNick, sourceLogin, sourceHostname);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_TOPIC_PROTECTION, sourceNick, sourceHostname, sourceLogin));
    }

    protected void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient){
        super.onVoice(channel, sourceNick, sourceLogin, sourceHostname, recipient);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_VOICE, recipient, sourceNick, sourceHostname, sourceLogin));
    }

    protected void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient){
        super.onDeVoice(channel, sourceNick, sourceLogin, sourceHostname, recipient);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_VOICE, recipient, sourceNick, sourceHostname, sourceLogin));
    }

    protected void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask){
        super.onSetChannelBan(channel, sourceNick, sourceLogin, sourceHostname, hostmask);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_BAN, hostmask, sourceNick, sourceHostname, sourceLogin));
    }

    protected void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask){
        super.onRemoveChannelBan(channel, sourceNick, sourceLogin, sourceHostname, hostmask);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_BAN, hostmask, sourceNick, sourceHostname, sourceLogin));
    }

    protected void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key){
        super.onSetChannelKey(channel, sourceNick, sourceLogin, sourceHostname, key);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_KEY, key, sourceNick, sourceHostname, sourceLogin));
    }

    protected void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key){
        super.onRemoveChannelKey(channel, sourceNick, sourceLogin, sourceHostname, key);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_KEY, key, sourceNick, sourceHostname, sourceLogin));
    }

    protected void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit){
        super.onSetChannelLimit(channel, sourceNick, sourceLogin, sourceHostname, limit);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_SET_LIMIT, limit+"", sourceNick, sourceHostname, sourceLogin));
    }

    protected void onRemoveChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname){
        super.onRemoveChannelLimit(channel, sourceNick, sourceLogin, sourceHostname);
        bot.event(new ChannelChangeEvent(this, channel, ChannelChangeEvent.TYPE_REMOVE_LIMIT, sourceNick, sourceHostname, sourceLogin));
    }

    protected void onServerResponse(int code, String response){
        super.onServerResponse(code, response);
        bot.event(new ServerResponseEvent(this, code, response));
    }

    protected void onUnknown(String line){
        super.onUnknown(line);
        bot.event(new UnknownEvent(this, line));
    }
}