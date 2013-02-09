package org.tymoonnext.bot;

import NexT.err.NLogger;
import java.io.File;
import java.util.logging.Logger;
import org.tymoonnext.bot.stream.StdOut;
import org.tymoonnext.bot.stream.Stream;

/**
 * Commons class for global information.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Commons {
    public static final Logger log = NLogger.get("kizai");
    public static final String VERSION = "2.1.3";
    public static final String FQDN = "機材";
    public static final String LICENSE = "GPLv3";
    public static final String COREDEV = "TymoonNET/NexT";
    public static final String WEBSITE = "http://tymoon.eu";
    
    public static final File f_BASEDIR = new File(".");
    public static final File f_MODULES = new File(f_BASEDIR, "modules");
    public static final File f_CONFIG = new File(f_BASEDIR, "bot.cfg");
    
    public static final Stream stdout = new StdOut();
    
    public static final String MODULE_PACKAGE="org.tymoonnext.bot.module.";
}
