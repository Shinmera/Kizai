package org.tymoonnext.bot.module.clhs;

import NexT.Commons;
import NexT.db.mongo.DataModel;
import NexT.db.mongo.MongoException;
import NexT.util.StringUtils;
import NexT.util.Toolkit;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Indexer {
    public static final String CLHS_SYMBOL_INDEX = "http://www.lispworks.com/documentation/HyperSpec/Front/X_AllSym.htm";
    public static final String CLHS_BASE_URL = "http://www.lispworks.com/documentation/HyperSpec/";
    
    public static void buildSymbolIndex(){
        try {
            String index = Toolkit.downloadFileToString(new URL(CLHS_SYMBOL_INDEX));
            String list = StringUtils.inBetween(index, "<UL>", "</UL>").trim();
            String[] rawList = list.split("\n");
            
            for(int i=0;i<rawList.length;i++){
                String line = rawList[i].trim();
                if(!line.isEmpty()){
                    try{
                        DataModel model = DataModel.getHull("clhs");
                        String href = StringUtils.inBetween(line, "HREF=\"", "\">");
                        String name = StringUtils.inBetween(line, "<B>", "</B>");

                        name = name.replaceAll("&amp;", "&")
                                        .replaceAll("&gt;", ">")
                                        .replaceAll("&lt;", "<");
                        href = CLHS_BASE_URL + href.substring(2);
                        
                        Commons.log.info("[CLHS][Indexer] Building index for "+name);
                        model.set("symbol", name);
                        model.set("link", href);
                        buildPageIndex(model);
                        model.insert();
                    }catch(MongoException ex){
                        Commons.log.log(Level.WARNING, "[CLHS][Indexer] Failed to insert symbol record!", ex);
                    }
                }
            }
        } catch (MalformedURLException ex) {
            Commons.log.log(Level.SEVERE, "[CLHS][Indexer] WTF?", ex);
        }
    }
    
    public static void buildPageIndex(DataModel model){
        //magic
    }
}
