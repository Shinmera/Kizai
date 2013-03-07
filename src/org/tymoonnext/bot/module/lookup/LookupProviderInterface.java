package org.tymoonnext.bot.module.lookup;
import org.tymoonnext.bot.module.cmd.ParseException;

/**
 * Implemented by classes that can look up a term in a database to provide a definition.
 * @author Mithent
 */
public interface LookupProviderInterface {
    public String getDefinition(String term) throws ConnectionException, ParseException;
}
