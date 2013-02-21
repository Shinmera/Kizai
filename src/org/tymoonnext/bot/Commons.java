package org.tymoonnext.bot;

import NexT.err.NLogger;
import NexT.util.Toolkit;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.logging.Level;
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
    public static final String VERSION = "2.1.3";
    public static final String FQDN = "機材";
    public static final String LICENSE = "GPLv3";
    public static final String COREDEV = "TymoonNET/NexT";
    public static final String WEBSITE = "http://tymoon.eu";
    
    public static final File f_BASEDIR = new File(".");
    public static final File f_MODULES = new File(f_BASEDIR, "modules");
    public static final File f_CONFIG = new File(f_BASEDIR, "bot.cfg");
    public static final File f_CONFIGDIR = new File(f_BASEDIR, "config");
    
    public static final Logger log = NLogger.get("kizai");
    public static final Stream stdout = new StdOut();
    
    public static final String MODULE_PACKAGE = "org.tymoonnext.bot.module.";
    public static final long STARTUP_TIME = System.currentTimeMillis();
    
    public static MessageDigest md;
    
    static{
        try{ md = MessageDigest.getInstance("SHA-512");
        }catch(NoSuchAlgorithmException ex){
            log.log(Level.WARNING, "+COMMONS+ Cannot find SHA-512 algorithm!");
            try{ md = MessageDigest.getInstance("SHA-1");
            }catch(NoSuchAlgorithmException ex2){
                log.log(Level.WARNING, "+COMMONS+ Cannot find SHA-1 algorithm!");
                try{ md = MessageDigest.getInstance("SHA");
                }catch(NoSuchAlgorithmException ex3){
                    log.log(Level.WARNING, "+COMMONS+ Cannot find SHA algorithm!");
                    try{ md = MessageDigest.getInstance("MD5");
                    }catch(NoSuchAlgorithmException ex4){
                        log.log(Level.WARNING, "+COMMONS+ Cannot find MD5 algorithm!");
                        log.log(Level.WARNING, "+COMMONS+ Hashing function will always return input string!!");
                    }
                }
            }
        }
    }
    
    /**
     * Wrapper to invoke any kind of public method on an Object given the
     * specific arguments. 
     * 
     * @param <T> The Object type to expect on return.
     * @param o The Object to invoke the method on.
     * @param func The method name.
     * @param arg An optional list of arguments to pass.
     * @return The return value of the method or null on fail.
     */
    public static <T> T invoke(Object o, String func, Object... arg){
        Class[] classes = null;
        if(arg != null){
            classes = new Class[arg.length];
            for(int i=0;i<arg.length;i++){
                classes[i] = arg[i].getClass();
            }
        }
        
        try {
            Method m = o.getClass().getMethod(func, classes);
            return (T) m.invoke(o, arg);
        } catch (IllegalAccessException ex) {
            log.log(Level.WARNING, "+COMMONS+ Invocation of '"+func+"' on '"+o+"' with '"+Toolkit.implode(arg, ",")+"' failed.", ex);
        } catch (IllegalArgumentException ex) {
            log.log(Level.WARNING, "+COMMONS+ Invocation of '"+func+"' on '"+o+"' with '"+Toolkit.implode(arg, ",")+"' failed.", ex);
        } catch (InvocationTargetException ex) {
            log.log(Level.WARNING, "+COMMONS+ Invocation of '"+func+"' on '"+o+"' with '"+Toolkit.implode(arg, ",")+"' failed.", ex);
        } catch (NoSuchMethodException ex) {
            log.log(Level.WARNING, "+COMMONS+ Invocation of '"+func+"' on '"+o+"' with '"+Toolkit.implode(arg, ",")+"' failed.", ex);
        } catch (SecurityException ex) {
            log.log(Level.WARNING, "+COMMONS+ Invocation of '"+func+"' on '"+o+"' with '"+Toolkit.implode(arg, ",")+"' failed.", ex);
        }
        return null;
    }
    
    /**
     * Converts the given string into a hash. The algorithm used depends on the
     * availability of algorithms: SHA-512 SHA-1 SHA MD5 PLAIN
     * Note that if no hashing algorithms could be loaded for your system, the
     * input will be returned in plain format. Pay attention to the init
     * sequence to see if the hashing algorithm failed to load.
     * @param in The string to hash.
     * @return The hashed value.
     */
    public static String hash(String in){
        if(md == null) return in;
        
        md.update(in.getBytes());
        byte[] mb = md.digest();
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < mb.length; i++) {
            byte temp = mb[i];
            String s = Integer.toHexString(new Byte(temp));
            while (s.length() < 2) {
                s = "0" + s;
            }
            s = s.substring(s.length() - 2);
            out.append(s);
        }
        return out.toString();
    }
    
    public static String getUUID(){
        return UUID.randomUUID().toString();
    }
}
