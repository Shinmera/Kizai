package org.tymoonnext.bot.module.athenaeum;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public interface ModifiableSource extends Source{    
    /**
     * Modify existing records of a volume of this source. If the offset exceeds
     * the existing amount of records or is -1, add the records. 
     * 
     * @param volume The volume to modify records of. If this volume does not
     * exist, it should be created automatically.
     * @param from A result offset start or -1.
     * @param to A result offset end or -1.
     * @param data An array of records to modify.
     * @param user The user issuing the request.
     * @return A single result describing the overall status after the insert.
     * @throws SourceException In case a source-side error occurs.
     */
    public Result modify(String volume, int from, int to, String[] data, String user) throws SourceException;
    
    /**
     * Remove existing records or an entire volume of this source. If the offset
     * exceeds the existing amount of  records, ignore them.
     * 
     * @param volume The volume to remove records from. If this volume does not
     * exist, an InexistentVolumeException should be thrown.
     * @param from A result offset start.
     * @param to A result offset end.
     * @param user The user issuing the request.
     * @return A single result describing the overall status after the insert.
     * @throws SourceException In case a source-side error occurs.
     * @throws InexistentVolumeException In case the requested volume does not
     * exist.
     */
    public Result remove(String volume, int from, int to, String user) throws SourceException, InexistentVolumeException;
}
