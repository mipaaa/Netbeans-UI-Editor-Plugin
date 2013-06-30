/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bisanti.uieditor;

import java.util.Map;

/**
 * Written and authored by Jason Bisanti. Free to use and reproduce.
 *
 * @author Jason Bisanti
 */
public class UIProperty implements Comparable<UIProperty>
{
    private Object name;
    
    private Object value;

    public UIProperty(Object name, Object value)
    {
        this.name = name;
        this.value = value;
    }
    
    public UIProperty(Map.Entry entry)
    {
        this(entry.getKey(), entry.getValue());
    }

    public Object getName()
    {
        return name;
    }

    public void setName(Object name)
    {
        this.name = name;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    @Override
    public int compareTo(UIProperty o)
    {
        return String.valueOf(this.name).compareTo(String.valueOf(o.name));
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final UIProperty other = (UIProperty) obj;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name)))
        {
            return false;
        }
        if (this.value != other.value && (this.value == null || !this.value.equals(other.value)))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 61 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 61 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }
    
}
