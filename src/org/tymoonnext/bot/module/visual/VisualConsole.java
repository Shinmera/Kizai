package org.tymoonnext.bot.module.visual;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.stream.Stream;


/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class VisualConsole extends JPanel implements Stream, ActionListener{
    public static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    
    private Kizai bot;
    private boolean closed = false;
    private JTable out;
    private DefaultTableModel table;
    private JTextField in;
    private JScrollPane scroll;
    
    public VisualConsole(Kizai bot){
        this.bot=bot;
        
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
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);
        add(in, BorderLayout.SOUTH);
        
        Commons.log.addHandler(new VisualLoggerWrapper(this));
        bot.registerStream(this);
    }

    public boolean isClosed(){return closed;}

    public void send(String msg, String dst){
        String[] dat = {sdf.format(System.currentTimeMillis()), dst, msg};
        table.addRow(dat);
        JScrollBar vertical = scroll.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
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