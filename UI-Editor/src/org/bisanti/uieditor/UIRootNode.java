/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bisanti.uieditor;

import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/**
 * Written and authored by Jason Bisanti. Free to use and reproduce.
 *
 * @author Jason Bisanti
 */
public class UIRootNode extends AbstractNode
{
    public UIRootNode(SortedMap<String, Collection<UIProperty>> nodes, PropertyChangeListener pcl)
    {
        super(Children.create(new RootChildFactory(nodes, pcl), true));
        super.setName("ROOT");
    }
    
    @Override
    protected Sheet createSheet()
    {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);

        if (set == null)
        {
            set = Sheet.createPropertiesSet();
            set.put(new PropertySupport.ReadOnly(UINode.VALUE_PROP, Object.class, UINode.VALUE_DISPLAY, "") 
            {
                @Override
                public Object getValue() throws IllegalAccessException, InvocationTargetException
                {
                    return "";
                }
            });
            sheet.put(set);
        }

        return sheet;
    }
    
    private static class RootChildFactory extends ChildFactory<String>
    {
        private final SortedMap<String, Collection<UIProperty>> nodes;
        private final PropertyChangeListener pcl;
        
        RootChildFactory(SortedMap<String, Collection<UIProperty>> nodes, PropertyChangeListener pcl)
        {
            this.nodes = nodes;
            this.pcl = pcl;
        }

        @Override
        protected boolean createKeys(List<String> list)
        {
            list.addAll(this.nodes.keySet());
            return true;
        }

        @Override
        protected Node createNodeForKey(String key)
        {
            UIParentNode node = new UIParentNode(key, this.nodes.get(key), this.pcl);
            return node;
        }
        
    }
    
}
