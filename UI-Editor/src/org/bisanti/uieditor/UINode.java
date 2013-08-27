
package org.bisanti.uieditor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults.ActiveValue;
import javax.swing.UIDefaults.LazyValue;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import org.bisanti.util.Pair;
import org.bisanti.util.StringUtil;
import org.bisanti.util.Util;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;


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
    
    public static final String VALUE_DESCRIPTION = "An editable or non-editable textual representation of the Property's value";
    
    private UIProperty prop;
    
    private Object originalValue;
    
    private boolean original;
    
    private Image image;
    
    public UINode(UIProperty prop)
    {
        super(Children.LEAF);
        super.setName(prop.getName().toString());
        this.prop = prop;
        this.originalValue = prop.getValue();
        if(this.originalValue instanceof LazyValue)
        {
            this.originalValue = ((LazyValue)this.originalValue).createValue(UIManager.getDefaults());
            this.prop.setValue(this.originalValue);
        }
        else if(this.originalValue instanceof ActiveValue)
        {
            this.originalValue = ((ActiveValue)this.originalValue).createValue(UIManager.getDefaults());
            this.prop.setValue(this.originalValue);
        }
        
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
    
    public UIProperty getUIProperty()
    {
        return this.prop;
    }
    
    public static boolean hasEditor(Class type)
    {        
        return findEditor(type) != null;
    }
    
    public static Pair<PropertyEditor, Class> findEditor(Class type)
    {
        if(type == null)
        {
            return null;
        }
        
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
        Class c = value == null ? null : value.getClass();
        Property p;
        
        Pair<PropertyEditor, Class> pair = findEditor(c);
        if(pair != null && validEditor(pair.getFirst()))
        {
            p =  new EditorProp(pair.getSecond(), true);
        }
        else if(value instanceof Icon)
        {
            IconProperty ip = new IconProperty(value, VALUE_PROP, VALUE_DISPLAY, VALUE_DESCRIPTION);
            
            try
            {
                this.image = ImageUtilities.icon2Image(ip.getIcon());
                ImageObserver io = new ImageObserver()
                {
                    @Override
                    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
                    {
                        return false;
                    }
                };
                Image orig = super.getIcon(1);
                if(this.image.getHeight(io) > orig.getHeight(io))
                {
                    this.image = this.image.getScaledInstance(orig.getWidth(io), orig.getHeight(io), 0);
                    ip.setIcon(ImageUtilities.image2Icon(this.image));
                }
                
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        fireIconChange();
                        fireOpenedIconChange();
                    }
                });
            }
            catch(Exception ex){}
            
            p = ip;
        }
        else
        {
            String nl = StringUtil.NEW_LINE;
            final String val = "CLASS: " + nl + (c == null ? null : c.getName()) + " " + nl + nl + "VALUE: " + nl + value;
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

    @Override
    public Image getIcon(int type)
    {
        return this.image == null ? super.getIcon(type) : this.image; 
    }

    @Override
    public Image getOpenedIcon(int type)
    {
        return this.image == null ? super.getOpenedIcon(type) : this.image;
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
            super(VALUE_PROP, clazz, VALUE_DISPLAY, VALUE_DESCRIPTION);
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
