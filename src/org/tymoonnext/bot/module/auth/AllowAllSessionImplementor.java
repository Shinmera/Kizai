package org.tymoonnext.bot.module.auth;

import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.auth.UserVerifyEvent;
import org.tymoonnext.bot.stream.Stream;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */

public class AllowAllSessionImplementor extends SessionImplementor{
    private Stream s;
    
    public AllowAllSessionImplementor(Kizai bot, Stream s){
        super(bot);
        this.s=s;
    }

    public void onUserVerify(UserVerifyEvent evt) {
        if(evt.getStream() == s)evt.getUser().activateSession();
    }

}
