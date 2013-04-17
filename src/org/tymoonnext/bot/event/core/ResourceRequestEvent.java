package org.tymoonnext.bot.event.core;

import NexT.data.DObject;
import org.tymoonnext.bot.event.Event;
import org.tymoonnext.bot.meta.Arguments;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.stream.Stream;


/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class ResourceRequestEvent<Type extends Object> extends Event{
    private String resourceIdentifier;
    private DObject args;
    private Type object;
    private Module objOrigin;
    
    @Arguments({"origin", "resource"})
    public ResourceRequestEvent(Stream origin, String resource){this(origin, resource, null);}
    
    @Arguments({"origin", "resource", "args"})
    public ResourceRequestEvent(Stream origin, String resource, DObject args){
        super(origin);
        this.resourceIdentifier = resource;
        this.args = args;
    }
    
    public String getIdent(){return resourceIdentifier;}
    public DObject getArgs(){return args;}
    public DObject getArg(String key){return (args == null)? null : args.get(key);}
    public Type getObject(){return object;}
    public void setObject(Type t, Module origin){object=t;objOrigin=origin;}
    
    public String toString(){
        return "<"+getClass().getSimpleName()+"|"+resourceIdentifier+">";
    }
}
