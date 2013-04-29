package org.tymoonnext.bot.module.athenaeum;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public interface Source {
    /**
     * Query this source for a list of matchable pages. The query string contains
     * asterisks that should stand for zero or more of any character.
     * 
     * @param query The query string.
     * @param from A result offset start.
     * @param to A result offset end.
     * @param user The username issuing the request.
     * @return A result set.
     * @throws SourceException In case a source-side error occurs.
     */
    public ResultSet search(String query, int from, int to, String user) throws SourceException;
    
    /**
     * Query this source for a certain topic. The query string matches a specific
     * term, and is always in lowercase. One result per page is expected.
     * 
     * @param volume The volume to get information from.
     * @param from A result offset start.
     * @param to A result offset end.
     * @param user The username issuing the request.
     * @return A result set.
     * @throws SourceException In case a source-side error occurs.
     * @throws InexistentVolumeException In case the requested volume does not
     * exist.
     */
    public ResultSet get(String volume, int from, int to, String user) throws SourceException, InexistentVolumeException;
    
    /**
     * Returns the Source's name representation that is used to address it from
     * a command.
     * @return 
     */
    public String getName();
}
