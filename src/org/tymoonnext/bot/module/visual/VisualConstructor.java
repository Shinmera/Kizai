package org.tymoonnext.bot.module.visual;

import NexT.data.DObject;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.meta.Arguments;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class VisualConstructor extends JDialog implements ActionListener, MouseListener{
    private HashMap<String,Object> objectMap;
    private VisualConstructor[] valueList;
    private Object target;
    private Object result;
    private String name;
    private boolean isPrimitive=false;
    
    private JPanel inner;
    private JComboBox primaryList;
    private JList argumentList;
    private JButton buttonCreate;
    private JButton buttonCancel;
    private JTextField primitiveInput;
    
    public VisualConstructor(Window parent, Object target){this(parent, target, target.toString());}
    public VisualConstructor(Window parent, Object target, String name){
        super(parent, "Visual Constructor - "+name, ModalityType.APPLICATION_MODAL);
        this.target = target;
        this.name = name;
        
        if(parent!=null)setLocation(parent.getLocation().x+10, parent.getLocation().y+10);
        objectMap = new HashMap<String,Object>();
        valueList = new VisualConstructor[0];
        
        inner = new JPanel();
        buttonCreate = new JButton("Construct");
        buttonCancel = new JButton("Cancel");
        argumentList = new JList();
        primaryList = new JComboBox();
        primitiveInput = new JTextField();
        buttonCreate.addActionListener(this);
        buttonCancel.addActionListener(this);
        primaryList.addActionListener(this);
        primitiveInput.addActionListener(this);
        argumentList.addMouseListener(this);
        inner.setBorder(new EmptyBorder(5,5,5,5));
        argumentList.setCellRenderer(new VisualConstructorCellRenderer());
        
        setPreferredSize(new Dimension(300,200));
        inner.setLayout(new GridBagLayout());

        if(target == String.class ||
           target == Integer.class ||
           target == Long.class ||
           target == Double.class ||
           target == Boolean.class){
            isPrimitive = true;
        }else{
            fillObjectMap(target);
        }
        
        addBaseUIElements();
        add(inner);
        pack();
    }
    
    private void addBaseUIElements(){
        GridBagConstraints c = new GridBagConstraints();
        primaryList.setModel(new DefaultComboBoxModel(objectMap.keySet().toArray()));
        JScrollPane scroll = new JScrollPane(argumentList);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        if(!isPrimitive){
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1.0;
            inner.add(new JLabel("Target: "+target), c);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 1;
            c.weightx=1.0;
            inner.add(primaryList, c);

            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 2;
            c.weighty=1.0;
            c.weightx=1.0;
            inner.add(scroll, c);
        }else{
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1.0;
            inner.add(primitiveInput, c);
        }

        JPanel buttons = new JPanel();
        buttons.add(buttonCreate);
        buttons.add(buttonCancel);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 3;
        c.weighty=0;
        c.weightx=1.0;
        inner.add(buttons, c);

        if(objectMap.size() > 0)
            primaryList.setSelectedIndex(0);
    }
    
    private void fillObjectMap(Object target){ 
        Object[] funcs;
        if(target instanceof Class){funcs = ((Class)target).getConstructors();
        }else{                      funcs = target.getClass().getMethods();}
        
        for(Object func : funcs){
            StringBuilder sb = new StringBuilder();
            if(func instanceof Method)sb.append(((Method)func).getName());
            else                      sb.append(((Class)target).getSimpleName());
            
            sb.append("(");
            Class[] params = (Class[])Commons.invoke(func, "getParameterTypes");
            String[] names = null;
            if(Commons.invoke(func, "getAnnotation", Arguments.class) != null)
                names = ((Arguments)Commons.invoke(func, "getAnnotation", Arguments.class)).value();
            
            for(int i=0;i<params.length;i++){
                sb.append(params[i].getSimpleName());
                if(names!=null)sb.append(" ").append(names[i]);
                if(i<params.length-1)sb.append(", ");
            }
            
            sb.append(")");
            objectMap.put(sb.toString(), func);
        }
    }    
    
    public void actionPerformed(ActionEvent e){
        if(e.getSource() == primaryList){            
            Object choice = objectMap.get(primaryList.getSelectedItem().toString());
            Class[] params = (Class[])Commons.invoke(choice, "getParameterTypes");
            
            valueList = new VisualConstructor[params.length];
            String[] names = null;
            if(Commons.invoke(choice, "getAnnotation", Arguments.class) != null)
                names = ((Arguments)Commons.invoke(choice, "getAnnotation", Arguments.class)).value();

            for(int i=0;i<params.length;i++){
                //Fuck the douchebag who thought it was a good idea to make ifs non-short-circuiting.
                String name = null; if(names != null)name = (i<names.length) ? names[i] : null;
                valueList[i] = new VisualConstructor(this, params[i], name);
            }
            argumentList.setListData(valueList);            
            
        }else if(e.getSource() == buttonCreate || e.getSource() == primitiveInput){
            if(!isPrimitive){
                Object choice = objectMap.get(primaryList.getSelectedItem().toString());
                try{
                    Object[] vals = new Object[valueList.length];
                    for(int i=0;i<valueList.length;i++){
                        vals[i] = valueList[i].getResult();
                    }

                    if(target instanceof Class){result = ((Constructor)choice).newInstance(vals);
                    }else{                      result = ((Method)choice).invoke(target, vals);}

                }catch(InstantiationException ex){
                    Commons.log.log(Level.FINE, toString()+" Result build failed.", ex);
                    JOptionPane.showMessageDialog(this, "<html>Result Build Failed.<br>InstantiationException occurred.</html>",
                                                  "Result Build Failed", JOptionPane.ERROR_MESSAGE);
                }catch(IllegalAccessException ex){
                    Commons.log.log(Level.FINE, toString()+" Access to Method denied.", ex);
                    JOptionPane.showMessageDialog(this, "<html>Result Build Failed.<br>Access to Method denied.</html>",
                                                  "Result Build Failed", JOptionPane.ERROR_MESSAGE);
                }catch(IllegalArgumentException ex){
                    Commons.log.log(Level.FINE, toString()+" Arguments do not match.", ex);
                    JOptionPane.showMessageDialog(this, "<html>Result Build Failed.<br>Arguments do not match.</html>",
                                                  "Result Build Failed", JOptionPane.ERROR_MESSAGE);
                }catch(InvocationTargetException ex){
                    Commons.log.log(Level.FINE, toString()+" Error occurred inside Method.", ex);
                    JOptionPane.showMessageDialog(this, "<html>Result Build Failed.<br>Error inside Method: "+ex.getMessage()+"</html>",
                                                  "Result Build Failed", JOptionPane.ERROR_MESSAGE);
                }
            }else{
                result = DObject.parse(primitiveInput.getText()).get();
            }
            setVisible(false);
            
        }else if(e.getSource() == buttonCancel){
            setVisible(false);
        }
    }
    
    public void mouseClicked(MouseEvent e){
        if(e.getClickCount() == 2 && !e.isConsumed()){
            e.consume();
            if(argumentList.getSelectedValue() != null){
                ((VisualConstructor)argumentList.getSelectedValue()).setVisible(true);
                argumentList.repaint();
            }
        }
    }
    
    public void setVisible(boolean visible){
        super.setVisible(visible);
        if(isPrimitive)primitiveInput.requestFocus();
        else           primaryList.requestFocus();
    }
    
    public String toString(){return "["+getClass().getSimpleName()+"|"+name+"]";}
    
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    
    public String getName(){return name;}
    public Object getResult(){return result;}
    public Object getTarget(){return target;}
    
    public void setName(String name){this.name=name;}
    public void setResult(Object result){this.result=result;}
    
    class VisualConstructorCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            VisualConstructor constructor = (VisualConstructor)value;
            String text = ((Class)constructor.getTarget()).getSimpleName() + ": " + constructor.getResult();
            label.setText(text);
            return label;
        }
    }
}
