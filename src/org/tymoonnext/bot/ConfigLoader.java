package org.tymoonnext.bot;

import NexT.data.DObject;
import java.lang.reflect.Field;
import java.util.logging.Level;
import org.tymoonnext.bot.Commons;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public abstract class ConfigLoader {
    
    public ConfigLoader(){}
    
    public ConfigLoader(DObject configblock){
        this();
        load(configblock);
    }
    
    public void load(DObject configblock){
        Class c = this.getClass();
        Field[] fields = c.getFields();
        for(Field field : fields){
            field.setAccessible(true);
            if(configblock.contains(field.getName())){
                try{
                    field.set(this, configblock.get(field.getName()).get());
                }catch(IllegalAccessException ex){
                    Commons.log.log(Level.WARNING, toString()+" Failed to set field "+field.getName()+" to '"+configblock.get(field.getName()).get()+"'");
                }
            }else{
                try{
                    if(field.get(this) == null)
                        Commons.log.log(Level.WARNING, toString()+" Field "+field.getName()+" has no value!");
                }catch(IllegalAccessException ex){
                    Commons.log.log(Level.WARNING, toString()+" Failed to check field "+field.getName());
                }
            }
        }
    }
    
    public void save(DObject configblock){
        Class c = this.getClass();
        Field[] fields = c.getFields();
        for(Field field : fields){
            field.setAccessible(true);
            try{
                configblock.set(field.getName(), field.get(this));
            }catch(IllegalAccessException ex){
                Commons.log.log(Level.WARNING, toString()+" Failed to get field "+field.getName());
            }
        }
    }
    
    public String toString(){
        return "-ConfigLoader-";
    }
}
