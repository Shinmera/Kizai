package org.tymoonnext.bot.module.visual;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.Event;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.core.CommandEvent;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class VisualSummary extends JPanel implements EventListener{
    private Kizai bot;
    private int evtCount = 0;
    private int cmdCount = 0;
    private int modCount = 0;
    
    private JLabel l_evtCount;
    private JLabel l_cmdCount;
    private JLabel l_modCount;
    private JLabel l_lastCmd;
    private JLabel l_lastEvt;
    
    public VisualSummary(Kizai bot){
        this.bot=bot;        
        setLayout(new GridLayout(0,2, 5, 2));
        setBorder(new EmptyBorder(10,10,10,10));
        
        l_evtCount = new JLabel("0");
        l_cmdCount = new JLabel("0");
        l_modCount = new JLabel("0");
        l_lastCmd = new JLabel("-");
        l_lastEvt = new JLabel("-");
        add("", new JLabel(Commons.FQDN+" "+Commons.VERSION));
        add("Events: ", l_evtCount);
        add("Commands: ", l_cmdCount);
        add("Modules: ", l_modCount);
        add("Last Cmd: ", l_lastCmd);
        add("Last Evt: ", l_lastEvt);
        
        try{bot.bindEvent(Event.class, this, "onEvent");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(CommandEvent.class, this, "onCommand");}catch(NoSuchMethodException ex){}
    }
    
    private void add(String desc, JLabel c){
        c.setHorizontalAlignment(JLabel.RIGHT);
        JLabel l_desc = new JLabel(desc);
        l_desc.setFont(l_desc.getFont().deriveFont(Font.BOLD));
        add(l_desc);
        add(c);
    }
    
    public void onEvent(Event evt){
        evtCount++;
        l_lastEvt.setText(evt.toString());
        updateAll();
    }
    
    public void onCommand(CommandEvent cmd){
        cmdCount++;
        l_lastCmd.setText(cmd.getCommand());
        updateAll();
    }
    
    private void updateAll(){
        modCount = bot.getModules().length;
        l_evtCount.setText(evtCount+"");
        l_cmdCount.setText(cmdCount+"");
        l_modCount.setText(modCount+"");
    }
    
    public String toString(){return "[Visual|Summary]";}
}
