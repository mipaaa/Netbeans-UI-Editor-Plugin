/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bisanti.uieditor;

import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import org.bisanti.util.FileUtil;
import org.bisanti.util.StringUtil;
import org.bisanti.util.Util;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node.Property;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.bisanti.uieditor//UIEditor//EN",
autostore = false)
@TopComponent.Description(preferredID = "UIEditorTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "org.bisanti.uieditor.UIEditorTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_UIEditorAction",
preferredID = "UIEditorTopComponent")
public final class UIEditorTopComponent extends TopComponent implements
        ExplorerManager.Provider, PropertyChangeListener
{
    private final Map<String, String> installedLafs =
            new TreeMap<String, String>();
    
    private final String STEEL_THEME = "Steel";
    
    private final String OCEAN_THEME = "Ocean";
    
    private final File FILE = 
            new File(FileUtil.MAIN_DIR + File.separator + "ui_editor.uidefaults");
    
    private final ExplorerManager manager = new ExplorerManager();
    
    private final Set<UIProperty> changed = new TreeSet<UIProperty>();
    
    private final Set<UIProperty> applied = new TreeSet<UIProperty>();
    
    private final Map JAVA_DEFAULTS = UIManager.getDefaults();
    
    private final LookAndFeel DEFAULT_LAF = UIManager.getLookAndFeel();

    public UIEditorTopComponent()
    {
        for(LookAndFeelInfo lafi: UIManager.getInstalledLookAndFeels())
        {
            this.installedLafs.put(lafi.getName(), lafi.getClassName());
        }
        initComponents();
        if(this.FILE.exists())
        {
            try
            {
                UISettings settings = FileUtil.readObjects(this.FILE, UISettings.class).get(0);
                this.applyLaf(settings.getLafName());
                this.apply(settings.getProperties());
                SwingUtilities.updateComponentTreeUI(WindowManager.getDefault().getMainWindow());
                this.deleteButton.setEnabled(true);
            } 
            catch (Exception ex)
            {
                Exceptions.printStackTrace(ex);
            }
        }
        else
        {
            this.deleteButton.setEnabled(false);
        }
        
        this.outlineView1.getOutline().setRootVisible(false);
        setName(NbBundle.getMessage(UIEditorTopComponent.class, "CTL_UIEditorTopComponent"));
        setToolTipText(NbBundle.getMessage(UIEditorTopComponent.class, "HINT_UIEditorTopComponent"));
        this.updateLafDescription();
        this.lafComboBox.setSelectedItem(UIManager.getLookAndFeel().getName());
        setName(NbBundle.getMessage(UIEditorTopComponent.class, "CTL_UIEditorTopComponent"));
        setToolTipText(NbBundle.getMessage(UIEditorTopComponent.class, "HINT_UIEditorTopComponent"));
        this.refreshProperties();
    }
    
    private void updateLafDescription()
    {
        LookAndFeel laf = UIManager.getLookAndFeel();
        this.lafDescription.setText(laf.getID() + " (" + laf.getDescription() + ")");
    }
    
    private void applyLaf(String name)
    {
        try
        {
            String selection = this.lafComboBox.getSelectedItem().toString();
            if (selection.contains("Metal"))
            {
                String theme = this.themeComboBox.getSelectedItem().toString();
                if (STEEL_THEME.equals(theme))
                {
                    MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
                }
                else
                {
                    if (OCEAN_THEME.equals(theme))
                    {
                        MetalLookAndFeel.setCurrentTheme(new OceanTheme());
                    }
                }
            }
            UIManager.setLookAndFeel(name);
            this.updateLafDescription();
        } 
        catch (Exception ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void refreshProperties()
    {        
        SortedMap<String, Collection<UIProperty>> nodes = 
                new TreeMap<String, Collection<UIProperty>>(String.CASE_INSENSITIVE_ORDER);
        
        LookAndFeel laf = UIManager.getLookAndFeel();
        for(Map.Entry entry: laf.getDefaults().entrySet())
        {
            String name = entry.getKey().toString();
            int index = name.indexOf(".");
            Collection<UIProperty> kids;
            if(index > -1)
            {
                String parent = name.substring(0, index);
                kids = nodes.get(parent);
                if(kids == null)
                {
                    kids = new ArrayList<UIProperty>();
                    nodes.put(parent, kids);
                }
                  
            }
            else
            {
                final String misc = "~Miscellaneous";
                kids = nodes.get(misc);
                if(kids == null)
                {
                    kids = new ArrayList<UIProperty>();
                    nodes.put(misc, kids);
                }              
            }
            UIProperty prop = new UIProperty(entry);
            kids.add(prop);
        }
        
        for(Collection list: nodes.values())
        {
            Collections.sort((List)list);
        }
        
        UIRootNode root = new UIRootNode(nodes, this);
        Property[] props = root.getPropertySets()[0].getProperties();
        for(Property col: props)
        {
            this.outlineView1.removePropertyColumn(col.getName());
            this.outlineView1.addPropertyColumn(col.getName(), col.getDisplayName(), col.getShortDescription());
        }
        this.manager.setRootContext(root);
    }
    
    private void apply(Collection<UIProperty> props)
    {
        for(UIProperty prop: props)
        {
            UIManager.getDefaults().put(prop.getName(), prop.getValue());
            this.applied.add(prop);
        }
        SwingUtilities.updateComponentTreeUI(WindowManager.getDefault().getMainWindow());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        lafComboBox = new javax.swing.JComboBox();
        themeLabel = new javax.swing.JLabel();
        themeComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        lafDescription = new javax.swing.JLabel();
        setLafButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        outlineView1 = new org.openide.explorer.view.OutlineView("Property");
        applyPropsButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.jLabel1.text")); // NOI18N

        lafComboBox.setModel(new javax.swing.DefaultComboBoxModel(this.installedLafs.keySet().toArray(new String[0])));
        lafComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                lafComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(themeLabel, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.themeLabel.text")); // NOI18N

        themeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { OCEAN_THEME, STEEL_THEME}));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lafDescription, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.lafDescription.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(setLafButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.setLafButton.text")); // NOI18N
        setLafButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setLafButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(applyPropsButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.applyPropsButton.text")); // NOI18N
        applyPropsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyPropsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.saveButton.text")); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(deleteButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.deleteButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(resetButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.resetButton.text")); // NOI18N
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(outlineView1, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lafComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(themeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(themeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setLafButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 176, Short.MAX_VALUE)
                        .addComponent(resetButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lafDescription))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(applyPropsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 184, Short.MAX_VALUE)
                        .addComponent(saveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lafComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(themeLabel)
                    .addComponent(themeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(setLafButton)
                    .addComponent(resetButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lafDescription))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deleteButton)
                    .addComponent(saveButton)
                    .addComponent(applyPropsButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(outlineView1, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void lafComboBoxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_lafComboBoxItemStateChanged
    {//GEN-HEADEREND:event_lafComboBoxItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            boolean visible = this.lafComboBox.getSelectedItem().toString().contains("Metal");
            this.themeComboBox.setVisible(visible);
            this.themeLabel.setVisible(visible);
        }
    }//GEN-LAST:event_lafComboBoxItemStateChanged

    private void setLafButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_setLafButtonActionPerformed
    {//GEN-HEADEREND:event_setLafButtonActionPerformed
        this.applyLaf(this.installedLafs.get(this.lafComboBox.getSelectedItem().toString()));
        SwingUtilities.updateComponentTreeUI(WindowManager.getDefault().getMainWindow());
        this.refreshProperties();
    }//GEN-LAST:event_setLafButtonActionPerformed

    private void applyPropsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_applyPropsButtonActionPerformed
    {//GEN-HEADEREND:event_applyPropsButtonActionPerformed
        this.apply(this.changed);
        this.changed.clear();
    }//GEN-LAST:event_applyPropsButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveButtonActionPerformed
    {//GEN-HEADEREND:event_saveButtonActionPerformed
        DialogDescriptor confirm = new DialogDescriptor("This will set your current UI settings as the default and erase any previously saved settings. Are you sure you want to continue?", "Confirm Save");
        confirm.setMessageType(DialogDescriptor.WARNING_MESSAGE);
        if(DialogDisplayer.getDefault().notify(confirm) == DialogDescriptor.OK_OPTION)
        {
            if(this.FILE.exists())
            {
                this.FILE.delete();
            }
            
            UISettings uis = new UISettings(UIManager.getLookAndFeel().getName(), this.applied);
            Set<UIProperty> removed = new TreeSet<UIProperty>();

            if (!FileUtil.isSerializable(this.applied))
            {
                Iterator<UIProperty> it = this.applied.iterator();
                while (it.hasNext())
                {
                    UIProperty uip = it.next();
                    if (!FileUtil.isSerializable(uip))
                    {
                        removed.add(uip);
                        it.remove();
                    }
                }
            }            
            
            try
            {
                this.FILE.createNewFile();
                FileUtil.writeObjects(this.FILE, uis);
                if (!removed.isEmpty())
                {
                    DialogDescriptor.Message message = new DialogDescriptor.Message("The following properties are not Serializable and could not be saved:\n\n" + StringUtil.toString(removed, "\n"));
                    message.setTitle("Not All Properties Saved");
                    message.setMessageType(DialogDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(message);
                }
            } catch (Exception ex)
            {
                DialogDescriptor.Message message = new DialogDescriptor.Message("Unable to save current settings:\n\n" + ex.getMessage());
                message.setTitle("Error During Save");
                message.setMessageType(DialogDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(message);
            }
            
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_resetButtonActionPerformed
    {//GEN-HEADEREND:event_resetButtonActionPerformed
        DialogDescriptor confirm = new DialogDescriptor("This will undo all of your changes and reset the default Look And Feel and color scheme for this platform. Are you sure you want to continue?", "Reset?");
        confirm.setMessageType(DialogDescriptor.WARNING_MESSAGE);
        if(DialogDisplayer.getDefault().notify(confirm) == DialogDescriptor.OK_OPTION)
        {
            try
            {
                UIManager.setLookAndFeel(this.DEFAULT_LAF);
                SwingUtilities.updateComponentTreeUI(WindowManager.getDefault().getMainWindow());
                UIManager.getDefaults().putAll(this.JAVA_DEFAULTS);
                SwingUtilities.updateComponentTreeUI(WindowManager.getDefault().getMainWindow());
                this.refreshProperties();
            } 
            catch (UnsupportedLookAndFeelException ex)
            {
                Exceptions.printStackTrace(ex);
            }
        }
    }//GEN-LAST:event_resetButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyPropsButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox lafComboBox;
    private javax.swing.JLabel lafDescription;
    private org.openide.explorer.view.OutlineView outlineView1;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton setLafButton;
    private javax.swing.JComboBox themeComboBox;
    private javax.swing.JLabel themeLabel;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened()
    {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed()
    {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p)
    {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p)
    {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public ExplorerManager getExplorerManager()
    {
        return this.manager;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if(Util.equal(UINode.VALUE_PROP, evt.getPropertyName()))
        {
            this.changed.add((UIProperty)evt.getNewValue());
        }
    }
}
