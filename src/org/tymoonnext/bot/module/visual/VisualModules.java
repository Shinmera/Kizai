package org.tymoonnext.bot.module.visual;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.core.ModuleLoadEvent;
import org.tymoonnext.bot.event.core.ModuleUnloadEvent;
import org.tymoonnext.bot.module.Module;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class VisualModules extends JPanel implements EventListener{
    private Vector<Module> modules;
    private JList list;
    
    public VisualModules(Kizai bot){
        modules = new Vector<Module>();
        setLayout(new BorderLayout());
        
        try{bot.bindEvent(ModuleLoadEvent.class, this, "onModuleLoad");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(ModuleUnloadEvent.class, this, "onModuleUnload");}catch(NoSuchMethodException ex){}
        
        modules.addAll(Arrays.asList(bot.getModules()));
        list = new JList(modules);
        list.setFont(VisualBase.F_MONOSPACED);
        
        JScrollPane scroll = new JScrollPane(list);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        add(new JLabel("Loaded Modules: "), BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }
    
    public void onModuleLoad(ModuleLoadEvent evt){
        modules.add(evt.getModule());
        list.setListData(modules);
    }
    
    public void onModuleUnload(ModuleUnloadEvent evt){
        modules.remove(evt.getModule());
        list.setListData(modules);
    }
    
    public String toString(){return "[Visual|Modules]";}
}
