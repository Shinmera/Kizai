package org.tymoonnext.bot;

import NexT.err.NLogger;
import NexT.util.Toolkit;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.tymoonnext.bot.stream.StdOut;
import org.tymoonnext.bot.stream.Stream;

/**
 * Commons class for global information.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Commons {
    public static final String VERSION = "2.7.6";
    public static final String FQDN = "機材";
    public static final String LICENSE = "GPLv3";
    public static final String COREDEV = "TymoonNET/NexT";
    public static final String WEBSITE = "http://tymoon.eu";
    
    public static final File f_BASEDIR = new File(".");
    public static final File f_MODULES = new File(f_BASEDIR, "modules");
    public static final File f_CONFIG = new File(f_BASEDIR, "bot.cfg");
    public static final File f_CONFIGDIR = new File(f_BASEDIR, "config");
    
    public static final Level LOGLEVEL = Level.INFO;
    public static final Level LOGLEVEL_FILE = Level.FINEST;
    
    public static final Logger log = NLogger.get("Kizai", LOGLEVEL, LOGLEVEL_FILE);
    public static final Stream stdout = new StdOut();
    public static final Reflections reflections;
    private static final List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
    
    public static final String MODULE_PACKAGE = "org.tymoonnext.bot.module.";
    public static final String EVENT_PACKAGE = "org.tymoonnext.bot.event.";
    public static final long STARTUP_TIME = System.currentTimeMillis();
    
    public static MessageDigest md;
    
    static{
        NexT.Commons.log = log;
        try{ md = MessageDigest.getInstance("SHA-512");
            log.finer("+COMMONS+ Choosing SHA-512 as hashing algorithm.");
        }catch(NoSuchAlgorithmException ex){
            log.log(Level.WARNING, "+COMMONS+ Cannot find SHA-512 algorithm!");
            try{ md = MessageDigest.getInstance("SHA-1");
                log.finer("+COMMONS+ Choosing SHA-1 as hashing algorithm.");
            }catch(NoSuchAlgorithmException ex2){
                log.log(Level.WARNING, "+COMMONS+ Cannot find SHA-1 algorithm!");
                try{ md = MessageDigest.getInstance("SHA");
                    log.finer("+COMMONS+ Choosing SHA as hashing algorithm.");
                }catch(NoSuchAlgorithmException ex3){
                    log.log(Level.WARNING, "+COMMONS+ Cannot find SHA algorithm!");
                    try{ md = MessageDigest.getInstance("MD5");
                        log.finer("+COMMONS+ Choosing MD5 as hashing algorithm.");
                    }catch(NoSuchAlgorithmException ex4){
                        log.log(Level.WARNING, "+COMMONS+ Cannot find MD5 algorithm!");
                        log.log(Level.WARNING, "+COMMONS+ Hashing function will always return input string!!");
                    }
                }
            }
        }
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());
        reflections = getPackageReflections(Commons.class.getPackage().getName());
    }
    
    public static Reflections getPackageReflections(String pkg){
        return new Reflections(new ConfigurationBuilder()
            .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
            .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
            .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(pkg))));
    }
    
    /**
     * Wrapper to invoke any kind of public method on an Object given the
     * specific arguments. This method returns the invoked function's result
     * or null if no value was returned or if the call failed. This way of
     * invoking functions is dangerous as all exceptions are caught and thus
     * might screw up the program flow. Only use this if you absolutely do not
     * care if the function actually works or not or if you're absolutely sure
     * it will always work.
     * 
     * @param <T> The Object type to expect on return.
     * @param o The Object to invoke the method on.
     * @param func The method name.
     * @param arg An optional list of arguments to pass.
     * @return The return value of the method or null on failure.
     */
    public static <T> T invoke(Object o, String func, Object... arg){
        log.fine("+COMMONS+ Invoking '"+func+"' on '"+o+"' with args: "+Toolkit.implode(arg, ", "));
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
     * Wrapper to retrieve any kind of field on an object. This method returns
     * the requested field's value, or null if isn't set or failed to retrieve
     * the value. This way of retriving fields is dangerous as all exceptions
     * are caught and thus the program might not return what you expected.
     * Only use this if you are absolutely sure that the value you're requesting
     * is available.
     * 
     * @param <T> The Object type to expect on return.
     * @param o The Object to retrieve the value from.
     * @param var The field name.
     * @return The value of the field or null on failure.
     */
    public static <T> T retrieve(Object o, String var){
        log.fine("+COMMONS+ Retrieving object '"+var+"' on '"+o+"'");
        try{
            Field f = o.getClass().getDeclaredField(var);
            f.setAccessible(true);
                return (T) f.get(o);
        }catch(IllegalArgumentException ex){
            log.log(Level.WARNING, "+COMMONS+ Retrieval of '"+var+"' on '"+o+"' failed.", ex);
        }catch(IllegalAccessException ex){
            log.log(Level.WARNING, "+COMMONS+ Retrieval of '"+var+"' on '"+o+"' failed.", ex);
        }catch(NoSuchFieldException ex){
            log.log(Level.WARNING, "+COMMONS+ Retrieval of '"+var+"' on '"+o+"' failed.", ex);
        }catch(SecurityException ex){
            log.log(Level.WARNING, "+COMMONS+ Retrieval of '"+var+"' on '"+o+"' failed.", ex);
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
        log.finer("+COMMONS+ Hashing '"+in+"' to '"+out.toString()+"'");
        return out.toString();
    }
    
    public static String getUUID(){
        String uid = UUID.randomUUID().toString();
        log.finer("+COMMONS+ Generating UUID: "+uid);
        return uid;
    }
    
    public static String getVersionString(){
        return Commons.FQDN+" v"+Commons.VERSION+" ("+Commons.LICENSE+") by "+Commons.COREDEV+" "+Commons.WEBSITE;
    }
}
