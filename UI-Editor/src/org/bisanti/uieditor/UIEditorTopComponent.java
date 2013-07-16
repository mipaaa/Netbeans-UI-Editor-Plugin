/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bisanti.uieditor;

import java.awt.Font;
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
    public static final String FILE = 
            FileUtil.MAIN_DIR + File.separator + "ui_editor.uidefaults";
    
    private static final Set<UIProperty> applied = new TreeSet<UIProperty>();
    
    private static final Map JAVA_DEFAULTS = UIManager.getDefaults();
    
    private static final LookAndFeel DEFAULT_LAF = UIManager.getLookAndFeel();
    
    private final Map<String, String> installedLafs =
            new TreeMap<String, String>();
    
    private final String STEEL_THEME = "Steel";
    
    private final String OCEAN_THEME = "Ocean";
    
    private final ExplorerManager manager = new ExplorerManager();
    
    private final Set<UIProperty> changed = new TreeSet<UIProperty>();

    public UIEditorTopComponent()
    {
        for(LookAndFeelInfo lafi: UIManager.getInstalledLookAndFeels())
        {
            this.installedLafs.put(lafi.getName(), lafi.getClassName());
        }
        initComponents();
        
        this.updateLafDescription();
        File file = new File(FILE);
        this.deleteButton.setEnabled(file.exists());        
        this.propTreeTable.getOutline().setRootVisible(false);
        setName(NbBundle.getMessage(UIEditorTopComponent.class, "CTL_UIEditorTopComponent"));
        setToolTipText(NbBundle.getMessage(UIEditorTopComponent.class, "HINT_UIEditorTopComponent"));
        this.lafComboBox.setSelectedItem(UIManager.getLookAndFeel().getName());
        setName(NbBundle.getMessage(UIEditorTopComponent.class, "CTL_UIEditorTopComponent"));
        setToolTipText(NbBundle.getMessage(UIEditorTopComponent.class, "HINT_UIEditorTopComponent"));
        this.refreshProperties();
    }
    
    public static void loadSettings()
    {
        File file = new File(FILE);
        if(file.exists())
        {
            try
            {
                UISettings settings = FileUtil.readObjects(file, UISettings.class).get(0);
                UIManager.setLookAndFeel(settings.getLafName());
                apply(settings.getProperties());
                SwingUtilities.updateComponentTreeUI(WindowManager.getDefault().getMainWindow());
            } 
            catch (Exception ex)
            {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private static void apply(Collection<UIProperty> props)
    {
        for(UIProperty prop: props)
        {
            UIManager.getDefaults().put(prop.getName(), prop.getValue());
            applied.add(prop);
        }
        SwingUtilities.updateComponentTreeUI(WindowManager.getDefault().getMainWindow());
    }
    
    private void updateLafDescription()
    {
        LookAndFeel laf = UIManager.getLookAndFeel();
        this.lafDescription.setText(laf.getID() + " (" + laf.getDescription() + ")");
        this.lafComboBox.setSelectedItem(laf.getName());
        if(laf.getName().contains("Metal"))
        {
            String theme = MetalLookAndFeel.getCurrentTheme().getName();
            if (STEEL_THEME.equals(theme))
            {
                this.themeComboBox.setSelectedItem(STEEL_THEME);
            } 
            else if (OCEAN_THEME.equals(theme))
            {
                this.themeComboBox.setSelectedItem(OCEAN_THEME);
            }
        }
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
                else if (OCEAN_THEME.equals(theme))
                {
                    MetalLookAndFeel.setCurrentTheme(new OceanTheme());
                }
            }
            UIManager.setLookAndFeel(name);
            SwingUtilities.updateComponentTreeUI(WindowManager.getDefault().getMainWindow());
            this.updateLafDescription();
        } 
        catch (Exception ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void refreshProperties()
    {        
        SortedMap<String, Collection<UIProperty>> propNodes = 
                new TreeMap<String, Collection<UIProperty>>(String.CASE_INSENSITIVE_ORDER);
        
        SortedMap<String, Collection<UIProperty>> fontNodes = 
                new TreeMap<String, Collection<UIProperty>>(String.CASE_INSENSITIVE_ORDER);
        
        LookAndFeel laf = UIManager.getLookAndFeel();
        for(Map.Entry entry: laf.getDefaults().entrySet())
        {
            String name = entry.getKey().toString();
            if(StringUtil.contains(name, "font", "Font"))
            {
                fontNodes.put(name, Collections.singleton(new UIProperty(entry)));
                continue;
            }
            
            int index = name.indexOf(".");
            Collection<UIProperty> kids;
            if(index > -1)
            {
                String parent = name.substring(0, index);
                kids = propNodes.get(parent);
                if(kids == null)
                {
                    kids = new ArrayList<UIProperty>();
                    propNodes.put(parent, kids);
                }
                  
            }
            else
            {
                final String misc = "~Miscellaneous";
                kids = propNodes.get(misc);
                if(kids == null)
                {
                    kids = new ArrayList<UIProperty>();
                    propNodes.put(misc, kids);
                }              
            }
            UIProperty prop = new UIProperty(entry);
            kids.add(prop);
        }
        
        for(Collection list: propNodes.values())
        {
            Collections.sort((List)list);
        }
        
        UIRootNode root = new UIRootNode(propNodes, this);
        Property[] props = root.getPropertySets()[0].getProperties();
        for(Property col: props)
        {
            this.propTreeTable.removePropertyColumn(col.getName());
            this.propTreeTable.addPropertyColumn(col.getName(), col.getDisplayName(), col.getShortDescription());
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
        saveButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        lafsPanel = new javax.swing.JPanel();
        applyPropsButton = new javax.swing.JButton();
        propTreeTable = new org.openide.explorer.view.OutlineView("Property");
        jPanel1 = new javax.swing.JPanel();
        outlineView1 = new org.openide.explorer.view.OutlineView();
        jButton1 = new javax.swing.JButton();

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

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.saveButton.text")); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(deleteButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.deleteButton.text")); // NOI18N
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(resetButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.resetButton.text")); // NOI18N
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(applyPropsButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.applyPropsButton.text")); // NOI18N
        applyPropsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyPropsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout lafsPanelLayout = new javax.swing.GroupLayout(lafsPanel);
        lafsPanel.setLayout(lafsPanelLayout);
        lafsPanelLayout.setHorizontalGroup(
            lafsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lafsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lafsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(propTreeTable, javax.swing.GroupLayout.DEFAULT_SIZE, 695, Short.MAX_VALUE)
                    .addComponent(applyPropsButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        lafsPanelLayout.setVerticalGroup(
            lafsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lafsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(applyPropsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(propTreeTable, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.lafsPanel.TabConstraints.tabTitle"), lafsPanel); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.jButton1.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(outlineView1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 695, Short.MAX_VALUE)
                    .addComponent(jButton1))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(outlineView1, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lafComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(themeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(themeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setLafButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 97, Short.MAX_VALUE)
                        .addComponent(saveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(resetButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lafDescription)))
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
                    .addComponent(resetButton)
                    .addComponent(deleteButton)
                    .addComponent(saveButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lafDescription))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
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
        this.refreshProperties();
    }//GEN-LAST:event_setLafButtonActionPerformed

    private void applyPropsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_applyPropsButtonActionPerformed
    {//GEN-HEADEREND:event_applyPropsButtonActionPerformed
        apply(this.changed);
        this.changed.clear();
    }//GEN-LAST:event_applyPropsButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveButtonActionPerformed
    {//GEN-HEADEREND:event_saveButtonActionPerformed
        DialogDescriptor confirm = new DialogDescriptor("This will set your current UI settings as the default and erase any previously saved settings. Are you sure you want to continue?", "Confirm Save");
        confirm.setMessageType(DialogDescriptor.WARNING_MESSAGE);
        if(DialogDisplayer.getDefault().notify(confirm) == DialogDescriptor.OK_OPTION)
        {
            File file = new File(FILE);
            if(file.exists())
            {
                file.delete();
            }
            
            UISettings uis = new UISettings(UIManager.getLookAndFeel().getClass().getCanonicalName(), applied);
            Set<UIProperty> removed = new TreeSet<UIProperty>();

            if (!FileUtil.isSerializable(applied))
            {
                Iterator<UIProperty> it = applied.iterator();
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
                file.createNewFile();
                FileUtil.writeObjects(file, uis);
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
                UIManager.setLookAndFeel(DEFAULT_LAF);
                SwingUtilities.updateComponentTreeUI(WindowManager.getDefault().getMainWindow());
                UIManager.getLookAndFeelDefaults().putAll(JAVA_DEFAULTS);
                SwingUtilities.updateComponentTreeUI(WindowManager.getDefault().getMainWindow());
                this.refreshProperties();
                this.updateLafDescription();
            } 
            catch (UnsupportedLookAndFeelException ex)
            {
                Exceptions.printStackTrace(ex);
            }
        }
    }//GEN-LAST:event_resetButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteButtonActionPerformed
    {//GEN-HEADEREND:event_deleteButtonActionPerformed
        DialogDescriptor confirm = new DialogDescriptor("This will permanently erase your default settings and they will not be applied the next time NetBeans starts. Are you sure you want to erase them?", "Erase?");
        confirm.setMessageType(DialogDescriptor.WARNING_MESSAGE);
        if(DialogDisplayer.getDefault().notify(confirm) == DialogDescriptor.OK_OPTION)
        {
            File file = new File(FILE);
            file.delete();
            this.deleteButton.setEnabled(false);
        }        
    }//GEN-LAST:event_deleteButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyPropsButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JComboBox lafComboBox;
    private javax.swing.JLabel lafDescription;
    private javax.swing.JPanel lafsPanel;
    private org.openide.explorer.view.OutlineView outlineView1;
    private org.openide.explorer.view.OutlineView propTreeTable;
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
