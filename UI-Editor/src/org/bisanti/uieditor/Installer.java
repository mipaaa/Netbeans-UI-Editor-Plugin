/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bisanti.uieditor;

import javax.swing.SwingUtilities;
import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall
{

    @Override
    public void restored()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                UIEditorTopComponent.loadSettings();
            }
        });
    }
}
