
package org.bisanti.uieditor;

import java.awt.Color;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.InvocationTargetException;
import javax.swing.plaf.ColorUIResource;
import org.bisanti.util.Pair;
import org.bisanti.util.Util;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;


/**
 * Written and authored by Jason Bisanti. Free to use and reproduce.
 *
 * @author Jason Bisanti
 */
public class UINode extends AbstractNode
{
    public static final String VALUE_PROP = "VALUE";
    
    public static final String VALUE_DISPLAY = "Value";
    
    private UIProperty prop;
    
    public UINode(UIProperty prop)
    {
        super(Children.LEAF);
        super.setName(prop.getName().toString());
        this.prop = prop;
    }
    
    public static boolean hasEditor(Class type)
    {
        
        return findEditor(type) != null;
    }
    
    public static Pair<PropertyEditor, Class> findEditor(Class type)
    {
        PropertyEditor ped = PropertyEditorManager.findEditor(type);
        
        if(validEditor(ped))
        {
            return new Pair<PropertyEditor, Class>(ped, type);
        }
        else
        {
            for(Class inf: type.getInterfaces())
            {
                ped = PropertyEditorManager.findEditor(inf);
                if(validEditor(ped))
                {
                    return new Pair<PropertyEditor, Class>(ped, inf);
                }
            }
        }
        
        Class parent = type.getSuperclass();
        while (parent != Object.class)
        {
            ped = PropertyEditorManager.findEditor(parent);
            if(validEditor(ped))
            {
                return new Pair<PropertyEditor, Class>(ped, parent);
            }
            parent = parent.getSuperclass();
        }
        
        return null;
    }
    
    public static boolean validEditor(PropertyEditor ped)
    {
        return ped != null && !ped.getClass().getName().contains("ObjectEditor");
    }
    
    private Property create(final Object value, Class c)
    {
        if(Boolean.class.isAssignableFrom(c))
        {
            return new PropertySupport.ReadWrite<Boolean>(VALUE_PROP, Boolean.class, VALUE_DISPLAY, "")
            {
                @Override
                public Boolean getValue() throws IllegalAccessException, InvocationTargetException
                {
                    return (Boolean) prop.getValue();
                }

                @Override
                public void setValue(Boolean t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                {                    
                    if(!Util.equal(prop.getValue(), t))
                    {
                        prop.setValue(t);
                        UINode.this.firePropertyChange(VALUE_PROP, null, prop);
                    }                    
                }
            };
        }
        if(Color.class.isAssignableFrom(c))
        {
            return new PropertySupport.ReadWrite<Color>(VALUE_PROP, Color.class, VALUE_DISPLAY, "")
            {
                @Override
                public Color getValue() throws IllegalAccessException, InvocationTargetException
                {
                    return (Color) prop.getValue();
                }

                @Override
                public void setValue(Color t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                {
                    
                    if(value instanceof ColorUIResource)
                    {
                        t = new ColorUIResource(t);
                    }
                    
                    if(!Util.equal(prop.getValue(), t))
                    {
                        prop.setValue(t);
                        UINode.this.firePropertyChange(VALUE_PROP, null, prop);
                    }
                    
                }
            };
        }
        else
        {
            final String val = c.getName() + ": " + value;
            return new PropertySupport.ReadOnly<String>(VALUE_PROP, String.class, VALUE_DISPLAY, "")
            {
                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException
                {
                   return val;
                }
            };
        }
    }
    
    protected Property[] createProperties()
    {
        Object value = this.prop.getValue();
        Class c = value.getClass();
        return new Property[]{this.create(value, c)};
    }
    
    @Override
    protected Sheet createSheet()
    {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);

        if (set == null)
        {
            set = Sheet.createPropertiesSet();
            set.put(this.createProperties());
            sheet.put(set);
        }

        return sheet;
    }
}
