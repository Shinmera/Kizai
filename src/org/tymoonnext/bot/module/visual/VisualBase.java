package org.tymoonnext.bot.module.visual;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class VisualBase extends JFrame{
    public static final Font F_MONOSPACED = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    
    private VisualConsole console;
    private VisualModules modules;
    private VisualSummary summary;
    private Kizai bot;
    
    public VisualBase(final Kizai bot){
        this.bot = bot;
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(1024,768));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setTitle(Commons.getVersionString());
        
        console = new VisualConsole(bot);
        modules = new VisualModules(bot);
        summary = new VisualSummary(bot);
        
        JPanel left = new JPanel();
        left.setLayout(new BorderLayout());
        left.add(modules, BorderLayout.CENTER);
        left.add(summary, BorderLayout.NORTH);
        
        JSplitPane split = new JSplitPane();
        split.setLeftComponent(left);
        split.setRightComponent(console);
        split.setDividerLocation(300);
        split.setDividerSize(5);
        
        add(split);
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent ev){
                new Thread(){public void run(){bot.shutdown();}}.start();
            }
        });
        setVisible(true);
        pack();
    }
}
