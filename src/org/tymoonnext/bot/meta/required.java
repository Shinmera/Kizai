package org.tymoonnext.bot.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Required annotation used for the automatic ConfigLoader. If this annotation
 * is set and the value cannot be found in the configuration block, the
 * configurator will throw an exception 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface required{}