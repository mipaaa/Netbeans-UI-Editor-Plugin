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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import org.bisanti.util.FileUtil;
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
            new File(FileUtil.MAIN_DIR + File.separator + "ui_editor.defaults");
    
    private final ExplorerManager manager = new ExplorerManager();
    
    private final Set<UIProperty> changed = new HashSet<UIProperty>();

    public UIEditorTopComponent()
    {
        for(LookAndFeelInfo lafi: UIManager.getInstalledLookAndFeels())
        {
            this.installedLafs.put(lafi.getName(), lafi.getClassName());
        }
        initComponents();
        this.deleteButton.setEnabled(this.FILE.exists());
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
    
    private void applyLaf()
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
                } else
                {
                    if (OCEAN_THEME.equals(theme))
                    {
                        MetalLookAndFeel.setCurrentTheme(new OceanTheme());
                    }
                }
            }
            UIManager.setLookAndFeel(this.installedLafs.get(selection));
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
                final String misc = "* Miscellaneous";
                kids = nodes.get(misc);
                if(kids == null)
                {
                    kids = new ArrayList<UIProperty>();
                    nodes.put(misc, kids);
                }              
            }
            kids.add(new UIProperty(entry));
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(outlineView1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lafComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(themeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(themeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setLafButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lafDescription))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(saveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 83, Short.MAX_VALUE)
                        .addComponent(applyPropsButton)))
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
                    .addComponent(setLafButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lafDescription))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(applyPropsButton)
                    .addComponent(saveButton)
                    .addComponent(deleteButton))
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
        this.applyLaf();
        SwingUtilities.updateComponentTreeUI(WindowManager.getDefault().getMainWindow());
        this.refreshProperties();
    }//GEN-LAST:event_setLafButtonActionPerformed

    private void applyPropsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_applyPropsButtonActionPerformed
    {//GEN-HEADEREND:event_applyPropsButtonActionPerformed
        for(UIProperty prop: this.changed)
        {
            UIManager.getDefaults().put(prop.getName(), prop.getValue());
        }
        SwingUtilities.updateComponentTreeUI(WindowManager.getDefault().getMainWindow());        
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
            
            
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyPropsButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox lafComboBox;
    private javax.swing.JLabel lafDescription;
    private org.openide.explorer.view.OutlineView outlineView1;
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
