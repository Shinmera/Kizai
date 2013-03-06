package org.tymoonnext.bot.module.visual;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class VisualBase extends JFrame{
    private VisualConsole console;
    private VisualModules modules;
    private Kizai bot;
    
    public VisualBase(final Kizai bot){
        this.bot = bot;
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(700, 500));
        setPreferredSize(new Dimension(700,500));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setTitle(Commons.getVersionString());
        
        console = new VisualConsole(bot);
        modules = new VisualModules(bot);
        
        add(modules, BorderLayout.WEST);
        add(console, BorderLayout.CENTER);
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent ev){
                new Thread(){public void run(){bot.shutdown();}}.start();
            }
        });
        setVisible(true);
        pack();
    }
    
    public VisualConsole getConsole(){return console;}
}
