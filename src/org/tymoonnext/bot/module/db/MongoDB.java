package org.tymoonnext.bot.module.db;

import NexT.db.DBException;
import NexT.db.mongo.MongoWrapper;
import java.io.File;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;


/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class MongoDB extends Database{
    public static final File CONFIG = new File(Commons.f_CONFIGDIR, "mongo.conf");
    private MongoWrapper wrapper;
    
    public MongoDB(Kizai bot) throws DBException{
        super(bot, CONFIG);
        wrapper = new MongoWrapper(conf.user, conf.pw, conf.db, conf.host, conf.port);
    }

    public void shutdown(){
        wrapper.close();
    }

}
