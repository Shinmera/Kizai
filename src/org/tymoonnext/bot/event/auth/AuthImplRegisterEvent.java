package org.tymoonnext.bot.event.auth;

import org.tymoonnext.bot.event.Event;
import org.tymoonnext.bot.module.auth.AuthImplementor;
import org.tymoonnext.bot.stream.Stream;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class AuthImplRegisterEvent extends Event{
    private AuthImplementor impl;
    private Class<? extends Stream> streamType;
    
    public AuthImplRegisterEvent(Stream origin, AuthImplementor impl){this(origin, impl, Stream.class);}
    public AuthImplRegisterEvent(Stream origin, AuthImplementor impl, Class<? extends Stream> streamType){
        super(origin);
        this.impl=impl;
        this.streamType=streamType;
    }
    
    public AuthImplementor getAuthImplementor(){return impl;}
    public Class<? extends Stream> getStreamType(){return streamType;}
}
