package org.tymoonnext.bot.module.cmd;

import NexT.util.StringUtils;
import NexT.util.Toolkit;


/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public abstract class ArgumentChecker {
    public static final ArgumentChecker ANY = new ArgumentChecker(){
        public boolean isValid(String args){return true;}
    };
    public static final ArgumentChecker NONE = new ArgumentChecker(){
        public boolean isValid(String args){return false;}
    };
    public static final ArgumentChecker NUMERIC = new ArgumentChecker(){
        public boolean isValid(String args){return Toolkit.isNumeric(args);}
    };
    public static final ArgumentChecker INTEGER = new ArgumentChecker(){
        public boolean isValid(String args){
            try{Integer.parseInt(args);return true;}
            catch(Exception ex){return false;}
        }
    };
    public static final ArgumentChecker ALPHA = new ArgumentChecker(){
        public boolean isValid(String args){
            return args.equals(StringUtils.sanitizeString(args, "a-zA-Z0-9-_"));
        }
    };
    
    public abstract boolean isValid(String args);
}
