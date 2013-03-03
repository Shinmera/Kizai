package org.tymoonnext.bot.module.lookup;

/**
 * Implemented by classes that can look up a term in a database to provide a definition.
 * @author Mithent
 */
public interface LookupProviderInterface {
    public String getDefinition(String term);
}
