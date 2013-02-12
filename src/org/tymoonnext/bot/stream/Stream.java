package org.tymoonnext.bot.stream;

/**
 * Stream interface
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public interface Stream {
    public void send(String msg, String dst);
    public void close();
}
