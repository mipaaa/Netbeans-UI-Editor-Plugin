
package org.bisanti.uieditor;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.InvocationTargetException;
import javax.swing.UIDefaults.ActiveValue;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
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
@SuppressWarnings({"unchecked","rawtypes"})
public class UINode extends AbstractNode
{
    public static final String VALUE_PROP = "VALUE";
    
    public static final String VALUE_DISPLAY = "Value";
    
    private UIProperty prop;
    
    private Object originalValue;
    
    private boolean original;
    
    public UINode(UIProperty prop)
    {
        super(Children.LEAF);
        super.setName(prop.getName().toString());
        this.prop = prop;
        this.originalValue = prop.getValue();
        if(UIEditorTopComponent.applied.contains(prop))
        {
            prop.setValue(UIManager.get(prop.getName()));
            this.original = false;
            UINode.this.firePropertyChange(VALUE_PROP, null, prop);
        }
        else
        {
            this.original = true;
        }
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
        else if(ActiveValue.class.isAssignableFrom(c) && ((ActiveValue)value).createValue(UIManager.getDefaults()) instanceof Font)
        {
            Font newValue = (Font) ((ActiveValue)value).createValue(UIManager.getDefaults());
            this.prop.setValue(new FontUIResource(newValue));
            p = new EditorProp(Font.class, true);
        }
        else if(ActiveValue.class.isAssignableFrom(c) && ((ActiveValue)value).createValue(UIManager.getDefaults()) instanceof Color)
        {
            Color newValue = (Color) ((ActiveValue)value).createValue(UIManager.getDefaults());
            this.prop.setValue(new ColorUIResource(newValue));
            p = new EditorProp(Color.class, true);
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
        return this.original ? null : "<b>* " + this.getDisplayName();
    }
    
    public boolean isOriginalValue()
    {
        return this.original;
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
                ((UIParentNode)getParentNode()).update();
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
            else if(value instanceof FontUIResource)
            {
                prop.setValue(new FontUIResource((Font) newValue));
            }
            else
            {
                prop.setValue(newValue);
            }
        }
    }
}
