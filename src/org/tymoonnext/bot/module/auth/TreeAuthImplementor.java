package org.tymoonnext.bot.module.auth;

import NexT.data.DObject;
import NexT.data.DParse;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.auth.AuthEvent;
import org.tymoonnext.bot.event.auth.UserRetrieveEvent;
import org.tymoonnext.bot.event.auth.UserVerifyEvent;
import org.tymoonnext.bot.module.Module;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class TreeAuthImplementor extends Module implements EventListener{
    private User any;
    private boolean checkedAny = false;
    
    public TreeAuthImplementor(Kizai bot){
        super(bot);
        try{bot.bindEvent(AuthEvent.class, this, "onAuth");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(AuthEvent.class, this, "onAuthFinal",Integer.MIN_VALUE);}catch(NoSuchMethodException ex){}
    }
    
    public void onAuth(AuthEvent evt){
        String checkbranch = evt.getStream().getClass().getSimpleName()+"."+
                             evt.getCommand().getChannel()+"."+
                             evt.getCommand().getCommand();
        checkbranch = checkbranch.trim().toLowerCase();
        
        //Get the User object from wherever
        UserRetrieveEvent retrUser = new UserRetrieveEvent(evt.getStream(), evt.getCommand().getUser());
        bot.event(retrUser);
        User u = retrUser.getUser();
        if(u == null)return;
        
        //Perform check
        if(u.getConfig().contains("tree")){
            DObject config = u.getConfig().get("tree");
            if(config.contains("perms")){
                String[] permtree = config.get("perms").toString().split("\n");
                if(checkBranch(permtree, checkbranch)){
                    Commons.log.info(toString()+" Granting permission for "+checkbranch);
                    evt.setGranted(true);
                }else{
                    Commons.log.info(toString()+" Denying permission for "+checkbranch);
                }
            }
        }
    }
    
    public void onAuthFinal(AuthEvent evt){
        if(any == null && !checkedAny){
            UserRetrieveEvent retrUser = new UserRetrieveEvent(evt.getStream(), "any");
            bot.event(retrUser);
            any = retrUser.getUser();
            checkedAny = true;
        }
        
        String checkbranch = evt.getStream().getClass().getSimpleName()+"."+
                             evt.getCommand().getChannel()+"."+
                             evt.getCommand().getCommand();
        checkbranch = checkbranch.trim().toLowerCase();
        
        //Perform check on any (global) user
        if(any != null){
            if(any.getConfig().contains("tree")){
                DObject config = any.getConfig().get("tree");
                if(config.contains("perms")){
                    String[] permtree = config.get("perms").toString().split("\n");
                    if(checkBranch(permtree, checkbranch)){
                        Commons.log.info(toString()+" Granting permission for "+checkbranch+" (any)");
                        evt.setGranted(true);
                    }
                }
            }
        }
    }
    
    public boolean checkBranch(String tree[], String branch){
        String[] leaves = branch.trim().toLowerCase().split("\\.");
        
        for(String _branch : tree){
            String[] _leaves = _branch.trim().toLowerCase().split("\\.");
            
            for(int i=0;i<leaves.length;i++){
                if(leaves[i].equals("*")) return true;          //Check-branch allows anything here, matches.
                if(_leaves[i].equals("*")){
                                                                //Tree-branch allows anything here, skip leaf.
                }else{
                    if(!leaves[i].equals(_leaves[i])) break;    //Leaf mismatch, leave this branch.
                }
                if(_leaves.length-1 == i) return true;          //Tree-branch reached endpoint, ignore further check-branch leaves.
            }
        }
        return false;
    }

    @Override
    public void shutdown() {
        bot.unbindAllEvents(this);
    }
}
