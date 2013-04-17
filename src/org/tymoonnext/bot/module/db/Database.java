package org.tymoonnext.bot.module.db;

import NexT.data.ConfigLoader;
import NexT.data.DParse;
import NexT.data.required;
import java.io.File;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.module.Module;


/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public abstract class Database extends Module{
    
    protected class C extends ConfigLoader{
        @required public String db;
        @required public int port;
        public String user, pw, host="localhost";
    };
    protected C conf = new C();
    
    public Database(Kizai bot, File config){
        super(bot);
        conf.load(DParse.parse(config));
    }

    public abstract void shutdown();
    
    public String getHost(){return conf.host;}
    public String getDBName(){return conf.db;}
    public String getUser(){return conf.user;}
    public String getPass(){return conf.pw;}
    public int getPort(){return conf.port;}
}
