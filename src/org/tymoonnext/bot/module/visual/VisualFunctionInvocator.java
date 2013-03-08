package org.tymoonnext.bot.module.visual;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.meta.Arguments;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class VisualFunctionInvocator extends JDialog implements ActionListener, MouseListener{
    private Object target;
    private HashMap<String,Method> methodMap;
    private VisualObjectConstructor[] valueList;
    
    private JComboBox methodList;
    private JList argumentList;
    private JButton buttonInvoke;
    
    public VisualFunctionInvocator(Frame owner, Object target){
        super(owner, "Method Invocator - "+target, ModalityType.APPLICATION_MODAL);
        this.target = target;
        
        if(owner!=null)setLocation(owner.getLocation().x+10, owner.getLocation().y+10);
        setPreferredSize(new Dimension(300,400));
        methodMap = new HashMap<String,Method>();
        valueList = new VisualObjectConstructor[0];
        GridBagConstraints c = new GridBagConstraints();
        
        Method[] methods = target.getClass().getMethods();
        for(Method method : methods){
            StringBuilder sb = new StringBuilder();
            sb.append(method.getName()).append("(");
            
            Class[] params = method.getParameterTypes();
            String[] names = null;
            if(method.getAnnotation(Arguments.class) != null)
                names = ((Arguments)method.getAnnotation(Arguments.class)).value();
            
            for(int i=0;i<params.length;i++){
                sb.append(params[i].getSimpleName());
                if(names!=null)sb.append(" ").append(names[i]);
                if(i<params.length-1)sb.append(", ");
            }
            
            sb.append(")");
            methodMap.put(sb.toString(), method);
        }
        
        JPanel inner = new JPanel();
        argumentList = new JList();
        methodList = new JComboBox(methodMap.keySet().toArray());
        buttonInvoke = new JButton("Invoke");
        JScrollPane scroll = new JScrollPane(argumentList);
        
        inner.setLayout(new GridBagLayout());
        inner.setBorder(new EmptyBorder(5,5,5,5));
        methodList.addActionListener(this);
        buttonInvoke.addActionListener(this);
        argumentList.addMouseListener(this);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        inner.add(new JLabel("Object: "+target), c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx=1.0;
        inner.add(methodList, c);
        
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 2;
        c.weighty=1.0;
        c.weightx=1.0;
        inner.add(scroll, c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 3;
        c.weighty=0;
        c.weightx=1.0;
        inner.add(buttonInvoke, c);
        
        methodList.setSelectedIndex(0);
        
        add(inner);
        pack();
    }

    public void actionPerformed(ActionEvent e){
        if(e.getSource() == methodList){
            
            Method method = methodMap.get(methodList.getSelectedItem().toString());
            Class[] params = method.getParameterTypes();
            valueList = new VisualObjectConstructor[params.length];
            String[] names = null;
            if(method.getAnnotation(Arguments.class) != null)
                names = ((Arguments)method.getAnnotation(Arguments.class)).value();
            
            for(int i=0;i<params.length;i++){
                //Fuck the douchebag who thought it was a good idea to make ifs non-short-circuiting.
                String name = null; if(names != null)name = (i<names.length) ? names[i] : null;
                valueList[i] = new VisualObjectConstructor(this, params[i], name);
            }
            argumentList.setListData(valueList);
            
        }else if(e.getSource() == buttonInvoke){
            Method method = methodMap.get(methodList.getSelectedItem().toString());
            try{
                Commons.log.info(toString()+" Attempting to invoke method "+method.getName()+"...");
                
                Object[] vals = new Object[valueList.length];
                for(int i=0;i<valueList.length;i++){
                    vals[i] = valueList[i].getValue();
                }
                
                Object result = method.invoke(target, vals);
                
                JOptionPane.showMessageDialog(this, "<html>Invocation succeded. Result: <br>"+result+"</html>",
                                              "Invocation Successful", JOptionPane.INFORMATION_MESSAGE);
                
            }catch(IllegalAccessException ex){
                Commons.log.log(Level.FINE, toString()+" Access to Method denied.", ex);
                JOptionPane.showMessageDialog(this, "<html>Invocation failed.<br>Access to Method denied.</html>",
                                              "Failed to Invoke Method", JOptionPane.ERROR_MESSAGE);
            }catch(IllegalArgumentException ex){
                Commons.log.log(Level.FINE, toString()+" Arguments do not match.", ex);
                JOptionPane.showMessageDialog(this, "<html>Invocation failed.<br>Arguments do not match.</html>",
                                              "Failed to Invoke Method", JOptionPane.ERROR_MESSAGE);
            }catch(InvocationTargetException ex){
                Commons.log.log(Level.FINE, toString()+" Error occurred inside Method.", ex);
                JOptionPane.showMessageDialog(this, "<html>Invocation failed.<br>Error inside Method: "+ex.getMessage()+"</html>",
                                              "Failed to Invoke Method", JOptionPane.ERROR_MESSAGE);
            }catch(SecurityException ex){
                Commons.log.log(Level.FINE, toString()+" Failed to access Method.", ex);
                JOptionPane.showMessageDialog(this, "<html>Invocation failed.<br>Failed to access Method.</html>",
                                              "Failed to Invoke Method", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public String toString(){return "[Visual|FunctionInvocator]";}

    @Override
    public void mouseClicked(MouseEvent e){
        if(e.getClickCount() == 2 && !e.isConsumed()){
            e.consume();
            if(argumentList.getSelectedValue() != null){
                ((VisualObjectConstructor)argumentList.getSelectedValue()).setVisible(true);
                argumentList.repaint();
            }
        }
    }
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
}
