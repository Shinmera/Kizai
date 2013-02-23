package org.tymoonnext.bot.module.irc;

import NexT.data.DObject;
import java.util.HashMap;
import java.util.HashSet;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.IRCBot.DisconnectEvent;
import org.tymoonnext.bot.event.IRCBot.JoinEvent;
import org.tymoonnext.bot.event.IRCBot.KickEvent;
import org.tymoonnext.bot.event.IRCBot.NoticeEvent;
import org.tymoonnext.bot.event.IRCBot.PartEvent;
import org.tymoonnext.bot.event.IRCBot.QuitEvent;
import org.tymoonnext.bot.event.auth.UserRegisterEvent;
import org.tymoonnext.bot.event.auth.UserRetrieveEvent;
import org.tymoonnext.bot.event.auth.UserVerifyEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.auth.SessionImplementor;
import org.tymoonnext.bot.module.auth.User;
import org.tymoonnext.bot.stream.Stream;

/**
 * Session implementation for IRC NickServ.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class NickServSessionImplementor extends SessionImplementor implements CommandListener{
    private HashMap<String, User> pendingStatus;
    private HashSet<User> identified;
    
    public NickServSessionImplementor(Kizai bot){
        super(bot);
        pendingStatus = new HashMap<String, User>();
        identified = new HashSet<User>();
        
        try{bot.bindEvent(NoticeEvent.class, this, "onNotice");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(PartEvent.class, this, "onPart");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(QuitEvent.class, this, "onQuit");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(KickEvent.class, this, "onKick");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(JoinEvent.class, this, "onJoin");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(DisconnectEvent.class, this, "onDisconnect");}catch(NoSuchMethodException ex){}
        bot.registerCommand("ident", this);
    }
    
    public void onNotice(NoticeEvent evt){
        if(evt.sender.equalsIgnoreCase("nickserv")){
            if(evt.notice.startsWith("STATUS ")){
                String[] parts = evt.notice.split(" ");
                if(pendingStatus.containsKey(parts[1]) && parts[2].equals("3")){
                    Commons.log.info(toString()+" Received STATUS 3 command from NickServ, identified.");
                    
                    User u = pendingStatus.get(parts[1]);
                    identifyUser(u);
                    pendingStatus.remove(parts[1]);
                    
                    if(!u.isLoggedIn()){
                        u.activateSession();
                        evt.getStream().send("You are now identified.", u.getName());
                    }
                }else{
                    Commons.log.info(toString()+" Received STATUS "+parts[2]+" for "+parts[1]+", ignoring.");
                }
            }
        }
    }
    
    public void onPart(PartEvent evt){unidentifyUser(getUser(evt.sender));}
    public void onQuit(QuitEvent evt){unidentifyUser(getUser(evt.sender));}
    public void onKick(KickEvent evt){unidentifyUser(getUser(evt.recipient));}
    public void onJoin(JoinEvent evt){requestIdent(evt.getStream(),getUser(evt.sender));}
    public void onDisconnect(DisconnectEvent evt){unidentifyAll();}
    
    private User getUser(String user){
        UserRetrieveEvent evt = new UserRetrieveEvent(user);
        bot.event(evt);
        return evt.getUser();
    }
    
    private void initUser(User user){
        if(!user.getConfig().contains("nickserv")){
            user.getConfig().set("nickserv", new DObject());
            user.getConfig().get("nickserv").set("identified", Boolean.FALSE);
        }
    }
    
    private void identifyUser(User user){
        initUser(user);
        user.getConfig().get("nickserv").set("identified", Boolean.TRUE);
        identified.add(user);
    }
    
    private void unidentifyUser(User user){
        initUser(user);
        user.getConfig().get("nickserv").set("identified", Boolean.FALSE);
        identified.remove(user);
    }
    
    private void unidentifyAll(){
        for(User u : identified){
            unidentifyUser(u);
        }
    }
    
    private void requestIdent(Stream stream, User user){
        if(user == null)return;
        pendingStatus.put(user.getName(), user);
        stream.send("STATUS "+user.getName(), "NickServ");
    }

    @Override
    public void onUserVerify(UserVerifyEvent evt){
        if(evt.getStream().getClass() == IRC.class){
            User user = evt.getUser();
            initUser(user);
            
            requestIdent(evt.getStream(), user);
            
            if((Boolean)user.getConfig().get("nickserv").get("identified").get()){
                user.activateSession();
            }
        }
    }

    public void onCommand(CommandEvent evt){
        User user = getUser(evt.getUser());
        if(user == null){
            evt.getStream().send("[Auth] Creating new profile...", evt.getChannel());
            DObject conf = new DObject();
            conf.set("name", evt.getUser());
            user = new User(conf);
            bot.event(new UserRegisterEvent(evt.getStream(), user));
        }
        requestIdent(evt.getStream(), user);
    }
    
    public void shutdown(){
        super.shutdown();
        unidentifyAll();
    }

}
