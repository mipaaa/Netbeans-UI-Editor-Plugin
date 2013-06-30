/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bisanti.uieditor;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;
import org.bisanti.util.Util;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Written and authored by Jason Bisanti. Free to use and reproduce.
 *
 * @author Jason Bisanti
 */
public class UIParentNode extends AbstractNode
{    
    public UIParentNode(String name, Collection<UIProperty> children, PropertyChangeListener pcl)
    {
        super(Children.create(new UIChildFactory(children, pcl), true));
        super.setName(name);
    }
    
    private static class UIChildFactory extends ChildFactory<UIProperty>
    {
        private final Collection<UIProperty> children;
        
        private PropertyChangeListener pcl;
        
        UIChildFactory(Collection<UIProperty> children, PropertyChangeListener pcl)
        {
            this.children = children;
            this.pcl = pcl;
        }

        @Override
        protected boolean createKeys(List<UIProperty> list)
        {
            if(!Util.isNullOrEmpty(this.children))
            {
                list.addAll(this.children);
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(UIProperty key)
        {
            Node node = new UINode(key);
            node.addPropertyChangeListener(this.pcl);
            return node;
        }        
    }
}
