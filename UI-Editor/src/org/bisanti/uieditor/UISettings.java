/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bisanti.uieditor;

import java.util.Collection;
import javax.swing.LookAndFeel;

/**
 * Written and authored by Jason Bisanti. Free to use and reproduce.
 *
 * @author Jason Bisanti
 */
public class UISettings implements java.io.Serializable
{
    private LookAndFeel laf;
    
    private Collection<UIProperty> properties;

    public UISettings(LookAndFeel laf, Collection<UIProperty> properties)
    {
        this.laf = laf;
        this.properties = properties;
    }

    public LookAndFeel getLaf()
    {
        return laf;
    }

    public void setLaf(LookAndFeel laf)
    {
        this.laf = laf;
    }

    public Collection<UIProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(Collection<UIProperty> properties)
    {
        this.properties = properties;
    }
    
}
