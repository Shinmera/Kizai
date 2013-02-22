package org.tymoonnext.bot;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.tymoonnext.bot.module.Module;

/**
 * A custom class loader that is capable of reloading certain classes from disk.
 * This is tuned to only allow reloading of classes that are within the 
 * Commons.MODULE_PACKAGE package and within the Commons.f_MODULES folder.
 * @author Shinmera
 * @license GPLv3
 * @version 1.0.1
 */
public class ReloadingCapableClassLoader extends ClassLoader{
    
    public ReloadingCapableClassLoader(){
        super(ReloadingCapableClassLoader.getSystemClassLoader());
    }
    
    @Override
    public Class<?> loadClass(String s) throws ClassNotFoundException {
        return findClass(s);
    }

    @Override
    public Class<?> findClass(String s) throws ClassNotFoundException {
        try {
            byte[] bytes = loadClassData(s);
            return defineClass(s, bytes, 0, bytes.length);
        } catch (IOException ex) {
            Commons.log.info(toString()+" Loading "+s+" through super.");
            return super.loadClass(s);
        }
    }

    private byte[] loadClassData(String className) throws IOException {
        //We don't handle anything other than modules.
        if(!className.startsWith(Commons.MODULE_PACKAGE))throw new IOException();
        //This is to avoid reloading the superclass. Reloading it would result in a desynchronized environment and thus in ClassCastExceptions.
        if( className.equals(Module.class.getName())) throw new IOException();
        
        File f = new File(Commons.f_MODULES, className.substring(Commons.MODULE_PACKAGE.length()).replaceAll("\\.", File.pathSeparator) +".class");
        byte buff[] = new byte[(int) f.length()];
        DataInputStream dis = new DataInputStream(new FileInputStream(f));
        dis.readFully(buff);
        dis.close();
        return buff;
    }
    
    public String toString(){return "+"+this.getClass().getSimpleName()+"+";}
}
