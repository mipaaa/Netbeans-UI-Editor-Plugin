package org.bisanti.uieditor;

import java.util.Set;

/**
 * <i>
 * Written and authored by Jason Bisanti. Free to use and reproduce, but please
 * keep my name as the original author!
 * <br><br></i>
 * @author Jason Bisanti
 */
public class UISettings implements java.io.Serializable
{    
    private String lafName;
    
    private Set<UIProperty> properties;

    public UISettings(String laf, Set<UIProperty> properties)
    {
        this.lafName = laf;
        this.properties = properties;
    }

    public String getLafName()
    {
        return lafName;
    }

    public void setLafName(String lafName)
    {
        this.lafName = lafName;
    }

    public Set<UIProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(Set<UIProperty> properties)
    {
        this.properties = properties;
    }
    
}
