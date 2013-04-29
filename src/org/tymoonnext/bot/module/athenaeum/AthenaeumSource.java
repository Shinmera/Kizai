package org.tymoonnext.bot.module.athenaeum;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class AthenaeumSource implements ModifiableSource{

    @Override
    public Result modify(String volume, int from, int to, String[] data, String user) throws SourceException{
        return null;
    }

    @Override
    public Result remove(String volume, int from, int to, String user) throws SourceException, InexistentVolumeException{
        return null;
    }

    @Override
    public ResultSet search(String query, int from, int to, String user) throws SourceException{
        return null;
    }

    @Override
    public ResultSet get(String volume, int from, int to, String user) throws SourceException, InexistentVolumeException{
        return null;
    }

    @Override
    public String getName(){return "athenaeum";}

}
