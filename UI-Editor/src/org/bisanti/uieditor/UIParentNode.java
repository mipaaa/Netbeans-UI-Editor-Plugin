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
import org.openide.util.WeakListeners;

/**
 * Written and authored by Jason Bisanti. Free to use and reproduce.
 *
 * @author Jason Bisanti
 */
public class UIParentNode extends AbstractNode
{        
    private boolean valuesChanged;
    
    public UIParentNode(String name, Collection<UIProperty> children, PropertyChangeListener pcl)
    {
        super(Children.create(new UIChildFactory(children, pcl), true));
        super.setName(name);
        for(UIProperty prop: children)
        {
            if(UIEditorTopComponent.applied.contains(prop))
            {
                this.valuesChanged = true;
                break;
            }
        }
    }
    
    protected void update()
    {
        for(Node node: super.getChildren().getNodes())
        {
            if(node instanceof UINode && !((UINode)node).isOriginalValue())
            {
                this.valuesChanged = true;
                return;
            }
        }
        this.valuesChanged = false;
    }

    @Override
    public String getHtmlDisplayName()
    {
        return this.valuesChanged ? "<b>* " + this.getDisplayName() : null;
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
            node.addPropertyChangeListener(WeakListeners.propertyChange(this.pcl, node));
            return node;
        }        
    }
}
