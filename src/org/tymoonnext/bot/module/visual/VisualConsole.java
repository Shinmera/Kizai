package org.tymoonnext.bot.module.visual;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.cmd.CommandInstanceEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.auth.AllowAllSessionImplementor;
import org.tymoonnext.bot.module.cmd.CommandInstance;
import org.tymoonnext.bot.module.core.ext.CommandModule;
import org.tymoonnext.bot.stream.Stream;



/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class VisualConsole extends JPanel implements Stream, ActionListener, CommandListener{
    public static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    
    private Kizai bot;
    private boolean closed = false;
    private JTable out;
    private DefaultTableModel table;
    private JTextField in;
    private JScrollPane scroll;
    private VisualLoggerWrapper logwrap;
    
    public VisualConsole(Kizai bot){
        this.bot=bot;
        
        logwrap = new VisualLoggerWrapper(this);
        out = new JTable();
        in = new JTextField();
        table = new DefaultTableModel(0,3);
        out = new JTable(table);
        scroll = new JScrollPane(out);
        
        in.addActionListener(this);
        out.setEnabled(false);
        out.setTableHeader(null);
        out.setDefaultRenderer(Object.class, new ConsoleCellRenderer());
        out.getColumnModel().getColumn(0).setMaxWidth(300);
        out.getColumnModel().getColumn(1).setMaxWidth(200);
        out.getColumnModel().getColumn(2).setCellRenderer(new TextAreaRenderer());
        out.setFont(VisualBase.F_MONOSPACED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);        
        scroll.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            private int max;
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if(e.getAdjustable().getMaximum() > max){
                    max = e.getAdjustable().getMaximum();
                    e.getAdjustable().setValue(max);
                }
            }
        });

        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);
        add(in, BorderLayout.SOUTH);
        
        Commons.log.addHandler(logwrap);
        bot.registerStream(this);
        CommandModule.register(bot, "console", "level", "level{OFF|SEVERE|WARNING|INFO|FINE|FINER|FINEST|ALL}".split(" "), "Set the logging level of the messages appearing on the console.", this);
        new AllowAllSessionImplementor(bot, this);
    }

    public boolean isClosed(){return closed;}

    public void send(String msg, String dst){
        String[] dat = {sdf.format(System.currentTimeMillis()), dst, msg};
        table.addRow(dat);
    }

    public void close(){closed=true;}

    public String toString(){return "~Visual~";}
    public String getID(){return "visual";}

    public void actionPerformed(ActionEvent e) {
        String cmd = in.getText();
        in.setText("");
        if(cmd.contains(" "))
            bot.event(new CommandEvent(this, cmd.substring(0,cmd.indexOf(' ')),
                                             cmd.substring(cmd.indexOf(' ')+1),
                                             System.getProperty("user.name")));
        else
            bot.event(new CommandEvent(this, cmd, null, System.getProperty("user.name")));
    }

    public void onCommand(CommandEvent cmd) {
        if(cmd.getCommand().equals("console level")){
            CommandInstance i = ((CommandInstanceEvent)cmd).get();
            if(i.getValue("level").equals("NONE"))logwrap.setLevel(Level.OFF);
            else if(i.getValue("level").equals("SEVERE"))logwrap.setLevel(Level.SEVERE);
            else if(i.getValue("level").equals("WARNING"))logwrap.setLevel(Level.WARNING);
            else if(i.getValue("level").equals("INFO"))logwrap.setLevel(Level.INFO);
            else if(i.getValue("level").equals("FINE"))logwrap.setLevel(Level.FINE);
            else if(i.getValue("level").equals("FINER"))logwrap.setLevel(Level.FINER);
            else if(i.getValue("level").equals("FINEST"))logwrap.setLevel(Level.FINEST);
            else if(i.getValue("level").equals("ALL"))logwrap.setLevel(Level.ALL);
            cmd.getStream().send("Logging level changed to "+i.getValue("level"), cmd.getChannel());
        }
    }
}

class ConsoleCellRenderer extends DefaultTableCellRenderer{
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        JLabel l = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        l.setForeground(Color.BLACK);
        l.setBackground(Color.WHITE);
        l.setHorizontalAlignment(JLabel.CENTER);
        
        if(col == 1){
            if(l.getText().equals("INFO"))          l.setForeground(Color.GREEN);
            else if(l.getText().equals("WARNING"))  l.setForeground(Color.ORANGE);
            else if(l.getText().equals("SEVERE"))   l.setForeground(Color.RED);
            else if(l.getText().equals("FINE"))     l.setForeground(Color.GRAY);
            else if(l.getText().equals("FINER"))    l.setForeground(Color.LIGHT_GRAY);
            else if(l.getText().equals("FINEST"))   l.setForeground(Color.LIGHT_GRAY);
        }
        return l;
    }
}