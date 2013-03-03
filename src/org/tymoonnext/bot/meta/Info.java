package org.tymoonnext.bot.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description annotation to add more information about a class that can be 
 * retrieved by the bot at runtime.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Info{
    String value();
}