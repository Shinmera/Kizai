package org.tymoonnext.bot.module.visual;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.core.ModuleLoadEvent;
import org.tymoonnext.bot.event.core.ModuleUnloadEvent;
import org.tymoonnext.bot.meta.Info;
import org.tymoonnext.bot.module.Module;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class VisualModules extends JPanel implements EventListener, MouseListener{
    private Kizai bot;
    private Vector<Module> modules;
    private JList list;
    
    public VisualModules(Kizai bot){
        this.bot=bot;
        modules = new Vector<Module>();
        setLayout(new BorderLayout());
        
        try{bot.bindEvent(ModuleLoadEvent.class, this, "onModuleLoad");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(ModuleUnloadEvent.class, this, "onModuleUnload");}catch(NoSuchMethodException ex){}
        
        modules.addAll(Arrays.asList(bot.getModules()));
        list = new JList(modules);
        list.setFont(VisualBase.F_MONOSPACED);
        list.setCellRenderer(new ModuleCellRenderer());
        list.addMouseListener(this);
        
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

    public void mouseClicked(MouseEvent ev){
        if(ev.getButton() == MouseEvent.BUTTON3){
            list.setSelectedIndex(list.locationToIndex(ev.getPoint()));
            Module m = (Module)list.getSelectedValue();
            
            ModuleMenuActionListener lst = new ModuleMenuActionListener(bot, m);
            JPopupMenu menu = new JPopupMenu(m.toString());
            JMenuItem i_info = new JMenuItem("Information");
            JMenuItem i_reload = new JMenuItem("Reload Module");
            JMenuItem i_unload = new JMenuItem("Unload Module");
            i_reload.addActionListener(lst);
            i_unload.addActionListener(lst);
            i_info.addActionListener(lst);
            menu.add(i_info);
            menu.add(i_reload);
            menu.add(i_unload);
            menu.setVisible(true);
            Border titleUnderline = BorderFactory.createMatteBorder(1, 0, 0, 0, menu.getForeground());
            TitledBorder labelBorder = BorderFactory.createTitledBorder(titleUnderline, menu.getLabel(),
                                                                        TitledBorder.CENTER, TitledBorder.ABOVE_TOP, 
                                                                        menu.getFont(), menu.getForeground());
            menu.setBorder(BorderFactory.createCompoundBorder(menu.getBorder(),
            labelBorder));
            menu.show(list, ev.getX(), ev.getY());
        }
    }

    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    
    class ModuleCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Module m = (Module)value;
            String text = "<html><b>"+m+"</b> <i>"+m.getClass().getPackage().getName()+"</i>";
            if(m.getClass().getAnnotation(Info.class) != null){
                text+="<br>"+m.getClass().getAnnotation(Info.class).value();
            }
            label.setToolTipText(text);
            return label;
        }
    }
}

class ModuleMenuActionListener implements ActionListener{
    private Module m;
    private Kizai bot;
    
    public ModuleMenuActionListener(Kizai bot, Module m){
        this.bot=bot;
        this.m=m;
    }
    
    public void actionPerformed(ActionEvent e){
        if(e.getActionCommand().equals("Information")){
            if(m.getClass().getAnnotation(Info.class) != null){
                JOptionPane.showMessageDialog(null, 
                                              "<html>"+m+"<br><br>"+m.getClass().getAnnotation(Info.class).value()+"</html>",
                                              "Information About "+m,
                                              JOptionPane.INFORMATION_MESSAGE);
            }else{
                JOptionPane.showMessageDialog(null, "No additional information specified.", "Module Info?",
                                              JOptionPane.QUESTION_MESSAGE);
            }
        }if(e.getActionCommand().equals("Reload Module")){
            bot.reloadModule(m);
        }else if(e.getActionCommand().equals("Unload Module")){
            bot.unloadModule(m);
        }
    }
    
}