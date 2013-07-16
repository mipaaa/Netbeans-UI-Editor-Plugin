
package org.bisanti.uieditor;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalTheme;

/**
 * Written and authored by Jason Bisanti. Free to use and reproduce.
 *
 * @author Jason Bisanti
 */
public class CustomMetalTheme extends MetalTheme
{
    private ColorUIResource primary1;
    
    private ColorUIResource primary2;
    
    private ColorUIResource primary3;
    
    private ColorUIResource secondary1;
    
    private ColorUIResource secondary2;
    
    private ColorUIResource secondary3;
    
    private FontUIResource control;
    private FontUIResource system;
    private FontUIResource user;
    private FontUIResource menu;
    private FontUIResource window;
    private FontUIResource sub;
    
    public CustomMetalTheme()
    {
        DefaultMetalTheme dmt = new DefaultMetalTheme();
        this.primary1 = dmt.getPrimaryControlDarkShadow();
        this.primary2 = dmt.getPrimaryControlShadow();
        this.primary3 = dmt.getPrimaryControl();
        this.secondary1 = dmt.getControlDarkShadow();
        this.secondary2 = dmt.getControlShadow();
        this.secondary3 = dmt.getControl();
        this.control = dmt.getControlTextFont();
        this.system = dmt.getSystemTextFont();
        this.user = dmt.getUserTextFont();
        this.menu = dmt.getMenuTextFont();
        this.window = dmt.getWindowTitleFont();
        this.sub = dmt.getSubTextFont();
    }
    
    @Override
    public String getName()
    {
        return "Custom Metal Theme";
    }

    @Override
    protected ColorUIResource getPrimary1()
    {
        return this.primary1;
    }

    @Override
    protected ColorUIResource getPrimary2()
    {
        return this.primary2;
    }

    @Override
    protected ColorUIResource getPrimary3()
    {
        return this.primary3;
    }

    @Override
    protected ColorUIResource getSecondary1()
    {
        return this.secondary1;
    }

    @Override
    protected ColorUIResource getSecondary2()
    {
        return this.secondary2;
    }

    @Override
    protected ColorUIResource getSecondary3()
    {
        return this.secondary3;
    }

    @Override
    public FontUIResource getControlTextFont()
    {
        return this.control;
    }

    @Override
    public FontUIResource getSystemTextFont()
    {
        return this.system;
    }

    @Override
    public FontUIResource getUserTextFont()
    {
        return this.user;
    }

    @Override
    public FontUIResource getMenuTextFont()
    {
        return this.menu;
    }

    @Override
    public FontUIResource getWindowTitleFont()
    {
        return this.window;
    }

    @Override
    public FontUIResource getSubTextFont()
    {
        return this.sub;
    }
    
}
