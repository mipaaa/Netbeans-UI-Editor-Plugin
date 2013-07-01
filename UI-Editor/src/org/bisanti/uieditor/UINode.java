
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
    
    private final Object originalValue;
    
    private boolean original = true;
    
    public UINode(UIProperty prop)
    {
        super(Children.LEAF);
        super.setName(prop.getName().toString());
        this.prop = prop;
        this.originalValue = prop.getValue();
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
    
    protected Property[] createProperties()
    {
        Object value = this.prop.getValue();
        Class c = value.getClass();
        Property p;
        
        Pair<PropertyEditor, Class> pair = findEditor(c);
        if(pair != null && validEditor(pair.getFirst()))
        {
            p =  new EditorProp(pair.getSecond(), true);
        }
        else
        {
            final String val = c.getName() + ": " + value;
            p = new EditorProp<String>(String.class, false)
            {
                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException
                {
                    return val;
                }                                           
            };
        }
        
        return new Property[]{p};
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

    @Override
    public String getHtmlDisplayName()
    {
        return this.original ? null : "<b>" + this.getDisplayName();
    }
    
    private class EditorProp<T> extends PropertySupport.ReadWrite<T>
    {
        private final boolean editable;
        
        EditorProp(Class<T> clazz, boolean editable)
        {
            super(VALUE_PROP, clazz, VALUE_DISPLAY, "");
            this.editable = editable;
        }

        @Override
        public boolean canWrite()
        {
            return this.editable;
        }

        @Override
        public T getValue() throws IllegalAccessException, InvocationTargetException
        {
            return (T) prop.getValue();
        }

        @Override
        public void setValue(T t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            if(!Util.equal(prop.getValue(), t))
            {
                this.set(t);
                original = Util.equal(originalValue, t);
                UINode.this.firePropertyChange(VALUE_PROP, null, prop);
            }
        }
        
        void set(Object newValue)
        {
            Object value = prop.getValue();
            if (value instanceof ColorUIResource)
            {
                prop.setValue(new ColorUIResource((Color) newValue));
            }
            else
            {
                prop.setValue(newValue);
            }
        }
    }
}
