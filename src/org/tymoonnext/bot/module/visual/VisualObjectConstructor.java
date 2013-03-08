package org.tymoonnext.bot.module.visual;

import NexT.data.DObject;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Constructor;
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
public class VisualObjectConstructor<T extends Object> extends JDialog implements ActionListener, MouseListener{
    private String name;
    private T value;
    private Class<T> object;
    private HashMap<String,Constructor> constructorMap;
    private VisualObjectConstructor[] valueList;
    
    private JComboBox constructorList;
    private JList argumentList;
    private JButton buttonCreate;
    private JTextField primitiveInput;
    private boolean isPrimitive = false;
    
    public VisualObjectConstructor(Frame owner, Class<T> objectClass){this(owner, objectClass, null);}
    public VisualObjectConstructor(Frame owner, Class<T> objectClass, String name){
        super(owner);
        this.object=objectClass;
        this.name = name;
        
        setTitle("Object Constructor - "+objectClass.getSimpleName());
        constructorMap = new HashMap<String,Constructor>();
        valueList = new VisualObjectConstructor[0];
        
        JPanel inner = new JPanel();
        buttonCreate = new JButton("Invoke");
        buttonCreate.addActionListener(this);
        inner.setBorder(new EmptyBorder(5,5,5,5));
        
        if(objectClass == String.class ||
           objectClass == Integer.class ||
           objectClass == Long.class ||
           objectClass == Double.class ||
           objectClass == Boolean.class)
            isPrimitive = true;
        
        if(!isPrimitive){
            setPreferredSize(new Dimension(300,400));
            inner.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            
            Constructor[] constructors = objectClass.getConstructors();
            for(Constructor constructor : constructors){
                StringBuilder sb = new StringBuilder();

                Class[] params = constructor.getParameterTypes();
                String[] names = null;
                if(constructor.getAnnotation(Arguments.class) != null)
                    names = ((Arguments)constructor.getAnnotation(Arguments.class)).value();
                
                for(int i=0;i<params.length;i++){
                    sb.append(params[i].getSimpleName());
                    if(names!=null && names.length>i)sb.append(" ").append(names[i]);
                    if(i<params.length-1)sb.append(", ");
                }

                constructorMap.put(sb.toString(), constructor);
            }

            argumentList = new JList();
            constructorList = new JComboBox(constructorMap.keySet().toArray());
            JScrollPane scroll = new JScrollPane(argumentList);
            constructorList.addActionListener(this);
            argumentList.addMouseListener(this);
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1.0;
            inner.add(new JLabel("Class: "+object.getSimpleName()), c);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 1;
            c.weightx=1.0;
            inner.add(constructorList, c);

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
            inner.add(buttonCreate, c);
            
            if(constructorMap.size() > 0)
                constructorList.setSelectedIndex(0);
        }else{
            setPreferredSize(new Dimension(300,20));
            inner.setLayout(new BorderLayout());
            primitiveInput = new JTextField();
            primitiveInput.setPreferredSize(new Dimension(100000,100000));
            primitiveInput.addActionListener(this);
            
            inner.add(primitiveInput, BorderLayout.CENTER);
            inner.add(buttonCreate, BorderLayout.EAST);
        }
        
        add(inner);
        pack();
    }

    public void actionPerformed(ActionEvent e){
        if(e.getSource() == constructorList){
            
            Constructor constructor = constructorMap.get(constructorList.getSelectedItem().toString());
            Class[] params = constructor.getParameterTypes();
            valueList = new VisualObjectConstructor[params.length];
            String[] names = null;
            if(constructor.getAnnotation(Arguments.class) != null)
                names = ((Arguments)constructor.getAnnotation(Arguments.class)).value();
            
            for(int i=0;i<params.length;i++){
                String name = null; if(names != null)name = (i<names.length) ? names[i] : null;
                valueList[i] = new VisualObjectConstructor(null, params[i], name);
            }
            argumentList.setListData(valueList);
            
        }else if(e.getSource() == buttonCreate || e.getSource() == primitiveInput){
            if(!isPrimitive){
                Constructor constructor = constructorMap.get(constructorList.getSelectedItem().toString());
                try{
                    Commons.log.info(toString()+" Attempting to create object "+object.getName()+"...");

                    Object[] vals = new Object[valueList.length];
                    for(int i=0;i<valueList.length;i++){
                        vals[i] = valueList[i].getValue();
                    }

                    value = (T)constructor.newInstance(vals);


                }catch(InstantiationException ex){
                    Commons.log.log(Level.FINE, toString()+" Instantiation failed.", ex);
                    JOptionPane.showMessageDialog(this, "<html>Instantiation failed.<br>InstantiationException occurred.</html>",
                                                  "Failed to Create Object", JOptionPane.ERROR_MESSAGE);
                }catch(IllegalAccessException ex){
                    Commons.log.log(Level.FINE, toString()+" Access to Method denied.", ex);
                    JOptionPane.showMessageDialog(this, "<html>Instantiation failed.<br>Access to Method denied.</html>",
                                                  "Failed to Create Object", JOptionPane.ERROR_MESSAGE);
                }catch(IllegalArgumentException ex){
                    Commons.log.log(Level.FINE, toString()+" Arguments do not match.", ex);
                    JOptionPane.showMessageDialog(this, "<html>Instantiation failed.<br>Arguments do not match.</html>",
                                                  "Failed to Create Object", JOptionPane.ERROR_MESSAGE);
                }catch(InvocationTargetException ex){
                    Commons.log.log(Level.FINE, toString()+" Error occurred inside Method.", ex);
                    JOptionPane.showMessageDialog(this, "<html>Instantiation failed.<br>Error inside Method: "+ex.getMessage()+"</html>",
                                                  "Failed to Create Object", JOptionPane.ERROR_MESSAGE);
                }catch(SecurityException ex){
                    Commons.log.log(Level.FINE, toString()+" Failed to access Method.", ex);
                    JOptionPane.showMessageDialog(this, "<html>Instantiation failed.<br>Failed to access Method.</html>",
                                                  "Failed to Create Object", JOptionPane.ERROR_MESSAGE);
                }
            }else{
                Commons.log.info(toString()+" Creating primitive "+object.getName()+" through DObject...");
                value = (T)DObject.parse(primitiveInput.getText()).get();
            }
            setVisible(false);
        }
    }
    
    public void mouseClicked(MouseEvent e){
        if(e.getClickCount() == 2 && !e.isConsumed()){
            e.consume();
            if(argumentList.getSelectedValue() != null){
                ((VisualObjectConstructor)argumentList.getSelectedValue()).setVisible(true);
                repaint();
            }
        }
    }
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    
    public Class<T> getConstructorClass(){return object;}
    public T getValue(){return value;}
    
    public String toString(){
        return object.getSimpleName() + 
                ((name ==null)? "" : " "+name) + 
                ((value==null)? " : Unset" : " : "+value);
    }
}
