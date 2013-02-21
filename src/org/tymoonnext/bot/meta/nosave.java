package org.tymoonnext.bot.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Nosave annotation used for the automatic ConfigLoader. If this annotation is
 * set, the value will not be saved to the config.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface nosave{}