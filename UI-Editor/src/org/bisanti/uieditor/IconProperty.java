/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bisanti.uieditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager;
import org.openide.nodes.PropertySupport;

/**
 * Written and authored by Jason Bisanti. Free to use and reproduce.
 * <br><br>
 *
 * @author Jason Bisanti
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class IconProperty extends PropertySupport.ReadOnly
{
    private static final Set<JComponent> components = new HashSet<JComponent>();
    static
    {
        components.add(new JMenuItem());
        components.add(new JRadioButtonMenuItem());
        components.add(new JCheckBoxMenuItem());
        components.add(new JButton());
        components.add(new JLabel());
        components.add(new JRadioButton());
        components.add(new JCheckBox());
    }
    
    private Icon icon;
    
    private IconEditorSupport ies;
    
    public IconProperty(Object icon, String name, String displayName, String shortDescription)
    {
        super(name, Object.class, displayName, shortDescription);
        this.icon = (Icon) icon;
        this.ies = new IconEditorSupport();        
    }    

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException
    {
        return null;
    }
    
    @Override
    public PropertyEditor getPropertyEditor()
    {
        return this.ies;
    }
    
    public Icon getIcon()
    {
        return this.icon;
    }
    
    public void setIcon(Icon icon)
    {
        this.icon = icon;
    }
    
    private class IconEditorSupport implements PropertyEditor
    {        
        private JLabel label = new JLabel(icon);
        
        private boolean triedPainting;
        
        private JComponent myComponent;

        @Override
        public void setValue(Object value){}

        @Override
        public Object getValue()
        {
            return null;
        }

        @Override
        public boolean isPaintable()
        {
            return true;
        }

        @Override
        public void paintValue(Graphics gfx, Rectangle box)
        {
            if(this.triedPainting)
            {
                if(this.myComponent != null)
                {
                    icon.paintIcon(this.myComponent, gfx, 0, 0);
                }
            }
            else
            {
                boolean painted = false;
                for (JComponent jc : components)
                {
                    try
                    {
                        icon.paintIcon(jc, gfx, 0, 0);
                        painted = true;
                        this.myComponent = jc;
                    } 
                    catch (Exception ex){}

                    if (painted)
                    {
                        break;
                    }
                }
                
                this.triedPainting = true;
            }
            
            Color oldColor = gfx.getColor();
            gfx.setColor(UIManager.getColor("Label.disabledForeground"));
            gfx.drawString(" (" + icon.toString() + ")", box.x + icon.getIconWidth(), (int)box.getCenterY()+3);
            gfx.setColor(oldColor);
        }

        @Override
        public String getJavaInitializationString()
        {
            return null;
        }

        @Override
        public String getAsText()
        {
            return null;
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException{}

        @Override
        public String[] getTags()
        {
            return new String[0];
        }

        @Override
        public Component getCustomEditor()
        {
            try
            {
                return this.label;
            }
            catch(Exception ex)
            {
                this.label = new JLabel("<not displayable>");
                return this.label;
            }
        }

        @Override
        public boolean supportsCustomEditor()
        {
            return true;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener){}

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener){}
        
    }

}
